package com.ustadmobile.port.android.view.clazzassignment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Chat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CourseAssignmentSubmissionListItem(
    submission: CourseAssignmentSubmission,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val submissionPlainText = remember(submission.casText) {
        submission.casText?.htmlToPlainText()
    }

    val submitterDateTime = rememberFormattedDateTime(
        timeInMillis = submission.casTimestamp,
        timeZoneId = TimeZone.currentSystemDefault().id,
    )

    ListItem(
        modifier = modifier.clickable {
            onClick()
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.AssignmentTurnedIn,
                contentDescription = "",
            )
        },
        text = {
            Text(submitterDateTime)
        },
        secondaryText = {
            Row {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = "",
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = submissionPlainText ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    )
}