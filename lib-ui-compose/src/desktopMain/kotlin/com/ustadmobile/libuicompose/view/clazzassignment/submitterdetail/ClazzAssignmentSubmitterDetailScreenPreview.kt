package com.ustadmobile.libuicompose.view.clazzassignment.submitterdetail

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailUiState
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.composites.CourseBlockAndAssignment
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.flowOf

@Composable
@Preview
fun ClazzAssignmentSubmitterDetailScreenPreview(){

    val uiStateVal = ClazzAssignmentSubmitterDetailUiState(
        block = CourseBlockAndAssignment(
            courseBlock = CourseBlock().apply {
                cbMaxPoints = 50f
            }
        ),
        draftMark = CourseAssignmentMark().apply {

        },
        submissionList = emptyList(),
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
    )


    ClazzAssignmentSubmitterDetailScreen(uiStateVal, flowOf(""))
}