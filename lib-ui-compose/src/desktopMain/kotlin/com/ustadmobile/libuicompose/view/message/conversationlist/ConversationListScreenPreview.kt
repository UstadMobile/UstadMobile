package com.ustadmobile.libuicompose.view.message.conversationlist

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.lib.db.composites.MessageAndSenderPerson
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.libuicompose.components.UstadPersonAvatar

@Preview
@Composable
fun ConversationListScreenPreview() {
    Column {
        ConversationItem(
            message = MessageAndSenderPerson().apply {
                message = Message().apply {
                    messageText = "How are you?"
                    messageTimestamp = 0
                }
                senderPerson = Person().apply {
                    firstNames = "Ali"
                    lastName = "Ahmadi"
                }
            },
            onListItemClick = {}
        )
        ConversationItem(
            message = MessageAndSenderPerson().apply {
                message = Message().apply {
                    messageText = "Can you help me on my exercise?"
                    messageTimestamp = 0
                }
                senderPerson = Person().apply {
                    firstNames = "Mohammad"
                }
            },
            onListItemClick = {}
        )
    }
}