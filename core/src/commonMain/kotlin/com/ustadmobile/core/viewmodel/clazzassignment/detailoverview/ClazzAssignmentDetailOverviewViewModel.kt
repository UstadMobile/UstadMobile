package com.ustadmobile.core.viewmodel.clazzassignment.detailoverview

import com.ustadmobile.core.db.dao.CourseAssignmentMarkDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import kotlin.jvm.JvmInline


/**
 * @param submittedSubmissionList List of submissions made by the active user (or their group).
 * null if not yet loaded
 */
data class ClazzAssignmentDetailOverviewUiState(

    val assignment: ClazzAssignment? = null,

    val courseBlock: CourseBlock? = null,

    internal val submitterUid: Long = 0,

    val submissionMark: AverageCourseAssignmentMark? = null,

    val submittedSubmissionList: List<CourseAssignmentSubmissionWithAttachment>? = null,

    val draftSubmissionList: List<CourseAssignmentSubmissionWithAttachment> =
        emptyList(),

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

    val addTextVisible: Boolean = false,

    val addFileVisible: Boolean = false

) {

    val caDescriptionVisible: Boolean
        get() = !assignment?.caDescription.isNullOrBlank()

    val cbDeadlineDateVisible: Boolean
        get() = courseBlock?.cbDeadlineDate.isDateSet()

    val submitSubmissionButtonVisible: Boolean
        get() = draftSubmissionList.isNotEmpty()

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
                (submittedSubmissionList == null || submittedSubmissionList.isNotEmpty())
            ) {
                return false
            }

            return true
        }


    val addTextSubmissionVisible: Boolean
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

                launch {
                    activeRepo.courseAssignmentSubmissionDao.getAllSubmissionsForUser(
                        accountPersonUid = accountManager.activeSession?.person?.personUid ?: 0L,
                        assignmentUid = entityUidArg,
                    ).collect {
                        _uiState.update { prev ->
                            prev.copy(
                                submittedSubmissionList = it
                            )
                        }
                    }
                }


            }
        }

    }

}
