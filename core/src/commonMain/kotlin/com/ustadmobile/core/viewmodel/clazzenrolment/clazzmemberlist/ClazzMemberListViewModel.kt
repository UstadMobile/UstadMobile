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
import com.ustadmobile.core.viewmodel.clazz.parseAndUpdateTerminologyStringsIfNeeded
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.composites.PersonAndClazzMemberListDetails
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

data class ClazzMemberListUiState(

    val studentList: ListPagingSourceFactory<PersonAndClazzMemberListDetails> = { EmptyPagingSource() },

    val teacherList: ListPagingSourceFactory<PersonAndClazzMemberListDetails> = { EmptyPagingSource() },

    val pendingStudentList: ListPagingSourceFactory<PersonAndClazzMemberListDetails> = {
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

    val terminologyStrings: CourseTerminologyStrings? = null
)

class ClazzMemberListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle
): UstadListViewModel<ClazzMemberListUiState>(
    di, savedStateHandle, ClazzMemberListUiState(), ClazzDetailViewModel.DEST_NAME,
) {

    private val approveOrDeclinePendingEnrolmentUseCase: IApproveOrDeclinePendingEnrolmentRequestUseCase by
        on(accountManager.activeEndpoint).instance()

    private val clazzUid = savedStateHandle[UstadView.ARG_CLAZZUID]?.toLong()
        ?: throw IllegalArgumentException("No clazzuid")

    private var lastTeacherListPagingSource: PagingSource<Int, PersonAndClazzMemberListDetails>? = null

    private var lastStudentListPagingsource: PagingSource<Int, PersonAndClazzMemberListDetails>? = null

    private var lastPendingStudentListPagingSource: PagingSource<Int, PersonAndClazzMemberListDetails>? = null

    private fun getMembersAsPagingSource(
        roleId: Int
    ) : PagingSource<Int, PersonAndClazzMemberListDetails>  {
        return activeRepo.clazzEnrolmentDao.findByClazzUidAndRole(
            clazzUid = clazzUid,
            roleId = roleId,
            sortOrder = _uiState.value.activeSortOrderOption.flag,
            filter = _uiState.value.selectedChipId,
            searchText = _appUiState.value.searchState.searchText.toQueryLikeParam(),
            accountPersonUid = activeUserPersonUid,
            currentTime = systemTimeInMillis(),
        )
    }

    private val teacherListPagingSource: ListPagingSourceFactory<PersonAndClazzMemberListDetails> = {
        getMembersAsPagingSource(ClazzEnrolment.ROLE_TEACHER).also {
            lastTeacherListPagingSource = it
        }
    }

    private val studentListPagingSource: ListPagingSourceFactory<PersonAndClazzMemberListDetails> = {
        getMembersAsPagingSource(ClazzEnrolment.ROLE_STUDENT).also {
            lastStudentListPagingsource = it
        }
    }

    private val pendingStudentListPagingSource: ListPagingSourceFactory<PersonAndClazzMemberListDetails> = {
        getMembersAsPagingSource(ClazzEnrolment.ROLE_STUDENT_PENDING).also {
            lastPendingStudentListPagingSource = it
        }
    }


    init {
        _uiState.update { prev ->
            prev.copy(
                studentList = studentListPagingSource,
                teacherList = teacherListPagingSource,
                pendingStudentList = pendingStudentListPagingSource,
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
            launch {
                activeRepo.clazzDao.getClazzNameAndTerminologyAsFlow(clazzUid).collect { nameAndTerminology ->
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

            _uiState.whenSubscribed {
                launch {
                    activeDb.clazzDao.personHasPermissionWithClazzAsFlow(
                        accountPersonUid = activeUserPersonUid, clazzUid = clazzUid,
                        permission = Role.PERMISSION_CLAZZ_ADD_TEACHER
                    ).collect { canAddTeacher ->
                        _uiState.takeIf { it.value.addTeacherVisible != canAddTeacher }?.update { prev ->
                            prev.copy(addTeacherVisible = canAddTeacher)
                        }
                    }
                }

                launch {
                    activeDb.clazzDao.personHasPermissionWithClazzAsFlow(
                        accountPersonUid = activeUserPersonUid, clazzUid = clazzUid,
                        permission = Role.PERMISSION_CLAZZ_ADD_STUDENT
                    ).collect { canAddStudent ->
                        _uiState.takeIf { it.value.addStudentVisible != canAddStudent }?.update { prev ->
                            prev.copy(addStudentVisible = canAddStudent)
                        }
                    }
                }
            }
        }
    }

    private fun invalidatePagingSources(){
        lastTeacherListPagingSource?.invalidate()
        lastStudentListPagingsource?.invalidate()
        lastPendingStudentListPagingSource?.invalidate()
    }

    override fun onUpdateSearchResult(searchText: String) {
        invalidatePagingSources()
    }

    override fun onClickAdd() {
        //Do nothing
    }

    fun onClickFilterChip(filterOption: MessageIdOption2) {
        _uiState.update { prev ->
            prev.copy(selectedChipId = filterOption.value)
        }

        invalidatePagingSources()
    }

    fun onClickRespondToPendingEnrolment(
        enrolmentDetails: PersonAndClazzMemberListDetails,
        approved: Boolean
    ) {
        viewModelScope.launch {
            approveOrDeclinePendingEnrolmentUseCase(
                personUid = enrolmentDetails.person?.personUid ?: 0,
                clazzUid = clazzUid,
                approved = approved
            )
        }
    }

    fun onClickAddNewMember(role: Int) {
        val goToOnPersonSelectedArg = ClazzEnrolmentEditViewModel.DEST_NAME
            .appendQueryArgs(mapOf(
                UstadView.ARG_CLAZZUID to clazzUid.toString(),
                UstadView.ARG_POPUPTO_ON_FINISH to destinationName,
                ClazzEnrolmentEditViewModel.ARG_ROLE to role.toString(),
            ))

        val args = mutableMapOf(
            PersonListViewModel.ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ to clazzUid.toString(),
            UstadView.ARG_LISTMODE to ListViewMode.PICKER.mode,
            PersonViewModelConstants.ARG_GO_TO_ON_PERSON_SELECTED to goToOnPersonSelectedArg,
        )

        navController.navigate(
            viewName = PersonListViewModel.DEST_NAME,
            args = args
        )
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
        invalidatePagingSources()
    }

    companion object {

        const val DEST_NAME = "CourseMembers"

    }
}
