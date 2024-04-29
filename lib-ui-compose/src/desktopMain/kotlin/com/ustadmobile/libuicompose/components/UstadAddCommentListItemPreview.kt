package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.flowOf

@Composable
@Preview
private fun UstadAddCommentListItemPreview() {
    UstadAddCommentListItem(
        commentText = flowOf(""),
        commentLabel = "Add",
        enabled = true,
        currentUserPersonUid = 0,
        currentUserPersonName = "name",
        currentUserPictureUri = null,
        onSubmitComment = {}
    )
}