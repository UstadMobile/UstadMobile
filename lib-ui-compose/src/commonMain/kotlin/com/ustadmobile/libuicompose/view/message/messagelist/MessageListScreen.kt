package com.ustadmobile.libuicompose.view.message.messagelist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.compose.itemKey
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListUiState
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListViewModel
import com.ustadmobile.libuicompose.components.LazyColumnVerticalScrollbar
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.ext.scrollBarPadding
import com.ustadmobile.libuicompose.util.linkify.rememberLinkExtractor
import com.ustadmobile.libuicompose.util.rememberDateFormat
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import com.ustadmobile.libuicompose.util.rememberTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import java.util.TimeZone

@Composable
fun MessageListScreen(
    viewModel: MessageListViewModel
) {
    val uiState: MessageListUiState by viewModel.uiState.collectAsStateWithLifecycle(
        MessageListUiState(), Dispatchers.Main.immediate)

    MessageListScreen(
        uiState = uiState,
        onChangeNewMessageText = viewModel::onChangeNewMessageText,
        onClickSend = viewModel::onClickSend,
    )
}

@Composable
fun MessageListScreen(
    uiState: MessageListUiState,
    onChangeNewMessageText: (String) -> Unit = { },
    onClickSend: () -> Unit = { },
){

    val mediatorResult = rememberDoorRepositoryPager(
        uiState.messages, rememberEmptyFlow()
    )

    val linkExtractor = rememberLinkExtractor()
    val lazyPagingItems = mediatorResult.lazyPagingItems
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val itemCount = lazyPagingItems.itemCount
    val timeFormatter = rememberTimeFormatter()
    val dateFormatter = rememberDateFormat(TimeZone.getDefault().id)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize().weight(1f)
        ) {
            LazyColumn(
                reverseLayout = true,
                state = lazyListState,
                modifier = Modifier.fillMaxSize().scrollBarPadding()
            ) {
                items(
                    count = lazyPagingItems.itemCount,
                    key = lazyPagingItems.itemKey { it.messageUid  },
                ) {  index ->
                    /*
                     * When there is a new message, if the user is at the bottom of the list go to the
                     * latest item automatically.
                     */
                    val message = lazyPagingItems[index]
                    val previousMessage = if(lazyPagingItems.itemCount > index + 1) {
                        lazyPagingItems[index + 1]
                    }else {
                        null
                    }

                    LaunchedEffect(itemCount) {
                        if(index == 1) {
                            coroutineScope.launch {
                                lazyListState.scrollToItem(0)
                            }
                        }
                    }

                    MessageListItem(
                        message = message,
                        previousMessage = previousMessage,
                        activeUserUid = uiState.activePersonUid,
                        linkExtractor = linkExtractor,
                        timeFormatter = timeFormatter,
                        dateFormatter = dateFormatter,
                        uiState = uiState,
                        modifier = Modifier.fillMaxSize().defaultItemPadding(),
                    )
                }
            }

            LazyColumnVerticalScrollbar(
                state = lazyListState,
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
            )
        }


        NewMessageBox(
            text = uiState.newMessageText,
            modifier = Modifier.defaultItemPadding(),
            onChangeNewMessageText = onChangeNewMessageText,
            onClickSend = onClickSend,
        )
    }
}

