package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import app.cash.paging.PagingSource
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import org.kodein.di.DI


data class ContentEntryDetailAttemptsStatementListUiState(
    val attemptsStatementList: () -> PagingSource<Int, StatementEntity> = { EmptyPagingSource() },
)

class ContentEntryDetailAttemptsStatementListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle, destinationName: String = DEST_NAME, )
    : UstadListViewModel<ContentEntryDetailAttemptsStatementListUiState>(
    di, savedStateHandle, ContentEntryDetailAttemptsStatementListUiState(), destinationName) {

    protected val entityUidArg: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0


    companion object {
        const val DEST_NAME = "ContentEntryDetailAttemptsStatementList"
    }

    override fun onUpdateSearchResult(searchText: String) {
        TODO("Not yet implemented")
    }

    override fun onClickAdd() {
        TODO("Not yet implemented")
    }
}