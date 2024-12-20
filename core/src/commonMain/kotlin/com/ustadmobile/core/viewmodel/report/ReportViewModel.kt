package com.ustadmobile.core.viewmodel.report

import com.ustadmobile.core.domain.report.model.Report2
import com.ustadmobile.core.domain.report.model.ReportFilter2
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI

class ReportViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String,
) : UstadEditViewModel(di, savedStateHandle, CourseBlockEditViewModel.DEST_NAME) {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState

    fun onXAxisChanged(newXAxis: MessageIdOption2) {
        _uiState.value = _uiState.value.copy(selectedXAxis = newXAxis.value)
    }

    fun onYAxisChanged(newYAxis: MessageIdOption2) {
        _uiState.value = _uiState.value.copy(selectedYAxis = newYAxis.value)
    }

    fun onSeriesTitleChanged(newTitle: String) {
        _uiState.value = _uiState.value.copy(seriesTitle = newTitle)
    }

    // Update subgroup selection
    fun onSubgroupChanged(newSubgroup: MessageIdOption2) {
        _uiState.value = _uiState.value.copy(selectedXAxis = newSubgroup.value)
    }

    // Update chart type selection
    fun onChartTypeChanged(newChartType: MessageIdOption2) {
        _uiState.value = _uiState.value.copy(selectedChartType = newChartType.value)

    }

    // Update time range selection
    fun onTimeRangeChanged(newTimeRange: MessageIdOption2) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = newTimeRange.value)

    }

    // Add a new filter
    fun onAddFilter() {
    }

    // Add a new series
    fun onAddSeries() {
        // Logic for adding series goes here (e.g., navigating to another screen or updating state)
    }

    fun onRemoveFilter(index: Int) {

    }

    companion object {
        const val DEST_NAME = "Report"
        const val DEST_NAME_HOME = "ReportHome"
        val ALL_DEST_NAMES = listOf(DEST_NAME, DEST_NAME_HOME)

    }
}

data class ReportUiState(
    val xAxisOptions: List<MessageIdOption2> = listOf(),
    val yAxisOptions: List<MessageIdOption2> = listOf(),
    val subgroupOptions: List<MessageIdOption2> = listOf(),
    val chartTypeOptions: List<MessageIdOption2> = listOf(),
    val timeRangeOptions: List<MessageIdOption2> = listOf(),
    val selectedXAxis: Int = 0,
    val selectedYAxis: Int = 0,
    val selectedSubgroup: Int = 0,
    val selectedChartType: Int = 0,
    val selectedTimeRange: Int = 0,
    val seriesTitle: String = "",
    val report2: Report2? = null,
    val filters: List<ReportFilter2> = listOf(),
    val xAxisError: String? = null,
    val yAxisError: String? = null,
    val subgroupError: String? = null,
    val chartTypeError: String? = null,
    val timeRangeError: String? = null,
    val fieldsEnabled: Boolean = true
)