package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
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
            ListItem(
                modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                leadingContent = {
                    UstadPersonAvatar(
                        personUid = currentUserPersonUid,
                        modifier = Modifier.size(40.dp)
                    )
                },
                headlineContent = {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = {
                            onCommentChanged(it)
                         },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(commentLabel) },
                        enabled = enabled,
                    )
                },
                trailingContent = {
                    IconButton(
                        onClick = {
                            onSubmitComment()
                            bottomSheetExpanded = false
                        },
                        enabled = enabled && commentText.isNotEmpty(),
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

    ListItem(
        modifier = modifier,
        leadingContent = {
            UstadPersonAvatar(
                personUid = currentUserPersonUid,
                modifier = Modifier.size(40.dp)
            )
        },
        headlineContent = {
            OutlinedTextField(
                value = commentText,
                onValueChange = {
                    onCommentChanged(it)
                },
                modifier = Modifier.fillMaxWidth()
                    .clickable(
                        onClick = {
                            if (editCommentInBottomSheet) bottomSheetExpanded = !bottomSheetExpanded
                        }),
                label = { Text(commentLabel) },
                enabled = enabled && !editCommentInBottomSheet,
            )
        },
        trailingContent = {
            if(!editCommentInBottomSheet) IconButton(
                onClick = {
                    onSubmitComment()
                },
                enabled = enabled && commentText.isNotEmpty(),
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = stringResource(MR.strings.send)
                )
            }
        }
    )
}