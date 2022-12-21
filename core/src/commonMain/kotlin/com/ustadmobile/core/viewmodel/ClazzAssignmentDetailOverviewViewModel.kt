package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission

data class ClazzAssignmentDetailOverviewUiState(

    val assignment: ClazzAssignmentWithCourseBlock? = null,

    val caDetailDescriptionVisible: Boolean = false,

    val assignmentStatus: Int = CourseAssignmentSubmission.MARKED,

    val pointsVisible: Boolean = false,

    val assignmentMark: CourseAssignmentMark? = null,

    val timeZone: String = "UTC",

    var publicMode: Boolean = false,

    val fieldsEnabled: Boolean = true,

) {

    val caDescriptionVisible: Boolean
        get() = !assignment?.caDescription.isNullOrBlank()

    val cbDeadlineDateVisible: Boolean
        get() = assignment?.block?.cbDeadlineDate.isDateSet()

    val submissionStatusVisible: Boolean
        get() = assignmentStatus != CourseAssignmentSubmission.NOT_SUBMITTED

    val penaltyVisible: Boolean
        get() = pointsVisible && assignmentMark?.camPenalty != 0

}