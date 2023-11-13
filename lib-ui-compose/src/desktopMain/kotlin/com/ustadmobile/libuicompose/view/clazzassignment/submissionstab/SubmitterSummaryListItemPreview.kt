package com.ustadmobile.libuicompose.view.clazzassignment.submissionstab

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary

@Composable
@Preview
fun SubmitterSummaryListItemPreview (){


    SubmitterSummaryListItem(
        submitterSummary = AssignmentSubmitterSummary().apply {
            name = "Submitter"
        }
    )
}
