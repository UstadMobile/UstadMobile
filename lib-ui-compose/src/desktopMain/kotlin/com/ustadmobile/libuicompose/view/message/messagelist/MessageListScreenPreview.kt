package com.ustadmobile.libuicompose.view.message.messagelist

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable

@Preview
@Composable
fun MessageListScreenPreview() {
    Column{
        NewMessageBox(
            text = "New message",
            onChangeNewMessageText = { },
            onClickSend = { }
        )
    }
}