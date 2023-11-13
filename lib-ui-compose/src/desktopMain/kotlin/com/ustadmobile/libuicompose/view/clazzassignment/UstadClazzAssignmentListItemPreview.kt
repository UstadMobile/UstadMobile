package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.entities.*
import java.util.*

@Composable
@Preview
private fun UstadClazzAssignmentListItemPreview() {

    val block = CourseBlockWithCompleteEntity().apply {
        cbDescription = "Description"
        cbDeadlineDate = 1672707505000
        cbMaxPoints = 100
        cbIndentLevel = 1
        assignment = ClazzAssignmentWithMetrics().apply {
            caTitle = "Module"
//To be fixed as part of the assignment screens
//            mark = CourseAssignmentMark().apply {
//                camPenalty = 20
//                camMark = 20F
//            }
            progressSummary = AssignmentProgressSummary().apply {
                activeUserHasViewLearnerRecordsPermission = false
            }
            fileSubmissionStatus = CourseAssignmentSubmission.NOT_SUBMITTED
        }
    }

    UstadClazzAssignmentListItem(
        courseBlock = block
    )
}