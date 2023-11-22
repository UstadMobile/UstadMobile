package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import com.ustadmobile.core.viewmodel.clazzassignment.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.CourseAssignmentMark

@Composable
@Preview
private fun UstadCourseAssignmentMarkListItemPreview() {
    UstadCourseAssignmentMarkListItem(
        uiState = UstadCourseAssignmentMarkListItemUiState(
            mark = CourseAssignmentMarkAndMarkerName(
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
        )
    )
}