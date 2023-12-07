package com.ustadmobile.libuicompose.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.testTag
import dev.icerock.moko.resources.compose.stringResource

@Composable
@Preview
private fun UstadAddCommentListItemPreview() {
    UstadAddCommentListItem(
        commentText = "",
        commentLabel = "Add",
        enabled = true,
        currentUserPersonUid = 0,
        onSubmitComment = {}
    )
}