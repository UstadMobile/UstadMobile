package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.CourseBlockWithCompleteEntity
import kotlin.jvm.JvmInline

val ClazzAssignmentWithMetrics.listItemUiState
    get() = ClazzAssignmentWithMetricsUiState(this)

@JvmInline
value class ClazzAssignmentWithMetricsUiState(
    val assignment: ClazzAssignmentWithMetrics,
) {

    val assignmentMarkVisible: Boolean
        get() = assignment.mark != null

    val submissionStatusIconVisible: Boolean
        get() = assignment.progressSummary?.activeUserHasViewLearnerRecordsPermission != null &&
                assignment.progressSummary?.activeUserHasViewLearnerRecordsPermission == false ||
                assignment.fileSubmissionStatus != CourseAssignmentSubmission.NOT_SUBMITTED

    val submissionStatusVisible: Boolean
        get() = assignment.progressSummary?.activeUserHasViewLearnerRecordsPermission != null &&
                assignment.progressSummary?.activeUserHasViewLearnerRecordsPermission == false

    val progressTextVisible: Boolean
        get() = assignment.progressSummary?.activeUserHasViewLearnerRecordsPermission != null &&
                assignment.progressSummary?.activeUserHasViewLearnerRecordsPermission == true

}



val CourseBlockWithCompleteEntity.listItemUiState
    get() = CourseBlockWithCompleteEntityUiState(this)

@JvmInline
value class CourseBlockWithCompleteEntityUiState(
    val block: CourseBlockWithCompleteEntity,
) {

    val cbDescriptionVisible: Boolean
        get() = !block.cbDescription.isNullOrBlank()

    val cbDeadlineDateVisible: Boolean
        get() = block.cbDeadlineDate.isDateSet()

}