package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.ext.whenSubscribed
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
        SortOrderOption(MR.strings.most_recent, SORT_BY_RECENT_ATTEMPT_DESC, null),
        SortOrderOption(MR.strings.least_recent, SORT_BY_RECENT_ATTEMPT_ASC, null),
    ),
    val sortOption: SortOrderOption = sortOptions.first(),
    val showSortOptions: Boolean = true,
)

class ContentEntryDetailAttemptsPersonListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle, destinationName: String = DEST_NAME,
) : UstadListViewModel<ContentEntryDetailAttemptsPersonListUiState>(
    di, savedStateHandle, ContentEntryDetailAttemptsPersonListUiState(), destinationName
) {

    private val entityUidArg: Long = savedStateHandle[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0

    private val attemptsPersonListPagingSource: ListPagingSourceFactory<PersonAndPictureAndNumAttempts> =
        {
            activeRepo.statementDao().findPersonsWithAttempts(
                contentEntryUid = entityUidArg,
                accountPersonUid = activeUserPersonUid,
                searchText = _appUiState.value.searchState.searchText.toQueryLikeParam(),
                sortOrder = _uiState.value.sortOption.flag,
            )
        }

    init {
        _appUiState.update {
            it.copy(searchState = createSearchEnabledState())
        }

        _uiState.update { prev ->
            prev.copy(attemptsPersonList = attemptsPersonListPagingSource)
        }

        viewModelScope.launch {
            listOf(activeDb, activeRepo).forEach { db ->
                val sortOptions = buildSortOptions(db)
                _uiState.update { prev -> prev.copy(sortOptions = sortOptions) }
            }
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                activeRepo.contentEntryDao().findLiveContentEntry(entityUidArg).collect { contentEntry ->
                    _appUiState.update { prev ->
                        prev.copy(title = contentEntry?.title ?: "")
                    }
                }
            }
        }
    }

    /*
     * Some content has progress data, some doesn't. Some content has score data, some doesn't. When
     * setting the sort options we need to check what is available for this content. Because the
     * list of people who completed content is loaded through the paging source, we need to make a
     * separate query.
     */
    private suspend fun buildSortOptions(db: UmAppDatabase): List<SortOrderOption> {
        val (hasProgressData, hasScoreData) = db.statementDao()
            .scoreOrProgressDataExistsForContent(
                contentEntryUid = entityUidArg,
                accountPersonUid = activeUserPersonUid
            ).let { list ->
                Pair(
                    first = list.any { it.extensionProgress != null },
                    second = list.any { it.resultScoreScaled != null }
                )
            }

        return buildList {
            addAll(listOf(
                SortOrderOption(MR.strings.first_name, SORT_FIRST_NAME_ASC, true),
                SortOrderOption(MR.strings.first_name, SORT_FIRST_NAME_DESC, false),
                SortOrderOption(MR.strings.last_name, SORT_LAST_NAME_ASC, true),
                SortOrderOption(MR.strings.last_name, SORT_LAST_NAME_DESC, false)
            ))

            if(hasScoreData) {
                addAll(listOf(
                    SortOrderOption(MR.strings.by_score, SORT_BY_SCORE_ASC, true),
                    SortOrderOption(MR.strings.by_score, SORT_BY_SCORE_DESC, false)
                ))
            }

            if(hasProgressData) {
                addAll(listOf(
                    SortOrderOption(MR.strings.progress_key, SORT_BY_COMPLETION_ASC, true),
                    SortOrderOption(MR.strings.progress_key, SORT_BY_COMPLETION_DESC, false)
                ))
            }

            addAll(listOf(
                SortOrderOption(MR.strings.most_recent, SORT_BY_RECENT_ATTEMPT_DESC, null),
                SortOrderOption(MR.strings.least_recent, SORT_BY_RECENT_ATTEMPT_ASC, null),
            ))
        }
    }


    fun onClickEntry(
        entry: PersonAndPictureAndNumAttempts
    ) {
        navController.navigate(
            viewName = ContentEntryDetailAttemptsSessionListViewModel.DEST_NAME,
            args = mapOf(
                UstadView.ARG_PERSON_UID to entry.person.personUid.toString(),
                UstadView.ARG_CONTENT_ENTRY_UID to entityUidArg.toString(),
            )
        )
    }

    override fun onUpdateSearchResult(searchText: String) {
        //will use the searchText as per the appUiState
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    override fun onClickAdd() {
        //Not used
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
