package com.ustadmobile.view.message.messagelist

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListUiState
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadLinkify
import com.ustadmobile.mui.components.UstadTextField
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import js.core.jso
import web.cssom.JustifyContent
import mui.icons.material.Send
import mui.material.Container
import mui.material.Box
import mui.material.Stack
import mui.material.StackDirection
import mui.material.IconButton
import mui.material.Typography
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.aria.ariaLabel
import react.useEffect
import react.useRef
import react.useRequiredContext
import react.useState
import web.cssom.Display
import web.cssom.Flex
import web.cssom.px
import web.html.HTMLElement


external interface MessageListScreenProps : Props {

    var uiState: MessageListUiState

    var onChangeNewMessageText: (String) -> Unit

    var onClickSend: () -> Unit

}


private val MessageListScreenComponent2 = FC<MessageListScreenProps> { props ->

    val infiniteQueryResult = usePagingSource(
        props.uiState.messages, true, 50
    )
    val muiAppState = useMuiAppState()

    val newMessageBoxRef = useRef<HTMLElement>(null)
    var newMessageBoxHeight: Int by useState(0)
    val newMessageBoxPaddingPx = 8

    useEffect(newMessageBoxRef.current?.clientHeight) {
        newMessageBoxHeight =
            (newMessageBoxRef.current?.clientHeight ?: 0) + (newMessageBoxPaddingPx * 2)
    }

    VirtualList {
        style = jso {
            height = "calc(100vh - ${(muiAppState.appBarHeight + newMessageBoxHeight)}px)".unsafeCast<Height>()
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
            VirtualListOutlet()
        }
    }

    Container {
        ref = newMessageBoxRef

        NewMessageBox {
            text = props.uiState.newMessageText
            onChangeNewMessageText = props.onChangeNewMessageText
            onClickSend = props.onClickSend
        }
    }
}

val MessageListScreenPreview = FC<Props> {

    val messages = { ListPagingSource(
        listOf(Message().apply {
            messageText = "Sallam, WHere are you from?"
            messageSenderPersonUid = 1
        },
            Message().apply {
                messageText = "Sallam, WHere are you from?"
                messageSenderPersonUid = 2
            },
        )
    ) }

    val MessageListUiState by useState {
        MessageListUiState(
            messages = messages,
            activePersonUid = 2
        )
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

external interface ChatItemProps: Props {

    var message: Message?
    var activeUserUid: Long

}

val ChatItem = FC<ChatItemProps> { props ->

    val theme by useRequiredContext(ThemeContext)
    val isFromMe = props.message?.messageSenderPersonUid == props.activeUserUid

    Box {
        sx {
            display = Display.flex
            flex = Flex.minContent
            justifyContent = if (isFromMe) JustifyContent.end else JustifyContent.start
        }

        Typography {
            sx {
                backgroundColor = theme.palette.primary.light
                color = theme.palette.primary.contrastText
                padding = 10.px
                margin = 5.px
                borderTopLeftRadius = 48.px
                borderTopRightRadius = 48.px
                borderBottomLeftRadius = if (isFromMe) 48.px else 0.px
                borderBottomRightRadius = if (isFromMe) 0.px else 48.px
            }
            + UstadLinkify.create {
                + (props.message?.messageText ?: "")
            }
        }
    }
}

external interface NewMessageBoxProps: Props {

    var text: String
    var fieldsEnabled: Boolean
    var onChangeNewMessageText: (String) -> Unit
    var onClickSend: () -> Unit

}

val NewMessageBox = FC<NewMessageBoxProps> { props ->

    val strings = useStringProvider()

    Box {
        Stack {
            direction = responsive(StackDirection.row)
            spacing = responsive(20.px)

            UstadTextField {
                id = "newComment"
                fullWidth = true
                value = props.text
                label = ReactNode(strings[MR.strings.message])
                onTextChange = {
                    props.onChangeNewMessageText(it)
                }
            }

            IconButton {
                id = "send_message"
                ariaLabel = strings[MR.strings.message]
                onClick = {
                    props.onClickSend()
                }

                Send()
            }
        }
    }
}