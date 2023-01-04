package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.CourseBlockWithCompleteEntity

data class ClazzAssignmentUiState(

    val assignment: ClazzAssignmentWithMetrics = ClazzAssignmentWithMetrics(),

    val block: CourseBlockWithCompleteEntity = CourseBlockWithCompleteEntity(),

    val timeZone: String = "UTC"

) {

    val cbDescriptionVisible: Boolean
        get() = !block.cbDescription.isNullOrBlank()

    val cbDeadlineDateVisible: Boolean
        get() = block.cbDeadlineDate.isDateSet()

    val assignmentMarkVisible: Boolean
        get() = assignment.mark != null

    val submissionStatusIconVisible: Boolean
        get() = assignment.progressSummary?.hasMetricsPermission != null &&
                assignment.progressSummary?.hasMetricsPermission == false ||
                assignment.fileSubmissionStatus != 0

    val submissionStatusVisible: Boolean
        get() = assignment.progressSummary?.hasMetricsPermission != null &&
                assignment.progressSummary?.hasMetricsPermission == false

    val progressTextVisible: Boolean
        get() = assignment.progressSummary?.hasMetricsPermission != null &&
                assignment.progressSummary?.hasMetricsPermission == true


}