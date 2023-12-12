package com.ustadmobile.libuicompose.view.message.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadLinkifyText
import com.ustadmobile.libuicompose.util.linkify.ILinkExtractor
import com.ustadmobile.libuicompose.util.linkify.rememberLinkExtractor
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

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

    Column(
        modifier = Modifier.fillMaxSize().defaultScreenPadding()
    ) {
        LazyColumn(
            reverseLayout = true,
            modifier = Modifier
                .defaultItemPadding()
                .weight(1f)
        ){
            ustadPagedItems(
                pagingItems = lazyPagingItems,
                key = { it.messageUid },
            ) {  message ->
                ChatItem(
                    message = message,
                    activeUserUid = uiState.activePersonUid,
                    linkExtractor = linkExtractor,
                )
            }
        }

        NewMessageBox(
            text = uiState.newMessageText,
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
) {
    val isFromMe = message?.messageSenderPersonUid == activeUserUid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .align(if (isFromMe) Alignment.End else Alignment.Start)
                .clip(
                    RoundedCornerShape(
                        topStart = 48f,
                        topEnd = 48f,
                        bottomStart = if (isFromMe) 48f else 0f,
                        bottomEnd = if (isFromMe) 0f else 48f
                    )
                )
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp)
        ) {
            UstadLinkifyText(
                text = message?.messageText ?: "",
                //color = MaterialTheme.colorScheme.onPrimaryContainer,
                linkExtractor = linkExtractor,
            )
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

    Row(modifier = modifier.padding(16.dp)) {
        TextField(
            value = text,
            onValueChange = onChangeNewMessageText,
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
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