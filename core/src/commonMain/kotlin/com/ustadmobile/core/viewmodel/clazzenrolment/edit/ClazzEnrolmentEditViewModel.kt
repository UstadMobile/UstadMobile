package com.ustadmobile.core.viewmodel.clazzenrolment.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.ext.processEnrolmentIntoClass
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI

data class ClazzEnrolmentEditUiState(

    val clazzEnrolment: ClazzEnrolmentWithLeavingReason? = null,

    val roleSelectedError: String? = null,

    val startDateError: String? = null,

    val endDateError: String? = null,

    val fieldsEnabled: Boolean = true,

    val courseTerminology: CourseTerminology? = null,

    val roleOptions: List<Int> = listOf(ClazzEnrolment.ROLE_STUDENT, ClazzEnrolment.ROLE_TEACHER),

) {

    val leavingReasonEnabled: Boolean
        get() = clazzEnrolment?.clazzEnrolmentOutcome !=
                ClazzEnrolment.OUTCOME_IN_PROGRESS
}

class ClazzEnrolmentEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(ClazzEnrolmentEditUiState(fieldsEnabled = false))

    val uiState: Flow<ClazzEnrolmentEditUiState> = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                loadingState = LoadingUiState.INDETERMINATE,
                title = createEditTitle(
                    newEntityStringResource = MR.strings.new_enrolment,
                    editEntityStringResource = MR.strings.edit_enrolment,
                ),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MR.strings.save),
                    onClick = this::onClickSave
                )
            )
        }

        _uiState.update { prev ->
            prev.copy(fieldsEnabled = false)
        }

        viewModelScope.launch {
            loadEntity(
                serializer = ClazzEnrolmentWithLeavingReason.serializer(),
                onLoadFromDb = { db ->
                    db.clazzEnrolmentDao.takeIf { entityUidArg != 0L }
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
                        timeZone = activeRepo.clazzDao.getClazzTimeZoneByClazzUidAsync(
                            clazzEnrolmentClazzUid
                        ) ?: throw IllegalStateException("Could not find timezone for clazzUid")
                    }
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(clazzEnrolment = it)
                    }
                }
            )

            suspend fun userHasPermission(permission: Long): Boolean {
                return activeRepo.clazzDao.personHasPermissionWithClazz(
                    accountPersonUid = activeUserPersonUid,
                    clazzUid = _uiState.value.clazzEnrolment?.clazzEnrolmentClazzUid ?: 0L,
                    permission = permission
                )
            }

            val canAddTeacher = async {
                userHasPermission(Role.PERMISSION_CLAZZ_ADD_TEACHER)
            }
            val canAddStudent = async {
                userHasPermission(Role.PERMISSION_CLAZZ_ADD_STUDENT)
            }

            val roleOptions = buildList {
                if(canAddStudent.await())
                    add(ClazzEnrolment.ROLE_STUDENT)

                if(canAddTeacher.await())
                    add(ClazzEnrolment.ROLE_TEACHER)
            }

            _uiState.update { prev ->
                prev.copy(
                    roleOptions = roleOptions,
                    fieldsEnabled = true
                )
            }

            loadingState = LoadingUiState.NOT_LOADING
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
                )

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
                activeDb.processEnrolmentIntoClass(entity)
            }else {
                activeDb.clazzEnrolmentDao.updateAsync(entity)
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
