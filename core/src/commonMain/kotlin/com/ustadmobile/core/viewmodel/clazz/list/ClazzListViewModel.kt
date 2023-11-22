package com.ustadmobile.core.viewmodel.clazz.list

import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.*
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.core.viewmodel.UstadListViewModel
import app.cash.paging.PagingSource
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI


data class ClazzListUiState(

    val newClazzListOptionVisible: Boolean = true,

    val clazzList: () -> PagingSource<Int, ClazzWithListDisplayDetails> = { EmptyPagingSource() },

    val sortOptions: List<SortOrderOption> = DEFAULT_SORT_OTIONS,

    val activeSortOrderOption: SortOrderOption = sortOptions.first(),

    val fieldsEnabled: Boolean = true,

    val selectedChipId: Int = ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED,

    val canAddNewCourse: Boolean = false,

    val filterOptions: List<MessageIdOption2> = listOf(
        MessageIdOption2(MR.strings.currently_enrolled, ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED),
        MessageIdOption2(MR.strings.past_enrollments, ClazzDaoCommon.FILTER_PAST_ENROLLMENTS),
        MessageIdOption2(MR.strings.all, 0)
    ),

    ) {
    companion object {

        val DEFAULT_SORT_OTIONS = listOf(
            SortOrderOption(MR.strings.name_key, ClazzDaoCommon.SORT_CLAZZNAME_ASC, true),
            SortOrderOption(MR.strings.name_key, ClazzDaoCommon.SORT_CLAZZNAME_DESC, false),
            SortOrderOption(MR.strings.attendance, ClazzDaoCommon.SORT_ATTENDANCE_ASC, true),
            SortOrderOption(MR.strings.attendance, ClazzDaoCommon.SORT_ATTENDANCE_DESC, false)
        )

    }
}


class ClazzListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    destinationName: String = DEST_NAME,
): UstadListViewModel<ClazzListUiState>(
    di, savedStateHandle, ClazzListUiState(), destinationName
) {

    private val filterAlreadySelectedList = savedStateHandle[ARG_FILTER_EXCLUDE_SELECTED_CLASS_LIST]
        ?.split(",")?.filter { it.isNotEmpty() }?.map { it.trim().toLong() }
        ?: listOf()

    private val filterExcludeMembersOfSchool =
        savedStateHandle[PersonListViewModel.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL]?.toLong() ?: 0L

    private val filterByPermission = savedStateHandle[UstadView.ARG_FILTER_BY_PERMISSION]?.toLong()
        ?: Role.PERMISSION_CLAZZ_SELECT

    private var lastPagingSource: PagingSource<Int, ClazzWithListDisplayDetails>? = null

    private val pagingSourceFactory: () -> PagingSource<Int, ClazzWithListDisplayDetails> =  {
        activeRepo.clazzDao.findClazzesWithPermission(
            searchQuery =  _appUiState.value.searchState.searchText.toQueryLikeParam(),
            accountPersonUid = accountManager.currentAccount.personUid,
            excludeSelectedClazzList = filterAlreadySelectedList,
            excludeSchoolUid = filterExcludeMembersOfSchool,
            sortOrder = _uiState.value.activeSortOrderOption.flag,
            filter = _uiState.value.selectedChipId,
            currentTime = systemTimeInMillis(),
            permission = filterByPermission,
            selectedSchool = 0,
        ).also {
            lastPagingSource = it
        }
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(),
                title = listTitle(MR.strings.courses, MR.strings.courses),
                fabState = createFabState(true, MR.strings.course)
            )
        }

        _uiState.update { prev ->
            prev.copy(
                clazzList = pagingSourceFactory
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                activeRepo.scopedGrantDao.userHasSystemLevelPermissionAsFlow(
                    accountManager.currentAccount.personUid, Role.PERMISSION_CLAZZ_INSERT
                ).distinctUntilChanged().collect { hasPermission ->
                    _uiState.update { prev ->
                        prev.copy(
                            canAddNewCourse = hasPermission,
                            newClazzListOptionVisible = hasPermission && listMode == ListViewMode.PICKER
                        )
                    }
                }
            }
        }


    }

    override fun onUpdateSearchResult(searchText: String) {
        lastPagingSource?.invalidate()
    }

    override fun onClickAdd() {
        navigateToCreateNew(ClazzEditViewModel.DEST_NAME)
    }

    fun onClickJoinExistingClazz() {
        navController.navigate(JoinWithCodeView.VIEW_NAME, mapOf(
            UstadView.ARG_CODE_TABLE to Clazz.TABLE_ID.toString()
        ))
    }

    fun onClickEntry(entry: Clazz) {
        navigateOnItemClicked(ClazzDetailViewModel.DEST_NAME, entry.clazzUid, entry)
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update { prev ->
            prev.copy(
                activeSortOrderOption = sortOption
            )
        }
        lastPagingSource?.invalidate()
    }

    fun onClickFilterChip(filterOption: MessageIdOption2) {
        _uiState.update { prev ->
            prev.copy(
                selectedChipId = filterOption.value
            )
        }

        lastPagingSource?.invalidate()
    }



    companion object {

        const val DEST_NAME = "CourseList"

        const val DEST_NAME_HOME = "CourseListHome"

        val ALL_DEST_NAMES = listOf(DEST_NAME, DEST_NAME_HOME)

        const val ARG_FILTER_EXCLUDE_SELECTED_CLASS_LIST = "excludeAlreadySelectedClazzList"


    }

}
