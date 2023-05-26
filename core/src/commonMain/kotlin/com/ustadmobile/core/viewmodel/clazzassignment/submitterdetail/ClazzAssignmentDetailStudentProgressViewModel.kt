package com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail

import com.ustadmobile.core.db.dao.CourseAssignmentMarkDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzassignment.UstadAssignmentSubmissionHeaderUiState
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.entities.*

/**
 * @param submittedMarks Marks for this submitter that have been submitted (e.g. in the database)
 * @param draftMark mark for the given submitter that is being edited/drafted on screen (e.g. for
 * the currently active user to submit)
 */
data class ClazzAssignmentDetailStudentProgressUiState(

    val submitMarkError: String? = null,

    val submitterName: String = "",

    val courseBlock: CourseBlock? = null,

    val gradeFilterChips: List<ListFilterIdOption> = emptyList(),

    val submissionList: List<CourseAssignmentSubmission> = emptyList(),

    val submissionAttachments: List<CourseAssignmentSubmissionAttachment> = emptyList(),

    val submittedMarks: List<CourseAssignmentMarkWithPersonMarker> = emptyList(),

    val draftMark: CourseAssignmentMark? = null,

    val submissionScore: AverageCourseAssignmentMark? = null,

    val markNextStudentVisible: Boolean =  true,

    val markStudentVisible: Boolean = true,

    val assignment: ClazzAssignmentWithCourseBlock? = null,

    val fieldsEnabled: Boolean = true,

    val submissionHeaderUiState: UstadAssignmentSubmissionHeaderUiState =
        UstadAssignmentSubmissionHeaderUiState(),

    val selectedChipId: Int = CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES,

    val gradeFilterOptions: List<MessageIdOption2> = listOf(
        MessageIdOption2(MessageID.most_recent, CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES),
        MessageIdOption2(MessageID.all, CourseAssignmentMarkDaoCommon.ARG_FILTER_ALL_SCORES)
    ),

    val privateCommentsList: List<CommentsAndName> = emptyList(),

    val newPrivateCommentText: String = "",

    val activeUserPersonUid: Long = 0,

) {

    val submissionStatus: Int
        get() {
            return when {
                submittedMarks.isNotEmpty() -> CourseAssignmentSubmission.MARKED
                submissionList.isNotEmpty() -> CourseAssignmentSubmission.SUBMITTED
                else -> CourseAssignmentSubmission.NOT_SUBMITTED
            }
        }

    private val latestUniqueMarksByMarker: List<CourseAssignmentMarkWithPersonMarker>
        get() = submittedMarks.filter { markWithMarker ->
            markWithMarker.camLct == submittedMarks.filter {
                it.camMarkerSubmitterUid == markWithMarker.camMarkerSubmitterUid
            }.maxOf { it.camLct }
        }

    val averageScore: Float
        get() {
            return latestUniqueMarksByMarker.let {
                it.sumOf { it.camMark.toDouble() }.toFloat() / it.size
            }
        }

    fun markListItemUiState(
        mark: CourseAssignmentMarkWithPersonMarker
    ): UstadCourseAssignmentMarkListItemUiState {
        return UstadCourseAssignmentMarkListItemUiState(
            mark
        )
    }

}

class ClazzAssignmentDetailStudentProgressViewModel {

    companion object {

        const val DEST_NAME = "CourseAssignmentSubmitter"

    }

}