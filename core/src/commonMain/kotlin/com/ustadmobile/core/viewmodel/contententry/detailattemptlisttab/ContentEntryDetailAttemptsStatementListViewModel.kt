package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.ext.toggle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.composites.xapi.StatementConst
import com.ustadmobile.lib.db.composites.xapi.StatementEntityAndVerb
import com.ustadmobile.lib.db.composites.xapi.VerbEntityAndName
import com.ustadmobile.lib.db.entities.xapi.VerbEntity
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

/**
 * @param deselectedVerbUids - storing deselected verbs makes life easier: no need to update when
 *        the list of available verbs is loaded or updated.
 */
data class ContentEntryDetailAttemptsStatementListUiState(
    val attemptsStatementList: () -> PagingSource<Int, StatementEntityAndVerb> = { EmptyPagingSource() },
    val sortOptions: List<SortOrderOption> = listOf(
        SortOrderOption(MR.strings.by_timestamp, StatementConst.SORT_BY_TIMESTAMP_DESC, true),
        SortOrderOption(MR.strings.by_timestamp, StatementConst.SORT_BY_TIMESTAMP_ASC, false),
        SortOrderOption(MR.strings.by_score, StatementConst.SORT_BY_SCORE_DESC, false),
        SortOrderOption(MR.strings.by_score, StatementConst.SORT_BY_SCORE_ASC, true)
    ),
    val sortOption: SortOrderOption = sortOptions.first(),
    val showSortOptions: Boolean = true,
    val availableVerbs: List<VerbEntityAndName> = emptyList(),
    val deselectedVerbUids: List<Long> = emptyList()
)

class ContentEntryDetailAttemptsStatementListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle, destinationName: String = DEST_NAME,
) : UstadListViewModel<ContentEntryDetailAttemptsStatementListUiState>(
    di, savedStateHandle, ContentEntryDetailAttemptsStatementListUiState(), destinationName
) {
    private val argPersonUid = savedStateHandle[UstadView.ARG_PERSON_UID]?.toLong() ?: 0

    private val argContextRegistrationIdHi =
        savedStateHandle[UstadView.ARG_CONTEXT_REGISTRATION_ID_HI]?.toLong() ?: 0

    private val argContextRegistrationIdLo =
        savedStateHandle[UstadView.ARG_CONTEXT_REGISTRATION_ID_LO]?.toLong() ?: 0

    private val argContentEntryUid = savedStateHandle[UstadView.ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0

    private val attemptsStatementListPagingSource: ListPagingSourceFactory<StatementEntityAndVerb> = {
        activeRepo.statementDao().findStatementsBySession(
            registrationHi = argContextRegistrationIdHi,
            registrationLo = argContextRegistrationIdLo,
            accountPersonUid = activeUserPersonUid,
            selectedPersonUid = argPersonUid,
            contentEntryUid = argContentEntryUid,
            searchText = _appUiState.value.searchState.searchText.toQueryLikeParam(),
            sortOrder = _uiState.value.sortOption.flag,
            deSelectedVerbUids = _uiState.value.deselectedVerbUids
        )
    }

    init {
        _uiState.update {
            it.copy(attemptsStatementList = attemptsStatementListPagingSource)
        }
        _appUiState.update { prev ->
            prev.copy(
                searchState = createSearchEnabledState(visible = true),
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepo.statementDao().getUniqueVerbsForSession(
                        registrationHi = argContextRegistrationIdHi,
                        registrationLo = argContextRegistrationIdLo,
                        selectedPersonUid = argPersonUid,
                        contentEntryUid = argContentEntryUid
                    ).collect { verbs ->
                        _uiState.update { state ->
                            state.copy(availableVerbs = verbs)
                        }
                    }
                }

                launch {
                    activeRepo.contentEntryDao().findLiveContentEntry(
                        argContentEntryUid
                    ).collect { contentEntry ->
                        _appUiState.update { prev ->
                            prev.copy(
                                title = contentEntry?.title ?: "",
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onUpdateSearchResult(searchText: String) {
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update { prev ->
            prev.copy(
                sortOption = sortOption,
                attemptsStatementList = attemptsStatementListPagingSource
            )
        }
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    fun onVerbFilterToggled(verbEntity: VerbEntity) {
        _uiState.update { state ->
            state.copy(
                deselectedVerbUids = state.deselectedVerbUids.toggle(verbEntity.verbUid)
            )
        }

        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    override fun onClickAdd() {
        //Not used
    }

    companion object {
        const val DEST_NAME = "ContentEntryDetailAttemptsStatementList"
    }
}