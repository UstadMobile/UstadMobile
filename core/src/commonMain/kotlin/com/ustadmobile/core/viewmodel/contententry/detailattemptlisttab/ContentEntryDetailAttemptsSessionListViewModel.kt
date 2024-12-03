package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import app.cash.paging.PagingSource
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.composites.StatementAndPersonAndPicture
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ContentEntryDetailAttemptsSessionListUiState(
    val personName: String = "",
    val contentEntryTitle: String = "",
    val attemptsSessionList: () -> PagingSource<Int, StatementAndPersonAndPicture> = { EmptyPagingSource() },
    val personUid: Long = 0
)

class ContentEntryDetailAttemptsSessionListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle, destinationName: String = DEST_NAME,
) : UstadListViewModel<ContentEntryDetailAttemptsSessionListUiState>(
    di, savedStateHandle, ContentEntryDetailAttemptsSessionListUiState(), destinationName
) {

    private val entityUidArg = savedStateHandle[UstadView.ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0

    private val argPersonUid = savedStateHandle[UstadView.ARG_PERSON_UID]?.toLong() ?: 0
   // private val statementIdHi: Int =savedStateHandle[UstadView.ARG_STATEMENT_ID_HI]?.toInt()?:0
  //  private val statementIdLo: Int =savedStateHandle[UstadView.ARG_STATEMENT_ID_LO]?.toInt()?:0

    private fun getAttemptsSessionListAsPagingSource(contentEntryUid: Long, personUid: Long)
            : PagingSource<Int, StatementAndPersonAndPicture> {
        return activeRepo.xapiSessionEntityDao().getSessionList(contentEntryUid,personUid)
    }

    private val attemptsSessionListPagingSource: ListPagingSourceFactory<StatementAndPersonAndPicture> = {
        getAttemptsSessionListAsPagingSource(
            contentEntryUid = entityUidArg,
            personUid = argPersonUid
        )
    }

    init {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                activeRepo.personDao().getNamesByUid(argPersonUid).collect { personNames ->
                    _uiState.update {
                        it.copy(personName = "${personNames?.firstNames} ${personNames?.lastName}")
                        it.copy(contentEntryTitle = entityUidArg.toString())
                        it.copy(personUid = argPersonUid)
                        it.copy(attemptsSessionList = attemptsSessionListPagingSource)
                    }
                    _appUiState.update { prev ->
                        prev.copy(
                            title = "${personNames?.firstNames} ${personNames?.lastName} - $entityUidArg"
                        )
                    }
                }
            }

        }
    }

    fun onClickEntry(
        entry: StatementAndPersonAndPicture
    ) {
        navController.navigate(
            viewName = ContentEntryDetailAttemptsStatementListViewModel.DEST_NAME,
            args = mapOf(
                UstadView.ARG_PERSON_UID to argPersonUid.toString(),
                UstadView.ARG_CONTENT_ENTRY_UID to entityUidArg.toString(),
            )
        )
    }

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