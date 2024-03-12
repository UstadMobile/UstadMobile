package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.clazzassignment.isFromSubmitterGroup
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.libuicompose.components.UstadLinkifyText
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.util.linkify.ILinkExtractor
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.util.rememberDayOrDate
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import java.text.DateFormat

@Composable
fun CommentListItem(
    commentAndName: CommentsAndName?,
    linkExtractor: ILinkExtractor,
    localDateTimeNow: LocalDateTime,
    timeFormatter: DateFormat,
    dateFormatter: DateFormat,
    dayOfWeekStringMap: Map<DayOfWeek, String>,
    modifier: Modifier = Modifier,
){

    val dayOrDate = rememberDayOrDate(
        localDateTimeNow = localDateTimeNow,
        timestamp = commentAndName?.comment?.commentsDateTimeAdded ?: 0,
        timeZone = kotlinx.datetime.TimeZone.currentSystemDefault(),
        showTimeIfToday = true,
        timeFormatter = timeFormatter,
        dateFormatter = dateFormatter,
        dayOfWeekStringMap = dayOfWeekStringMap

    )

    val fullName = "${commentAndName?.firstNames ?: ""} ${commentAndName?.lastName ?: ""}"
    val groupSuffix = commentAndName?.comment
        ?.takeIf { it.isFromSubmitterGroup }
        ?.let { " (${stringResource(MR.strings.group)} ${it.commentsFromSubmitterUid})" }

    ListItem(
        modifier = modifier,
        leadingContent = {
            UstadPersonAvatar(
                personName = fullName,
                pictureUri = commentAndName?.pictureUri,
            )
        },
        headlineContent = { Text("$fullName$groupSuffix") },
        supportingContent = {
            UstadLinkifyText(
                text = commentAndName?.comment?.commentsText ?: "",
                linkExtractor = linkExtractor,
            )
        },
        trailingContent = { Text(dayOrDate) }
    )

}