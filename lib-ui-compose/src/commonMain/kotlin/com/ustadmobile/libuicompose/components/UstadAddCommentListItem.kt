package com.ustadmobile.libuicompose.components

import  androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadAddCommentListItem(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    personUid: Long,
    commentInBottomSheet: Boolean = false, //Will be true on Android, false on desktop
    onCommentTextChanged: (String) -> Unit = { },
    onClickAddComment: () -> Unit = {  },
){
    //If using bottom, show in bottom sheet modal, otherwise, use "normal" outlined text field.

    ListItem(
        modifier = modifier,
        icon = {
//            UstadPersonAvatar(
//                personUid = personUid,
//                modifier = Modifier.size(40.dp)
//            )
        },
        text = {
            TextButton(
                onClick = onClickAddComment,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(0.dp, Color.Transparent),
                enabled = enabled,
//                colors = ButtonDefaults.buttonColors(
//                    backgroundColor = colorResource(id = org.wordpress.aztec.R.color.grey_a_40),
//                )
            ) {
                Text(
                    text = text,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
//                    color = contentColorFor(
//                        colorResource(id = org.wordpress.aztec.R.color.grey_a_40))
                )
            }
        }
    )
}