package com.ustadmobile.libuicompose.view.message.conversationlist

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.composites.MessageAndOtherPerson
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.lib.db.entities.Person

@Preview
@Composable
fun ConversationListScreenPreview() {
    Column {
        ConversationItem(
            message = MessageAndOtherPerson().apply {
                message = Message().apply {
                    messageText = "How are you?"
                    messageTimestamp = 0
                }
                otherPerson = Person().apply {
                    firstNames = "Ali"
                    lastName = "Ahmadi"
                }
            },
            onListItemClick = {}
        )
        ConversationItem(
            message = MessageAndOtherPerson().apply {
                message = Message().apply {
                    messageText = "Can you help me on my exercise?"
                    messageTimestamp = 0
                }
                otherPerson = Person().apply {
                    firstNames = "Mohammad"
                }
            },
            onListItemClick = {}
        )
    }
}