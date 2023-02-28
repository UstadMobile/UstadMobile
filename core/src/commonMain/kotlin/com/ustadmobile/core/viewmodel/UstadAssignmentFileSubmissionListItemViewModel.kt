package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionWithAttachment


data class UstadAssignmentFileSubmissionListItemUiState(

    val showFiles: Boolean = true,

    val fileSubmission: CourseAssignmentSubmissionWithAttachment = CourseAssignmentSubmissionWithAttachment(),

    val assignment: ClazzAssignmentWithCourseBlock = ClazzAssignmentWithCourseBlock(),

    val dateTimeMode: Int = 0,

    val isSubmitted: Boolean = false,

    val fileNameText: String = "",

//    val selectablePagedListAdapter: SubmissionAdapter = "",

)