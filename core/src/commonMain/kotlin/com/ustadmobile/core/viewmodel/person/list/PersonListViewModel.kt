package com.ustadmobile.core.viewmodel.person.list

import com.ustadmobile.core.db.dao.PersonDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.PersonViewModelConstants.ARG_GO_TO_ON_PERSON_SELECTED
import com.ustadmobile.door.paging.*
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class PersonListUiState(
    val personList: () -> PagingSource<Int, PersonWithDisplayDetails> = { EmptyPagingSource() },
    val sortOptions: List<SortOrderOption> = listOf(
        SortOrderOption(MessageID.first_name, PersonDaoCommon.SORT_FIRST_NAME_ASC, true),
        SortOrderOption(MessageID.first_name, PersonDaoCommon.SORT_FIRST_NAME_DESC, false),
        SortOrderOption(MessageID.last_name, PersonDaoCommon.SORT_LAST_NAME_ASC, true),
        SortOrderOption(MessageID.last_name, PersonDaoCommon.SORT_LAST_NAME_DESC, false)
    ),
    val sortOption: SortOrderOption = sortOptions.first(),
    val showAddItem: Boolean = false,
)

class EmptyPagingSource<Key: Any, Value: Any>(): PagingSource<Key, Value>() {

    override fun getRefreshKey(state: PagingState<Key, Value>): Key? {
        return null
    }

    override suspend fun load(params: LoadParams<Key>): LoadResult<Key, Value> {
        return DoorLoadResult.Page<Key, Value>(
            emptyList(), null, null
        ).toLoadResult()
    }
}




class PersonListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = PersonListView.VIEW_NAME,
): UstadListViewModel<PersonListUiState>(
    di, savedStateHandle, PersonListUiState(), destinationName
) {

    private val filterExcludeMembersOfClazz = savedStateHandle[ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ]?.toLong() ?: 0L

    private val filterExcludeMemberOfSchool = savedStateHandle[ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL]?.toLong() ?: 0L

    private val filterAlreadySelectedList = savedStateHandle[PersonListView.ARG_EXCLUDE_PERSONUIDS_LIST]
        ?.split(",")?.filter { it.isNotEmpty() }?.map { it.trim().toLong() }
        ?: listOf()

    private val filterByPermission = savedStateHandle[UstadView.ARG_FILTER_BY_PERMISSION]?.trim()?.toLong()
        ?: Role.PERMISSION_PERSON_SELECT

    private val pagingSourceFactory: () -> PagingSource<Int, PersonWithDisplayDetails> = {
        activeRepo.personDao.findPersonsWithPermissionAsPagingSource(
            getSystemTimeInMillis(), filterExcludeMembersOfClazz,
            filterExcludeMemberOfSchool, filterAlreadySelectedList,
            accountManager.activeAccount.personUid, _uiState.value.sortOption.flag,
            _appUiState.value.searchState.searchText.toQueryLikeParam()
        ).also {
            lastPagingSource?.invalidate()
            lastPagingSource = it
        }
    }

    private var lastPagingSource: PagingSource<Int, PersonWithDisplayDetails>? = null

    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(),
                title = listTitle(MessageID.people, MessageID.select_person)
            )
        }

        _uiState.update { prev ->
            prev.copy(
                personList = pagingSourceFactory
            )
        }

        viewModelScope.launch {
            collectHasPermissionFlowAndSetAddNewItemUiState(
                hasPermissionFlow = {
                    activeRepo.scopedGrantDao.userHasSystemLevelPermissionAsFlow(
                        accountManager.activeAccount.personUid, Role.PERMISSION_PERSON_INSERT
                    )
                },
                fabMessageId = MessageID.person,
                onSetAddListItemVisibility = { visible ->
                    _uiState.update { prev -> prev.copy(showAddItem = visible) }
                }
            )
        }
    }


    override fun onUpdateSearchResult(searchText: String) {
        //will use the searchText as per the appUiState
        lastPagingSource?.invalidate()
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update { prev ->
            prev.copy(
                sortOption = sortOption
            )
        }
        lastPagingSource?.invalidate()
    }

    override fun onClickAdd() {
        navigateToCreateNew(PersonEditView.VIEW_NAME, savedStateHandle[ARG_GO_TO_ON_PERSON_SELECTED]?.let {
            mapOf(ARG_GO_TO_ON_PERSON_SELECTED to it)
        } ?: emptyMap())
    }

    fun onClickEntry(entry: Person) {
        val goToOnPersonSelected = savedStateHandle[ARG_GO_TO_ON_PERSON_SELECTED]

        if(goToOnPersonSelected != null) {
            val args = UMFileUtil.parseURLQueryString(goToOnPersonSelected) +
                mapOf(UstadView.ARG_PERSON_UID to entry.personUid.toString())
            val goToDestName = goToOnPersonSelected.substringBefore("?")
            navController.navigate(goToDestName, args)
        }else {
            navigateOnItemClicked(PersonDetailView.VIEW_NAME, entry.personUid, entry)
        }
    }

    companion object {

        const val DEST_NAME = "People"

    }

}


