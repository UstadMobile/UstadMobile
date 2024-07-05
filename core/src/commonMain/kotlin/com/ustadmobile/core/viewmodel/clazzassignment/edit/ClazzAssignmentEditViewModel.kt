package com.ustadmobile.core.viewmodel.clazzassignment.edit

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.peerreviewallocation.UpdatePeerReviewAllocationUseCase
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.appstate.SnackBarDispatcher
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditUiState
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.edit.ClazzAssignmentEditUiState.Companion.ASSIGNMENT_COMPLETION_CRITERIAS
import com.ustadmobile.core.viewmodel.clazzassignment.peerreviewerallocationedit.PeerReviewerAllocationEditViewModel
import com.ustadmobile.core.viewmodel.courseblock.CourseBlockViewModelConstants
import com.ustadmobile.core.viewmodel.courseblock.edit.CourseBlockEditViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import org.kodein.di.DI
import org.kodein.di.instance

@kotlinx.serialization.Serializable
data class ClazzAssignmentEditUiState(

    val fieldsEnabled: Boolean = false,

    val markingTypeEnabled: Boolean = true,

    val groupSetEnabled: Boolean = true,

    val reviewerCountError: String? = null,

    val courseTerminology: CourseTerminology? = null,

    val submissionRequiredError: String? = null,

    val sizeLimitError: String? = null,

    val courseBlockEditUiState: CourseBlockEditUiState = CourseBlockEditUiState(
        completionCriteriaOptions = ASSIGNMENT_COMPLETION_CRITERIAS
    ),

    val groupSubmissionOn: Boolean = false,

    val groupSetError: String? = null,
) {

    val entity: CourseBlockAndEditEntities?
        get() = courseBlockEditUiState.block

    val peerMarkingVisible: Boolean
        get() = entity?.assignment?.caMarkingType == ClazzAssignment.MARKED_BY_PEERS

    val textSubmissionVisible: Boolean
        get() = entity?.assignment?.caRequireTextSubmission == true

    val fileSubmissionVisible: Boolean
        get()  = entity?.assignment?.caRequireFileSubmission == true

    //Set fields enabled on both the assignment ui state and the courseblockedit state
    fun copyWithFieldsEnabledSet(
        fieldsEnabled: Boolean
    ) = copy(
        fieldsEnabled = fieldsEnabled,
        courseBlockEditUiState = courseBlockEditUiState.copy(
            fieldsEnabled = fieldsEnabled
        )
    )

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
): UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState = MutableStateFlow(
        ClazzAssignmentEditUiState(
            courseBlockEditUiState = CourseBlockEditUiState(
                completionCriteriaOptions = ASSIGNMENT_COMPLETION_CRITERIAS,
                timeZone = TimeZone.currentSystemDefault().id,
                maxPointsRequired = true,
            )
        )
    )

    val uiState: Flow<ClazzAssignmentEditUiState> = _uiState.asStateFlow()

    private val snackDisaptcher: SnackBarDispatcher by instance()

    private val clazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                userAccountIconVisible = false,
                loadingState = LoadingUiState.INDETERMINATE,
                title = createEditTitle(MR.strings.new_assignment, MR.strings.edit_assignment),
            )
        }

        viewModelScope.launch {
            val courseTerminology = savedStateHandle.getJson(
                ARG_TERMINOLOGY, CourseTerminology.serializer()
            )

            loadEntity(
                serializer = CourseBlockAndEditEntities.serializer(),
                onLoadFromDb = { null  }, //does not load from db, always via json
                makeDefault = {
                    val assignmentUid = activeDb.doorPrimaryKeyManager.nextId(ClazzAssignment.TABLE_ID)
                    CourseBlockAndEditEntities(
                        courseBlock = CourseBlock().apply {
                            cbUid = activeDb.doorPrimaryKeyManager.nextId(CourseBlock.TABLE_ID)
                            cbType = CourseBlock.BLOCK_ASSIGNMENT_TYPE
                            cbEntityUid = assignmentUid
                            cbCompletionCriteria = ClazzAssignment.COMPLETION_CRITERIA_GRADED
                            cbMaxPoints = 10f
                            cbMinPoints = 0f
                        },
                        assignment = ClazzAssignment().apply {
                            caUid = assignmentUid
                            caClazzUid = clazzUid
                        }
                    )
                },
                uiUpdate = {
                    val groupSubmissionOn = savedStateHandle[STATE_KEY_GROUP_SUBMISSION_ON]
                    _uiState.update { prev ->
                        prev.copy(
                            courseTerminology = courseTerminology,
                            courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                                block = it
                            ),
                            groupSubmissionOn = groupSubmissionOn?.toBoolean() ?:
                                it?.assignment?.caGroupUid?.let { it != 0L } ?: false
                        )
                    }
                }
            )

            if(savedStateHandle[KEY_INIT_STATE] == null) {
                savedStateHandle[KEY_INIT_STATE] = withContext(Dispatchers.Default) {
                    json.encodeToString(_uiState.value)
                }
            }


            _uiState.update { prev ->
                prev.copyWithFieldsEnabledSet(fieldsEnabled = true)
            }

            _appUiState.update { prev ->
                prev.copy(
                    loadingState = LoadingUiState.NOT_LOADING,
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = systemImpl.getString(MR.strings.done),
                        onClick = this@ClazzAssignmentEditViewModel::onClickSave,
                    )
                )
            }

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

            launch {
                navResultReturner.filteredResultFlowForKey(CourseBlockEditViewModel.KEY_HTML_DESCRIPTION).collect { result ->
                    val descriptionHtml = result.result as? String ?: return@collect
                    onCourseBlockChanged(
                        _uiState.value.entity?.courseBlock?.copy(
                            cbDescription = descriptionHtml
                        )
                    )
                }
            }

            launch {
                navResultReturner.filteredResultFlowForKey(RESULT_KEY_GROUPSET).collect { result ->
                    val groupSet = result.result as? CourseGroupSet ?: return@collect

                    val newState = _uiState.updateAndGet { prev ->
                        prev.copy(
                            courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                                block = prev.entity?.copy(
                                    assignment = prev.entity?.assignment?.shallowCopy {
                                        caGroupUid = groupSet.cgsUid
                                    },
                                    assignmentCourseGroupSetName = groupSet.takeIf { it.cgsUid != 0L }?.cgsName
                                )
                            ),
                            groupSetError = null,
                        )
                    }

                    scheduleEntityCommitToSavedState(
                        entity = newState.entity,
                        serializer = CourseBlockAndEditEntities.serializer(),
                        commitDelay = 200,
                    )
                }
            }

            launch {
                navResultReturner.filteredResultFlowForKey(RESULT_KEY_PEER_REVIEW_ALLOCATIONS).collect { result ->
                    @Suppress("UNCHECKED_CAST")
                    val allocations = result.result as? List<PeerReviewerAllocation> ?: return@collect
                    val newState = _uiState.updateAndGet { prev ->
                        prev.copy(
                            courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                                block = prev.courseBlockEditUiState.block?.copy(
                                    assignmentPeerAllocations = allocations,
                                )
                            ),
                        )
                    }

                    scheduleEntityCommitToSavedState(
                        entity = newState.entity,
                        serializer = CourseBlockAndEditEntities.serializer(),
                        commitDelay = 200,
                    )
                }
            }
        }
    }

    fun onClickEditDescription() {
        navigateToEditHtml(
            currentValue = _uiState.value.entity?.courseBlock?.cbDescription,
            resultKey = CourseBlockEditViewModel.KEY_HTML_DESCRIPTION,
            title = systemImpl.getString(MR.strings.description),
        )
    }

    fun onGroupSubmissionOnChanged(groupSubmissionOn: Boolean) {
        savedStateHandle[STATE_KEY_GROUP_SUBMISSION_ON] = groupSubmissionOn.toString()
        _uiState.update { prev ->
            prev.copy(
                groupSubmissionOn = groupSubmissionOn,
                groupSetError = if(prev.groupSetError != null && groupSubmissionOn) {
                    prev.groupSetError
                }else {
                    null
                }
            )
        }
    }

    fun onAssignmentChanged(assignment: ClazzAssignment?) {
        val newState = _uiState.updateAndGet { prev ->
            prev.copy(
                courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                    block = prev.courseBlockEditUiState.block?.copy(
                        assignment = assignment,
                    )
                ),
                sizeLimitError = if(prev.sizeLimitError != null && assignment?.caSizeLimit == prev.entity?.assignment?.caSizeLimit){
                    prev.sizeLimitError
                } else {
                    null
                },
                submissionRequiredError = if(prev.submissionRequiredError != null &&
                    assignment?.caRequireFileSubmission == false && !assignment.caRequireTextSubmission
                ) {
                    prev.submissionRequiredError
                }else {
                    null
                },
                reviewerCountError = if(prev.reviewerCountError != null &&
                    assignment?.caPeerReviewerCount == prev.entity?.assignment?.caPeerReviewerCount &&
                    assignment?.caMarkingType == ClazzAssignment.MARKED_BY_PEERS
                ) {
                    prev.reviewerCountError
                }else {
                    null
                },
            )
        }

        scheduleEntityCommitToSavedState(
            entity = newState.entity,
            serializer = CourseBlockAndEditEntities.serializer(),
            commitDelay = 200,
        )
    }

    fun onCourseBlockChanged(courseBlock: CourseBlock?) {
        if(courseBlock == null) {
            Napier.w("Change courseblock shoudl not really be null")
            return
        }

        val newState = _uiState.updateAndGet { prev ->
            val prevBlock = prev.entity?.courseBlock

            prev.copy(
                courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                    block = prev.entity?.copy(courseBlock = courseBlock),
                    caMaxPointsError = updateErrorMessageOnChange(
                        prevFieldValue = prev.courseBlockEditUiState.block?.courseBlock?.cbMaxPoints,
                        currentFieldValue = courseBlock.cbMaxPoints,
                        currentErrorMessage = prev.courseBlockEditUiState.caMaxPointsError
                    ),
                    caDeadlineError = if(
                        prev.courseBlockEditUiState.caDeadlineError != null &&
                            prevBlock?.cbDeadlineDate == courseBlock.cbDeadlineDate &&
                            prevBlock.cbHideUntilDate == courseBlock.cbHideUntilDate
                    ) {
                        prev.courseBlockEditUiState.caDeadlineError
                    }else {
                        null
                    },
                    caGracePeriodError = if(
                        prev.courseBlockEditUiState.caGracePeriodError != null &&
                            prevBlock?.cbDeadlineDate == courseBlock.cbDeadlineDate &&
                            prevBlock.cbGracePeriodDate == courseBlock.cbGracePeriodDate
                    ) {
                        prev.courseBlockEditUiState.caGracePeriodError
                    }else {
                        null
                    },
                    caTitleError = updateErrorMessageOnChange(
                        prev.courseBlockEditUiState.block?.courseBlock?.cbTitle,
                        courseBlock.cbTitle,
                        prev.courseBlockEditUiState.caTitleError
                    )
                )
            )
        }

        scheduleEntityCommitToSavedState(
            entity = newState.entity,
            serializer = CourseBlockAndEditEntities.serializer(),
            commitDelay = 200,
        )
    }

    fun onPictureChanged(pictureUri: String?) {
        val newState = _uiState.updateAndGet { prev ->
            prev.copy(
                courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                    block = prev.courseBlockEditUiState.block?.copy(
                        courseBlockPicture = prev.courseBlockEditUiState.block.courseBlockPicture?.copy(
                            cbpPictureUri = pictureUri
                        )
                    )
                )
            )
        }

        scheduleEntityCommitToSavedState(
            entity = newState.entity,
            serializer = CourseBlockAndEditEntities.serializer(),
            commitDelay = 200,
        )
    }

    private fun ClazzAssignmentEditUiState.hasErrors() : Boolean {
        return submissionRequiredError != null ||
            courseBlockEditUiState.caMaxPointsError != null ||
            courseBlockEditUiState.caDeadlineError != null ||
            courseBlockEditUiState.caGracePeriodError != null ||
            reviewerCountError != null ||
            sizeLimitError != null ||
            groupSetError != null ||
            courseBlockEditUiState.hasErrors
    }


    private suspend fun checkNoSubmissionsMade() : Boolean{
        return activeDb.courseAssignmentSubmissionDao.checkNoSubmissionsMadeAsync(
            _uiState.value.entity?.assignment?.caUid ?: 0L
        )
    }

    fun onClickSubmissionType() {
        navigateForResult(
            nextViewName = CourseGroupSetListViewModel.DEST_NAME,
            key = RESULT_KEY_GROUPSET,
            currentValue = null,
            serializer = String.serializer(),
            args = mapOf(
                ARG_CLAZZUID to (_uiState.value.entity?.assignment?.caClazzUid ?: 0).toString()
            ),
        )
    }

    fun onClickAssignReviewers() {
        val assignmentVal = _uiState.value.entity?.assignment ?: return

        navigateForResult(
            nextViewName = PeerReviewerAllocationEditViewModel.DEST_NAME,
            key = RESULT_KEY_PEER_REVIEW_ALLOCATIONS,
            currentValue = null,
            serializer = ListSerializer(PeerReviewerAllocation.serializer()),
            args = mapOf(
                PeerReviewerAllocationEditViewModel.ARG_ALLOCATIONS to json.encodeToString(
                    serializer = ListSerializer(PeerReviewerAllocation.serializer()),
                    value = _uiState.value.entity?.assignmentPeerAllocations ?: emptyList()
                ),
                PeerReviewerAllocationEditViewModel.ARG_GROUP_SET_UID to assignmentVal.caGroupUid.toString(),
                ARG_CLAZZUID to assignmentVal.caClazzUid.toString(),
                PeerReviewerAllocationEditViewModel.ARG_NUM_REVIEWERS_PER_SUBMITTER to
                        assignmentVal.caPeerReviewerCount.toString(),
                UstadView.ARG_CLAZZ_ASSIGNMENT_UID to assignmentVal.caUid.toString(),
            )
        )
    }

    fun onClickSave() {
        if(!_uiState.value.fieldsEnabled)
            return

        launchWithLoadingIndicator(
            onSetFieldsEnabled = { _uiState.update { prev -> prev.copyWithFieldsEnabledSet(fieldsEnabled = true) } }
        ) {
            val initState = savedStateHandle[KEY_INIT_STATE]?.let { initStateJson ->
                withContext(Dispatchers.Default) {
                    json.decodeFromString(ClazzAssignmentEditUiState.serializer(), initStateJson)
                }
            } ?: return@launchWithLoadingIndicator

            val assignment = _uiState.value.entity?.assignment ?: return@launchWithLoadingIndicator
            val courseBlock = _uiState.value.entity?.courseBlock ?: return@launchWithLoadingIndicator

            if(!assignment.caRequireFileSubmission && !assignment.caRequireTextSubmission) {
                _uiState.update { prev ->
                    prev.copy(submissionRequiredError = systemImpl.getString(MR.strings.text_file_submission_error))
                }
            }

            if(courseBlock.cbMaxPoints.let { it == null || it <= 0 } ) {
                _uiState.update { prev ->
                    prev.copy(
                        courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                            caMaxPointsError = systemImpl.getString(MR.strings.field_required_prompt)
                        )
                    )
                }
            }

            if(courseBlock.cbDeadlineDate <= courseBlock.cbHideUntilDate) {
                _uiState.update { prev ->
                    prev.copy(
                        courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                            caDeadlineError = systemImpl.getString(MR.strings.end_is_before_start_error)
                        )
                    )
                }
            }

            if(courseBlock.cbGracePeriodDate < courseBlock.cbDeadlineDate) {
                _uiState.update { prev ->
                    prev.copy(
                        courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                            caGracePeriodError = systemImpl.getString(MR.strings.after_deadline_date_error)
                        )
                    )
                }
            }

            if(assignment.caSizeLimit !in ATTACHMENT_LIMIT_MIN..ATTACHMENT_LIMIT_MAX){
                _uiState.update {   prev ->
                    prev.copy(
                        sizeLimitError = systemImpl.formatString(MR.strings.size_limit_error,
                            ATTACHMENT_LIMIT_MIN.toString(), ATTACHMENT_LIMIT_MAX.toString())
                    )
                }
            }

            if(courseBlock.cbTitle.isNullOrBlank()) {
                _uiState.update { prev ->
                    prev.copy(
                        courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                            caTitleError = systemImpl.getString(MR.strings.required)
                        )
                    )
                }
            }

            if(_uiState.value.groupSubmissionOn && assignment.caGroupUid == 0L) {
                _uiState.update { prev ->
                    prev.copy(
                        groupSetError = systemImpl.getString(MR.strings.field_required_prompt)
                    )
                }
            }

            var errorSnack: String? = null

            if(initState.entity?.assignment?.caGroupUid != assignment.caGroupUid &&
                !checkNoSubmissionsMade()
            ) {
                errorSnack = systemImpl.getString(MR.strings.error) + "Cannot change groups after submissions made"
            }

            if(initState.entity?.assignment?.caMarkingType != assignment.caMarkingType &&
                errorSnack == null && !checkNoSubmissionsMade()
            ) {
                errorSnack = systemImpl.getString(MR.strings.error) + "Cannot change marking type after submissions made"
            }

            if(assignment.caMarkingType == ClazzAssignment.MARKED_BY_PEERS &&
                assignment.caPeerReviewerCount < 1
            ) {
                _uiState.update { prev ->
                    prev.copy(reviewerCountError = systemImpl.getString(MR.strings.score_greater_than_zero))
                }
            }

            val errorSnackVal = errorSnack
            if(_uiState.value.hasErrors() || errorSnackVal != null) {
                errorSnackVal?.also {
                    snackDisaptcher.showSnackBar(Snack(it))
                }

                return@launchWithLoadingIndicator
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
                        courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                            block = prev.courseBlockEditUiState.block?.copy(
                                assignmentPeerAllocations = newAllocations,
                            )
                        ),
                    )
                }
            }

            //If group submission has been switched off, set group uid to zero now
            _uiState.takeIf {
                it.value.entity?.assignment?.caGroupUid != 0L && !it.value.groupSubmissionOn
            }?.update { prev ->
                prev.copy(
                    courseBlockEditUiState = prev.courseBlockEditUiState.copy(
                        block = prev.courseBlockEditUiState.block?.copy(
                            assignment = prev.courseBlockEditUiState.block.assignment?.shallowCopy {
                                caGroupUid = 0L
                            },
                            assignmentCourseGroupSetName = null,
                        )
                    ),
                )
            }

            finishWithResult(_uiState.value.entity)
        }
    }

    companion object {

        const val RESULT_KEY_GROUPSET = "groupSet"

        const val RESULT_KEY_PEER_REVIEW_ALLOCATIONS = "peerAllocationsResult"

        const val ARG_TERMINOLOGY = "terminology"

        const val DEST_NAME = "CourseAssignmentEdit"
        const val ATTACHMENT_LIMIT_MIN = 5
        const val ATTACHMENT_LIMIT_MAX = 100

        const val STATE_KEY_GROUP_SUBMISSION_ON = "groupSubmissionOn"
    }
}
