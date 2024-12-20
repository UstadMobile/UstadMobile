package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.composites.PersonAndPictureAndNumAttempts
import kotlinx.coroutines.flow.update
import org.kodein.di.DI

data class ContentEntryDetailAttemptsPersonListUiState(
    val attemptsPersonList: () -> PagingSource<Int, PersonAndPictureAndNumAttempts> =
        { EmptyPagingSource() },
)

class ContentEntryDetailAttemptsPersonListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle, destinationName: String = DEST_NAME,
) : UstadListViewModel<ContentEntryDetailAttemptsPersonListUiState>(
    di, savedStateHandle, ContentEntryDetailAttemptsPersonListUiState(), destinationName
) {

    protected val entityUidArg: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    val appBarTitle = systemImpl.getString(MR.strings.library)

    private fun getAttemptsPersonListAsPagingSource(contentEntryUid: Long):
            PagingSource<Int, PersonAndPictureAndNumAttempts> {
        val pagingSource =
            activeRepo.statementDao().findPersonsWithAttempts(
                contentEntryUid = contentEntryUid,
                accountPersonUid = activeUserPersonUid,
            )
        return pagingSource
    }

    private val attemptsPersonListPagingSource: ListPagingSourceFactory<PersonAndPictureAndNumAttempts> =
        {
            getAttemptsPersonListAsPagingSource(contentEntryUid = entityUidArg)
        }

    init {
        _uiState.update { prev ->
            prev.copy(
                attemptsPersonList = attemptsPersonListPagingSource,
            )
        }
        _appUiState.update { prev ->

            prev.copy(
                title = appBarTitle
            )
        }
    }

    fun onClickEntry(
        entry: PersonAndPictureAndNumAttempts
    ) {
        navController.navigate(
            viewName = ContentEntryDetailAttemptsSessionListViewModel.DEST_NAME,
            args = mapOf(
                UstadView.ARG_PERSON_UID to (entry.person?.personUid ?: 0).toString(),
                UstadView.ARG_CONTENT_ENTRY_UID to entityUidArg.toString(),
            )
        )
    }

    override fun onUpdateSearchResult(searchText: String) {
        TODO("Not yet implemented")
    }

    override fun onClickAdd() {
        TODO("Not yet implemented")
    }


    companion object {
        const val DEST_NAME = "ContentEntryDetailAttemptsPersonList"
    }
}
