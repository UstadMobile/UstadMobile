package com.ustadmobile.libuicompose.view.clazzassignment.detailoverview

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.CourseBlock

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
            editableSubmission = CourseAssignmentSubmission().apply {
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
        )
    )
}