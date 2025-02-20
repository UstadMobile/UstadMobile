package com.ustadmobile.core.viewmodel.report.list

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.core.viewmodel.report.edit.ReportEditViewModel
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Report
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ReportListUiState(
    val reportList: () -> PagingSource<Int, Report> = { EmptyPagingSource() },
    val addSheetOrDialogVisible: Boolean = false,
)

class ReportListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
) : UstadListViewModel<ReportListUiState>(
    di, savedStateHandle, ReportListUiState(), destinationName
) {

    private val pagingSourceFactory: () -> PagingSource<Int, Report> = {
        activeRepo.reportDao().findAllReports()
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(visible = false),
                title = savedStateHandle[ARG_TITLE] ?: listTitle(
                    MR.strings.report,
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
                        reportList = pagingSourceFactory
                    )
                }
            }
        }
    }

    override fun onUpdateSearchResult(searchText: String) {

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
            activeRepo.withDoorTransactionAsync {
                activeRepo.reportDao().deleteReportByUid(uid)
            }
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

