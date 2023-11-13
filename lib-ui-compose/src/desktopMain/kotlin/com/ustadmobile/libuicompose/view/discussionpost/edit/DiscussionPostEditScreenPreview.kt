package com.ustadmobile.libuicompose.view.discussionpost.edit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import com.ustadmobile.core.viewmodel.discussionpost.edit.DiscussionPostEditUiState
import com.ustadmobile.lib.db.entities.DiscussionPost

@Composable
@Preview
fun DiscussionPostEditFragmentPreview(){
    val uiStateVal = DiscussionPostEditUiState(
        discussionPost = DiscussionPost().apply {
            discussionPostTitle = "How do I upload my homework?"
            discussionPostMessage= "Hi everyone, how do I finish and upload my homework to this moduel? Thanks! "

        }

    )

    DiscussionPostEditScreen(uiState = uiStateVal)
}