package com.ustadmobile.port.android.view.clazzassignment

import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import com.ustadmobile.port.android.view.composable.UstadPersonAvatar
import java.util.TimeZone


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CommentListItem(
    commentAndName: CommentsAndName?,
    modifier: Modifier = Modifier,
){

    val formattedDateTime = rememberFormattedDateTime(
        timeInMillis = commentAndName?.comment?.commentsDateTimeAdded ?: 0,
        timeZoneId = TimeZone.getDefault().id,
        joinDateAndTime = {date, time -> "$date\n$time"}
    )

    ListItem(
        modifier = modifier,
        icon = {
            UstadPersonAvatar(
                personUid = commentAndName?.comment?.commentsPersonUid ?: 0L,
                modifier = Modifier.size(40.dp)
            )
        },
        text = { Text("${commentAndName?.firstNames} ${commentAndName?.lastName}") },
        secondaryText = { Text(commentAndName?.comment?.commentsText ?: "") },
        trailing = { Text(formattedDateTime) }
    )

}

@Composable
@Preview
private fun CommentListItemPreview() {
    CommentListItem(
        commentAndName = CommentsAndName().apply {
            comment = Comments().apply {
                commentsDateTimeAdded = systemTimeInMillis()
                commentsUid = 1
                commentsText = "I like this activity. Shall we discuss this in our next meeting?"
            }
            firstNames = "Bob"
            lastName = "Dylan"
        }
    )
}
