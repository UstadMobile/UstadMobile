package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import app.cash.paging.PagingSource
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI


data class ContentEntryDetailAttemptsStatementListUiState(
    val attemptsStatementList: () -> PagingSource<Int, StatementEntity> = { EmptyPagingSource() },
)

class ContentEntryDetailAttemptsStatementListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle, destinationName: String = DEST_NAME, )
    : UstadListViewModel<ContentEntryDetailAttemptsStatementListUiState>(
    di, savedStateHandle, ContentEntryDetailAttemptsStatementListUiState(), destinationName) {

    private val entityUidArg = savedStateHandle[UstadView.ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0

    private val argPersonUid = savedStateHandle[UstadView.ARG_PERSON_UID]?.toLong() ?: 0
    private fun getAttemptsStatementListAsPagingSource(contentEntryUid: Long, personUid: Long) : PagingSource<Int, StatementEntity>  {
        return activeRepo.statementDao().getStatementList(contentEntryUid,personUid)
    }
    private val attemptsStatementListPagingSource: ListPagingSourceFactory<StatementEntity> = {

        getAttemptsStatementListAsPagingSource(contentEntryUid=entityUidArg,personUid=argPersonUid )
    }
init {
    viewModelScope.launch {
        _uiState.whenSubscribed {
            // Fetch the person's name from the database based on personUid
            activeRepo.personDao().getNamesByUid(argPersonUid).collect { personNames ->
                // Update the UI state with the fetched name
                _uiState.update {

                    it.copy(attemptsStatementList = attemptsStatementListPagingSource)
                }


                _appUiState.update { prev ->
                    prev.copy(
                        title = "${personNames?.firstNames} ${personNames?.lastName} - $entityUidArg")
                }
            }
        }

    }
}
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