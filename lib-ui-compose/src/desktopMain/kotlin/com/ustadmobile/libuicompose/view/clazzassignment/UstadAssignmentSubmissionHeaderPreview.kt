package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.*
import com.ustadmobile.core.viewmodel.clazzassignment.UstadAssignmentSubmissionHeaderUiState
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.AverageCourseAssignmentMark

@Composable
@Preview
private fun UstadAssignmentSubmissionHeaderPreview() {
    UstadAssignmentSubmissionHeader(
        uiState = UstadAssignmentSubmissionHeaderUiState(
            assignmentStatus = CourseAssignmentSubmission.NOT_SUBMITTED,
            assignmentMark = AverageCourseAssignmentMark().apply {
                averagePenalty = 3
            },
        )
    )
}