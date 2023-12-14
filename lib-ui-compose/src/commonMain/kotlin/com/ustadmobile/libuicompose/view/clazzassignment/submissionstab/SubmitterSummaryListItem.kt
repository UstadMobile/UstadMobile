package com.ustadmobile.libuicompose.view.clazzassignment.submissionstab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.listItemUiState
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewConstants.ASSIGNMENT_STATUS_MAP
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SubmitterSummaryListItem (
    submitterSummary: AssignmentSubmitterSummary?,
    onClick: (AssignmentSubmitterSummary) -> Unit = {},
){

    val personUiState = submitterSummary?.listItemUiState

    ListItem(
        modifier = Modifier.clickable {
            submitterSummary?.also {  onClick(it) }
        },
        leadingContent = {
            Icon(
                Icons.Filled.Person,
                contentDescription = "",
                modifier = Modifier.size(40.dp)
            )
        },
        headlineContent = { Text(submitterSummary?.name ?: "") },
        supportingContent = {
            if (personUiState?.latestPrivateCommentVisible == true){
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        Icons.Filled.Comment,
                        contentDescription = "",
                        modifier = Modifier.size(12.dp)
                    )
                    Text(submitterSummary.latestPrivateComment ?: "")
                }
            }
        },
        trailingContent = {
            Row{
                if (personUiState?.fileSubmissionStatusIconVisible == true){
                    Icon(
                        ASSIGNMENT_STATUS_MAP[
                            submitterSummary.fileSubmissionStatus] ?: Icons.Default.Done,
                        contentDescription = "",
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    stringResource(
                    SubmissionConstants.STATUS_MAP[submitterSummary?.fileSubmissionStatus]
                        ?: MR.strings.not_submitted_cap
                )
                )

            }

        }
    )
}
