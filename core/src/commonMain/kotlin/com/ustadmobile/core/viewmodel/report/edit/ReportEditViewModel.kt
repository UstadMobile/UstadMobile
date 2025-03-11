package com.ustadmobile.core.viewmodel.report.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.RelativeReportTimeRange
import com.ustadmobile.core.domain.report.model.ReportFilter3
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.replace
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditViewModel
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditViewModel.Companion.ARG_REPORT_SERIES_UID
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
    val yAxisError: String? = null,
    val seriesTitleError: String? = null,
    val subGroupError: String? = null,
    val chartTypeError: String? = null,
    val timeRangeError: String? = null,
    val quantityError: String? = null,
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
                                reportSeriesVisualType = null,
                                reportSeriesSubGroup = null,
                                reportSeriesYAxis = null,
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

        // Validate quantity if timeRange is RelativeReportTimeRange
        val quantityError = if (currentReport.timeRange is RelativeReportTimeRange) {
            val qty = (currentReport.timeRange as RelativeReportTimeRange).reportUnitQuantity
            if (qty < 1) systemImpl.getString(MR.strings.quantity_must_be_at_least_1) else null
        } else {
            null
        }
        _uiState.update { prev ->
            prev.copy(
                reportTitleError = if (prev.reportOptions2.title.isEmpty()) {
                    requiredFieldMessage
                } else {
                    null
                },
                xAxisError =  if (prev.reportOptions2.xAxis == null) {
                    requiredFieldMessage
                } else {
                    null
                },
                seriesTitleError = if (prev.reportOptions2.series.any { it.reportSeriesTitle.isEmpty() }) {
                    requiredFieldMessage
                } else {
                    null
                },
                yAxisError = if (prev.reportOptions2.series.any { it.reportSeriesYAxis == null }) {
                    requiredFieldMessage
                } else {
                    null
                },
                timeRangeError = if (prev.reportOptions2.timeRange == null) {
                    requiredFieldMessage
                } else {
                    null
                },
                quantityError = quantityError,
                )
        }
        if (_uiState.value.hasErrors()) {
            loadingState = LoadingUiState.NOT_LOADING
            return
        }
        viewModelScope.launch {
            activeRepo.withDoorTransactionAsync {
                val currentReport = _uiState.value.reportOptions2
                val report = Report(
                    reportUid = entityUidArg,
                    reportTitle = currentReport.title,
                    reportOptions = json.encodeToString(currentReport),
                )
                try {
                    if (entityUidArg == 0L) {
                        activeRepo.reportDao().insertAsync(report)
                        println("Report options inserted successfully: ${report}")
                    } else {
                        activeRepo.reportDao().updateAsync(report)
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
        val quantityError = when (val timeRange = newOptions.timeRange) {
            is RelativeReportTimeRange -> {
                if (timeRange.reportUnitQuantity < 1) systemImpl.getString(MR.strings.quantity_must_be_at_least_1) else null
            }
            else -> null
        }
        _uiState.update { currentState ->
            currentState.copy(
                reportOptions2 = newOptions,
                reportTitleError = updateErrorMessageOnChange(
                    currentState.reportOptions2.title,
                    newOptions.title,
                    currentState.reportTitleError
                ),
                xAxisError = updateErrorMessageOnChange(
                    currentState.reportOptions2.xAxis,
                    newOptions.xAxis,
                    currentState.xAxisError
                ),
                seriesTitleError = updateErrorMessageOnChange(
                    currentState.reportOptions2.series.map { it.reportSeriesTitle },
                    newOptions.series.map { it.reportSeriesTitle },
                    currentState.seriesTitleError
                ),
                yAxisError = updateErrorMessageOnChange(
                    currentState.reportOptions2.series.map { it.reportSeriesYAxis },
                    newOptions.series.map { it.reportSeriesYAxis },
                    currentState.yAxisError
                ),
                timeRangeError = updateErrorMessageOnChange(
                    currentState.reportOptions2.timeRange,
                    newOptions.timeRange,
                    currentState.timeRangeError
                ),
                quantityError = quantityError
            )
        }
        scheduleEntityCommitToSavedState(
            entity = newOptions,
            serializer = ReportOptions2.serializer(),
            commitDelay = 200
        )
    }


    fun onSeriesChanged(updatedSeries: ReportSeries2) {
        _uiState.update { prev ->
            prev.copy(
                reportOptions2 = prev.reportOptions2.copy(
                    series = prev.reportOptions2.series.replace(updatedSeries) {
                        it.reportSeriesUid == updatedSeries.reportSeriesUid
                    }
                )
            )
        }
        onEntityChanged(_uiState.value.reportOptions2)
    }


    private fun onFilterChanged(filter2: ReportFilter3, seriesId: Int) {
        if (filter2.reportFilterField != null) {
            _uiState.update { prev ->
                val updatedSeriesList = prev.reportOptions2.series.map { series ->
                    if (series.reportSeriesUid == seriesId) {
                        val updatedFilters =
                            series.reportSeriesFilters?.toMutableList() ?: mutableListOf()
                        updatedFilters.add(filter2)
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
            onEntityChanged(_uiState.value.reportOptions2)
        }
    }


    fun onAddFilter(seriesId: Int) {
        navigateForResult(
            nextViewName = ReportFilterEditViewModel.DEST_NAME,
            key = RESULT_KEY_REPORT_FILTER,
            currentValue = null,
            serializer = Report.serializer(),
            args = mapOf(ARG_REPORT_SERIES_UID to seriesId.toString())
        )
    }


    fun onAddSeries() {
        _uiState.update { prev ->
            prev.copy(
                reportOptions2 = prev.reportOptions2.copy(
                    series = prev.reportOptions2.series + ReportSeries2(
                        reportSeriesUid = (prev.reportOptions2.series.maxOfOrNull { it.reportSeriesUid }
                            ?: 0) + 1,
                        reportSeriesVisualType = null,
                        reportSeriesSubGroup = null,
                        reportSeriesYAxis = null
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

    private fun ReportEditUiState.hasErrors(): Boolean {
        return reportTitleError != null ||
                xAxisError != null ||
                seriesTitleError != null ||
                subGroupError != null ||
                chartTypeError != null ||
                yAxisError != null ||
                timeRangeError != null ||
                quantityError != null
    }

    companion object {
        const val DEST_NAME = "ReportEdit"
        const val RESULT_KEY_REPORT_FILTER = "reportFilter"
    }
}