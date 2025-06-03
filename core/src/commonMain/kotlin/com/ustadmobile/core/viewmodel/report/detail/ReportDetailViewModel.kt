package com.ustadmobile.core.viewmodel.report.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState.Companion.INDETERMINATE
import com.ustadmobile.core.impl.appstate.LoadingUiState.Companion.NOT_LOADING
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.onActiveLearningSpace
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.report.edit.ReportEditViewModel
import com.ustadmobile.lib.db.composites.StatementReportRow
import com.ustadmobile.lib.db.entities.Report
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.kodein.di.DI
import org.kodein.di.instance

data class ReportDetailUiState(
    val report: Report? = null,
    val reportOptions2: ReportOptions2 = ReportOptions2(),
    val reportResults: List<List<StatementReportRow>> = emptyList(),
    val errorMessage: String? = null
)

class ReportDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : DetailViewModel<Report>(di, savedStateHandle, DEST_NAME) {

    private val reportUid = savedStateHandle[ARG_ENTITY_UID]?.toLong() ?: 0
    private val runReportUseCase: RunReportUseCase by di.onActiveLearningSpace().instance()

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
                    )
            )
        }
        viewModelScope.launch {
            _uiState.whenSubscribed {
                launchIfHasPermission(
                    setLoadingState = true,
                    permissionCheck = { true }
                ) {
                    val reportFlow = activeRepoWithFallback.reportDao().findByUidLive(reportUid)
                    launch {
                        reportFlow
                            .distinctUntilChanged { old, new ->
                                old?.reportLastModTime == new?.reportLastModTime
                            }
                            .collectLatest { report ->
                                try {
                                    val reportNonNull = report
                                        ?: throw IllegalStateException("Report not found for uid $reportUid")
                                    val optionsJson = reportNonNull.reportOptions
                                    val reportTitle = reportNonNull.reportTitle

                                    val parsedOptions = when {
                                        !optionsJson.isNullOrBlank() -> {
                                            try {
                                                json.decodeFromString(
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
                                    _appUiState.update { prev ->
                                        prev.copy(title = parsedOptions.title)
                                    }
                                    val request = RunReportUseCase.RunReportRequest(
                                        reportUid = reportUid,
                                        reportOptions = parsedOptions,
                                        accountPersonUid = activeUserPersonUid,
                                        timeZone = TimeZone.currentSystemDefault()
                                    )
                                    runReportUseCase(request).collect { reportResult ->
                                        _uiState.update { prev ->
                                            prev.copy(reportResults = reportResult.results)
                                        }
                                    }

                                } catch (e: Exception) {
                                    val errorMsg = when (e) {
                                        is IllegalArgumentException -> systemImpl.getString(
                                            MR.strings.invalid_report_format
                                        )

                                        is IllegalStateException -> e.message
                                            ?: systemImpl.getString(MR.strings.invalid_report_config)

                                        else -> e.message
                                            ?: systemImpl.getString(MR.strings.unknown_error)
                                    }
                                    _uiState.update {
                                        it.copy(errorMessage = errorMsg)
                                    }
                                } finally {
                                    _appUiState.update { it.copy(loadingState = NOT_LOADING) }
                                }
                            }
                    }
                }
            }
        }
    }

    fun onClickEdit() {
        navController.navigate(
            ReportEditViewModel.DEST_NAME,
            mapOf(ARG_ENTITY_UID to reportUid.toString())
        )
    }

    companion object {
        const val DEST_NAME = "ReportDetailView"
    }
}