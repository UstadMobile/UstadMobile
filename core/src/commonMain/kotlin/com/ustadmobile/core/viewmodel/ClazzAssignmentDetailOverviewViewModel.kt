package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.lib.db.entities.*


data class ClazzAssignmentDetailOverviewUiState(


    val submittedCourseAssignmentSubmission: List<CourseAssignmentSubmissionWithAttachment> =
        emptyList(),

    val markList: List<CourseAssignmentMarkWithPersonMarker> = emptyList(),

    val addedCourseAssignmentSubmission: List<CourseAssignmentSubmissionWithAttachment> = emptyList(),

    val gradeFilterChips: List<ListFilterIdOption> = emptyList(),

    var clazzAssignmentClazzComments: List<CommentsWithPerson> = emptyList(),

    var clazzAssignmentPrivateComments: List<CommentsWithPerson> = emptyList(),

    var showPrivateComments: Boolean = true,

    var showSubmission: Boolean = true,

    var addTextSubmissionVisible: Boolean = true,

    var addFileSubmissionVisible: Boolean = true,

    var submissionMark: AverageCourseAssignmentMark? = null,

    var submissionStatus: Int? = null,

    var unassignedError: String? = null

)