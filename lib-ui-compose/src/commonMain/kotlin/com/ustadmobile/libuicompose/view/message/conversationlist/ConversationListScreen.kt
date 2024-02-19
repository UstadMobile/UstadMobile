package com.ustadmobile.libuicompose.view.message.conversationlist

import androidx.compose.foundation.layout.fillMaxSize
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
import com.ustadmobile.lib.db.composites.MessageAndOtherPerson
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.util.rememberDateFormat
import com.ustadmobile.libuicompose.util.rememberTimeFormatter
import java.util.TimeZone

@Composable
fun ConversationListScreen(
    viewModel: ConversationListViewModel
) {
    val uiState: ConversationListUiState by viewModel.uiState.collectAsState(ConversationListUiState())

    ConversationListScreen(
        uiState = uiState,
        onClickEntry = viewModel::onClickEntry,
    )
}

@Composable
fun ConversationListScreen(
    uiState: ConversationListUiState,
    onClickEntry: (MessageAndOtherPerson) -> Unit = { },
){

    val pager = remember(uiState.conversations) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.conversations
        )
    }

    val dateFormatter = rememberDateFormat(TimeZone.getDefault().id)
    val timeFormatter = rememberTimeFormatter()

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ){
        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { it.message?.messageUid ?: 0 },
        ) {  message ->
            ConversationListItem(
                message = message,
                uiState = uiState,
                timeFormatter = timeFormatter,
                dateFormatter = dateFormatter,
                onListItemClick = onClickEntry,
            )
        }
    }
}
