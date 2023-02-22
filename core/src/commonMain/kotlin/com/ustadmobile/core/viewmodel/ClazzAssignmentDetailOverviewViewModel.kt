package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.ext.isDateSet
import com.ustadmobile.lib.db.entities.*


data class ClazzAssignmentDetailOverviewUiState(


    val clazzAssignment: ClazzAssignmentWithCourseBlock? = null,

    val submittedCourseAssignmentSubmission: List<CourseAssignmentSubmissionWithAttachment> =
        emptyList(),

    val markList: List<CourseAssignmentMarkWithPersonMarker> = emptyList(),

    val addedCourseAssignmentSubmission: List<CourseAssignmentSubmissionWithAttachment> = emptyList(),

    val gradeFilterChips: List<ListFilterIdOption> = emptyList(),

    val clazzAssignmentClazzComments: List<CommentsWithPerson> = emptyList(),

    val clazzAssignmentPrivateComments: List<CommentsWithPerson> = emptyList(),

    val showPrivateComments: Boolean = true,

    val showSubmission: Boolean = true,

    val addTextSubmissionVisible: Boolean = true,

    val addFileSubmissionVisible: Boolean = true,

    val submissionMark: AverageCourseAssignmentMark? = null,

    val submissionStatus: Int? = null,

    val unassignedError: String? = null

) {

    val caDescriptionVisible: Boolean
        get() = !clazzAssignment?.caDescription.isNullOrBlank()

    val cbDeadlineDateVisible: Boolean
        get() = clazzAssignment?.block?.cbDeadlineDate.isDateSet()

}