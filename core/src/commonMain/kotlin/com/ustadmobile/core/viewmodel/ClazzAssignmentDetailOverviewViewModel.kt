package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.dao.CourseAssignmentMarkDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.lib.db.entities.*
import kotlin.jvm.JvmInline


data class ClazzAssignmentDetailOverviewUiState(


    val clazzAssignment: ClazzAssignmentWithCourseBlock? = null,

    val draftSubmissionList: List<CourseAssignmentSubmissionWithAttachment> =
        emptyList(),

    val submittedSubmissionList: List<CourseAssignmentSubmissionWithAttachment> =
        emptyList(),

    val markList: List<CourseAssignmentMarkWithPersonMarker> = emptyList(),

    val addedCourseAssignmentSubmission: List<CourseAssignmentSubmissionWithAttachment> = emptyList(),

    val publicCommentList: List<CommentsWithPerson> = emptyList(),

    val privateCommentList: List<CommentsWithPerson> = emptyList(),

    val showPrivateComments: Boolean = true,

    val showSubmission: Boolean = true,

    val addTextSubmissionVisible: Boolean = true,

    val addFileSubmissionVisible: Boolean = true,

    val submissionMark: AverageCourseAssignmentMark? = null,

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

    val deadlinePassed: Boolean = false,

    val unassignedError: String? = null

) {

    val caDescriptionVisible: Boolean
        get() = !clazzAssignment?.caDescription.isNullOrBlank()

    val cbDeadlineDateVisible: Boolean
        get() = clazzAssignment?.block?.cbDeadlineDate.isDateSet()

    val submitSubmissionButtonVisible: Boolean
        get() = hasFilesToSubmit && !deadlinePassed && unassignedError.isNullOrBlank()

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