package com.ustadmobile.core.viewmodel.clazzassignment.detailoverview

import com.ustadmobile.core.db.dao.CourseAssignmentMarkDaoCommon
import com.ustadmobile.core.domain.assignment.submitassignment.SubmitAssignmentUseCase
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.core.util.ext.textLength
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.UstadAssignmentSubmissionHeaderUiState
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import app.cash.paging.PagingSource
import com.ustadmobile.core.viewmodel.clazzassignment.latestUniqueMarksByMarker
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import kotlin.jvm.JvmInline
import kotlin.math.roundToInt


/**
 * @param latestSubmissionAttachments List of submissions made by the active user (or their group).
 * null if not yet loaded
 *
 * @param newPrivateCommentText the text in the textfield for a new private comment
 */
data class ClazzAssignmentDetailOverviewUiState(

    val assignment: ClazzAssignment? = null,

    val courseBlock: CourseBlock? = null,

    /**
     * The submitter uid of the active user - see CourseAssignmentSubmission.casSubmitterUid
     */
    internal val submitterUid: Long = 0,

    val latestSubmission: CourseAssignmentSubmission? = null,

    var submissionTooLong: Boolean = false,

    val latestSubmissionAttachments: List<CourseAssignmentSubmissionAttachment>? = null,

    val markList: List<CourseAssignmentMarkAndMarkerName> = emptyList(),

    val courseComments: () -> PagingSource<Int, CommentsAndName> = { EmptyPagingSource() },

    val privateComments: () -> PagingSource<Int, CommentsAndName> = { EmptyPagingSource() },

    val fieldsEnabled: Boolean = true,

    val selectedChipId: Int = CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES,

    val gradeFilterChips: List<MessageIdOption2> = listOf(
        MessageIdOption2(MR.strings.most_recent, CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES),
        MessageIdOption2(MR.strings.all, CourseAssignmentMarkDaoCommon.ARG_FILTER_ALL_SCORES)
    ),

    val submissionHeaderUiState: UstadAssignmentSubmissionHeaderUiState =
        UstadAssignmentSubmissionHeaderUiState(),

    val unassignedError: String? = null,

    val addFileVisible: Boolean = false,

    val submissionError: String? = null,

    val newPrivateCommentText: String = "",

    val newCourseCommentText: String = "",

    val activeUserPersonUid: Long = 0,
) {

    val caDescriptionVisible: Boolean
        get() = !courseBlock?.cbDescription.isNullOrBlank()

    val cbDeadlineDateVisible: Boolean
        get() = courseBlock?.cbDeadlineDate.isDateSet()

    val submitSubmissionButtonVisible: Boolean
        get() = activeUserCanSubmit

    val unassignedErrorVisible: Boolean
        get() = !unassignedError.isNullOrBlank()

    val showClassComments: Boolean
        get() = assignment?.caClassCommentEnabled == true

    val activeUserIsSubmitter: Boolean
        get() = submitterUid > 0L

    val isWithinDeadlineOrGracePeriod: Boolean
        get() {
            val timeNow = systemTimeInMillis()
            if(timeNow < (courseBlock?.cbDeadlineDate ?: UNSET_DISTANT_FUTURE)) {
                return true
            }
            if(courseBlock?.cbGracePeriodDate.isDateSet() &&
                timeNow <= (courseBlock?.cbGracePeriodDate ?: 0L)
            ) {
                return true
            }

            return false
        }

    val activeUserCanSubmit: Boolean
        get() {
            if(!activeUserIsSubmitter)
                return false

            if(!isWithinDeadlineOrGracePeriod)
                return false

            if(assignment?.caSubmissionPolicy == ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE &&
                (latestSubmission?.casTimestamp ?: 1) > 0
            ) {
                return false
            }

            return true
        }


    val canEditSubmissionText: Boolean
        get() = activeUserCanSubmit && assignment?.caRequireTextSubmission == true

    val addFileSubmissionVisible: Boolean
        get() = activeUserCanSubmit && assignment?.caRequireFileSubmission == true

    val submissionStatus: Int?
        get() {
            return if(submissionMark != null) {
                CourseAssignmentSubmission.MARKED
            }else if((latestSubmission?.casTimestamp ?: 0) > 0) {
                CourseAssignmentSubmission.SUBMITTED
            }else {
                null
            }
        }

    val privateCommentSectionVisible: Boolean
        get() = activeUserIsSubmitter && unassignedError == null

    val submitPrivateCommentVisible: Boolean
        get() = privateCommentSectionVisible && assignment?.caPrivateCommentsEnabled == true

    val currentSubmissionLength: Int? by lazy {
        latestSubmission?.textLength(assignment?.caTextLimitType ?: ClazzAssignment.TEXT_WORD_LIMIT)
    }

    val pointsVisible: Boolean
        get() = submissionMark != null

    val latePenaltyVisible: Boolean
        get() = submissionMark.let { it  != null && it.averagePenalty != 0 }

    val submissionTextFieldVisible: Boolean
        get() = assignment?.caRequireTextSubmission == true && activeUserIsSubmitter

    private val latestUniqueMarksByMarker: List<CourseAssignmentMarkAndMarkerName>
        get() = markList.latestUniqueMarksByMarker()

    val visibleMarks: List<CourseAssignmentMarkAndMarkerName>
        get() = if(selectedChipId == CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES) {
            latestUniqueMarksByMarker
        }else {
            markList
        }

    val submissionMark: AverageCourseAssignmentMark?
        get() {
            val latestUnique = latestUniqueMarksByMarker
            if(latestUnique.isEmpty())
                return null

            return AverageCourseAssignmentMark().apply {
                averageScore = latestUnique.sumOf {
                    it.courseAssignmentMark?.camMark?.toDouble() ?: 0.toDouble()
                }.toFloat() / latestUnique.size
                averagePenalty = (latestUnique.sumOf {
                    it.courseAssignmentMark?.camPenalty?.toDouble() ?: 0.toDouble()
                } / latestUnique.size).roundToInt()
            }
        }
}

