package com.ustadmobile.libuicompose.view.message.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.core.viewmodel.message.detail.MessageDetailUiState
import com.ustadmobile.core.viewmodel.message.detail.MessageDetailViewModel

@Composable
fun MessageDetailScreen(
    viewModel: MessageDetailViewModel
) {
    val uiState: MessageDetailUiState by viewModel.uiState.collectAsState(MessageDetailUiState())

    MessageDetailScreen(
        uiState = uiState,
    )
}

@Composable
fun MessageDetailScreen(
    uiState: MessageDetailUiState,
){

    val pager = remember(uiState.messageList) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.messageList
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ){

//        ustadPagedItems(
//            pagingItems = lazyPagingItems,
////            key = { it.latestMessage?.messageUid ?: 0 },
//        ) {  message ->
//            ListItem(
//                modifier = Modifier.clickable {
////                    message?.also { onListItemClick(it) }
//                },
//                headlineContent = { Text(text = "${message?.message?.messageText}") },
//                leadingContent = {
//                    UstadPersonAvatar(
//                        message?.senderPerson?.personUid ?: 0,
//                    )
//                },
//            )
//        }
    }
}