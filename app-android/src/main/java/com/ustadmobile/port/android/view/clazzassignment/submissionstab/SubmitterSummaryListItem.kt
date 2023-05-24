package com.ustadmobile.port.android.view.clazzassignment.submissionstab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.listItemUiState
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewFragment


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SubmitterSummaryListItem (
    submitterSummary: AssignmentSubmitterSummary,
    onClick: (AssignmentSubmitterSummary) -> Unit = {},
){

    val personUiState = submitterSummary.listItemUiState

    ListItem(
        modifier = Modifier.clickable {
            onClick(submitterSummary)
        },
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_person_black_24dp),
                contentDescription = "",
                modifier = Modifier.size(40.dp)
            )
        },
        text = { Text(submitterSummary.name ?: "") },
        secondaryText = {
            if (personUiState.latestPrivateCommentVisible){
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_comment_24),
                        contentDescription = "",
                        modifier = Modifier.size(12.dp)
                    )
                    Text(submitterSummary.latestPrivateComment ?: "")
                }
            }
        },
        trailing = {
            Row{
                if (personUiState.fileSubmissionStatusIconVisible){
                    Icon(
                        painter = painterResource(
                            ClazzAssignmentDetailOverviewFragment.ASSIGNMENT_STATUS_MAP[
                                submitterSummary.fileSubmissionStatus] ?: R.drawable.ic_done_white_24dp
                        ),
                        contentDescription = "",
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (personUiState.fileSubmissionStatusTextVisible){
                    Text(
                        messageIdResource(
                        SubmissionConstants.STATUS_MAP[submitterSummary.fileSubmissionStatus]
                            ?: MessageID.not_submitted_cap
                    )
                    )
                }
            }

        }
    )
}
