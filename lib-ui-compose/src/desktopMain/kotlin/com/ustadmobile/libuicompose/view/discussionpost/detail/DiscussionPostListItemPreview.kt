package com.ustadmobile.libuicompose.view.discussionpost.detail

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.composites.DiscussionPostAndPosterNames
import com.ustadmobile.lib.db.entities.DiscussionPost

@Preview
@Composable
fun DiscussionPostListItemPreview() {
    DiscussionPostListItem(
        discussionPost = DiscussionPostAndPosterNames(
            discussionPost = DiscussionPost().apply {
                discussionPostMessage = "Hello <b>World</b>"
                discussionPostStartDate = 0
            },
            firstNames = "Bob",
            lastName = "Jones"
        ),
    )
}