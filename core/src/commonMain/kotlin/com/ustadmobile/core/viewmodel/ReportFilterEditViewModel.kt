package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportFilter2
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI

data class ReportFilterEditUiState(
    val filters: ReportFilter2? = null
)

class ReportFilterEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(ReportFilterEditUiState())
    val uiState: StateFlow<ReportFilterEditUiState> = _uiState

    init {
        loadingState = LoadingUiState.INDETERMINATE
        val title = systemImpl.getString(MR.strings.edit_filters)

        _appUiState.update {
            AppUiState(
                title = title,
                hideBottomNavigation = true
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.save),
                    onClick = this@ReportFilterEditViewModel::onClickSave
                )
            )
        }
    }
    fun onClickSave() {

    }
    companion object {

        const val DEST_NAME = "ReportFilterEdit"
        const val DEST_NAME_HOME = "ReportFilterEditHome"
        const val RESULT_KEY_REPORT = "arg"
        const val ARG = "arg"
    }
}