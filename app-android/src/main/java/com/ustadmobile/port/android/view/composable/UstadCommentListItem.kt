package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toughra.ustadmobile.R
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.entities.Comments
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.util.compose.rememberFormattedDateTime
import com.ustadmobile.port.android.util.compose.rememberFormattedTime
import java.util.TimeZone


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadCommentListItem(
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
            Icon(
                painter = painterResource(R.drawable.ic_person_black_24dp),
                contentDescription = "",
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
private fun UstadCommentListItemPreview() {
    UstadCommentListItem(
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
