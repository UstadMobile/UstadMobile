package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.AverageCourseAssignmentMark
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission

data class UstadAssignmentFileSubmissionHeaderUiState(

    val assignment: ClazzAssignmentWithCourseBlock? = null,

    val assignmentMark: AverageCourseAssignmentMark? = null,

    val assignmentStatus: Int? = null,

    val showPoints: Boolean = true,

) {

    val submissionStatusIconVisible: Boolean
        get() = assignmentStatus != CourseAssignmentSubmission.NOT_SUBMITTED


    val latePenaltyVisible: Boolean
        get() = showPoints &&
                assignmentMark?.averagePenalty != 0


}