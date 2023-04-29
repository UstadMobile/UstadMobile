package com.ustadmobile.core.viewmodel.clazzassignment.edit

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.peerreviewallocation.UpdatePeerReviewAllocationUseCase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.asCourseBlockWithEntity
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.ClazzAssignmentEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.CourseBlockViewModelConstants
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.lib.db.entities.ext.shallowCopyWithEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import org.kodein.di.DI
import org.kodein.di.instance

@kotlinx.serialization.Serializable
data class ClazzAssignmentEditUiState(

    val fieldsEnabled: Boolean = true,

    val markingTypeEnabled: Boolean = true,

    val groupSetEnabled: Boolean = true,

    val caTitleError: String? = null,

    val reviewerCountError: String? = null,

    val groupSet: CourseGroupSet? = null,

    val timeZone: String? = null,

    val entity: CourseBlockWithEntity? = null,

    val minScoreVisible: Boolean = false,

    val courseTerminology: CourseTerminology? = null,

    val submissionRequiredError: String? = null,

    val courseBlockEditUiState: CourseBlockEditUiState = CourseBlockEditUiState(
        completionCriteriaOptions = ASSIGNMENT_COMPLETION_CRITERIAS
    ),
) {

    val peerMarkingVisible: Boolean
        get() = entity?.assignment?.caMarkingType == ClazzAssignment.MARKED_BY_PEERS

    val textSubmissionVisible: Boolean
        get() = entity?.assignment?.caRequireTextSubmission == true

    val fileSubmissionVisible: Boolean
        get()  = entity?.assignment?.caRequireFileSubmission == true


    companion object {

        val ASSIGNMENT_COMPLETION_CRITERIAS = listOf(
            CourseBlockViewModelConstants.CompletionCriteria.ASSIGNMENT_SUBMITTED,
            CourseBlockViewModelConstants.CompletionCriteria.ASSIGNMENT_GRADED
        )

    }
}

class ClazzAssignmentEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    val peerReviewAllocationUseCaseFactory: (UmAppDatabase, UstadMobileSystemImpl)
        -> UpdatePeerReviewAllocationUseCase = { db, systemImpl ->
        UpdatePeerReviewAllocationUseCase(db, systemImpl)
    }
): UstadEditViewModel(di, savedStateHandle, ClazzAssignmentEditView.VIEW_NAME) {

    private val _uiState = MutableStateFlow(ClazzAssignmentEditUiState())

    val uiState: Flow<ClazzAssignmentEditUiState> = _uiState.asStateFlow()

    private val snackDisaptcher: SnackBarDispatcher by instance()

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                loadingState = LoadingUiState.INDETERMINATE,
                title = createEditTitle(MessageID.new_assignment, MessageID.edit_assignment),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.done),
                    onClick = this::onClickSave,
                )
            )
        }

        _uiState.update { prev ->
            prev.copy(fieldsEnabled = false)
        }

        viewModelScope.launch {
            loadEntity(
                serializer = CourseBlockWithEntity.serializer(),
                onLoadFromDb = { null  }, //does not load from db, always via json
                makeDefault = {
                    val assignmentUid = activeDb.doorPrimaryKeyManager.nextId(ClazzAssignment.TABLE_ID)
                    CourseBlockWithEntity().apply {
                        cbUid = activeDb.doorPrimaryKeyManager.nextId(CourseBlock.TABLE_ID)
                        cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                        cbEntityUid = assignmentUid
                        assignment = ClazzAssignment().apply {
                            caUid = assignmentUid
                            caClazzUid = savedStateHandle[UstadView.ARG_CLAZZUID]?.toLong() ?: 0
                        }
                    }
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(
                            entity = it,
                            courseBlockEditUiState = prev.courseBlockEditUiState.copy(courseBlock = it)
                        )
                    }
                }
            )

            launch {
                //If there are submissions, do not allow the user to change the groups
                _uiState.whenSubscribed {
                    activeDb.courseAssignmentSubmissionDao.checkNoSubmissionsMadeFlow(
                        _uiState.value.entity?.assignment?.caUid ?: 0
                    ).collect { noSubmissions ->
                        if(!noSubmissions && _uiState.value.groupSetEnabled) {
                            _uiState.update { prev ->
                                prev.copy(groupSetEnabled = false)
                            }
                        }
                    }
                }
            }

            if(savedStateHandle[KEY_INIT_STATE] == null) {
                savedStateHandle[KEY_INIT_STATE] = withContext(Dispatchers.Default) {
                    json.encodeToString(_uiState.value)
                }
            }

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }
        }

    }

    fun onAssignmentChanged(entity: ClazzAssignment?) {
        _uiState.update { prev ->
            prev.copy(
                entity = prev.entity?.shallowCopyWithEntity {
                    assignment = entity
                },
                submissionRequiredError = if(prev.submissionRequiredError != null &&
                    entity?.caRequireFileSubmission == false && !entity.caRequireTextSubmission
                ) {
                    prev.submissionRequiredError
                }else {
                    null
                }
            )
        }
    }

    fun onCourseBlockChanged(entity: CourseBlock?) {
        _uiState.update { prev ->
            val prevBlock = prev.courseBlockEditUiState.courseBlock

            prev.copy(
                entity = entity?.asCourseBlockWithEntity()?.also {
                    it.assignment = prev.entity?.assignment
                },
                courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                    courseBlock = entity,
                    caMaxPointsError = updateErrorMessageOnChange(
                        prevFieldValue = prev.courseBlockEditUiState.courseBlock?.cbMaxPoints,
                        currentFieldValue = entity?.cbMaxPoints,
                        currentErrorMessage = prev.courseBlockEditUiState.caMaxPointsError
                    ),
                    caDeadlineError = if(
                        prev.courseBlockEditUiState.caDeadlineError != null &&
                            prevBlock?.cbDeadlineDate == entity?.cbDeadlineDate &&
                            prevBlock?.cbHideUntilDate == entity?.cbHideUntilDate
                    ) {
                        prev.courseBlockEditUiState.caDeadlineError
                    }else {
                        null
                    },
                    caGracePeriodError = if(
                        prev.courseBlockEditUiState.caGracePeriodError != null &&
                            prevBlock?.cbDeadlineDate == entity?.cbDeadlineDate &&
                            prevBlock?.cbGracePeriodDate == entity?.cbGracePeriodDate
                    ) {
                        prev.courseBlockEditUiState.caGracePeriodError
                    }else {
                        null
                    }
                )
            )
        }
    }

    private fun ClazzAssignmentEditUiState.hasErrors() : Boolean {
        return submissionRequiredError != null ||
            courseBlockEditUiState.caMaxPointsError != null ||
            courseBlockEditUiState.caDeadlineError != null ||
            courseBlockEditUiState.caGracePeriodError != null ||
            reviewerCountError != null
    }


    private suspend fun checkNoSubmissionsMade() : Boolean{
        return activeDb.courseAssignmentSubmissionDao.checkNoSubmissionsMadeAsync(
            _uiState.value.entity?.assignment?.caUid ?: 0L
        )
    }

    fun onClickSave() {
        if(!_uiState.value.fieldsEnabled)
            return

        _uiState.update { prev ->
            prev.copy(fieldsEnabled = false)
        }

        viewModelScope.launch {
            val initState = savedStateHandle[KEY_INIT_STATE]?.let { initStateJson ->
                withContext(Dispatchers.Default) {
                    json.decodeFromString(ClazzAssignmentEditUiState.serializer(), initStateJson)
                }
            } ?: return@launch

            val assignment = _uiState.value.entity?.assignment ?: return@launch
            val courseBlock = _uiState.value.courseBlockEditUiState.courseBlock ?: return@launch

            if(!assignment.caRequireFileSubmission && !assignment.caRequireTextSubmission) {
                _uiState.update { prev ->
                    prev.copy(submissionRequiredError = systemImpl.getString(MessageID.text_file_submission_error))
                }
            }

            if(courseBlock.cbMaxPoints <= 0) {
                _uiState.update { prev ->
                    prev.copy(
                        courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                            caMaxPointsError = systemImpl.getString(MessageID.field_required_prompt)
                        )
                    )
                }
            }

            if(courseBlock.cbDeadlineDate <= courseBlock.cbHideUntilDate) {
                _uiState.update { prev ->
                    prev.copy(
                        courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                            caDeadlineError = systemImpl.getString(MessageID.end_is_before_start_error)
                        )
                    )
                }
            }

            if(courseBlock.cbGracePeriodDate < courseBlock.cbDeadlineDate) {
                _uiState.update { prev ->
                    prev.copy(
                        courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                            caGracePeriodError = systemImpl.getString(MessageID.after_deadline_date_error)
                        )
                    )
                }
            }

            var errorSnack: String? = null

            if(initState.entity?.assignment?.caGroupUid != assignment.caGroupUid &&
                !checkNoSubmissionsMade()
            ) {
                errorSnack = systemImpl.getString(MessageID.error) + "Cannot change groups after submissions made"
            }

            if(initState.entity?.assignment?.caMarkingType != assignment.caMarkingType &&
                errorSnack == null && !checkNoSubmissionsMade()
            ) {
                errorSnack = systemImpl.getString(MessageID.error) + "Cannot change marking type after submissions made"
            }

            if(assignment.caMarkingType == ClazzAssignment.MARKED_BY_PEERS &&
                assignment.caPeerReviewerCount < 1
            ) {
                _uiState.update { prev ->
                    prev.copy(reviewerCountError = systemImpl.getString(MessageID.score_greater_than_zero))
                }
            }

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }

            if(errorSnack != null) {
                snackDisaptcher.showSnackBar(Snack(errorSnack))
                return@launch
            }

            if(_uiState.value.hasErrors()) {
                return@launch
            }

            if(assignment.caMarkingType == ClazzAssignment.MARKED_BY_PEERS &&
                initState.entity?.assignment?.caPeerReviewerCount != assignment.caPeerReviewerCount
            ) {
                val newAllocations = peerReviewAllocationUseCaseFactory(
                    activeDb, systemImpl
                ).invoke(
                    existingAllocations = _uiState.value.entity?.assignmentPeerAllocations ?: emptyList(),
                    groupUid = assignment.caGroupUid,
                    clazzUid = assignment.caClazzUid,
                    assignmentUid = assignment.caUid,
                    numReviewsPerSubmission = assignment.caPeerReviewerCount,
                    allocateRemaining = true
                )

                _uiState.update { prev ->
                    prev.copy(
                        entity = prev.entity?.shallowCopyWithEntity {
                            assignmentPeerAllocations = newAllocations
                        }
                    )
                }
            }

            finishWithResult(_uiState.value.entity)
        }
    }
}
