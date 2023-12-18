package com.ustadmobile.libuicompose.view.discussionpost.coursediscussiondetail

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.core.viewmodel.discussionpost.courediscussiondetail.CourseDiscussionDetailUiState
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails


@Composable
@Preview
fun CourseDiscussionDetailScreenPreview(){

    CourseDiscussionDetailScreen(
        uiState = CourseDiscussionDetailUiState(

            courseBlock = CourseBlock().apply{
                cbTitle = "Discussions on Module 4: Statistics and Data Science"
                cbDescription = "Here Any discussion related to Module 4 of Data Science chapter goes here."
            },
        )
    )
}

@Preview
@Composable
fun CourseDiscussionDetailDiscussionListItemPreview() {
    CourseDiscussionDetailDiscussionListItem(
        discussionPostItem = DiscussionPostWithDetails().apply {
            discussionPostTitle = "Can I join after week 2?"
            discussionPostUid = 0L
            discussionPostMessage = "Iam late to class, CAn I join after?"
            discussionPostVisible = true
            postRepliesCount = 4
            postLatestMessage = "Just make sure you submit a late assignment."
            authorPersonFirstNames = "Mike"
            authorPersonLastName = "Jones"
            discussionPostStartDate = System.currentTimeMillis()
        },
        systemTimeZone = "UTC",
    )
}
