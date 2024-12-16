package com.ustadmobile.libuicompose.view.downloaduploadstatus

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListUiState
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsPersonListViewModel.Companion.DEST_NAME
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.composites.StatementAndPersonAndPicture
import org.kodein.di.DI

data class DownloadUploadStatusUiState(
    val item:String="All"
)


class DownloadUploadStatusViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle, destinationName: String = DEST_NAME,
) : UstadListViewModel<DownloadUploadStatusUiState>(
    di, savedStateHandle, DownloadUploadStatusUiState(), destinationName
) {
    companion object {
        const val DEST_NAME = "DownloadUploadStatusScreen"
    }

    override fun onUpdateSearchResult(searchText: String) {

    }

    override fun onClickAdd() {
    }
}