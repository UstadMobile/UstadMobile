package com.ustadmobile.libuicompose.view.clazzassignment.detailoverview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.lib.db.entities.*
import java.util.*
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName

@Composable
@Preview
fun ClazzAssignmentDetailOverviewScreenPreview(){

    ClazzAssignmentDetailOverviewScreen(
        uiState = ClazzAssignmentDetailOverviewUiState(
            assignment = ClazzAssignment().apply {
                caRequireTextSubmission = true
            },
            courseBlock = CourseBlock().apply {
                cbDeadlineDate = 1685509200000L
                cbDescription = "Complete your assignment or <b>else</b>"
            },
            submitterUid = 42L,
            addFileVisible = true,
            submissionTextFieldVisible = true,
            latestSubmissionAttachments = listOf(
                CourseAssignmentSubmissionAttachment().apply {
                    casaUid = 1L
                    casaFileName = "File.pdf"
                },
            ),
            latestSubmission = CourseAssignmentSubmission().apply {
                casText = ""
            },
            markList = listOf(
                CourseAssignmentMarkAndMarkerName(
                    courseAssignmentMark = CourseAssignmentMark().apply {
                        camMarkerSubmitterUid = 2
                        camMarkerComment = "Comment"
                        camMark = 8.1f
                        camPenalty = 0.9f
                        camMaxMark = 10f
                        camLct = 0
                    },
                    markerFirstNames = "John",
                    markerLastName = "Smith",
                )
            ),
//            courseComments = {
//                ListPagingSource(listOf(
//                    CommentsAndName().apply {
//                        comment = Comments().apply {
//                            commentsUid = 1
//                            commentsText = "This is a very difficult assignment."
//                        }
//                        firstNames = "Bob"
//                        lastName = "Dylan"
//                    }
//                ))
//            },
//            privateComments = {
//                ListPagingSource(
//                    listOf(
//                        CommentsAndName().apply {
//                            comment = Comments().apply {
//                                commentsUid = 2
//                                commentsText = "Can I please have extension? My rabbit ate my homework."
//                            }
//                            firstNames = "Bob"
//                            lastName = "Dylan"
//                        }
//                    ),
//                )
//            },
        )
    )
}