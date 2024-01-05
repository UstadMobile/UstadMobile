package com.ustadmobile.libuicompose.view.message.conversationlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.style.TextOverflow
import com.ustadmobile.lib.db.composites.MessageAndOtherPerson
import java.util.TimeZone
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import com.ustadmobile.libuicompose.util.rememberFormattedTime

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
    onClickEntry: (MessageAndOtherPerson) -> Unit = {},
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
            .fillMaxSize()
    ){
        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { it.message?.messageUid ?: 0 },
        ) {  message ->
            ConversationItem(message, onClickEntry)
        }
    }
}

@Composable
fun ConversationItem(
    message: MessageAndOtherPerson?,
    onListItemClick: (MessageAndOtherPerson) -> Unit,
){

    val formattedDateTime = rememberFormattedDateTime(
        timeInMillis = message?.message?.messageTimestamp ?: 0,
        timeZoneId = TimeZone.getDefault().id
    )

    ListItem(
        modifier = Modifier.clickable {
            message?.also { onListItemClick(it) }
        },
        headlineContent = { Text(text = "${message?.otherPerson?.fullName()}") },
        leadingContent = {
            UstadPersonAvatar(
                message?.otherPerson?.personUid ?: 0,
            )
            UstadPersonAvatar(
                pictureUri = message?.personPicture?.personPictureThumbnailUri,
                personName = message?.otherPerson?.fullName(),
            )
        },
        supportingContent = {
            Text(
                text = "${message?.message?.messageText}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = { Text(formattedDateTime) }
    )
}