package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadListViewModel
import org.kodein.di.DI

 class ContentEntryDetailAttemptsSessionListUiState()

class ContentEntryDetailAttemptsSessionListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle, destinationName: String = DEST_NAME, )
    : UstadListViewModel<ContentEntryDetailAttemptsSessionListUiState>(
    di, savedStateHandle, ContentEntryDetailAttemptsSessionListUiState(), destinationName) {

    companion object {
        const val DEST_NAME = "ContentEntryDetailAttemptsSessionList"
    }

    override fun onUpdateSearchResult(searchText: String) {
        TODO("Not yet implemented")
    }

    override fun onClickAdd() {
        TODO("Not yet implemented")
    }
}