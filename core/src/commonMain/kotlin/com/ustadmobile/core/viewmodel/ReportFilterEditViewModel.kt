package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportFilter3
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.report.ReportEditViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI

data class ReportFilterEditUiState(
    val filters: ReportFilter3? = ReportFilter3()
)

class ReportFilterEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    val seriesId: Int = savedStateHandle["reportSeriesUid"]?.toInt() ?: 0

    private val _uiState = MutableStateFlow(
        ReportFilterEditUiState(
            filters = ReportFilter3(reportFilterSeriesUid = seriesId)
        )
    )
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
                    text = systemImpl.getString(MR.strings.done),
                    onClick = this@ReportFilterEditViewModel::onClickSave
                )
            )
        }
    }

    fun onClickSave() {
        val filter = uiState.value.filters
        finishWithResult(
            ReportEditViewModel.DEST_NAME,
            entityUid = filter?.reportFilterUid?.toLong() ?: 0,
            result = mapOf(
                "filter" to filter?.copy(reportFilterSeriesUid = seriesId),
                "reportSeriesUid" to seriesId
            )
        )
    }


    fun onEntityChanged(value: ReportFilter3?) {
        _uiState.update { currentState ->
            currentState.copy(
                filters = value?.copy(
                    reportFilterSeriesUid = currentState.filters?.reportFilterSeriesUid ?: 0
                )
            )
        }
    }


    companion object {

        const val DEST_NAME = "ReportFilterEdit"
        const val DEST_NAME_HOME = "ReportFilterEditHome"
        const val ARG = "arg"
    }
}