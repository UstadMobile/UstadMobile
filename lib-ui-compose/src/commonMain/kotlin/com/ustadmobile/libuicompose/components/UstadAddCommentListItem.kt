package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UstadAddCommentListItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    commentText: String,
    commentLabel: String,
    onCommentChanged: (String) -> Unit = { },
    currentUserPersonUid: Long,
    onSubmitComment: () -> Unit = {  },
    editCommentInBottomSheet: Boolean = !isDesktop(), //Will be true on Android, false on desktop
){

    var bottomSheetExpanded by remember {
        mutableStateOf(false)
    }

    if(editCommentInBottomSheet && bottomSheetExpanded) {
        ModalBottomSheet(
            onDismissRequest= {
                bottomSheetExpanded = false
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).height(150.dp),
                verticalAlignment = Alignment.Top
            ) {
                UstadPersonAvatar(
                    personUid = currentUserPersonUid,
                    modifier = Modifier.size(40.dp).align(Alignment.Top)
                )

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedTextField(
                    value = commentText,
                    onValueChange = {
                        onCommentChanged(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(commentLabel) },
                    enabled = enabled,
                    trailingIcon = {
                        if(commentText.isNotEmpty()) IconButton(
                            onClick = {
                                onSubmitComment()
                                bottomSheetExpanded = false
                            },
                            enabled = enabled,
                        ) {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = stringResource(MR.strings.send)
                            )
                        }
                    }
                )
            }
        }
    }

    Row(
        modifier = modifier.defaultItemPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UstadPersonAvatar(
            personUid = currentUserPersonUid,
            modifier = Modifier.size(40.dp)
        )

        if (editCommentInBottomSheet){
            FilledTonalButton(
                onClick = { bottomSheetExpanded = !bottomSheetExpanded },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(0.dp, Color.Transparent),
                enabled = enabled,
                content = {
                    Text(
                    text = commentLabel,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                }
            )
        } else {
            OutlinedTextField(
                value = commentText,
                onValueChange = {
                    onCommentChanged(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(commentLabel) },
                enabled = enabled,
                trailingIcon = {
                    if(commentText.isNotEmpty()) IconButton(
                        onClick = onSubmitComment,
                        enabled = enabled,
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = stringResource(MR.strings.send)
                        )
                    }
                }
            )
        }
    }
}