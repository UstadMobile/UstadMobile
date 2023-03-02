package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.AverageCourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.CourseBlock

data class UstadAssignmentFileSubmissionHeaderUiState(

    val block: CourseBlock = CourseBlock(),

    val assignmentMark: AverageCourseAssignmentMark = AverageCourseAssignmentMark(),

    val assignmentStatus: Int? = null,

    val showPoints: Boolean = true,

    ) {

    val submissionStatusIconVisible: Boolean
        get() = assignmentStatus != CourseAssignmentSubmission.NOT_SUBMITTED


    val latePenaltyVisible: Boolean
        get() = showPoints &&
                assignmentMark.averagePenalty != 0


}