package com.ustadmobile.view.message.list

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListUiState
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import js.core.jso
import mui.material.*
import mui.system.sx
import react.FC
import react.Props
import react.create
import react.useState
import web.cssom.BackgroundClip
import web.cssom.Display


external interface MessageListScreenProps : Props {

    var uiState: MessageListUiState

    var onChangeNewMessageText: (String) -> Unit

    var onClickSend: () -> Unit

}


private val MessageListScreenComponent2 = FC<MessageListScreenProps> { props ->

    val strings = useStringProvider()

    val infiniteQueryResult = usePagingSource(
        props.uiState.messages, true, 50
    )
    val muiAppState = useMuiAppState()

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.messageUid.toString() }
            ) { messageItem ->
                ChatItem.create {
                    message = messageItem
                    activeUserUid = props.uiState.activePersonUid
                }
            }
        }

        Container {
            com.ustadmobile.view.components.virtuallist.VirtualListOutlet()
        }
    }

}

val MessageListScreenPreview = FC<Props> {


    val MessageListUiState by useState {
        MessageListUiState()
    }

    MessageListScreenComponent2 {
        uiState = MessageListUiState
    }
}

val MessageListScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        MessageListViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(MessageListUiState())

    MessageListScreenComponent2 {
        uiState = uiStateVal
        onChangeNewMessageText = viewModel::onChangeNewMessageText
        onClickSend = viewModel::onClickSend
    }
}


//@Composable
//fun MessageListScreen(
//    uiState: MessageListUiState,
//    onChangeNewMessageText: (String) -> Unit = { },
//    onClickSend: () -> Unit = { },
//){
//
//    val pager = remember(uiState.messages) {
//        Pager(
//            config = PagingConfig(pageSize = 20, enablePlaceholders = true, maxSize = 200),
//            pagingSourceFactory = uiState.messages
//        )
//    }
//
//    val linkExtractor = rememberLinkExtractor()
//
//    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
//
//
//    Column(
//        modifier = Modifier.fillMaxSize().defaultScreenPadding()
//    ) {
//        LazyColumn(
//            reverseLayout = true,
//            modifier = Modifier
//                .defaultItemPadding()
//                .weight(1f)
//        ){
//            ustadPagedItems(
//                pagingItems = lazyPagingItems,
//                key = { it.messageUid },
//            ) {  message ->
//                ChatItem(
//                    message = message,
//                    activeUserUid = uiState.activePersonUid,
//                    linkExtractor = linkExtractor,
//                )
//            }
//        }
//
//        NewMessageBox(
//            text = uiState.newMessageText,
//            onChangeNewMessageText = onChangeNewMessageText,
//            onClickSend = onClickSend,
//        )
//    }
//}

external interface ChatItemProps: Props {

    var message: Message?
    var activeUserUid: Long?

//    linkExtractor: ILinkExtractor,
}

val ChatItem = FC<ChatItemProps> { props ->

    val strings = useStringProvider()

    Box {
        sx {
            display = Display.grid
            height = 100.pct
            backgroundClip = BackgroundClip.borderBox
        }


//        UstadLinkifyText(
//            text = message?.messageText ?: "",
//            //color = MaterialTheme.colorScheme.onPrimaryContainer,
//            linkExtractor = linkExtractor,
//        )
    }
}
//@Composable
//fun ChatItem(
//    message: Message?,
//    activeUserUid: Long,
//    linkExtractor: ILinkExtractor,
//) {
//    val isFromMe = message?.messageSenderPersonUid == activeUserUid
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(4.dp)
//    ) {
//        Box(
//            modifier = Modifier
//                .align(if (isFromMe) Alignment.End else Alignment.Start)
//                .clip(
//                    RoundedCornerShape(
//                        topStart = 48f,
//                        topEnd = 48f,
//                        bottomStart = if (isFromMe) 48f else 0f,
//                        bottomEnd = if (isFromMe) 0f else 48f
//                    )
//                )
//                .background(MaterialTheme.colorScheme.primaryContainer)
//                .padding(16.dp)
//        ) {
//
//        }
//    }
//}

//@Composable
//fun NewMessageBox(
//    text: String,
//    onChangeNewMessageText: (String) -> Unit,
//    onClickSend: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//
//    Row(modifier = modifier.padding(16.dp)) {
//        TextField(
//            value = text,
//            onValueChange = onChangeNewMessageText,
//            modifier = Modifier
//                .weight(1f)
//                .padding(4.dp),
//            shape = RoundedCornerShape(24.dp),
//            colors = TextFieldDefaults.colors(
//                focusedIndicatorColor = Color.Transparent,
//                unfocusedIndicatorColor = Color.Transparent,
//                disabledIndicatorColor = Color.Transparent
//            ),
//            placeholder = {
//                Text(text = stringResource(MR.strings.message))
//            }
//        )
//
//        IconButton(
//            onClick = onClickSend,
//            modifier = Modifier
//                .clip(CircleShape)
//                .background(color = MaterialTheme.colorScheme.primaryContainer)
//                .align(Alignment.CenterVertically)
//        ) {
//            Icon(
//                imageVector = Icons.Filled.Send,
//                contentDescription = stringResource(MR.strings.send),
//                modifier = Modifier.fillMaxSize().padding(8.dp)
//            )
//        }
//    }
//}