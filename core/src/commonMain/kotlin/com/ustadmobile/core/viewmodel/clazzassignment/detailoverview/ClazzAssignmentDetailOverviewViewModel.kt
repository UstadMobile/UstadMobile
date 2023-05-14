package com.ustadmobile.core.viewmodel.clazzassignment.detailoverview

import com.ustadmobile.core.db.dao.CourseAssignmentMarkDaoCommon
import com.ustadmobile.core.domain.assignment.submitassignment.SubmitAssignmentUseCase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.UNSET_DISTANT_FUTURE
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.UstadAssignmentSubmissionHeaderUiState
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import kotlin.jvm.JvmInline


/**
 * @param latestSubmissionAttachments List of submissions made by the active user (or their group).
 * null if not yet loaded
 */
data class ClazzAssignmentDetailOverviewUiState(

    val assignment: ClazzAssignment? = null,

    val courseBlock: CourseBlock? = null,

    internal val submitterUid: Long = 0,

    val submissionMark: AverageCourseAssignmentMark? = null,

    val latestSubmission: CourseAssignmentSubmission? = null,

    val latestSubmissionAttachments: List<CourseAssignmentSubmissionAttachment>? = null,

    val markList: List<CourseAssignmentMarkWithPersonMarker> = emptyList(),

    val publicCommentList: List<CommentsWithPerson> = emptyList(),

    val privateCommentList: List<CommentsWithPerson> = emptyList(),

    val showPrivateComments: Boolean = true,

    val submissionStatus: Int? = null,

    val fieldsEnabled: Boolean = true,

    val selectedChipId: Int = CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES,

    val gradeFilterChips: List<MessageIdOption2> = listOf(
        MessageIdOption2(MessageID.most_recent, CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES),
        MessageIdOption2(MessageID.all, CourseAssignmentMarkDaoCommon.ARG_FILTER_ALL_SCORES)
    ),

    val submissionHeaderUiState: UstadAssignmentSubmissionHeaderUiState =
        UstadAssignmentSubmissionHeaderUiState(),

    val hasFilesToSubmit: Boolean = false,

    val unassignedError: String? = null,

    val submissionTextFieldVisible: Boolean = false,

    val addFileVisible: Boolean = false,

    val submissionError: String? = null,

) {

    val caDescriptionVisible: Boolean
        get() = !assignment?.caDescription.isNullOrBlank()

    val cbDeadlineDateVisible: Boolean
        get() = courseBlock?.cbDeadlineDate.isDateSet()

    val submitSubmissionButtonVisible: Boolean
        get() = activeUserCanSubmit

    val unassignedErrorVisible: Boolean
        get() = !unassignedError.isNullOrBlank()

    val showClassComments: Boolean
        get() = assignment?.caClassCommentEnabled == true

    val activeUserIsSubmitter: Boolean
        get() = submitterUid != 0L

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

            if(assignment?.caSubmissionPolicy != ClazzAssignment.SUBMISSION_POLICY_MULTIPLE_ALLOWED &&
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
    di, savedStateHandle, ClazzAssignmentDetailOverviewView.VIEW_NAME
){

    private val _uiState = MutableStateFlow(ClazzAssignmentDetailOverviewUiState())

    val uiState: Flow<ClazzAssignmentDetailOverviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepo.clazzAssignmentDao.findAssignmentCourseBlockAndSubmitterUidAsFlow(
                        assignmentUid = entityUidArg,
                        accountPersonUid = accountManager.activeAccount.personUid,
                    ).collect { assignmentData ->
                        _uiState.update { prev ->
                            prev.copy(
                                assignment = assignmentData?.clazzAssignment,
                                courseBlock = assignmentData?.courseBlock,
                                submitterUid = assignmentData?.submitterUid ?: 0
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
                                accountManager.activeSession?.person?.personUid ?: 0L,
                                assignmentUid = entityUidArg,
                            )
                        },
                        makeDefault = {
                            CourseAssignmentSubmission().apply {
                                casAssignmentUid = entityUidArg
                                casSubmitterPersonUid = accountManager.activeSession?.person?.personUid ?: 0L
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
                                accountPersonUid = accountManager.activeSession?.person?.personUid ?: 0L,
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
        }
    }

    fun onChangeSubmissionText(text: String) {
        _uiState.update { prev ->
            prev.copy(
                latestSubmission = prev.latestSubmission?.shallowCopy {
                    casText = text
                    casTimestamp = 0
                }
            )
        }
    }

    fun onClickSubmit() {
        if(loadingState == LoadingUiState.INDETERMINATE)
            return

        val submission = _uiState.value.latestSubmission ?: return

        loadingState = LoadingUiState.INDETERMINATE

        viewModelScope.launch {
            try {
                submitAssignmentUseCase(
                    db = activeDb,
                    systemImpl = systemImpl,
                    assignmentUid = entityUidArg,
                    accountPersonUid = accountManager.activeSession?.person?.personUid ?: 0L,
                    submission = submission
                )
                _uiState.takeIf { it.value.submissionError != null }?.update { prev ->
                    prev.copy(submissionError = null)
                }
                snackDispatcher.showSnackBar(Snack(systemImpl.getString(MessageID.submitted)))
            }catch(e: Exception) {
                _uiState.update { prev ->
                    prev.copy(submissionError = e.message)
                }
            }finally {
                loadingState = LoadingUiState.NOT_LOADING
            }
        }
    }

    companion object {

        const val STATE_LATEST_SUBMISSION = "latestSubmission"

        const val STATE_LATEST_SUBMISSION_ATTACHMENTS = "latestSubmissionAttachments"

    }
}
