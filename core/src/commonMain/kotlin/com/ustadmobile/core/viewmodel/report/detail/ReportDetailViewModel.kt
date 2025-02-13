package com.ustadmobile.core.viewmodel.report.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState.Companion.INDETERMINATE
import com.ustadmobile.core.impl.appstate.LoadingUiState.Companion.NOT_LOADING
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.report.edit.ReportEditViewModel
import com.ustadmobile.lib.db.entities.Report
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.kodein.di.DI

data class ReportDetailUiState(
    val report: Report? = null,
    val dialogVisible: Boolean = false,
    val reportOptions2: ReportOptions2 = ReportOptions2()
)

class ReportDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
) : DetailViewModel<Report>(di, savedStateHandle, DEST_NAME) {

    private val reportUid = savedStateHandle[ARG_ENTITY_UID]?.toLong() ?: 0

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
                    val siteFlow =activeRepo.reportDao().findByUidLive(reportUid)
                    launch {
                        siteFlow.collect{
                            val reportOptions = it?.let { optn ->
                                optn.reportOptions?.takeIf { options -> options.isNotBlank() }
                                    ?.let { options ->
                                        Json.decodeFromString(
                                            ReportOptions2.serializer(),
                                            options
                                        )
                                    } ?: ReportOptions2(title = optn.reportTitle ?: "")
                            }
                            _uiState.update { prev ->
                                prev.copy(
                                    reportOptions2 = reportOptions ?: ReportOptions2()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getReport(report: Report?) {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                _appUiState.update { prev ->
                    prev.copy(
                        title = report?.reportTitle ?: "",
                        loadingState = if (report != null) {
                            NOT_LOADING
                        } else {
                            INDETERMINATE
                        }
                    )
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