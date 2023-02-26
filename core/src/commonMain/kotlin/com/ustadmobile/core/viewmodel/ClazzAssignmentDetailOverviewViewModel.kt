package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.db.dao.CourseAssignmentMarkDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.MessageIdOption2
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

    val unassignedError: String? = null,

    val fieldsEnabled: Boolean = true,

    val selectedChipId: Int = CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES,

    val filterOptions: List<MessageIdOption2> = listOf(
        MessageIdOption2(MessageID.most_recent, CourseAssignmentMarkDaoCommon.ARG_FILTER_RECENT_SCORES),
        MessageIdOption2(MessageID.all, CourseAssignmentMarkDaoCommon.ARG_FILTER_ALL_SCORES)
    ),

) {

    val caDescriptionVisible: Boolean
        get() = !clazzAssignment?.caDescription.isNullOrBlank()

    val cbDeadlineDateVisible: Boolean
        get() = clazzAssignment?.block?.cbDeadlineDate.isDateSet()

}