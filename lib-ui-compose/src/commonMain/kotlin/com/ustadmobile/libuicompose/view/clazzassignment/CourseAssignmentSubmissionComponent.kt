package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.libuicompose.components.UstadHtmlText
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import kotlinx.datetime.TimeZone

@Composable
fun CourseAssignmentSubmissionComponent(
    submission: CourseAssignmentSubmission
) {
    val timeZoneId = remember { TimeZone.currentSystemDefault().id }
    val submittedTimeStamp = rememberFormattedDateTime(
        timeInMillis = submission.casTimestamp,
        timeZoneId = timeZoneId,
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        UstadHtmlText(
            modifier = Modifier.fillMaxWidth().defaultItemPadding(),
            html = submission.casText ?: ""
        )

        Text(
            modifier = Modifier.defaultItemPadding(),
            text = stringResource(MR.strings.submitted_key).capitalizeFirstLetter() + ": $submittedTimeStamp",
            style = MaterialTheme.typography.labelSmall,
        )
    }
}