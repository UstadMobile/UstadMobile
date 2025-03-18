package com.ustadmobile.core.viewmodel.report.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState.Companion.INDETERMINATE
import com.ustadmobile.core.impl.appstate.LoadingUiState.Companion.NOT_LOADING
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.report.edit.ReportEditViewModel
import com.ustadmobile.lib.db.composites.StatementReportRow
import com.ustadmobile.lib.db.entities.Report
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

data class ReportDetailUiState(
    val report: Report? = null,
    val dialogVisible: Boolean = false,
    val reportOptions2: ReportOptions2 = ReportOptions2(),
    val reportResults: List<List<StatementReportRow>> = emptyList(),
    val errorMessage: String? = null // Add error handling
)

class ReportDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : DetailViewModel<Report>(di, savedStateHandle, DEST_NAME) {

    private val reportUid = savedStateHandle[ARG_ENTITY_UID]?.toLong() ?: 0
    private val runReportUseCase: RunReportUseCase by di.onActiveEndpoint().instance()

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: Flow<ReportDetailUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                loadingState = INDETERMINATE,
                fabState = FabUiState(
                    visible = false,
                    text = systemImpl.getString(MR.strings.edit),
                    icon = FabUiState.FabIcon.EDIT,
                    onClick = this::onClickEdit,
                )
            )
        }
        _appUiState.update { prev ->
            prev.copy(
                fabState =
                    FabUiState(
                        visible = true,
                        text = systemImpl.getString(MR.strings.edit),
                        icon = FabUiState.FabIcon.EDIT,
                        onClick = this@ReportDetailViewModel::onClickEdit
                    ),
                title = "Graph title",
            )
        }
        viewModelScope.launch {
            _uiState.whenSubscribed {
                launchIfHasPermission(
                    setLoadingState = true,
                    permissionCheck = { true }
                ) {
                    val siteFlow = activeRepo.reportDao().findByUidLive(reportUid)
                    launch {
                        siteFlow.collect { report ->
                            try {
                                val reportNonNull = report
                                    ?: throw IllegalStateException("Report not found for uid $reportUid")
                                val optionsJson = reportNonNull.reportOptions
                                val reportTitle = reportNonNull.reportTitle

                                val parsedOptions = when {
                                    !optionsJson.isNullOrBlank() -> {
                                        try {
                                            Json.decodeFromString(
                                                ReportOptions2.serializer(),
                                                optionsJson
                                            )
                                        } catch (e: Exception) {
                                            throw IllegalArgumentException("Invalid report options format")
                                        }
                                    }

                                    !reportTitle.isNullOrBlank() -> {
                                        ReportOptions2(title = reportTitle)
                                    }

                                    else -> throw IllegalStateException("Report $reportUid has no options or title")
                                }

                                _uiState.update { it.copy(reportOptions2 = parsedOptions) }

                                val request = RunReportUseCase.RunReportRequest(
                                    reportUid = reportUid,
                                    reportOptions = parsedOptions,
                                    accountPersonUid = activeUserPersonUid,
                                    timeZone = TimeZone.currentSystemDefault()
                                )
                                runReport(request)

                            } catch (e: Exception) {
                                _appUiState.update { it.copy(loadingState = NOT_LOADING) }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun runReport(request: RunReportUseCase.RunReportRequest) {
        _appUiState.update { it.copy(loadingState = INDETERMINATE) }
        runReportUseCase(request)
            .onEach { result ->
                _uiState.update { prev ->
                    prev.copy(reportResults = result.results)
                }
                println("Flow: ${result.results}")
            }
            .catch { e ->
                println("Flow error: $e")
            }
            .launchIn(viewModelScope)
            .invokeOnCompletion { cause ->
                _appUiState.update { it.copy(loadingState = NOT_LOADING) }
                cause?.let { println("Flow Completion cause: $it") }
            }
    }


    fun onClickEdit() {
        navController.navigate(
            ReportEditViewModel.DEST_NAME,
            mapOf(ARG_ENTITY_UID to reportUid.toString())
        )
    }

    fun onDismissDialog() {
        _uiState.update { prev -> prev.copy(dialogVisible = false) }

    }

    fun onShowDialog() {
        _uiState.update { prev -> prev.copy(dialogVisible = true) }

    }

    companion object {
        const val DEST_NAME = "ReportDetailView"
        const val RESULT_KEY_REPORT_DETAIL = "detailReport"
    }
}