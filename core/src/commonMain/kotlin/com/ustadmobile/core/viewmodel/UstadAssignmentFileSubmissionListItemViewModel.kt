package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionWithAttachment

data class UstadAssignmentFileSubmissionListItemUiState(

    val showFiles: Boolean = true,

    val fileSubmission: CourseAssignmentSubmissionWithAttachment = CourseAssignmentSubmissionWithAttachment(),

    val assignment: ClazzAssignmentWithCourseBlock = ClazzAssignmentWithCourseBlock(),

    val fileNameText: String = "",

) {

    val isSubmitted: Boolean
        get() = fileSubmission.casTimestamp != 0L
}