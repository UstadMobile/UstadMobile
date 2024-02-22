package com.ustadmobile.libuicompose.view.message.messagelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.compose.stringResource


@Composable
fun NewMessageBox(
    text: String,
    onChangeNewMessageText: (String) -> Unit,
    onClickSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    key("send_message_box") {
        Row(modifier = modifier) {
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .testTag("message_text")
                    .onPreviewKeyEvent {
                        if(it.type == KeyEventType.KeyUp
                            && it.key == Key.Enter
                            && text.isNotBlank()
                            && !it.isAltPressed
                            && !it.isShiftPressed
                        ) {
                            onClickSend()
                            true
                        }else {
                            false
                        }
                    },
                value = text,
                onValueChange = onChangeNewMessageText,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(text = stringResource(MR.strings.message))
                }
            )

            if(text.isNotBlank()) {
                IconButton(
                    onClick = onClickSend,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = stringResource(MR.strings.send),
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
            }
        }
    }
}