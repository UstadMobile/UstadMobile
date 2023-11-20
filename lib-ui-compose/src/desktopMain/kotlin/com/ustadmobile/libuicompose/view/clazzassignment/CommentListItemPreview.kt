package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.entities.Comments

@Composable
@Preview
private fun CommentListItemPreview() {
    CommentListItem(
        commentAndName = CommentsAndName().apply {
            comment = Comments().apply {
                commentsDateTimeAdded = 0
                commentsUid = 1
                commentsText = "I like this activity. Shall we discuss this in our next meeting?"
            }
            firstNames = "Bob"
            lastName = "Dylan"
        }
    )
}
