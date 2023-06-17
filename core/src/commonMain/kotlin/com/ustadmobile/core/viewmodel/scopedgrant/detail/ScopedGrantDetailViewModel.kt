package com.ustadmobile.core.viewmodel.scopedgrant.detail

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState.Companion.INDETERMINATE
import com.ustadmobile.core.impl.appstate.LoadingUiState.Companion.NOT_LOADING
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.ScopedGrantDetailView
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.ScopedGrant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance


data class ScopedGrantDetailUiState(
    val scopedGrant: ScopedGrant? = null,
    val bitmaskList: List<BitmaskFlag> = emptyList()
)

class ScopedGrantDetailViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle
): DetailViewModel<ScopedGrant>(di, savedStateHandle, ScopedGrantDetailView.VIEW_NAME) {

    private val _uiState = MutableStateFlow(ScopedGrantDetailUiState())

    val uiState: Flow<ScopedGrantDetailUiState> = _uiState.asStateFlow()

    val entityUid: Long = savedStateHandle[ARG_ENTITY_UID]?.toLong() ?: 0

    init {

        val accountManager: UstadAccountManager by instance()

        _appUiState.update { prev ->
            prev.copy(
                loadingState = LoadingUiState.INDETERMINATE,
                fabState = FabUiState(
                    visible = false,
                    text = systemImpl.getString(MessageID.edit),
                    icon = FabUiState.FabIcon.EDIT,
                    onClick = this::onClickEdit,
                )
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch{

                    activeDb.scopedGrantDao.findByUidFlow(entityUid)
                        .collect{ scopedGrant ->
                            val list: List<BitmaskFlag> =
                                getPermissionAsList(scopedGrant?.sgPermissions?:0)

                            _uiState.update { prev ->
                                prev.copy(
                                    scopedGrant = scopedGrant,
                                    bitmaskList = list
                                )
                            }
                            _appUiState.update { prev ->
                                prev.copy(
                                    loadingState = if(scopedGrant != null){ NOT_LOADING } else { INDETERMINATE}
                                )
                            }
                        }
                }

                launch {
                    activeRepo.scopedGrantDao.userHasSystemLevelPermissionAsFlow(
                        accountManager.activeAccount.personUid, Role.PERMISSION_CLAZZ_UPDATE
                    ).collect{ hasUpdatePermission ->
                        _appUiState.update{ prev ->
                            prev.copy(
                                fabState = if(hasUpdatePermission){
                                    FabUiState(
                                        visible = true,
                                        text = systemImpl.getString(MessageID.edit),
                                        icon = FabUiState.FabIcon.EDIT,
                                        onClick = this@ScopedGrantDetailViewModel::onClickEdit
                                    )
                                }else{
                                    FabUiState()
                                }
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            _appUiState.whenSubscribed {
                activeDb.scopedGrantDao.userHasSystemLevelPermissionAsFlow(
                    accountManager.activeAccount.personUid, Role.PERMISSION_CLAZZ_UPDATE
                ).distinctUntilChanged().collect{ hasUpdatePermission ->
                    _appUiState.update { prev ->
                        prev.copy(
                            fabState = prev.fabState.copy(visible = hasUpdatePermission)
                        )
                    }
                }
            }
        }

    }


    private fun getPermissionAsList(permission: Long): List<BitmaskFlag> {

        val enabledPermissions = MutableLiveData(
            ScopedGrantEditPresenter.PERMISSION_MESSAGE_ID_LIST.map{
                it.toBitmaskFlag(permission)
            }.filter {

                it.enabled
            }
        )

        return enabledPermissions.getValue()?: emptyList()

    }


    fun onClickEdit(){
        navController.navigate(ScopedGrantEditView.VIEW_NAME,
            mapOf(ARG_ENTITY_UID to entityUid.toString()))
    }


}