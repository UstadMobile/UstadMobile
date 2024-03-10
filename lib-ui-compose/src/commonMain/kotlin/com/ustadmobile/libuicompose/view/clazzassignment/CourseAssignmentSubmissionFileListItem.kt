package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.composites.CourseAssignmentSubmissionFileAndTransferJob

@Composable
fun CourseAssignmentSubmissionFileListItem(
    fileAndTransferJob: CourseAssignmentSubmissionFileAndTransferJob
) {
    ListItem(
        leadingContent = {
            Icon(Icons.Default.Article, contentDescription = null)
        },
        headlineContent = {
            Text(fileAndTransferJob.submissionFile?.casaFileName ?: "")
        }
    )
}