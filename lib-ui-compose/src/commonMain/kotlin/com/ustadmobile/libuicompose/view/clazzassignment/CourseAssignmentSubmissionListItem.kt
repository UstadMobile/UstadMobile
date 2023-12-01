package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.ustadmobile.core.util.ext.htmlToPlainText
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import kotlinx.datetime.TimeZone

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
        joinDateAndTime = {date, time -> "$date\n$time"},
    )

    ListItem(
        modifier = modifier.clickable {
            onClick()
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.AssignmentTurnedIn,
                contentDescription = "",
            )
        },
        headlineContent = {
            Text(submissionPlainText ?: "",
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Text(submitterDateTime)
        },
    )
}