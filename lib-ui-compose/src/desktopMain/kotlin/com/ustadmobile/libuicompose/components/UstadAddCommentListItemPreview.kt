package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable

@Composable
@Preview
private fun UstadAddCommentListItemPreview() {
    UstadAddCommentListItem(
        commentText = "",
        commentLabel = "Add",
        enabled = true,
        currentUserPersonUid = 0,
        currentUserPersonName = "name",
        currentUserPictureUri = null,
        onSubmitComment = {}
    )
}