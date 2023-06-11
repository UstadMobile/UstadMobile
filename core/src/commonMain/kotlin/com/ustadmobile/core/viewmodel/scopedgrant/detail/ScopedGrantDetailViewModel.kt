package com.ustadmobile.core.viewmodel.scopedgrant.detail

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.ScopedGrantDetailView
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.lib.db.entities.ScopedGrant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI


data class ScopedGrantDetailUiState(
    val scopedGrant: ScopedGrant? = null,
)

class ScopedGrantDetailViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle
): DetailViewModel<ScopedGrant>(di, savedStateHandle, ScopedGrantDetailView.VIEW_NAME) {

    private val _uiState = MutableStateFlow(ScopedGrantDetailUiState())

    val uiState: Flow<ScopedGrantDetailUiState> = _uiState.asStateFlow()

    val entityUid: Long = savedStateHandle[ARG_ENTITY_UID]?.toLong() ?: 0

    init {


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

                }
            }
        }



    }

    fun onClickEdit(){
        navController.navigate(ScopedGrantEditView.VIEW_NAME,
            mapOf(ARG_ENTITY_UID to entityUid.toString()))
    }


}