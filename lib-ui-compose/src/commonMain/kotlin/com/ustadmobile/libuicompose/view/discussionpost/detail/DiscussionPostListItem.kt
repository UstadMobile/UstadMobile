package com.ustadmobile.libuicompose.view.discussionpost.detail

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.lib.db.composites.DiscussionPostAndPosterNames
import com.ustadmobile.libuicompose.components.HtmlText
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import java.util.TimeZone

@Composable
fun DiscussionPostListItem(
    discussionPost: DiscussionPostAndPosterNames?,
    modifier: Modifier = Modifier,
) {
    val dateTimeStr = rememberFormattedDateTime(
        timeInMillis = discussionPost?.discussionPost?.discussionPostStartDate  ?: 0L,
        timeZoneId = TimeZone.getDefault().id,
        joinDateAndTime = { date, time -> "$date\n$time" }
    )

    ListItem(
        modifier = modifier,
        leadingContent = {
            UstadPersonAvatar(
                personUid = discussionPost?.discussionPost?.discussionPostStartedPersonUid ?: 0L
            )
        },
        headlineContent = {
            Text(
                text = (discussionPost?.firstNames ?: "") + " " + (discussionPost?.lastName ?: "")
            )
        },
        supportingContent = {
            HtmlText(html = discussionPost?.discussionPost?.discussionPostMessage ?: "")
        },
        trailingContent = {
            Text(dateTimeStr)
        }
    )
}