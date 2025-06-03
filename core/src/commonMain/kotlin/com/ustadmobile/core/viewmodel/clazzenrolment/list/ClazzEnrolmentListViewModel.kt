package com.ustadmobile.core.viewmodel.clazzenrolment.list

import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.edit.ClazzEnrolmentEditViewModel
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.CourseTerminology
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI


data class ClazzEnrolmentListUiState(
    val enrolmentList: List<ClazzEnrolmentWithLeavingReason> = emptyList(),
    val personName: String? = null,
    var courseName: String? = null,
    val canEditTeacherEnrolments: Boolean = false,
    val canEditStudentEnrolments: Boolean = false,
    val courseTerminology: CourseTerminology? = null,
    val timeZone: String = "UTC",
) {

    fun enrolmentItemUiState(
        enrolment: ClazzEnrolmentWithLeavingReason
    ): ClazzEnrolmentListItemUiState {
        return ClazzEnrolmentListItemUiState(
            enrolment = enrolment,
            canEdit = if(enrolment.clazzEnrolmentRole == ClazzEnrolment.ROLE_TEACHER) {
                canEditTeacherEnrolments
            }else {
                canEditTeacherEnrolments
            },
            timeZone = timeZone,
        )
    }

}

data class ClazzEnrolmentListItemUiState(
    val canEdit: Boolean = false,
    val enrolment: ClazzEnrolmentWithLeavingReason,
    val timeZone: String,
)

class ClazzEnrolmentListViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadListViewModel<ClazzEnrolmentListUiState>(
    di, savedStateHandle, ClazzEnrolmentListUiState(), DEST_NAME
) {

    private val argClazzUid = savedStateHandle[UstadView.ARG_CLAZZUID]?.toLong() ?: 0

    private val argPersonUid = savedStateHandle[UstadView.ARG_PERSON_UID]?.toLong() ?: 0

    init {
        viewModelScope.launch {
            val permissionFlow = activeDb.coursePermissionDao().personHasPermissionWithClazzTripleAsFlow(
                accountPersonUid = activeUserPersonUid,
                clazzUid = argClazzUid,
                firstPermission = PermissionFlags.COURSE_MANAGE_TEACHER_ENROLMENT,
                secondPermission = PermissionFlags.COURSE_MANAGE_STUDENT_ENROLMENT,
                thirdPermission = PermissionFlags.PERSON_VIEW,
            ).shareIn(viewModelScope, SharingStarted.WhileSubscribed())

            val canViewMembersFlow = permissionFlow.map { it.thirdPermission }

            _uiState.whenSubscribed {
                launch {
                    val terminology = activeRepoWithFallback.courseTerminologyDao()
                        .getTerminologyForClazz(argClazzUid)
                    _uiState.update { prev ->
                        prev.copy(courseTerminology = terminology)
                    }
                }

                launch {
                    permissionFlow.distinctUntilChanged().collect {
                        val (canEditTeacherEnrolments, canEditStudentEnrolments, _) = it

                        _uiState.update { prev ->
                            prev.copy(
                                canEditTeacherEnrolments = canEditTeacherEnrolments,
                                canEditStudentEnrolments = canEditStudentEnrolments,
                            )
                        }
                    }
                }

                launch {
                    canViewMembersFlow.distinctUntilChanged().collectLatest { canViewMembers ->
                        val enrolmentsListFlow = if(canViewMembers) {
                            activeRepoWithFallback.clazzEnrolmentDao().findAllEnrolmentsByPersonAndClazzUid(
                                personUid = argPersonUid,
                                clazzUid = argClazzUid,
                            )
                        }else {
                            flowOf(emptyList())
                        }

                        enrolmentsListFlow.collect {
                            _uiState.update { prev ->
                                prev.copy(
                                    enrolmentList = it
                                )
                            }
                        }
                    }
                }

                launch {
                    canViewMembersFlow.distinctUntilChanged().filter { it }.collectLatest { canViewMembers ->
                        if(canViewMembers) {
                            val courseAndPersonName = activeRepoWithFallback
                                .clazzEnrolmentDao().getClazzNameAndPersonName(
                                    personUid = argPersonUid,
                                    clazzUid = argClazzUid,
                                )

                            val personName = "${courseAndPersonName?.firstNames} ${courseAndPersonName?.lastName}"

                            _uiState.update { prev ->
                                prev.copy(
                                    personName = personName,
                                    courseName = courseAndPersonName?.clazzName,
                                )
                            }

                            _appUiState.update { prev ->
                                prev.copy(
                                    title = personName
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun onClickEditEnrolment(enrolment: ClazzEnrolmentWithLeavingReason) {
        navigateForResult(
            nextViewName = ClazzEnrolmentEditViewModel.DEST_NAME,
            key = "",
            currentValue = null,
            serializer = ClazzEnrolmentWithLeavingReason.serializer(),
            args = mapOf(UstadView.ARG_ENTITY_UID to enrolment.clazzEnrolmentUid.toString()),
            overwriteDestination = true,
        )
    }

    fun onClickViewProfile() {
        navController.navigate(PersonDetailViewModel.DEST_NAME,
            mapOf(UstadView.ARG_ENTITY_UID to argPersonUid.toString())
        )
    }

    override fun onUpdateSearchResult(searchText: String) {
        //do nothing
    }

    override fun onClickAdd() {
        //do nothing
    }

    companion object {

        const val DEST_NAME = "CourseEnrolments"

    }

}