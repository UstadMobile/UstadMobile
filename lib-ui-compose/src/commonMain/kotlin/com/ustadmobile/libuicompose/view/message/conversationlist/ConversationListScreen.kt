package com.ustadmobile.libuicompose.view.message.conversationlist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListUiState
import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListViewModel
import com.ustadmobile.lib.db.composites.MessageAndOtherPerson
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadNothingHereYet
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.rememberDateFormat
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberTimeFormatter
import kotlinx.coroutines.flow.Flow
import java.util.TimeZone

@Composable
fun ConversationListScreen(
    viewModel: ConversationListViewModel
) {
    val uiState: ConversationListUiState by viewModel.uiState.collectAsState(ConversationListUiState())

    ConversationListScreen(
        uiState = uiState,
        refreshCommandFlow = viewModel.refreshCommandFlow,
        onClickEntry = viewModel::onClickEntry,
    )
}

@Composable
fun ConversationListScreen(
    uiState: ConversationListUiState,
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onClickEntry: (MessageAndOtherPerson) -> Unit = { },
){

    val repositoryResult = rememberDoorRepositoryPager(
        uiState.conversations, refreshCommandFlow
    )

    val dateFormatter = rememberDateFormat(TimeZone.getDefault().id)
    val timeFormatter = rememberTimeFormatter()

    val lazyPagingItems = repositoryResult.lazyPagingItems

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ){
        if(repositoryResult.isSettledEmpty) {
            item("empty_state") {
                UstadNothingHereYet()
            }
        }

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
