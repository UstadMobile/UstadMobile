package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import app.cash.paging.PagingSource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.composites.xapi.StatementConst
import com.ustadmobile.lib.db.composites.xapi.StatementEntityAndVerb
import com.ustadmobile.lib.db.entities.xapi.VerbEntity
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

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
    val availableVerbs: List<VerbEntity> = emptyList(),
    val selectedVerbIds: Set<String> = emptySet(),
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

    private fun getAttemptsStatementListAsPagingSource(
        contextRegistrationHi: Long,
        contextRegistrationLo: Long,
    ): PagingSource<Int, StatementEntityAndVerb> {
        val state = _uiState.value
        return activeRepo.statementDao().findStatementsBySession(
            registrationHi = contextRegistrationHi,
            registrationLo = contextRegistrationLo,
            searchText = _appUiState.value.searchState.searchText.toQueryLikeParam(),
            sortOrder = state.sortOption.flag,
            selectedVerbIds = state.selectedVerbIds.joinToString(",")
        )
    }

    private val attemptsStatementListPagingSource: ListPagingSourceFactory<StatementEntityAndVerb> =
        {
            getAttemptsStatementListAsPagingSource(
                contextRegistrationHi = argContextRegistrationIdHi,
                contextRegistrationLo = argContextRegistrationIdLo,
            )
        }

    init {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepo.personDao().getNamesByUid(argPersonUid).collect { personNames ->
                        _uiState.update {
                            it.copy(attemptsStatementList = attemptsStatementListPagingSource)
                        }
                        _appUiState.update { prev ->
                            prev.copy(
                                title = "${personNames?.firstNames} ${personNames?.lastName}",
                                searchState = createSearchEnabledState(visible = true),
                            )
                        }
                    }
                }

                launch {
                    activeRepo.statementDao().getUniqueVerbsForSession(
                        registrationHi = argContextRegistrationIdHi,
                        registrationLo = argContextRegistrationIdLo
                    ).collect { verbs ->
                        _uiState.update { it.copy(availableVerbs = verbs) }
                    }
                }
            }
        }
    }

    override fun onUpdateSearchResult(searchText: String) {
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    override fun onClickAdd() {
        TODO("Not yet implemented")
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update { prev ->
            prev.copy(sortOption = sortOption)
        }
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    fun onVerbFilterToggled(verbUrlId: String) {
        _uiState.update { state ->
            val newSelectedVerbIds = state.selectedVerbIds.toMutableSet()
            if (verbUrlId in newSelectedVerbIds) {
                newSelectedVerbIds.remove(verbUrlId)
            } else {
                newSelectedVerbIds.add(verbUrlId)
            }
            state.copy(selectedVerbIds = newSelectedVerbIds)
        }
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    companion object {
        const val DEST_NAME = "ContentEntryDetailAttemptsStatementList"
    }
}