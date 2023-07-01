package com.ustadmobile.core.viewmodel.scopedgrant.edit

import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.lib.db.entities.ScopedGrant
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ScopedGrantEditUiState(
    val entity: ScopedGrant? = null,

    var bitmaskList: List<BitmaskFlag> = listOf(),

    val fieldsEnabled: Boolean = true,
)

class ScopedGrantEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, ScopedGrantEditView.VIEW_NAME) {

    private val _uiState: MutableStateFlow<ScopedGrantEditUiState> = MutableStateFlow(
        ScopedGrantEditUiState(
            fieldsEnabled = false,
        )
    )

    val uiState: Flow<ScopedGrantEditUiState> = _uiState.asStateFlow()

    private val entityUid: Long
        get() = savedStateHandle[UstadView.ARG_SCOPED_GRANT_UID]?.toLong() ?:0

    init{

        loadingState = LoadingUiState.INDETERMINATE

        val title = if(entityUid == 0L) systemImpl.getString(MessageID.add_person_to_class) else
            systemImpl.getString(MessageID.edit_permissions)

        _appUiState.update {
            AppUiState(
                title = title,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.done),
                    onClick = this::onClickSave
                )
            )
        }

        viewModelScope.launch {

            awaitAll(
                async{
                    loadEntity(
                        serializer = ScopedGrant.serializer(),
                        onLoadFromDb = {it.scopedGrantDao.findByUid(entityUid)},
                        makeDefault = {
                            ScopedGrant().also{
                                it.sgTableId = savedStateHandle[UstadView.ARG_CODE_TABLE]?.toInt() ?:0
                                it.sgEntityUid = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0
                                it.sgGroupUid = savedStateHandle[UstadView.ARG_PERSON_GROUPUID]?.toLong()?:0
                            }

                        },
                        uiUpdate = { entityToDisplay ->
                            _uiState.update { it.copy(entity = entityToDisplay) }

                        }
                    )


                }
            )

            val generatedList = setBitmaskList(_uiState.value.entity?.sgPermissions?:0)


            _uiState.update { prev ->
                prev.copy(
                    fieldsEnabled = true,
                    bitmaskList = generatedList
                )
            }

            loadingState = LoadingUiState.NOT_LOADING

        }
    }


    fun onClickSave(){
        loadingState = LoadingUiState.INDETERMINATE

        val scopedGrant = _uiState.value.entity ?: return

        _uiState.update { prev ->
            prev.copy(
               fieldsEnabled = false,
            )
        }

        val selection = _uiState.value.bitmaskList
        scopedGrant.sgPermissions = getPermissionLongFromList(selection)


        viewModelScope.launch {
            if(entityUid == 0L){
                activeDb.scopedGrantDao.insertAsync(scopedGrant)
            }else{
                activeDb.scopedGrantDao.updateAsync(scopedGrant)
            }
            finishWithResult(scopedGrant)
        }

    }

    fun onToggleBitmask(flag: BitmaskFlag?){
        val currentList = _uiState.value.bitmaskList

        if(flag == null) return

        //Toggle this flag
        currentList.find { it.messageId == flag.messageId }?.enabled = !flag.enabled

        //Update:
        _uiState.update { prev ->
            prev.copy(bitmaskList = currentList)
        }

    }

    private fun setBitmaskList(permissions: Long): List<BitmaskFlag>{

        val enabledPermissions = MutableLiveData(
            ScopedGrantEditPresenter.PERMISSION_MESSAGE_ID_LIST.map{
                it.toBitmaskFlag(permissions)
            }
        )

        return enabledPermissions.getValue()?: emptyList()
    }

    private fun getPermissionLongFromList(bitmaskList: List<BitmaskFlag>): Long{
        val saveVal = bitmaskList.fold(0L) { acc, flag ->
            acc + (if (flag.enabled) flag.flagVal else 0)
        }
        return saveVal
    }

}