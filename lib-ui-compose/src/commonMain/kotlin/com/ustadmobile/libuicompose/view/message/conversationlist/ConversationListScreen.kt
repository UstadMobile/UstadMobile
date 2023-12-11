package com.ustadmobile.libuicompose.view.message.conversationlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListUiState
import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListViewModel
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import com.ustadmobile.libuicompose.components.ustadPagedItems

@Composable
fun ConversationListScreen(
    viewModel: ConversationListViewModel
) {
    val uiState: ConversationListUiState by viewModel.uiState.collectAsState(ConversationListUiState())

    MessageDetailScreen(
        uiState = uiState,
    )
}

@Composable
fun MessageDetailScreen(
    uiState: ConversationListUiState,
){

    val pager = remember(uiState.conversations) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.conversations
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    Column(
        modifier = Modifier.fillMaxSize().defaultScreenPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .defaultItemPadding()
                .weight(1f)
        ){

        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { it.message?.messageUid ?: 0 },
        ) {  message ->
//            ListItem(
//                headlineContent = { Text(text = "${message?.message?.messageText}") },
//                leadingContent = {
//                    UstadPersonAvatar(
//                        message?.senderPerson?.personUid ?: 0,
//                    )
//                },
//            )
        }
        }

        Row (
            modifier = Modifier.defaultItemPadding()
        ){
            IconButton(
                onClick = {},
                content = { Icon(Icons.Default.Add, contentDescription = null) }
            )
        }
    }

}