package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.libuicompose.components.UstadLinkifyText
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.util.linkify.ILinkExtractor
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import java.util.TimeZone


@Composable
fun CommentListItem(
    commentAndName: CommentsAndName?,
    linkExtractor: ILinkExtractor,
    modifier: Modifier = Modifier,
){

    val formattedDateTime = rememberFormattedDateTime(
        timeInMillis = commentAndName?.comment?.commentsDateTimeAdded ?: 0,
        timeZoneId = TimeZone.getDefault().id,
        joinDateAndTime = {date, time -> "$date\n$time"}
    )

    ListItem(
        modifier = modifier,
        leadingContent = {
            UstadPersonAvatar(
                personUid = commentAndName?.comment?.commentsPersonUid ?: 0L,
            )
        },
        headlineContent = { Text("${commentAndName?.firstNames} ${commentAndName?.lastName}") },
        supportingContent = {
            UstadLinkifyText(
                text = commentAndName?.comment?.commentsText ?: "",
                linkExtractor = linkExtractor,
            )
        },
        trailingContent = { Text(formattedDateTime) }
    )

}