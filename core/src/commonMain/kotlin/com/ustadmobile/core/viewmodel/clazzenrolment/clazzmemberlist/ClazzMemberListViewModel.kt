package com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist

import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.IApproveOrDeclinePendingEnrolmentRequestUseCase
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.locale.CourseTerminologyStrings
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.edit.ClazzEnrolmentEditViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.list.ClazzEnrolmentListViewModel
import com.ustadmobile.core.viewmodel.person.PersonViewModelConstants
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import app.cash.paging.PagingSource
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.ext.dayStringResource
import com.ustadmobile.core.util.ext.localFirstThenRepoIfNull
import com.ustadmobile.core.viewmodel.clazz.parseAndUpdateTerminologyStringsIfNeeded
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.EnrolmentRequestAndPersonDetails
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.composites.PersonAndClazzMemberListDetails
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
import org.kodein.di.instance
import org.kodein.di.on

data class ClazzMemberListUiState(

    val studentList: ListPagingSourceFactory<PersonAndClazzMemberListDetails> = { EmptyPagingSource() },

    val teacherList: ListPagingSourceFactory<PersonAndClazzMemberListDetails> = { EmptyPagingSource() },

    val pendingStudentList: ListPagingSourceFactory<EnrolmentRequestAndPersonDetails> = {
        EmptyPagingSource()
    },

    val addTeacherVisible: Boolean = false,

    val addStudentVisible: Boolean = false,

    val sortOptions: List<SortOrderOption> = listOf(
        SortOrderOption(MR.strings.first_name, ClazzEnrolmentDaoCommon.SORT_FIRST_NAME_ASC, true),
        SortOrderOption(MR.strings.first_name, ClazzEnrolmentDaoCommon.SORT_FIRST_NAME_DESC, false),
        SortOrderOption(MR.strings.last_name, ClazzEnrolmentDaoCommon.SORT_LAST_NAME_ASC, true),
        SortOrderOption(MR.strings.last_name, ClazzEnrolmentDaoCommon.SORT_LAST_NAME_DESC, false),
        SortOrderOption(MR.strings.date_enroll, ClazzEnrolmentDaoCommon.SORT_DATE_REGISTERED_ASC, true),
        SortOrderOption(MR.strings.date_enroll, ClazzEnrolmentDaoCommon.SORT_DATE_REGISTERED_DESC, false),
        SortOrderOption(MR.strings.date_left, ClazzEnrolmentDaoCommon.SORT_DATE_LEFT_ASC, true),
        SortOrderOption(MR.strings.date_left, ClazzEnrolmentDaoCommon.SORT_DATE_LEFT_DESC, false)
    ),

    val activeSortOrderOption: SortOrderOption = sortOptions.first(),

    val fieldsEnabled: Boolean = true,

    val selectedChipId: Int = ClazzEnrolmentDaoCommon.FILTER_ACTIVE_ONLY,

    val filterOptions: List<MessageIdOption2> = listOf(
        MessageIdOption2(MR.strings.active, ClazzEnrolmentDaoCommon.FILTER_ACTIVE_ONLY),
        MessageIdOption2(MR.strings.all, 0)
    ),

    val terminologyStrings: CourseTerminologyStrings? = null,

    val localDateTimeNow: LocalDateTime = Clock.System.now().toLocalDateTime(
        TimeZone.currentSystemDefault()
    ),

    val dayOfWeekStrings: Map<DayOfWeek, String> = emptyMap(),

)

class ClazzMemberListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle
): UstadListViewModel<ClazzMemberListUiState>(
    di, savedStateHandle, ClazzMemberListUiState(), ClazzDetailViewModel.DEST_NAME,
) {

    private val approveOrDeclinePendingEnrolmentUseCase: IApproveOrDeclinePendingEnrolmentRequestUseCase by
        on(accountManager.activeLearningSpace).instance()

    private val clazzUid = savedStateHandle[UstadView.ARG_CLAZZUID]?.toLong()
        ?: throw IllegalArgumentException("No clazzuid")

    private fun getMembersAsPagingSource(
        roleId: Int
    ) : PagingSource<Int, PersonAndClazzMemberListDetails>  {
        return activeRepoWithFallback.clazzEnrolmentDao().findByClazzUidAndRole(
            clazzUid = clazzUid,
            roleId = roleId,
            sortOrder = _uiState.value.activeSortOrderOption.flag,
            filter = _uiState.value.selectedChipId,
            searchText = _appUiState.value.searchState.searchText.toQueryLikeParam(),
            accountPersonUid = activeUserPersonUid,
            currentTime = systemTimeInMillis(),
            permission = PermissionFlags.PERSON_VIEW,
        )
    }

    private val teacherListPagingSource: ListPagingSourceFactory<PersonAndClazzMemberListDetails> = {
        getMembersAsPagingSource(ClazzEnrolment.ROLE_TEACHER)
    }

    private val studentListPagingSource: ListPagingSourceFactory<PersonAndClazzMemberListDetails> = {
        getMembersAsPagingSource(ClazzEnrolment.ROLE_STUDENT)
    }

    private val pendingStudentListPagingSource: ListPagingSourceFactory<EnrolmentRequestAndPersonDetails> = {
        activeRepoWithFallback.enrolmentRequestDao().findPendingEnrolmentsForCourse(
            clazzUid = clazzUid,
            includeDeleted = false,
            searchText = _appUiState.value.searchState.searchText.toQueryLikeParam(),
            statusFilter = EnrolmentRequest.STATUS_PENDING,
            sortOrder = _uiState.value.activeSortOrderOption.flag,
        )
    }


    init {
        _uiState.update { prev ->
            prev.copy(
                studentList = studentListPagingSource,
                teacherList = teacherListPagingSource,
                pendingStudentList = pendingStudentListPagingSource,
                dayOfWeekStrings = DayOfWeek.values().associateWith {
                    systemImpl.getString(it.dayStringResource)
                },
            )
        }

        _appUiState.update { prev ->
            prev.copy(
                navigationVisible = true,
                searchState = createSearchEnabledState(),
                actionBarButtonState = ActionBarButtonUiState(visible = false),
            )
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepoWithFallback.clazzDao().getClazzNameAndTerminologyAsFlow(clazzUid).collect { nameAndTerminology ->
                        parseAndUpdateTerminologyStringsIfNeeded(
                            currentTerminologyStrings = _uiState.value.terminologyStrings,
                            terminology = nameAndTerminology?.terminology,
                            json = json,
                            systemImpl = systemImpl,
                        ) {
                            _uiState.update { prev -> prev.copy(terminologyStrings = it) }
                        }

                        _appUiState.update { prev ->
                            prev.copy(title = nameAndTerminology?.clazzName ?: "")
                        }
                    }
                }

                launch {
                    //Note: we can use the db here, because the permission entities will be pulled
                    // down by the repo query that is running on the member list itself
                    activeDb.coursePermissionDao().personHasPermissionWithClazzPairAsFlow(
                        accountPersonUid = activeUserPersonUid,
                        clazzUid = clazzUid,
                        firstPermission = PermissionFlags.COURSE_MANAGE_TEACHER_ENROLMENT,
                        secondPermission = PermissionFlags.COURSE_MANAGE_STUDENT_ENROLMENT
                    ).distinctUntilChanged().collect {
                        _uiState.update { prev ->
                            prev.copy(
                                addTeacherVisible = it.firstPermission,
                                addStudentVisible = it.secondPermission
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

    override fun onClickAdd() {
        //Do nothing
    }

    fun onClickFilterChip(filterOption: MessageIdOption2) {
        _uiState.update { prev ->
            prev.copy(selectedChipId = filterOption.value)
        }

        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    fun onClickRespondToPendingEnrolment(
        enrolmentDetails: EnrolmentRequest,
        approved: Boolean
    ) {
        viewModelScope.launch {
            try {
                approveOrDeclinePendingEnrolmentUseCase(enrolmentDetails, approved)
                snackDispatcher.showSnackBar(
                    Snack(
                        systemImpl.formatString(
                            if(approved) MR.strings.enroled_into_name else MR.strings.declined_request_from_name,
                            (enrolmentDetails.erPersonFullname ?: "")
                        )
                    )
                )
            }catch (e: Throwable) {
                snackDispatcher.showSnackBar(
                    Snack(systemImpl.getString(MR.strings.error) + (e.message ?: ""))
                )
            }
        }
    }

    fun onClickAddNewMember(role: Int) {
        viewModelScope.launch {
            val clazzCode = activeRepo
                .takeIf { role == ClazzEnrolment.ROLE_STUDENT }
                ?.localFirstThenRepoIfNull {
                    it.clazzDao().findByUidAsync(clazzUid)?.clazzCode
                }

            val titleStringResource = if(role == ClazzEnrolment.ROLE_STUDENT) {
                MR.strings.add_a_student
            }else {
                MR.strings.add_a_teacher
            }
            val title = _uiState.value.terminologyStrings?.get(titleStringResource)
                ?: systemImpl.getString(titleStringResource)

            val goToOnPersonSelectedArg = ClazzEnrolmentEditViewModel.DEST_NAME
                .appendQueryArgs(
                    mapOf(
                        UstadView.ARG_CLAZZUID to clazzUid.toString(),
                        UstadView.ARG_POPUPTO_ON_FINISH to destinationName,
                        ClazzEnrolmentEditViewModel.ARG_ROLE to role.toString(),
                    )
                )

            val args = buildMap {
                put(PersonListViewModel.ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ, clazzUid.toString())
                put(UstadView.ARG_LISTMODE, ListViewMode.PICKER.mode)
                put(PersonViewModelConstants.ARG_GO_TO_ON_PERSON_SELECTED, goToOnPersonSelectedArg)
                put(ARG_TITLE, title)
                put(PersonListViewModel.ARG_REQUIRE_PERMISSION_TO_SHOW_LIST,
                    PermissionFlags.DIRECT_ENROL.toString())

                if(clazzCode != null)
                    put(PersonListViewModel.ARG_SHOW_ADD_VIA_INVITE_LINK_CODE, clazzCode)
            }

            navController.navigate(
                viewName = PersonListViewModel.DEST_NAME,
                args = args
            )
        }

    }

    fun onClickEntry(
        entry: PersonAndClazzMemberListDetails
    ) {
        navController.navigate(
            viewName = ClazzEnrolmentListViewModel.DEST_NAME,
            args = mapOf(
                UstadView.ARG_PERSON_UID to (entry.person?.personUid ?: 0).toString(),
                UstadView.ARG_CLAZZUID to clazzUid.toString(),
            )
        )
    }

    fun onSortOrderChanged(sortOption: SortOrderOption) {
        _uiState.update { prev ->
            prev.copy(activeSortOrderOption = sortOption)
        }
        _refreshCommandFlow.tryEmit(RefreshCommand())
    }

    companion object {

        const val DEST_NAME = "CourseMembers"

    }
}
