package com.ustadmobile.libuicompose.view.message.messagelist

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.libuicompose.util.linkify.rememberLinkExtractor
import com.ustadmobile.libuicompose.view.message.list.NewMessageBox
import com.ustadmobile.libuicompose.view.message.list.ChatItem

@Preview
@Composable
fun MessageListScreenPreview() {
    val linkExtractor = rememberLinkExtractor()
    Column{

        Column(
            modifier = Modifier.weight(1f)
        ) {
            ChatItem(
                message = Message(
                    messageText = "Hello World",
                    messageSenderPersonUid = 1
                ),
                activeUserUid = 1,
                linkExtractor = linkExtractor,
            )
            ChatItem(
                message = Message(
                    messageText = "How are you?",
                    messageSenderPersonUid = 2,
                ),
                activeUserUid = 1,
                linkExtractor = linkExtractor,
            )
        }

        NewMessageBox(
            text = "New message",
            onChangeNewMessageText = { },
            onClickSend = { }
        )
    }
}