package com.ustadmobile.libuicompose.view.clazzassignment

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
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
            // TODO error
//            UstadPersonAvatar(
//                personUid = commentAndName?.comment?.commentsPersonUid ?: 0L,
//                modifier = Modifier.size(40.dp)
//            )
        },
        text = { Text("${commentAndName?.firstNames} ${commentAndName?.lastName}") },
        secondaryText = { Text(commentAndName?.comment?.commentsText ?: "") },
        trailing = { Text(formattedDateTime) }
    )

}