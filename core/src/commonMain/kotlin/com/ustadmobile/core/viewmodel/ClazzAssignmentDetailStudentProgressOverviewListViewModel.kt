package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import kotlin.jvm.JvmInline

data class ClazzAssignmentDetailStudentProgressListOverviewUiState(

    val progressSummary: AssignmentProgressSummary? = null,

    val assignmentSubmitterList: List<AssignmentSubmitterSummary> = emptyList()

)

val AssignmentSubmitterSummary.listItemUiState
    get() = AssignmentSubmitterSummaryUiState(this)

@JvmInline
value class AssignmentSubmitterSummaryUiState(
    val person: AssignmentSubmitterSummary,
) {

    val fileSubmissionStatusIconVisible: Boolean
        get() = person.fileSubmissionStatus != CourseAssignmentSubmission.NOT_SUBMITTED

    val fileSubmissionStatusTextVisible: Boolean
        get() = person.fileSubmissionStatus != 0

    val latestPrivateCommentVisible: Boolean
        get() = !person.latestPrivateComment.isNullOrBlank()

}