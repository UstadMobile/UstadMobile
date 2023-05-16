package com.ustadmobile.port.android.view.clazzassignment

import  androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.composable.UstadPersonAvatar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddCommentListItem(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    personUid: Long,
    onClickAddComment: (() -> Unit) = {  }
){
    ListItem(
        modifier = modifier,
        icon = {
            UstadPersonAvatar(
                personUid = personUid,
                modifier = Modifier.size(40.dp)
            )
        },
        text = {
            TextButton(
                onClick = onClickAddComment,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(0.dp, Color.Transparent),
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = colorResource(id = R.color.grey_a_40),
                )
            ) {
                Text(
                    text = text,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                    color = contentColorFor(
                        colorResource(id = R.color.grey_a_40))
                )
            }
        }
    )
}

@Composable
@Preview
private fun AddCommentListItemPreview() {
    MdcTheme {
        AddCommentListItem(
            text = "Add",
            enabled = true,
            personUid = 0,
            onClickAddComment = {}
        )
    }
}