val CourseAssignmentMarkWithPersonMarker.listItemUiState
    get() = CourseAssignmentMarkWithPersonMarkerUiState(this)

@JvmInline
value class CourseAssignmentMarkWithPersonMarkerUiState(
    val mark: CourseAssignmentMarkWithPersonMarker,
) {

    val markerGroupNameVisible: Boolean
        get() = mark.isGroup && mark.camMarkerSubmitterUid != 0L

}

class ClazzAssignmentDetailOverviewViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
    private val submitAssignmentUseCase: SubmitAssignmentUseCase = SubmitAssignmentUseCase(),
) : DetailViewModel<ClazzAssignment>(
    di, savedStateHandle, DEST_NAME
){

    private val _uiState = MutableStateFlow(ClazzAssignmentDetailOverviewUiState())

    val uiState: Flow<ClazzAssignmentDetailOverviewUiState> = _uiState.asStateFlow()

    private var lastPrivateCommentsPagingSource: PagingSource<Int, CommentsAndName>? = null

    private val privateCommentsPagingSourceFactory: () -> PagingSource<Int, CommentsAndName> = {
        activeRepo.commentsDao.findPrivateCommentsForUserByAssignmentUid(
            accountPersonUid = activeUserPersonUid,
            assignmentUid = entityUidArg,
        ).also {
            lastPrivateCommentsPagingSource = it
        }
    }

    private var lastCourseCommentsPagingSourceFactory: PagingSource<Int, CommentsAndName>? = null

    private val courseCommentsPagingSourceFactory: () -> PagingSource<Int, CommentsAndName> = {
        activeRepo.commentsDao.findCourseCommentsByAssignmentUid(
            assignmentUid = entityUidArg
        ).also {
            lastCourseCommentsPagingSourceFactory = it
        }
    }

    private var savedSubmissionJob: Job? = null

    init {
        _uiState.update { prev ->
            prev.copy(activeUserPersonUid = activeUserPersonUid)
        }
        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepo.clazzAssignmentDao.findAssignmentCourseBlockAndSubmitterUidAsFlow(
                        assignmentUid = entityUidArg,
                        accountPersonUid = accountManager.currentAccount.personUid,
                    ).collect { assignmentData ->
                        _uiState.update { prev ->
                            val isEnrolledButNotInGroup = assignmentData?.submitterUid ==
                                CourseAssignmentSubmission.SUBMITTER_ENROLLED_BUT_NOT_IN_GROUP
                            prev.copy(
                                assignment = assignmentData?.clazzAssignment,
                                courseBlock = assignmentData?.courseBlock,
                                submitterUid = assignmentData?.submitterUid ?: 0,
                                unassignedError = if(isEnrolledButNotInGroup) {
                                    systemImpl.getString(MR.strings.unassigned_error)
                                }else {
                                    null
                                }
                            )
                        }

                        _appUiState.update { prev ->
                            prev.copy(
                                title = assignmentData?.courseBlock?.cbTitle ?: ""
                            )
                        }
                    }
                }

                launch {
                    activeRepo.courseAssignmentMarkDao.getAllMarksForUserAsFlow(
                        accountPersonUid = activeUserPersonUid,
                        assignmentUid = entityUidArg
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(
                                markList = it
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            awaitAll(
                async {
                    loadEntity(
                        serializer = CourseAssignmentSubmission.serializer(),
                        loadFromStateKeys = listOf(STATE_LATEST_SUBMISSION),
                        onLoadFromDb = { db ->
                            db.courseAssignmentSubmissionDao.getLatestSubmissionForUserAsync(
                                accountPersonUid = accountManager.currentUserSession.person.personUid,
                                assignmentUid = entityUidArg,
                            )
                        },
                        makeDefault = {
                            CourseAssignmentSubmission().apply {
                                casAssignmentUid = entityUidArg
                                casSubmitterPersonUid = accountManager.currentUserSession.person.personUid
                            }
                        },
                        uiUpdate = {
                            _uiState.update { prev ->
                                prev.copy(
                                    latestSubmission = it
                                )
                            }
                        }
                    )
                },
                async {
                    loadEntity(
                        serializer = ListSerializer(CourseAssignmentSubmissionAttachment.serializer()),
                        loadFromStateKeys = listOf(STATE_LATEST_SUBMISSION_ATTACHMENTS),
                        onLoadFromDb = { db ->
                            db.courseAssignmentSubmissionAttachmentDao.getLatestSubmissionAttachmentsForUserAsync(
                                accountPersonUid = accountManager.currentUserSession.person.personUid,
                                assignmentUid = entityUidArg
                            )
                        },
                        makeDefault = {
                            emptyList()
                        },
                        uiUpdate = {
                            _uiState.update { prev ->
                                prev.copy(
                                    latestSubmissionAttachments = it
                                )
                            }
                        }
                    )
                }
            )

            launch {
                navResultReturner.filteredResultFlowForKey(KEY_SUBMISSION_HTML).collect {
                    val newSubmissionHtml = it.result as? String ?: return@collect
                    onChangeSubmissionText(newSubmissionHtml)
                }
            }

        }

        _uiState.update { prev ->
            prev.copy(
                privateComments = privateCommentsPagingSourceFactory,
                courseComments = courseCommentsPagingSourceFactory,
            )
        }
    }

    fun onClickMarksFilterChip(option: MessageIdOption2) {
        _uiState.update { prev ->
            prev.copy(
               selectedChipId =  option.value,
            )
        }
    }

    /**
     * Used on mobile to bring user to a new screen to edit submission
     */
    fun onClickEditSubmissionText() {
        navigateToEditHtml(
            currentValue = _uiState.value.latestSubmission?.casText ?: "",
            resultKey = KEY_SUBMISSION_HTML
        )
    }

    fun onChangeSubmissionText(text: String) {
        val submissionToSave = _uiState.updateAndGet { prev ->
            if(prev.activeUserCanSubmit) {
                prev.copy(
                    latestSubmission = prev.latestSubmission?.shallowCopy {
                        casText = text
                        casTimestamp = 0
                    }
                )
            }else {
                prev
            }
        }.latestSubmission

        savedSubmissionJob?.cancel()
        savedSubmissionJob = viewModelScope.launch {
            delay(200)
            if(submissionToSave != null) {
                savedStateHandle.setJson(
                    key = STATE_LATEST_SUBMISSION,
                    serializer = CourseAssignmentSubmission.serializer(),
                    value = submissionToSave
                )
            }
        }

    }

    fun onChangePrivateCommentText(text: String) {
        _uiState.update { prev ->
            prev.copy(
                newPrivateCommentText = text
            )
        }
    }

    fun onClickSubmitPrivateComment() {
        val submitterUid = _uiState.value.submitterUid
        if(submitterUid <= 0)
            //invalid - this should never happen because private comment field would not be visible
            return


        if(loadingState == LoadingUiState.INDETERMINATE)
            return

        loadingState = LoadingUiState.INDETERMINATE

        viewModelScope.launch {
            try {
                activeRepo.commentsDao.insertAsync(Comments().apply {
                    commentSubmitterUid = submitterUid
                    commentsPersonUid = activeUserPersonUid
                    commentsEntityUid = entityUidArg
                    commentsText = _uiState.value.newPrivateCommentText
                    commentsDateTimeAdded = systemTimeInMillis()
                })
                _uiState.update { prev ->
                    prev.copy(newPrivateCommentText = "")
                }
            }finally {
                loadingState = LoadingUiState.NOT_LOADING
            }
        }
    }

    fun onChangeCourseCommentText(text: String) {
        _uiState.update { prev ->
            prev.copy(
                newCourseCommentText = text
            )
        }
    }

    fun onClickSubmitCourseComment() {
        if(loadingState == LoadingUiState.INDETERMINATE)
            return

        loadingState = LoadingUiState.INDETERMINATE
        viewModelScope.launch {
            try {
                activeRepo.commentsDao.insertAsync(Comments().apply {
                    commentSubmitterUid = 0
                    commentsPersonUid = activeUserPersonUid
                    commentsEntityUid = entityUidArg
                    commentsText = _uiState.value.newCourseCommentText
                    commentsDateTimeAdded = systemTimeInMillis()
                })
                _uiState.update { prev ->
                    prev.copy(newCourseCommentText = "")
                }
            }finally {
                loadingState = LoadingUiState.NOT_LOADING
            }
        }
    }

    fun onClickSubmit() {
        if(!_uiState.value.fieldsEnabled)
            return

        val submission = _uiState.value.latestSubmission ?: return

        _uiState.update { prev -> prev.copy(fieldsEnabled = false) }
        loadingState = LoadingUiState.INDETERMINATE

        viewModelScope.launch {
            try {
                val submissionResult = submitAssignmentUseCase(
                    repo = activeRepo,
                    submitterUid = _uiState.value.submitterUid,
                    assignmentUid = entityUidArg,
                    accountPersonUid = accountManager.currentUserSession.person.personUid,
                    submission = submission
                )

                val submissionToSave = _uiState.updateAndGet { prev ->
                    prev.copy(
                        latestSubmission = submissionResult.submission ?: prev.latestSubmission,
                        submissionError = null
                    )
                }.latestSubmission

                if(submissionToSave != null) {
                    savedStateHandle.setJson(
                        key = STATE_LATEST_SUBMISSION,
                        serializer = CourseAssignmentSubmission.serializer(),
                        value = submissionToSave,
                    )
                }


                snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.submitted_key)))
            }catch(e: Exception) {
                Napier.e("Exception submitting assignment: $e", e)
                _uiState.update { prev ->
                    prev.copy(submissionError = e.message)
                }
            }finally {
                _uiState.update { prev -> prev.copy(fieldsEnabled = true) }
                loadingState = LoadingUiState.NOT_LOADING
            }
        }
    }

    companion object {

        const val STATE_LATEST_SUBMISSION = "latestSubmission"

        const val STATE_LATEST_SUBMISSION_ATTACHMENTS = "latestSubmissionAttachments"

        const val KEY_SUBMISSION_HTML = "submissionHtml"

        const val DEST_NAME = "CourseAssignmentDetailOverviewView"

    }
}
