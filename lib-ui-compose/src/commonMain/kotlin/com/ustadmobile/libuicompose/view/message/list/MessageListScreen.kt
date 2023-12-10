package com.ustadmobile.libuicompose.view.message.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
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
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListUiState
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListViewModel
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.ustadPagedItems

@Composable
fun MessageListScreen(
    viewModel: MessageListViewModel
) {
    val uiState: MessageListUiState by viewModel.uiState.collectAsState(MessageListUiState())

    MessageListScreen(
        uiState = uiState,
    )
}

@Composable
fun MessageListScreen(
    uiState: MessageListUiState,
){

    val pager = remember(uiState.messages) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.messages
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
            ListItem(
                modifier = Modifier.clickable {
//                    message?.also { onListItemClick(it) }
                },
                headlineContent = { Text(text = "${message?.message?.messageText}") },
                leadingContent = {
                    UstadPersonAvatar(
                        message?.senderPerson?.personUid ?: 0,
                    )
                },
            )
        }
    }
}