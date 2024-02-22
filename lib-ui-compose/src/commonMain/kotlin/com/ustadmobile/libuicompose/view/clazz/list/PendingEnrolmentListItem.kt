package com.ustadmobile.libuicompose.view.clazz.list

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ustadmobile.lib.db.composites.EnrolmentRequestAndCoursePic
import com.ustadmobile.libuicompose.components.UstadTooltipBox
import com.ustadmobile.libuicompose.util.rememberDayOrDate
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import java.text.DateFormat
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.view.clazz.CourseImage

@Composable
fun PendingEnrolmentListItem(
    request: EnrolmentRequestAndCoursePic,
    onClickCancel: () -> Unit,
    timeNow: LocalDateTime,
    timeFormatter: DateFormat,
    dateFormatter: DateFormat,
    dayOfWeekMap: Map<DayOfWeek, String>,
) {
    val requestTimeStr = rememberDayOrDate(
        localDateTimeNow = timeNow,
        timestamp = request.enrolmentRequest?.erRequestTime ?: 0,
        timeZone = TimeZone.currentSystemDefault(),
        showTimeIfToday = true,
        timeFormatter = timeFormatter,
        dateFormatter = dateFormatter,
        dayOfWeekStringMap = dayOfWeekMap,
    )

    ListItem(
        headlineContent = {
            Text(request.enrolmentRequest?.erClazzName ?: "")
        },
        supportingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.width(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(requestTimeStr)
            }
        },
        leadingContent = {
            CourseImage(
                coursePicture = request.coursePicture,
                clazzName = request.enrolmentRequest?.erClazzName,
                modifier = Modifier.size(40.dp).clip(CircleShape),
            )
        },
        trailingContent = {
            UstadTooltipBox(
                tooltipText = stringResource(MR.strings.cancel)
            ) {
                IconButton(
                    onClick = onClickCancel
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(MR.strings.cancel))
                }
            }
        }
    )
}