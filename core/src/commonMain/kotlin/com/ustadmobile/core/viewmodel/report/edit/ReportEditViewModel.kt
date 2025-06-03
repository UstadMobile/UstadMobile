package com.ustadmobile.core.viewmodel.report.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.RelativeRangeReportPeriod
import com.ustadmobile.core.domain.report.model.ReportFilter3
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.replace
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditViewModel
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Report
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.di.DI

data class ReportEditUiState(
    val reportOptions2: ReportOptions2 = ReportOptions2(),
    val reportTitleError: String? = null,
    val xAxisError: String? = null,
    val seriesTitleErrors: Map<Int, String> = emptyMap(),
    val yAxisErrors: Map<Int, String> = emptyMap(),
    val subGroupError: String? = null,
    val chartTypeError: Map<Int, String> = emptyMap(),
    val timeRangeError: String? = null,
    val quantityError: String? = null,
    val submitted: Boolean = false
)

class ReportEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String = DEST_NAME
) : UstadEditViewModel(di, savedStateHandle, destName) {

    private val _uiState: MutableStateFlow<ReportEditUiState> =
        MutableStateFlow(ReportEditUiState())
    val uiState: Flow<ReportEditUiState> = _uiState.asStateFlow()
    private val entityUid: Long
        get() = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0
    private var nextTempFilterUid = -1

    init {
        loadingState = LoadingUiState.INDETERMINATE
        val title = if (entityUid == 0L)
            systemImpl.getString(MR.strings.add_a_new_report)
        else
            systemImpl.getString(MR.strings.edit_report)

        _appUiState.update {
            AppUiState(
                title = title,
                hideBottomNavigation = false
            )
        }
        launchIfHasPermission(
            setLoadingState = true,
            permissionCheck = { true }
        ) {
            loadEntity(
                serializer = ReportOptions2.serializer(),
                onLoadFromDb = { db ->
                    val report = db.reportDao().findByUid(entityUidArg)
                    report?.let {
                        it.reportOptions?.takeIf { options -> options.isNotBlank() }
                            ?.let { options ->
                                Json.decodeFromString(ReportOptions2.serializer(), options)
                            } ?: ReportOptions2(title = it.reportTitle ?: "")
                    }
                },
                makeDefault = {
                    ReportOptions2(
                        title = "",
                        series = listOf(
                            ReportSeries2(
                                reportSeriesUid = 1,
                                reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
                                reportSeriesSubGroup = null,
                                reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,
                                reportSeriesFilters = emptyList()
                            )
                        )
                    )
                },
                uiUpdate = { loadedReport ->
                    _uiState.update { prev ->
                        prev.copy(
                            reportOptions2 = loadedReport ?: ReportOptions2()
                        )
                    }
                }
            )

            launch {
                navResultReturner.filteredResultFlowForKey(RESULT_KEY_REPORT_FILTER)
                    .collect { result ->
                        val reportFilter = result.result as? ReportFilter3 ?: return@collect
                        val seriesId = reportFilter.reportFilterSeriesUid
                        onFilterChanged(reportFilter, seriesId)
                    }
            }
        }

        _appUiState.update { prev ->
            prev.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.done),
                    onClick = this@ReportEditViewModel::onClickSave
                )
            )
        }
    }

    fun onClickSave() {
        val requiredFieldMessage = systemImpl.getString(MR.strings.field_required_prompt)
        val currentReport = _uiState.value.reportOptions2

        // Validate quantity if timeRange is RelativeRangeReportPeriod
        val quantityError = if (currentReport.period is RelativeRangeReportPeriod) {
            val qty = (currentReport.period).rangeQuantity
            if (qty < 1) systemImpl.getString(MR.strings.quantity_must_be_at_least_1) else null
        } else {
            null
        }

        // Validate series-specific fields
        val seriesTitleErrors = currentReport.series.mapNotNull { series ->
            series.reportSeriesTitle.takeIf { it.isEmpty() }
                ?.let { series.reportSeriesUid to requiredFieldMessage }
        }.toMap()

        val yAxisErrors = currentReport.series.mapNotNull { series ->
            if (series.reportSeriesYAxis == null) {
                series.reportSeriesUid to requiredFieldMessage
            } else {
                null
            }
        }.toMap()

        val chartTypeErrors = currentReport.series.mapNotNull { series ->
            if (series.reportSeriesVisualType == null) {
                series.reportSeriesUid to requiredFieldMessage
            } else {
                null
            }
        }.toMap()

        // Validate all fields
        val newState = _uiState.value.copy(
            submitted = true,
            reportTitleError = if (currentReport.title.isEmpty()) requiredFieldMessage else null,
            xAxisError = if (currentReport.xAxis == null) requiredFieldMessage else null,
            timeRangeError = if (currentReport.period == null) requiredFieldMessage else null,
            quantityError = quantityError,
            chartTypeError = chartTypeErrors,
            yAxisErrors = yAxisErrors,
            seriesTitleErrors = seriesTitleErrors
        )

        _uiState.value = newState

        if (newState.hasErrors()) {
            loadingState = LoadingUiState.NOT_LOADING
            println("Report options error")
            return
        }
        viewModelScope.launch {
            activeRepoWithFallback.withDoorTransactionAsync {
                val currentReports = _uiState.value.reportOptions2
                val report = Report(
                    reportUid = entityUidArg,
                    reportTitle = currentReports.title,
                    reportOptions = json.encodeToString(currentReports),
                    reportOwnerPersonUid = activeUserPersonUid,
                )
                try {
                    if (entityUidArg == 0L) {
                        activeRepoWithFallback.reportDao().insertAsync(report)
                        println("Report options inserted successfully: ${report}")
                    } else {
                        activeRepoWithFallback.reportDao().updateAsync(report)
                        println("Report options updated successfully: ${report}")
                    }
                } catch (e: Exception) {
                    println("Error updating report options: ${e.message}")
                } finally {
                    finishWithResult(ReportDetailViewModel.DEST_NAME, report.reportUid, report)
                }
            }
        }
    }

    fun onEntityChanged(newOptions: ReportOptions2) {
        val requiredFieldMessage = systemImpl.getString(MR.strings.field_required_prompt)
        val quantityError = when (val timeRange = newOptions.period) {
            is RelativeRangeReportPeriod -> {
                if (timeRange.rangeQuantity < 1) systemImpl.getString(MR.strings.quantity_must_be_at_least_1) else null
            }

            else -> null
        }

        val seriesTitleErrors = newOptions.series.associate { series ->
            series.reportSeriesUid to if (series.reportSeriesTitle.isEmpty()) requiredFieldMessage else null
        }.filterValues { it != null }
            .mapValues { it.value as String }

        val yAxisErrors = newOptions.series.associate { series ->
            series.reportSeriesUid to if (series.reportSeriesYAxis == null) requiredFieldMessage else null
        }.filterValues { it != null }
            .mapValues { it.value as String }

        val chartTypeErrors = newOptions.series.associate { series ->
            series.reportSeriesUid to if (series.reportSeriesVisualType == null) requiredFieldMessage else null
        }.filterValues { it != null }
            .mapValues { it.value as String }

        _uiState.update { currentState ->
            val baseUpdate = currentState.copy(reportOptions2 = newOptions)

            if (!baseUpdate.submitted) return@update baseUpdate
            baseUpdate.copy(
                reportTitleError = if (newOptions.title.isEmpty()) systemImpl.getString(MR.strings.field_required_prompt) else null,
                xAxisError = if (newOptions.xAxis == null) systemImpl.getString(MR.strings.field_required_prompt) else null,
                seriesTitleErrors = seriesTitleErrors,
                yAxisErrors = yAxisErrors,
                timeRangeError = updateErrorMessageOnChange(
                    currentState.reportOptions2.period,
                    newOptions.period,
                    currentState.timeRangeError
                ),
                quantityError = quantityError,
                chartTypeError = chartTypeErrors,
            )
        }
        scheduleEntityCommitToSavedState(
            entity = newOptions,
            serializer = ReportOptions2.serializer(),
            commitDelay = 200
        )
    }

    fun onSeriesChanged(updatedSeries: ReportSeries2) {
        onEntityChanged(
            _uiState.value.reportOptions2.let { reportOptions ->
                reportOptions.copy(
                    series = reportOptions.series.replace(updatedSeries) {
                        it.reportSeriesUid == updatedSeries.reportSeriesUid
                    }
                )
            }
        )
    }

    private fun onFilterChanged(newFilter: ReportFilter3, seriesId: Int) {
        _uiState.update { prevState ->
            val updatedSeries = prevState.reportOptions2.series.map { series ->
                if (series.reportSeriesUid == seriesId) {
                    val currentFilters = series.reportSeriesFilters.orEmpty().toMutableList()
                    // Check if the filter already exists by UID
                    val existingIndex = currentFilters.indexOfFirst {
                        it.reportFilterUid == newFilter.reportFilterUid
                    }
                    if (existingIndex != -1) {
                        // Replace existing filter
                        currentFilters[existingIndex] = newFilter
                    } else {
                        // Append new filter
                        currentFilters.add(newFilter)
                    }
                    series.copy(reportSeriesFilters = currentFilters)
                } else {
                    series
                }
            }

            prevState.copy(
                reportOptions2 = prevState.reportOptions2.copy(series = updatedSeries)
            )
        }
        onEntityChanged(_uiState.value.reportOptions2)
    }

    fun onAddFilter(seriesId: Int) {
        val tempFilterUid = nextTempFilterUid-- // Generate a new temporary UID
        navigateForResult(
            nextViewName = ReportFilterEditViewModel.DEST_NAME,
            key = RESULT_KEY_REPORT_FILTER,
            currentValue = null,
            serializer = ReportFilter3.serializer(), // FIXED SERIALIZER
            args = mapOf(
                ARG_REPORT_SERIES_UID to seriesId.toString(),
                ReportFilterEditViewModel.ARG_TEMP_FILTER_UID to tempFilterUid.toString()
            )
        )
    }


    fun onAddSeries() {
        _uiState.update { prev ->
            val newUid = (prev.reportOptions2.series.maxOfOrNull { it.reportSeriesUid } ?: 0) + 1
            prev.copy(
                reportOptions2 = prev.reportOptions2.copy(
                    series = prev.reportOptions2.series + ReportSeries2(
                        reportSeriesUid = newUid,
                        reportSeriesTitle = "Series $newUid", // Set default title here
                        reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
                        reportSeriesSubGroup = null,
                        reportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION
                    ),
                )
            )
        }
    }

    fun onRemoveSeries(seriesId: Int) {
        _uiState.update { prev ->
            val updatedSeriesList =
                prev.reportOptions2.series.filterNot { it.reportSeriesUid == seriesId }

            prev.copy(
                reportOptions2 = prev.reportOptions2.copy(
                    series = updatedSeriesList
                )
            )
        }
    }

    fun onRemoveFilter(index: Int, seriesId: Int) {
        _uiState.update { prev ->
            val updatedSeriesList = prev.reportOptions2.series.map { series ->
                if (series.reportSeriesUid == seriesId) {
                    val updatedFilters = series.reportSeriesFilters?.toMutableList()?.apply {
                        removeAt(index)
                    }
                    series.copy(reportSeriesFilters = updatedFilters)
                } else {
                    series
                }
            }

            prev.copy(
                reportOptions2 = prev.reportOptions2.copy(
                    series = updatedSeriesList
                )
            )
        }
    }

    fun onEditFilter(seriesId: Int, filter: ReportFilter3) {
        navigateForResult(
            nextViewName = ReportFilterEditViewModel.DEST_NAME,
            key = RESULT_KEY_REPORT_FILTER,
            currentValue = null,
            serializer = Report.serializer(),
            args = mapOf(
                ARG_REPORT_SERIES_UID to seriesId.toString(),
                ReportFilterEditViewModel.ARG_EXISTING_FILTER to json.encodeToString(filter)
            )
        )
    }

    fun ReportEditUiState.hasErrors(): Boolean {
        if (!submitted) return false
        return reportTitleError != null ||
                xAxisError != null ||
                seriesTitleErrors.isNotEmpty() ||
                yAxisErrors.isNotEmpty() ||
                subGroupError != null ||
                chartTypeError.isNotEmpty() ||
                timeRangeError != null ||
                quantityError != null
    }

    companion object {
        const val DEST_NAME = "ReportEdit"
        const val RESULT_KEY_REPORT_FILTER = "reportFilter"
        const val ARG_REPORT_SERIES_UID = "reportSeriesUid"
    }
}