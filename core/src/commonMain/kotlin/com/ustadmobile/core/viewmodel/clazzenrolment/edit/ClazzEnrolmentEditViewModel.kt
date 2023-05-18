package com.ustadmobile.core.viewmodel.clazzenrolment.edit

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.processEnrolmentIntoClass
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
                    newEntityMessageId = MessageID.new_enrolment,
                    editEntityMessageId = MessageID.edit_enrolment,
                ),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.save),
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

    fun onEntityChanged(entity: ClazzEnrolmentWithLeavingReason) {
        _uiState.update { prev ->
            prev.copy(
                clazzEnrolment = entity
            )
        }
    }

    fun onClickSave() {
        if(loadingState == LoadingUiState.INDETERMINATE)
            return

        val entity = _uiState.value.clazzEnrolment ?: return

        loadingState = LoadingUiState.INDETERMINATE
        viewModelScope.launch {
            activeDb.processEnrolmentIntoClass(entity)
        }
    }

    companion object {

        const val DEST_NAME = "EnrolmentEdit"

        const val ARG_ROLE = "role"

    }
}
