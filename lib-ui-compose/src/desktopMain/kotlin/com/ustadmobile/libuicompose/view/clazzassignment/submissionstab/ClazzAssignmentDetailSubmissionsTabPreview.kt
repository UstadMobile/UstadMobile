package com.ustadmobile.libuicompose.view.clazzassignment.submissionstab

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabUiState
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
@Composable
@Preview
fun ClazzAssignmentDetailStudentProgressListOverviewScreenPreview() {
    val uiStateVal = ClazzAssignmentDetailSubmissionsTabUiState(
        progressSummary = AssignmentProgressSummary().apply {
            totalStudents = 10
            submittedStudents = 2
            markedStudents = 3
        },
//        assignmentSubmitterList = {
//            ListPagingSource(listOf(
//                AssignmentSubmitterSummary().apply {
//                    submitterUid = 1
//                    name = "Bob Dylan"
//                    latestPrivateComment = "Here is private comment"
//                    fileSubmissionStatus = CourseAssignmentSubmission.MARKED
//                },
//                AssignmentSubmitterSummary().apply {
//                    submitterUid = 2
//                    name = "Morris Rogers"
//                    latestPrivateComment = "Here is private comment"
//                    fileSubmissionStatus = CourseAssignmentSubmission.SUBMITTED
//                }
//            ))
//        },
    )

    ClazzAssignmentDetailSubmissionsTabScreen(uiStateVal)
}