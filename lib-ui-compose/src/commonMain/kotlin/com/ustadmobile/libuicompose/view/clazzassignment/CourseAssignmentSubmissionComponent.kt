package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.libuicompose.components.UstadHtmlText
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadTooltipBox
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import kotlinx.datetime.TimeZone

@Composable
fun CourseAssignmentSubmissionComponent(
    submission: CourseAssignmentSubmission,
    submissionNum: Int,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
) {
    val timeZoneId = remember { TimeZone.currentSystemDefault().id }
    val submittedTimeStamp = rememberFormattedDateTime(
        timeInMillis = submission.casTimestamp,
        timeZoneId = timeZoneId,
    )
    val labelText = stringResource(if(isCollapsed) MR.strings.expand else MR.strings.collapse)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            modifier = Modifier.clickable { onToggleCollapse() },
            headlineContent = {
                Text("${stringResource(MR.strings.submission)} $submissionNum")
            },
            supportingContent = {
                Text(submittedTimeStamp)
            },
            trailingContent = {
                UstadTooltipBox(
                    tooltipText = labelText
                ) {
                    IconButton(
                        onClick = onToggleCollapse
                    ) {
                        if(isCollapsed) {
                            Icon(Icons.Default.ExpandMore, contentDescription = labelText)
                        }else {
                            Icon(Icons.Default.ExpandLess, contentDescription = labelText)
                        }
                    }
                }

            },
        )

        if(!isCollapsed) {
            UstadHtmlText(
                modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                html = submission.casText ?: ""
            )
        }
    }
}