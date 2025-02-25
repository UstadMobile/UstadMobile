package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.composites.xapi.SessionTimeAndProgressInfo
import com.ustadmobile.lib.db.composites.xapi.SessionTimeAndProgressInfoConst
import com.ustadmobile.lib.db.composites.xapi.StatementConst
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ContentEntryDetailAttemptsSessionListUiState(

    val attemptsSessionList: () -> PagingSource<Int, SessionTimeAndProgressInfo> = { EmptyPagingSource() },
    val sortOptions: List<SortOrderOption> = listOf(
        SortOrderOption(MR.strings.most_recent, SessionTimeAndProgressInfoConst.SORT_BY_TIMESTAMP_DESC, true),
        SortOrderOption(MR.strings.most_recent, SessionTimeAndProgressInfoConst.SORT_BY_TIMESTAMP_ASC, false),
        SortOrderOption(MR.strings.least_recent, SessionTimeAndProgressInfoConst.SORT_BY_LEAST_RECENT_DESC, true),
        SortOrderOption(MR.strings.least_recent, SessionTimeAndProgressInfoConst.SORT_BY_LEAST_RECENT_ASC, false),
        SortOrderOption(MR.strings.by_score, SessionTimeAndProgressInfoConst.SORT_BY_SCORE_ASC, true),
        SortOrderOption(MR.strings.by_score, SessionTimeAndProgressInfoConst.SORT_BY_SCORE_DESC, false),
        SortOrderOption(MR.strings.by_completion, SessionTimeAndProgressInfoConst.SORT_BY_COMPLETION_ASC, true),
        SortOrderOption(MR.strings.by_completion, SessionTimeAndProgressInfoConst.SORT_BY_COMPLETION_DESC, false),
    ),
    val sortOption: SortOrderOption = sortOptions.first(),
    val showSortOptions: Boolean = true,
)


class ContentEntryDetailAttemptsSessionListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle, destinationName: String = DEST_NAME,
) : UstadListViewModel<ContentEntryDetailAttemptsSessionListUiState>(
    di, savedStateHandle, ContentEntryDetailAttemptsSessionListUiState(), destinationName
) {

    private val entityUidArg = savedStateHandle[UstadView.ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0
    private val argPersonUid = savedStateHandle[UstadView.ARG_PERSON_UID]?.toLong() ?: 0


    private fun getAttemptsSessionListAsPagingSource(contentEntryUid: Long, personUid: Long)
            : PagingSource<Int, SessionTimeAndProgressInfo> {
        return activeRepo.statementDao().findSessionsByPersonAndContent(contentEntryUid, personUid,
            accountPersonUid = activeUserPersonUid,
            sortOrder = _uiState.value.sortOption.flag
        )
    }

    private val attemptsSessionListPagingSource: ListPagingSourceFactory<SessionTimeAndProgressInfo> =
        {
            getAttemptsSessionListAsPagingSource(
                contentEntryUid = entityUidArg,
                personUid = argPersonUid,

                )
        }

    init {
        _uiState.update { it.copy(attemptsSessionList = attemptsSessionListPagingSource) }

        viewModelScope.launch {
            // Launch both the person names and content title fetching in parallel
            val personNamesDeferred = async {
                activeRepo.personDao().getNamesByUid(argPersonUid).firstOrNull()
            }

            val contentEntryDeferred = async {
                activeRepo.contentEntryDao().findLiveContentEntry(entityUidArg).firstOrNull()
            }

            val personNames = personNamesDeferred.await()
            val contentEntry = contentEntryDeferred.await()

            // Combine the user name and content title into a single string
            val combinedTitle = "${personNames?.firstNames} ${personNames?.lastName} - ${contentEntry?.title ?: ""}"

            // Update the UI state with the combined title
            _appUiState.update { prev ->
                prev.copy(
                    title = combinedTitle
                )
            }
        }

    }

    fun onClickEntry(
        entry: SessionTimeAndProgressInfo
    ) {
        navController.navigate(
            viewName = ContentEntryDetailAttemptsStatementListViewModel.DEST_NAME,
            args = mapOf(
                UstadView.ARG_PERSON_UID to argPersonUid.toString(),
                UstadView.ARG_CONTENT_ENTRY_UID to entityUidArg.toString(),
                UstadView.ARG_CONTEXT_REGISTRATION_ID_HI to entry.contextRegistrationHi.toString(),
                UstadView.ARG_CONTEXT_REGISTRATION_ID_LO to entry.contextRegistrationLo.toString(),
            )
        )
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update { prev ->
            prev.copy(
                sortOption = sortOption,
                attemptsSessionList = {
                    getAttemptsSessionListAsPagingSource(entityUidArg, argPersonUid)
                }
            )
        }
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    override fun onUpdateSearchResult(searchText: String) {
        TODO("Not yet implemented")
    }

    override fun onClickAdd() {
        TODO("Not yet implemented")
    }

    companion object {
        const val DEST_NAME = "ContentEntryDetailAttemptsSessionList"
    }
}