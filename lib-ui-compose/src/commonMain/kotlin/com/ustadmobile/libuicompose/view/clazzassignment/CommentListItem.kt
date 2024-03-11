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
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import dev.icerock.moko.resources.compose.stringResource
import java.util.TimeZone
import com.ustadmobile.core.MR

@Composable
fun CommentListItem(
    commentAndName: CommentsAndName?,
    linkExtractor: ILinkExtractor,
    modifier: Modifier = Modifier,
){

    val formattedDateTime = rememberFormattedDateTime(
        timeInMillis = commentAndName?.comment?.commentsDateTimeAdded ?: 0,
        timeZoneId = TimeZone.getDefault().id,
        joinDateAndTime = { date, time -> "$date\n$time"}
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
        trailingContent = { Text(formattedDateTime) }
    )

}