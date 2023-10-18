package com.ustadmobile.port.android.view.discussionpost.detail

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.DiscussionPostAndPosterNames
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import com.ustadmobile.port.android.view.composable.HtmlText
import com.ustadmobile.port.android.view.composable.UstadPersonAvatar
import java.util.TimeZone

@OptIn(ExperimentalMaterialApi::class)
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
        icon = {
            UstadPersonAvatar(
                personUid = discussionPost?.discussionPost?.discussionPostStartedPersonUid ?: 0L
            )
        },
        text = {
            Text(
                text = (discussionPost?.firstNames ?: "") + " " + (discussionPost?.lastName ?: "")
            )
        },
        secondaryText = {
            HtmlText(html = discussionPost?.discussionPost?.discussionPostMessage ?: "")
        },
        trailing = {
            Text(dateTimeStr)
        }
    )
}

@Preview
@Composable
fun DiscussionPostListItemPreview() {
    DiscussionPostListItem(
        discussionPost = DiscussionPostAndPosterNames(
            discussionPost = DiscussionPost().apply {
                discussionPostMessage = "Hello <b>World</b>"
                discussionPostStartDate = systemTimeInMillis()
            },
            firstNames = "Bob",
            lastName = "Jones"
        ),
    )
}
