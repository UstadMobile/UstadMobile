package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionWithAttachment

data class UstadAssignmentFileSubmissionListItemUiState(

    val submission: CourseAssignmentSubmissionWithAttachment = CourseAssignmentSubmissionWithAttachment(),

    val assignment: ClazzAssignmentWithCourseBlock = ClazzAssignmentWithCourseBlock(),

    val fileNameText: String = "",

) {

    val isSubmitted: Boolean
        get() = submission.casTimestamp != 0L
}