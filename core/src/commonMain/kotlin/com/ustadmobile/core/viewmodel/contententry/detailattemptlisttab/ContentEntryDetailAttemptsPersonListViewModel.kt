package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.composites.AttemptsPersonListConst.SORT_BY_COMPLETION_ASC
import com.ustadmobile.lib.db.composites.AttemptsPersonListConst.SORT_BY_COMPLETION_DESC
import com.ustadmobile.lib.db.composites.AttemptsPersonListConst.SORT_BY_RECENT_ATTEMPT_ASC
import com.ustadmobile.lib.db.composites.AttemptsPersonListConst.SORT_BY_RECENT_ATTEMPT_DESC
import com.ustadmobile.lib.db.composites.AttemptsPersonListConst.SORT_BY_SCORE_ASC
import com.ustadmobile.lib.db.composites.AttemptsPersonListConst.SORT_BY_SCORE_DESC
import com.ustadmobile.lib.db.composites.AttemptsPersonListConst.SORT_FIRST_NAME_ASC
import com.ustadmobile.lib.db.composites.AttemptsPersonListConst.SORT_FIRST_NAME_DESC
import com.ustadmobile.lib.db.composites.AttemptsPersonListConst.SORT_LAST_NAME_ASC
import com.ustadmobile.lib.db.composites.AttemptsPersonListConst.SORT_LAST_NAME_DESC
import com.ustadmobile.lib.db.composites.PersonAndPictureAndNumAttempts
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ContentEntryDetailAttemptsPersonListUiState(
    val attemptsPersonList: () -> PagingSource<Int, PersonAndPictureAndNumAttempts> =
        { EmptyPagingSource() },
    val sortOptions: List<SortOrderOption> = listOf(
        SortOrderOption(MR.strings.first_name, SORT_FIRST_NAME_ASC, true),
        SortOrderOption(MR.strings.first_name, SORT_FIRST_NAME_DESC, false),
        SortOrderOption(MR.strings.last_name, SORT_LAST_NAME_ASC, true),
        SortOrderOption(MR.strings.last_name, SORT_LAST_NAME_DESC, false),
        SortOrderOption(MR.strings.by_score, SORT_BY_SCORE_ASC, true),
        SortOrderOption(MR.strings.by_score, SORT_BY_SCORE_DESC, false),
        SortOrderOption(MR.strings.by_completion, SORT_BY_COMPLETION_ASC, true),
        SortOrderOption(MR.strings.by_completion, SORT_BY_COMPLETION_DESC, false),
        SortOrderOption(MR.strings.by_recent_attempt, SORT_BY_RECENT_ATTEMPT_DESC, true),
        SortOrderOption(MR.strings.by_recent_attempt, SORT_BY_RECENT_ATTEMPT_ASC, false),
    ),
    val sortOption: SortOrderOption = sortOptions.first(),
    val showSortOptions: Boolean = true,

    )

class ContentEntryDetailAttemptsPersonListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle, destinationName: String = DEST_NAME,
) : UstadListViewModel<ContentEntryDetailAttemptsPersonListUiState>(
    di, savedStateHandle, ContentEntryDetailAttemptsPersonListUiState(), destinationName
) {

    protected val entityUidArg: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0
    private suspend fun getContentEntryTitle(entityUid: Long): String? {
        return activeRepo.statementDao().getTitleByEntityUid(entityUid)
    }

    private suspend fun buildSortOptions(): List<SortOrderOption> {
        val options = mutableListOf(
            SortOrderOption(MR.strings.first_name, SORT_FIRST_NAME_ASC, true),
            SortOrderOption(MR.strings.first_name, SORT_FIRST_NAME_DESC, false),
            SortOrderOption(MR.strings.last_name, SORT_LAST_NAME_ASC, true),
            SortOrderOption(MR.strings.last_name, SORT_LAST_NAME_DESC, false)
        )

        if (activeRepo.statementDao().hasScoreData(entityUidArg)) {
            options.addAll(listOf(
                SortOrderOption(MR.strings.by_score, SORT_BY_SCORE_ASC, true),
                SortOrderOption(MR.strings.by_score, SORT_BY_SCORE_DESC, false)
            ))
        }

        if (activeRepo.statementDao().hasCompletionData(entityUidArg)) {
            options.addAll(listOf(
                SortOrderOption(MR.strings.by_completion, SORT_BY_COMPLETION_ASC, true),
                SortOrderOption(MR.strings.by_completion, SORT_BY_COMPLETION_DESC, false)
            ))
        }

        options.addAll(listOf(
            SortOrderOption(MR.strings.by_recent_attempt, SORT_BY_RECENT_ATTEMPT_DESC, true),
            SortOrderOption(MR.strings.by_recent_attempt, SORT_BY_RECENT_ATTEMPT_ASC, false)
        ))

        return options
    }

    private fun getAttemptsPersonListAsPagingSource(contentEntryUid: Long):
            PagingSource<Int, PersonAndPictureAndNumAttempts> {
        val pagingSource =
            activeRepo.statementDao().findPersonsWithAttempts(
                contentEntryUid = contentEntryUid,
                accountPersonUid = activeUserPersonUid,
                searchText = _appUiState.value.searchState.searchText.toQueryLikeParam(),
                sortOrder = _uiState.value.sortOption.flag,
                )
        return pagingSource
    }

    private val attemptsPersonListPagingSource: ListPagingSourceFactory<PersonAndPictureAndNumAttempts> =
        {
            getAttemptsPersonListAsPagingSource(contentEntryUid = entityUidArg)
        }

    init {
        viewModelScope.launch {
            val availableSortOptions = buildSortOptions()
            _uiState.update { prev ->
                prev.copy(
                    sortOptions = availableSortOptions,
                    sortOption = availableSortOptions.firstOrNull()!!
                )
            }
        }

        _uiState.update { prev ->
            prev.copy(
                attemptsPersonList = attemptsPersonListPagingSource,
            )
        }
        viewModelScope.launch {
            // Fetch the title using the entityUidArg
            val title = getContentEntryTitle(entityUidArg) ?: "Default Title"
            _appUiState.update { prev ->
                prev.copy(
                    title = title
                )
            }
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
        //will use the searchText as per the appUiState
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    override fun onClickAdd() {
        TODO("Not yet implemented")
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update { prev ->
            prev.copy(
                sortOption = sortOption
            )
        }
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    companion object {
        const val DEST_NAME = "ContentEntryDetailAttemptsPersonList"
    }
}
