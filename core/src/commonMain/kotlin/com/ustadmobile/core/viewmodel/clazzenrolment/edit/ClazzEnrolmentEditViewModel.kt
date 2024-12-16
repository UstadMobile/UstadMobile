package com.ustadmobile.core.viewmodel.clazzenrolment.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.locale.CourseTerminologyStrings
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

data class ClazzEnrolmentEditUiState(

    val clazzEnrolment: ClazzEnrolmentWithLeavingReason? = null,

    val roleSelectedError: String? = null,

    val startDateError: String? = null,

    val endDateError: String? = null,

    val fieldsEnabled: Boolean = false,

    val courseTerminology: CourseTerminologyStrings? = null,

    val roleOptions: List<Int> = listOf(ClazzEnrolment.ROLE_STUDENT, ClazzEnrolment.ROLE_TEACHER),

) {

    val outcomeVisible: Boolean
        get() = clazzEnrolment?.clazzEnrolmentRole == ClazzEnrolment.ROLE_STUDENT
}

class ClazzEnrolmentEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(ClazzEnrolmentEditUiState(fieldsEnabled = false))

    val uiState: Flow<ClazzEnrolmentEditUiState> = _uiState.asStateFlow()

    private val enrolIntoCourseUseCase: EnrolIntoCourseUseCase by di.onActiveEndpoint().instance()

    init {
        _appUiState.update { prev ->
            prev.copy(
                loadingState = LoadingUiState.INDETERMINATE,
                title = createEditTitle(
                    newEntityStringResource = MR.strings.new_enrolment,
                    editEntityStringResource = MR.strings.edit_enrolment,
                ),
            )
        }

        launchIfHasPermission(
            permissionCheck = { db ->
                db.coursePermissionDao().userHasEnrolmentEditPermission(
                    accountPersonUid = activeUserPersonUid,
                    clazzEnrolmentUid = entityUidArg,
                )
            }
        ) {
            loadEntity(
                serializer = ClazzEnrolmentWithLeavingReason.serializer(),
                onLoadFromDb = { db ->
                    db.clazzEnrolmentDao().takeIf { entityUidArg != 0L }
                        ?.findEnrolmentWithLeavingReason(entityUidArg)
                },
                makeDefault = {
                    ClazzEnrolmentWithLeavingReason().apply {
                        clazzEnrolmentClazzUid = savedStateHandle[UstadView.ARG_CLAZZUID]?.toLong()
                            ?: throw IllegalArgumentException("No clazzUid for enrolment!")
                        clazzEnrolmentPersonUid = savedStateHandle[UstadView.ARG_PERSON_UID]?.toLong()
                            ?: throw IllegalArgumentException("No personuid for enrolment!")
                        clazzEnrolmentDateJoined = systemTimeInMillis()
                        clazzEnrolmentRole = savedStateHandle[ARG_ROLE]?.toInt()
                            ?: ClazzEnrolment.ROLE_STUDENT
                        timeZone = activeRepoWithFallback.clazzDao().getClazzTimeZoneByClazzUidAsync(
                            clazzEnrolmentClazzUid
                        ) ?: throw IllegalStateException("Could not find timezone for clazzUid")
                    }
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(
                            clazzEnrolment = it,
                        )
                    }
                }
            )

            //This can be run directly against the database, because any entities required would
            // already have been pulled down by the inital launch permission check
            val (canManageStudentEnrolment, canManageTeacherEnrolment) = activeDb.coursePermissionDao().personHasPermissionWithClazzPairAsync(
                accountPersonUid = activeUserPersonUid,
                clazzUid = _uiState.value.clazzEnrolment?.clazzEnrolmentClazzUid ?: 0L,
                firstPermission = PermissionFlags.COURSE_MANAGE_STUDENT_ENROLMENT,
                secondPermission = PermissionFlags.COURSE_MANAGE_TEACHER_ENROLMENT
            )


            val terminology = async {
                activeRepoWithFallback.courseTerminologyDao().getTerminologyForClazz(
                    _uiState.value.clazzEnrolment?.clazzEnrolmentClazzUid ?: 0
                )
            }

            val roleOptions = buildList {
                if(canManageStudentEnrolment)
                    add(ClazzEnrolment.ROLE_STUDENT)

                if(canManageTeacherEnrolment)
                    add(ClazzEnrolment.ROLE_TEACHER)
            }

            val terminologyStrings = terminology.await()?.let {
                CourseTerminologyStrings(it, systemImpl, json)
            }

            _uiState.update { prev ->
                prev.copy(
                    roleOptions = roleOptions,
                    fieldsEnabled = true,
                    courseTerminology = terminologyStrings,
                )
            }

            _appUiState.update { prev ->
                prev.copy(
                    loadingState = LoadingUiState.NOT_LOADING,
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = systemImpl.getString(MR.strings.save),
                        onClick = this@ClazzEnrolmentEditViewModel::onClickSave
                    )
                )
            }
        }
    }

    fun onEntityChanged(entity: ClazzEnrolmentWithLeavingReason?) {
        _uiState.update { prev ->
            prev.copy(
                clazzEnrolment = entity,
                roleSelectedError = updateErrorMessageOnChange(
                    prevFieldValue = prev.clazzEnrolment?.clazzEnrolmentRole,
                    currentFieldValue = entity?.clazzEnrolmentRole,
                    currentErrorMessage = prev.roleSelectedError
                ),
                startDateError = updateErrorMessageOnChange(
                    prevFieldValue = prev.clazzEnrolment?.clazzEnrolmentDateJoined,
                    currentFieldValue = entity?.clazzEnrolmentDateJoined,
                    currentErrorMessage = prev.startDateError,
                ),
            )
        }

        scheduleEntityCommitToSavedState(
            entity = entity,
            serializer = ClazzEnrolmentWithLeavingReason.serializer(),
            commitDelay = 200,
        )
    }

    private fun ClazzEnrolmentEditUiState.hasErrors() : Boolean {
        return roleSelectedError != null ||
            startDateError != null ||
            endDateError != null
    }

    fun onClickSave() {
        if(loadingState == LoadingUiState.INDETERMINATE)
            return

        val entity = _uiState.value.clazzEnrolment ?: return
        val timeZoneVal = entity.timeZone
        if(timeZoneVal == null) {
            snackDispatcher.showSnackBar(Snack(message = "Error: no time zone for course"))
            return
        }

        loadingState = LoadingUiState.INDETERMINATE
        _uiState.update { prev ->
            prev.copy(fieldsEnabled = false)
        }

        if(entity.clazzEnrolmentRole == 0) {
            _uiState.update { prev ->
                prev.copy(
                    roleSelectedError = systemImpl.getString(MR.strings.field_required_prompt)
                )
            }
        }

        if(entity.clazzEnrolmentDateJoined <= MS_PER_HOUR * 24) {
            _uiState.update { prev ->
                prev.copy(
                    startDateError = systemImpl.getString(MR.strings.field_required_prompt)
                )
            }
        }

        if(entity.clazzEnrolmentDateLeft <= entity.clazzEnrolmentDateJoined) {
            _uiState.update { prev ->
                prev.copy(
                    endDateError = systemImpl.getString(MR.strings.end_is_before_start_error)
                )
            }
        }

        if(_uiState.value.hasErrors()) {
            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }

            return
        }


        viewModelScope.launch {
            if(entityUidArg == 0L) {
                enrolIntoCourseUseCase(
                    enrolment = entity,
                    timeZoneId = timeZoneVal
                )
            }else {
                activeDb.clazzEnrolmentDao().updateAsync(entity)
            }

            val popUpToOnFinish = savedStateHandle[UstadView.ARG_POPUPTO_ON_FINISH]
            if(popUpToOnFinish != null){
                navController.popBackStack(popUpToOnFinish, inclusive = false)
            }else {
                finishWithResult(entity)
            }
        }
    }

    companion object {

        const val DEST_NAME = "EnrolmentEdit"

        const val ARG_ROLE = "role"

    }
}
