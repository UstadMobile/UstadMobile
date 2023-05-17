package com.ustadmobile.core.viewmodel.clazzassignment

import com.ustadmobile.lib.db.entities.AverageCourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.CourseBlock

data class UstadAssignmentSubmissionHeaderUiState(

    val block: CourseBlock? = null,

    val assignmentMark: AverageCourseAssignmentMark? = null,

    val assignmentStatus: Int? = null,
) {

    val showPoints: Boolean
        get() = assignmentMark != null

    val submissionStatusIconVisible: Boolean
        get() = assignmentStatus != CourseAssignmentSubmission.NOT_SUBMITTED

    val latePenaltyVisible: Boolean
        get() = showPoints && assignmentMark != null && assignmentMark.averagePenalty != 0


}