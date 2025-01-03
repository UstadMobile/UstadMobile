package com.ustadmobile.core.viewmodel.report.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportFilter3
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.replace
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditViewModel
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Report
import kotlinx.coroutines.async
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
)

class ReportEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String = DEST_NAME
) : UstadEditViewModel(di, savedStateHandle, destName) {

    private val _uiState: MutableStateFlow<ReportEditUiState> =
        MutableStateFlow(ReportEditUiState())
    val uiState: Flow<ReportEditUiState> = _uiState.asStateFlow()

    init {
        loadingState = LoadingUiState.INDETERMINATE
        val title = systemImpl.getString(MR.strings.edit_report)

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
            async {
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
                        ReportOptions2()
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
                    navResultReturner.filteredResultFlowForKey(RESULT_KEY_REPORT)
                        .collect { result ->
                            val reportFilter = result.result as? ReportFilter3 ?: return@collect
                            val seriesId = reportFilter.reportFilterSeriesUid
                            onFilterChanged(reportFilter, seriesId)
                        }
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
        viewModelScope.launch {
            activeRepo.withDoorTransactionAsync {
                val currentReport = _uiState.value.reportOptions2
                val report = Report(
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
                }
            }
        }
    }

    fun onEntityChanged(newOptions: ReportOptions2) {
        _uiState.update { currentState ->
            currentState.copy(reportOptions2 = newOptions)
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
            key = RESULT_KEY_REPORT,
            currentValue = null,
            serializer = Report.serializer(),
            args = mapOf("reportSeriesUid" to seriesId.toString())
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
                        reportTimeRange = null,
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

    companion object {
        const val DEST_NAME = "Report"
        const val DEST_NAME_HOME = "ReportHome"
        val ALL_DEST_NAMES = listOf(DEST_NAME, DEST_NAME_HOME)
        const val RESULT_KEY_REPORT = "arg"
    }
}