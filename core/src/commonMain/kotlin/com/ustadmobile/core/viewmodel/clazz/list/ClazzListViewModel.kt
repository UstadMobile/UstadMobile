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
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.ext.dayStringResource
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazz.edit.ClazzEditViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.EnrolmentRequestAndCoursePic
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.lib.db.entities.EnrolmentRequest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.DI


data class ClazzListUiState(

    val newClazzListOptionVisible: Boolean = true,

    val clazzList: () -> PagingSource<Int, ClazzWithListDisplayDetails> = { EmptyPagingSource() },

    val sortOptions: List<SortOrderOption> = DEFAULT_SORT_OTIONS,

    val activeSortOrderOption: SortOrderOption = sortOptions.first(),

    val fieldsEnabled: Boolean = true,

    val selectedChipId: Int = ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED,

    val canAddNewCourse: Boolean = false,

    val pendingEnrolments: List<EnrolmentRequestAndCoursePic> = emptyList(),

    val filterOptions: List<MessageIdOption2> = listOf(
        MessageIdOption2(MR.strings.currently_enrolled, ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED),
        MessageIdOption2(MR.strings.all, 0)
    ),

    val dayOfWeekStrings: Map<DayOfWeek, String> = emptyMap(),

    val localDateTimeNow: LocalDateTime = Clock.System.now().toLocalDateTime(
        TimeZone.currentSystemDefault()
    )

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


    private val filterByPermission = savedStateHandle[UstadView.ARG_FILTER_BY_PERMISSION]?.toLong()
        ?: PermissionFlags.COURSE_VIEW


    private val pagingSourceFactory: () -> PagingSource<Int, ClazzWithListDisplayDetails> =  {
        activeRepoWithFallback.clazzDao().findClazzesWithPermission(
            searchQuery =  _appUiState.value.searchState.searchText.toQueryLikeParam(),
            accountPersonUid = accountManager.currentAccount.personUid,
            excludeSelectedClazzList = filterAlreadySelectedList,
            sortOrder = _uiState.value.activeSortOrderOption.flag,
            filter = _uiState.value.selectedChipId,
            currentTime = systemTimeInMillis(),
            permission = filterByPermission,
        )
    }

    init {
        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(),
                title = listTitle(MR.strings.courses, MR.strings.courses),
                fabState = createFabState(
                    hasAddPermission = activeUserPersonUid != 0L,
                    stringResource = MR.strings.course,
                )
            )
        }

        _uiState.update { prev ->
            prev.copy(
                dayOfWeekStrings = DayOfWeek.values().associateWith {
                    systemImpl.getString(it.dayStringResource)
                },
                clazzList = pagingSourceFactory
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                activeRepoWithFallback.systemPermissionDao().personHasSystemPermissionAsFlow(
                    accountManager.currentAccount.personUid, PermissionFlags.ADD_COURSE
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

        viewModelScope.launch {
            _uiState.whenSubscribed {
                activeRepoWithFallback.enrolmentRequestDao().findRequestsForUserAsFlow(
                    accountPersonUid = activeUserPersonUid,
                    statusFilter = EnrolmentRequest.STATUS_PENDING,
                ).collect {
                    _uiState.update { prev ->
                        prev.copy(pendingEnrolments = it)
                    }
                }
            }
        }
    }

    override fun onUpdateSearchResult(searchText: String) {
        _refreshCommandFlow.tryEmit(RefreshCommand())
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
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    fun onClickFilterChip(filterOption: MessageIdOption2) {
        _uiState.update { prev ->
            prev.copy(
                selectedChipId = filterOption.value
            )
        }

        _refreshCommandFlow.tryEmit(RefreshCommand())
    }


    fun onClickCancelEnrolmentRequest(enrolmentRequest: EnrolmentRequest) {
        viewModelScope.launch {
            activeRepoWithFallback.enrolmentRequestDao().updateStatus(
                uid = enrolmentRequest.erUid,
                status = EnrolmentRequest.STATUS_CANCELED,
                updateTime = systemTimeInMillis(),
            )
            snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.canceled_enrolment_request)))
        }
    }


    companion object {

        const val DEST_NAME = "CourseList"

        const val DEST_NAME_HOME = "CourseListHome"

        val ALL_DEST_NAMES = listOf(DEST_NAME, DEST_NAME_HOME)

        const val ARG_FILTER_EXCLUDE_SELECTED_CLASS_LIST = "excludeAlreadySelectedClazzList"


    }

}
