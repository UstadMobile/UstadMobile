package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

@Composable
fun UstadAddCommentListItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    commentText: Flow<String>,
    commentLabel: String,
    onCommentChanged: (String) -> Unit = { },
    currentUserPersonUid: Long,
    currentUserPersonName: String,
    currentUserPictureUri: String?,
    onSubmitComment: () -> Unit = {  },
    editCommentInBottomSheet: Boolean = !isDesktop(), //Will be true on Android, false on desktop
){
    val commentTextVal by commentText.collectAsState("", Dispatchers.Main.immediate)

    val onShowBottomSheetFunction = onShowBottomSheetFragmentFunction { onDismissFun: () -> Unit ->
        val focusRequester = remember { FocusRequester() }

        /**
         * For some reason, using Dispatchers.Main.immediate is insufficient to get consistent cursor
         * placement etc, so we need to keep an internal state.
         */
        /**
         * For some reason, using Dispatchers.Main.immediate is insufficient to get consistent cursor
         * placement etc, so we need to keep an internal state.
         */
        var commentTextState by remember {
            mutableStateOf(commentText)
        }

        LaunchedEffect(commentText) {
            if(commentText != commentTextState)
                commentTextState = commentText
        }

        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            UstadPersonAvatar(
                personUid = currentUserPersonUid,
                personName = currentUserPersonName,
                pictureUri = currentUserPictureUri,
            )

            Spacer(Modifier.width(16.dp))

            UstadOutlinedCommentTextField(
                modifier = Modifier.weight(1f).focusRequester(focusRequester),
                value = commentTextVal,
                onValueChange = {
                    onCommentChanged(it)
                },
                label = {
                    Text(commentLabel)
                },
                enabled = enabled,
                onSubmitComment = {
                    onDismissFun()
                    onSubmitComment()
                }
            )
        }


        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    ListItem(
        modifier = modifier,
        leadingContent = {
            UstadPersonAvatar(
                personUid = currentUserPersonUid,
                personName = currentUserPersonName,
                pictureUri = currentUserPictureUri,
                modifier = Modifier.size(40.dp)
            )
        },
        headlineContent = {
            if (editCommentInBottomSheet){
                FilledTonalButton(
                    onClick = onShowBottomSheetFunction,
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
                UstadOutlinedCommentTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = commentTextVal,
                    onValueChange = onCommentChanged,
                    label = { Text(commentLabel) },
                    enabled = enabled,
                    onSubmitComment = onSubmitComment
                )
            }
        }
    )
}

