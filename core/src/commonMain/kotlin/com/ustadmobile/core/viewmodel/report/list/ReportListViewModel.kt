package com.ustadmobile.core.viewmodel.report.list

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.formatter.CreateGraphFormatterUseCase
import com.ustadmobile.core.domain.report.formatter.GraphFormatter
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState.Companion.NOT_LOADING
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.onActiveLearningSpace
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.core.viewmodel.report.edit.ReportEditViewModel
import com.ustadmobile.lib.db.entities.Report
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import org.kodein.di.DI
import org.kodein.di.instance

data class ReportListUiState(
    val reportList: () -> PagingSource<Int, Report> = { EmptyPagingSource() },
    val addSheetOrDialogVisible: Boolean = false,
    val activeUserPersonUid: Long = 0L,
    val xAxisFormatter: GraphFormatter<String>? = null,
    val yAxisFormatter: GraphFormatter<Double>? = null
)

class ReportListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
) : UstadListViewModel<ReportListUiState>(
    di, savedStateHandle, ReportListUiState(), destinationName
) {

    private val runReportUseCase: RunReportUseCase by di.onActiveLearningSpace().instance()
    private val createGraphFormatterUseCase: CreateGraphFormatterUseCase by instance()

    private val pagingSourceFactory: () -> PagingSource<Int, Report> = {
        activeRepoWithFallback.reportDao().findAllReports()
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(visible = false),
                title = savedStateHandle[ARG_TITLE] ?: listTitle(
                    MR.strings.reports,
                    MR.strings.select_person
                ),
                fabState = FabUiState(
                    text = systemImpl.getString(MR.strings.report),
                    icon = FabUiState.FabIcon.ADD,
                    onClick = this::onClickAdd,
                )
            )
        }
        _appUiState.update { prev ->
            prev.copy(
                fabState = prev.fabState.copy(
                    visible = true
                )
            )
        }
        viewModelScope.launch {
            _uiState.whenSubscribed {
                _uiState.update { prev ->
                    prev.copy(
                        reportList = pagingSourceFactory,
                        activeUserPersonUid = activeUserPersonUid
                    )
                }
            }
        }
    }

    override fun onUpdateSearchResult(searchText: String) {

    }

    fun runReport(report: Report): Flow<RunReportUseCase.RunReportResult> = flow {
        try {
            val reportOptions = report.reportOptions?.let {
                json.decodeFromString<ReportOptions2>(it)
            }

            val request = reportOptions?.let {
                RunReportUseCase.RunReportRequest(
                    reportUid = report.reportUid,
                    reportOptions = it,
                    accountPersonUid = activeUserPersonUid,
                    timeZone = TimeZone.currentSystemDefault()
                )
            }

            if (request != null) {
                runReportUseCase(request).collect { reportResult ->
                    val xAxisFormatter = createGraphFormatterUseCase(
                        reportResult = reportResult,
                        options = CreateGraphFormatterUseCase.FormatterOptions(
                            paramType = String::class,
                            axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.X_AXIS_VALUES
                        )
                    )

                    val yAxisFormatter = createGraphFormatterUseCase(
                        reportResult = reportResult,
                        options = CreateGraphFormatterUseCase.FormatterOptions(
                            paramType = Double::class,
                            axis = CreateGraphFormatterUseCase.FormatterOptions.Axis.Y_AXIS_VALUES
                        )
                    )
                    _uiState.update { currentState ->
                        currentState.copy(
                            xAxisFormatter = xAxisFormatter,
                            yAxisFormatter = yAxisFormatter
                        )
                    }
                    emit(reportResult)
                }
            }
        } catch (e: Exception) {
            val errorResult = RunReportUseCase.RunReportResult(
                timestamp = Clock.System.now().toEpochMilliseconds(),
                request = RunReportUseCase.RunReportRequest(
                    reportUid = report.reportUid,
                    reportOptions = ReportOptions2(), // default empty options
                    accountPersonUid = activeUserPersonUid,
                    timeZone = TimeZone.currentSystemDefault()
                ),
                results = emptyList()
            )
            emit(errorResult)
            throw e
        } finally {
            _appUiState.update { it.copy(loadingState = NOT_LOADING) }
        }
    }

    override fun onClickAdd() {
        navigateToCreateNew(
            ReportEditViewModel.DEST_NAME,
            savedStateHandle[ARG_GO_TO_ON_REPORT_SELECTED]?.let {
                mapOf(
                    ARG_POPUP_TO_ON_REPORT_SELECTED to it
                )
            } ?: emptyMap())
    }

    fun onClickEntry(entry: Report) {
        navigateOnItemClicked(ReportDetailViewModel.DEST_NAME, entry.reportUid, entry)
    }

    fun onRemoveReport(uid: Long) {
        viewModelScope.launch {
            activeRepoWithFallback.reportDao().deleteReportByUid(uid)
        }
    }

    companion object {
        const val DEST_NAME = "Report"
        const val DEST_NAME_HOME = "ReportListHome"
        const val ARG_GO_TO_ON_REPORT_SELECTED = "goToOnReportSelected"
        const val ARG_POPUP_TO_ON_REPORT_SELECTED = "popUpToOnReportSelected"
        val ALL_DEST_NAMES = listOf(DEST_NAME, DEST_NAME_HOME)
    }
}