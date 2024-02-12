package com.ustadmobile.libuicompose.view.message.messagelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListUiState
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.compose.itemKey
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadLinkifyText
import com.ustadmobile.libuicompose.util.linkify.ILinkExtractor
import com.ustadmobile.libuicompose.util.linkify.rememberLinkExtractor
import com.ustadmobile.libuicompose.util.rememberTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import java.text.DateFormat
import java.util.Date

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

    val pager = remember(uiState.messages) {
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
            pagingSourceFactory = uiState.messages
        )
    }

    val linkExtractor = rememberLinkExtractor()
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val itemCount = lazyPagingItems.itemCount
    val timeFormatter = rememberTimeFormatter()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            reverseLayout = true,
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
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
                LaunchedEffect(itemCount) {
                    if(index == 1) {
                        coroutineScope.launch {
                            lazyListState.scrollToItem(0)
                        }
                    }
                }

                ChatItem(
                    message = message,
                    activeUserUid = uiState.activePersonUid,
                    linkExtractor = linkExtractor,
                    timeFormatter = timeFormatter,
                    modifier = Modifier.fillMaxSize().defaultItemPadding(),
                )
            }
        }

        NewMessageBox(
            text = uiState.newMessageText,
            modifier = Modifier.defaultItemPadding(),
            onChangeNewMessageText = onChangeNewMessageText,
            onClickSend = onClickSend,
        )
    }
}

@Composable
fun ChatItem(
    message: Message?,
    activeUserUid: Long,
    linkExtractor: ILinkExtractor,
    timeFormatter: DateFormat,
    modifier: Modifier = Modifier,
) {
    val isFromMe = message?.messageSenderPersonUid == activeUserUid

    Column(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .align(if (isFromMe) Alignment.End else Alignment.Start)
                .clip(
                    RoundedCornerShape(
                        topStart = 16f,
                        topEnd = 16f,
                        bottomStart = if (isFromMe) 16f else 0f,
                        bottomEnd = if (isFromMe) 0f else 16f
                    )
                )
                .background(
                    if(isFromMe) {
                        MaterialTheme.colorScheme.primaryContainer
                    }else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    }
                )
                .padding(8.dp)
        ) {
            Column {
                UstadLinkifyText(
                    text = message?.messageText ?: "",
                    linkExtractor = linkExtractor,
                )
                Text(
                    modifier = Modifier,
                    style = MaterialTheme.typography.labelSmall,
                    text = remember(message?.messageTimestamp ?: 0L) {
                        timeFormatter.format(Date(message?.messageTimestamp ?: 0L))
                    },
                    textAlign = TextAlign.End,
                )
            }

        }
    }
}

@Composable
fun NewMessageBox(
    text: String,
    onChangeNewMessageText: (String) -> Unit,
    onClickSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    key("send_message_box") {
        Row(modifier = modifier) {
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .onPreviewKeyEvent {
                        if(it.type == KeyEventType.KeyUp
                            && it.key == Key.Enter
                            && text.isNotBlank()
                            && !it.isAltPressed
                            && !it.isShiftPressed
                        ) {
                            onClickSend()
                            true
                        }else {
                            false
                        }
                    },
                value = text,
                onValueChange = onChangeNewMessageText,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(text = stringResource(MR.strings.message))
                }
            )

            if(text.isNotBlank()) {
                IconButton(
                    onClick = onClickSend,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = stringResource(MR.strings.send),
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
            }
        }
    }
}