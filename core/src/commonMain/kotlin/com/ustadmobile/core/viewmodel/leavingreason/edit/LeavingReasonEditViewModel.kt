package com.ustadmobile.core.viewmodel.leavingreason.edit

import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.DI

data class LeavingReasonEditUiState(

    val fieldsEnabled: Boolean = true,

    val reasonTitleError: String? = null
)

class LeavingReasonEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, PersonEditView.VIEW_NAME) {

    private val _uiState: MutableStateFlow<LeavingReasonEditUiState> = MutableStateFlow(
        LeavingReasonEditUiState(
            fieldsEnabled = false,
        )
    )

    val uiState: Flow<LeavingReasonEditUiState> = _uiState.asStateFlow()

    init {
        loadingState = LoadingUiState.INDETERMINATE

    }

}