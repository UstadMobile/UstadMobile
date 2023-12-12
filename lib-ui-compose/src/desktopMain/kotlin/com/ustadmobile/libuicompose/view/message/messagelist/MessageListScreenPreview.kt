package com.ustadmobile.libuicompose.view.message.messagelist

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.view.message.list.ChatBox
import com.ustadmobile.libuicompose.view.message.list.ChatItem

@Preview
@Composable
fun MessageListScreenPreview() {
    Column{

        Column(
            modifier = Modifier.weight(1f)
        ) {
            ChatItem(message = "How are you?", isFromMe = false)
            ChatItem(message = "How are you?", isFromMe = true)
        }

        ChatBox(onSendChatClickListener = {})
    }
}