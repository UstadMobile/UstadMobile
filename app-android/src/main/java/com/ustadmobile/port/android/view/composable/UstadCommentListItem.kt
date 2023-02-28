package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.clickable
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
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.util.compose.rememberFormattedTime


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadCommentListItem(
    commentwithperson: CommentsWithPerson,
    modifier: Modifier = Modifier,
    onClickComment: (CommentsWithPerson) -> Unit = {},
){

    val formattedTime = rememberFormattedTime(commentwithperson.commentsDateTimeAdded.toInt())

    ListItem(
        modifier = modifier.clickable {
            onClickComment(commentwithperson)
        },
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_person_black_24dp),
                contentDescription = "",
                modifier = Modifier.size(40.dp)
            )
        },
        text = { Text(commentwithperson.commentsPerson?.fullName() ?: "") },
        secondaryText = { Text(commentwithperson.commentsText ?: "") },
        trailing = { Text(formattedTime) }
    )

}

@Composable
@Preview
private fun UstadCommentListItemPreview() {
    UstadCommentListItem(
        commentwithperson = CommentsWithPerson().apply {
            commentsUid = 1
            commentsPerson = Person().apply {
                firstNames = "Bob"
                lastName = "Dylan"
            }
            commentsText = "I like this activity. Shall we discuss this in our next meeting?"
        }
    )
}