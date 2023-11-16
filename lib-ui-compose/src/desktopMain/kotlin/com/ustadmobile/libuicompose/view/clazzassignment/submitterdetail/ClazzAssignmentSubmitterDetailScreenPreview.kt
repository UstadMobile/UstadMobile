package com.ustadmobile.libuicompose.view.clazzassignment.submitterdetail

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailUiState
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.*

@Composable
@Preview
fun ClazzAssignmentSubmitterDetailScreenPreview(){

    val uiStateVal = ClazzAssignmentSubmitterDetailUiState(
        courseBlock = CourseBlock().apply {
            cbMaxPoints = 50
        },
        draftMark = CourseAssignmentMark().apply {

        },
        submissionList = listOf(
            CourseAssignmentSubmission().apply {
                casUid = 1
                casTimestamp = 1677744388299
                casText = "I can haz cheezburger"
                casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
            },
        ),
        marks = listOf(
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
//        privateCommentsList = {
//            ListPagingSource(listOf(
//                CommentsAndName(
//                    comment = Comments().apply {
//                        commentsText = "I like this activity. Shall we discuss this in our next meeting?"
//                    },
//                    firstNames = "Bob",
//                    lastName = "Dylan"
//                )
//            ))
//        },
    )


    ClazzAssignmentDetailStudentProgressScreen(uiStateVal)
}