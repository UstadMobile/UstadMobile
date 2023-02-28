package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.clickable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.composethemeadapter.MdcTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadAddCommentListItem(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    personUid: Long,
    onClickAddComment: (() -> Unit) = {  }
){
    ListItem(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = { onClickAddComment() }
            ),
        icon = {
            Icon(
                Icons.Default.Person,
                contentDescription = null
            )
        },
        text = {
            Text(text)
        }
    )
}

@Composable
@Preview
private fun UstadAddCommentListItemPreview() {
    MdcTheme {
        UstadAddCommentListItem(
            text = "Add",
            enabled = true,
            personUid = 0,
            onClickAddComment = {}
        )
    }
}