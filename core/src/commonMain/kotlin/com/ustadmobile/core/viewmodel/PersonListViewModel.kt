package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.dao.PersonDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.paging.*
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.kodein.di.DI

data class PersonListUiState(
    val personList: PagingSource<Int, PersonWithDisplayDetails> = EmptyPagingSource(),
    val sortOptions: List<SortOrderOption> = listOf(
        SortOrderOption(MessageID.first_name, PersonDaoCommon.SORT_FIRST_NAME_ASC, true),
        SortOrderOption(MessageID.first_name, PersonDaoCommon.SORT_FIRST_NAME_DESC, false),
        SortOrderOption(MessageID.last_name, PersonDaoCommon.SORT_LAST_NAME_ASC, true),
        SortOrderOption(MessageID.last_name, PersonDaoCommon.SORT_LAST_NAME_DESC, false)
    ),
    val sortOption: SortOrderOption = sortOptions.first()
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
    savedStateHandle: UstadSavedStateHandle
): UstadListViewModel(di, savedStateHandle) {

    val filterExcludeMembersOfClazz = savedStateHandle[ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ]?.toLong() ?: 0L

    val filterExcludeMemberOfSchool = savedStateHandle[ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL]?.toLong() ?: 0L

    val filterAlreadySelectedList = savedStateHandle[PersonListView.ARG_EXCLUDE_PERSONUIDS_LIST]
        ?.split(",")?.filter { it.isNotEmpty() }?.map { it.trim().toLong() }
        ?: listOf()

    val filterByPermission = savedStateHandle[UstadView.ARG_FILTER_BY_PERMISSION]?.trim()?.toLong()
        ?: Role.PERMISSION_PERSON_SELECT

    private val _uiState = MutableStateFlow(PersonListUiState())

    val uiState: Flow<PersonListUiState> = _uiState.asStateFlow()

    private var searchText: String? = null

    init {
        _uiState.update {prev ->
            prev.copy(
                personList = activeRepo.personDao.findPersonsWithPermissionAsPagingSource(
                    getSystemTimeInMillis(), filterExcludeMembersOfClazz,
                    filterExcludeMemberOfSchool, filterAlreadySelectedList,
                    accountManager.activeAccount.personUid, prev.sortOption.flag,
                    searchText.toQueryLikeParam()
                )
            )
        }
    }

    fun onClickAddNewItem() {

    }

    fun onClickEntry(entry: Person) {

    }


}


