package com.ustadmobile.port.android.view.composable

import  androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R

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
        modifier = modifier,
        icon = {
            Icon(
                Icons.Default.Person,
                contentDescription = null
            )
        },
        text = {
            OutlinedButton(
                onClick = onClickAddComment,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(0.dp, Color.Transparent),
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.grey_a_40),
                )
            ) {
                Text(
                    text,
                    color = contentColorFor(
                        colorResource(id = R.color.grey_a_40).copy(0.1F))
                )
            }
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