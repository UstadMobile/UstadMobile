package com.ustadmobile.libuicompose.view.message.conversationlist

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.libuicompose.components.UstadPersonAvatar

@Preview
@Composable
fun ConversationListScreenPreview() {
    Column {
        ListItem(
            headlineContent = { Text(text = "Ali Ahmadi") },
            leadingContent = { UstadPersonAvatar(0) },
            supportingContent = { Text(text = "How are you?") },
            trailingContent = { Text("11:30") }
        )
        ListItem(
            headlineContent = { Text(text = "Mohammad") },
            leadingContent = { UstadPersonAvatar(0) },
            supportingContent = { Text(text = "Can you help me on my exercise?") },
            trailingContent = { Text("11:10") }
        )
    }
}