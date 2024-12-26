package com.ustadmobile.core.viewmodel.report

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ReportFilterEditViewModel
import com.ustadmobile.core.viewmodel.ReportFilterEditViewModel.Companion.RESULT_KEY_REPORT
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel.Companion.ARG_DATE_OF_BIRTH
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Report
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.di.DI

class ReportEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destName: String
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState: MutableStateFlow<ReportEditUiState> =
        MutableStateFlow(ReportEditUiState())
    val uiState: Flow<ReportEditUiState> = _uiState.asStateFlow()
    private val entityUid: Long
        get() = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

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
            onSetFieldsEnabled = { enabled ->
            },
            permissionCheck = { db ->
                db.systemPermissionDao().personHasSystemPermission(
                    activeUserPersonUid, PermissionFlags.MANAGE_SITE_SETTINGS
                )
            }
        ) {
            awaitAll(
                async {
                    loadEntity(
                        serializer = Report.serializer(),
                        onLoadFromDb = {
                            it.reportDao().takeIf { entityUid != 0L }?.findByUid(entityUid)
                        },
                        makeDefault = {
                            Report().also {
                                it.reportTitle = savedStateHandle[ARG_DATE_OF_BIRTH]
                            }
                        },
                        uiUpdate = { entityToDisplay ->
                        }
                    )
                }
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.save),
                    onClick = this@ReportEditViewModel::onClickSave
                )
            )
        }
    }
    fun onClickSave() {
        viewModelScope.launch {
            activeRepo.withDoorTransactionAsync {
                try {
                    val currentReport = _uiState.value.reportOptions2
                    val report = Report(
                        reportUid = entityUid,
                        reportTitle = currentReport?.title,
                        reportOptions = Json.encodeToString(currentReport),
                    )
                    if (entityUid == 0L) {
                        activeRepo.reportDao().insert(report)
                        println("Report options inserted successfully: ${report}")
                    } else {
                        activeRepo.reportDao().update(report)
                        println("Report options updated successfully: ${report}")
                    }
                } catch (e: Exception) {
                    println("Error updating report options: ${e.message}")
                }
            }
        }
    }

    fun onEntityChanged(newOptions: ReportOptions2?) {
        _uiState.update { currentState ->
            currentState.copy(reportOptions2 = newOptions)
        }
    }


    fun onAddFilter() {
        navigateForResult(
            nextViewName = ReportFilterEditViewModel.DEST_NAME,
            key = RESULT_KEY_REPORT,
            currentValue = null,
            serializer = Report.serializer(),
        )
    }

    companion object {
        const val DEST_NAME = "Report"
        const val DEST_NAME_HOME = "ReportHome"
        val ALL_DEST_NAMES = listOf(DEST_NAME, DEST_NAME_HOME)
    }
}

data class ReportEditUiState(
    val reportOptions2: ReportOptions2? = null,
)