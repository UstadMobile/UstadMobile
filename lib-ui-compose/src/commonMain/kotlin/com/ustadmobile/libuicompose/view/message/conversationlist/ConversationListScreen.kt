package com.ustadmobile.libuicompose.view.message.conversationlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListUiState
import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import com.ustadmobile.lib.db.composites.MessageAndSenderPerson
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.ustadPagedItems

@Composable
fun ConversationListScreen(
    viewModel: ConversationListViewModel
) {
    val uiState: ConversationListUiState by viewModel.uiState.collectAsState(ConversationListUiState())

    ConversationListScreen(
        uiState = uiState,
    )
}

@Composable
fun ConversationListScreen(
    uiState: ConversationListUiState,
    onListItemClick: (MessageAndSenderPerson) -> Unit = {},
){

    val pager = remember(uiState.conversations) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.conversations
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){

        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { it.message?.messageUid ?: 0 },
        ) {  message ->
            ConversationItem(message, onListItemClick)
        }
    }
}

@Composable
fun ConversationItem(
    message: MessageAndSenderPerson?,
    onListItemClick: (MessageAndSenderPerson) -> Unit,
){
    ListItem(
        modifier = Modifier.clickable {
            message?.also { onListItemClick(it) }
        },
        headlineContent = { Text(text = "${message?.senderPerson?.fullName()}") },
        leadingContent = {
            UstadPersonAvatar(
                message?.senderPerson?.personUid ?: 0,
            )
        },
        supportingContent = { Text(text = "${message?.message?.messageText}") },
        trailingContent = { Text("${message?.message?.messageTimestamp}") }
    )
}