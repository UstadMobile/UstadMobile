package com.ustadmobile.libuicompose.view.discussionpost.detail

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import com.ustadmobile.core.viewmodel.discussionpost.detail.DiscussionPostDetailUiState2
import kotlinx.coroutines.flow.flowOf

@Composable
@Preview
fun DiscussionPostDetailScreenPreview(){
    val uiState = DiscussionPostDetailUiState2(
//        discussionPosts = {
//            ListPagingSource(listOf(
//                DiscussionPostAndPosterNames(
//                    discussionPost = DiscussionPost().apply {
//                        discussionPostTitle = "Submitting an assignment"
//                        discussionPostVisible = true
//                        discussionPostStartedPersonUid = 1
//                        discussionPostReplyToPostUid = 0
//                        discussionPostUid = 1
//                        discussionPostMessage = "How can I get the best grade?"
//                        discussionPostStartDate = 0
//                    },
//                    firstNames = "Mohammed",
//                    lastName = "Iqbaal",
//                ),
//                DiscussionPostAndPosterNames(
//                    discussionPost = DiscussionPost().apply {
//                        discussionPostReplyToPostUid = 42
//                        discussionPostTitle = null
//                        discussionPostVisible = true
//                        discussionPostStartedPersonUid = 1
//                        discussionPostUid = 2
//                        discussionPostMessage = "Use ChatGPT"
//                        discussionPostStartDate = 0
//                    },
//                    firstNames = "Cheaty",
//                    lastName = "McCheatface",
//                ),
//                DiscussionPostAndPosterNames(
//                    discussionPost = DiscussionPost().apply {
//                        discussionPostReplyToPostUid = 42
//                        discussionPostVisible = true
//                        discussionPostStartedPersonUid = 1
//
//                        discussionPostUid = 3
//                        discussionPostMessage = "Use BARD"
//                        discussionPostStartDate = 0
//                    },
//                    firstNames = "Chester",
//                    lastName = "Cheetah",
//                ),
//                DiscussionPostAndPosterNames(
//                    discussionPost = DiscussionPost().apply {
//                        discussionPostVisible = true
//                        discussionPostStartedPersonUid = 1
//                        discussionPostReplyToPostUid = 42
//                        discussionPostUid = 4
//                        discussionPostMessage = "Ask Jeeves"
//                        discussionPostStartDate = 0
//                    },
//                    firstNames = "Uncle",
//                    lastName = "Brandon",
//                ),
//            ))
//        },
        loggedInPersonUid = 1
    )

    DiscussionPostDetailScreen(uiState, flowOf(""))
}