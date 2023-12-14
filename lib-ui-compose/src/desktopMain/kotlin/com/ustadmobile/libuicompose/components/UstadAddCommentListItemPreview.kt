package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable

@Composable
@Preview
private fun UstadAddCommentListItemPreview() {
    UstadAddCommentListItem(
        text = "Add",
        enabled = true,
        personUid = 0,
        onClickAddComment = {}
    )
}