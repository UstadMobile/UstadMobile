package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.paging.PagingSource
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.flow.update
import org.kodein.di.DI

class ClazzListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadListViewModel<ClazzListUiState>(di, savedStateHandle, ClazzListUiState()) {

    private val filterAlreadySelectedList = savedStateHandle[ClazzList2View.ARG_FILTER_EXCLUDE_SELECTED_CLASS_LIST]
        ?.split(",")?.filter { it.isNotEmpty() }?.map { it.trim().toLong() }
        ?: listOf()

    private val filterExcludeMembersOfSchool =
        savedStateHandle[PersonListView.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL]?.toLong() ?: 0L

    private val filterByPermission = savedStateHandle[UstadView.ARG_FILTER_BY_PERMISSION]?.toLong()
        ?: Role.PERMISSION_CLAZZ_SELECT

    private val pagingSourceFactory: () -> PagingSource<Int, ClazzWithListDisplayDetails> =  {
        activeRepo.clazzDao.findClazzesWithPermission(
            searchQuery =  _appUiState.value.searchState.searchText.toQueryLikeParam(),
            accountPersonUid = accountManager.activeAccount.personUid,
            excludeSelectedClazzList = filterAlreadySelectedList,
            excludeSchoolUid = filterExcludeMembersOfSchool,
            sortOrder = _uiState.value.activeSortOrderOption.flag,
            filter = _uiState.value.selectedChipId,
            currentTime = systemTimeInMillis(),
            permission = filterByPermission,
            selectedSchool = 0,
        )
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(),
            )
        }
        _uiState.update { prev ->
            prev.copy(
                clazzList = pagingSourceFactory
            )
        }
    }

    override fun onUpdateSearchResult(searchText: String) {

    }

    override fun onClickAdd() {

    }
}

data class ClazzListUiState(

    val newClazzListOptionVisible: Boolean = true,

    val clazzList: () -> PagingSource<Int, ClazzWithListDisplayDetails> = { EmptyPagingSource() },

    val sortOptions: List<SortOrderOption> = DEFAULT_SORT_OTIONS,

    val activeSortOrderOption: SortOrderOption = sortOptions.first(),

    val fieldsEnabled: Boolean = true,

    val selectedChipId: Int = ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED,

    val filterOptions: List<MessageIdOption2> = listOf(
        MessageIdOption2(MessageID.currently_enrolled, ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED),
        MessageIdOption2(MessageID.past_enrollments, ClazzDaoCommon.FILTER_PAST_ENROLLMENTS),
        MessageIdOption2(MessageID.all, 0)
    ),

) {
    companion object {

        val DEFAULT_SORT_OTIONS = listOf(
            SortOrderOption(MessageID.name, ClazzDaoCommon.SORT_CLAZZNAME_ASC, true),
            SortOrderOption(MessageID.name, ClazzDaoCommon.SORT_CLAZZNAME_DESC, false),
            SortOrderOption(MessageID.attendance, ClazzDaoCommon.SORT_ATTENDANCE_ASC, true),
            SortOrderOption(MessageID.attendance, ClazzDaoCommon.SORT_ATTENDANCE_DESC, false)
        )

    }
}
