package com.ustadmobile.view.message.messagelist

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListUiState
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.Message
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
import mui.icons.material.Send
import mui.material.Container
import mui.material.Box
import mui.material.Stack
import mui.material.StackDirection
import mui.material.IconButton
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.aria.ariaLabel
import react.useEffect
import react.useRef
import react.useState
import web.cssom.BackgroundClip
import web.cssom.Display
import web.cssom.Width
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

    Box {
        sx {
            display = Display.block
            margin = newMessageBoxPaddingPx.px
            width = "calc(100% - ${newMessageBoxPaddingPx* 2}px)".unsafeCast<Width>()
        }
        ref = newMessageBoxRef

        NewMessageBox {
            text = props.uiState.newMessageText
            onChangeNewMessageText = props.onChangeNewMessageText
            onClickSend = props.onClickSend
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

external interface ChatItemProps: Props {

    var message: Message?
    var activeUserUid: Long?

}

val ChatItem = FC<ChatItemProps> { props ->


    Box {
        sx {
            display = Display.grid
            height = 100.pct
            backgroundClip = BackgroundClip.borderBox
        }


        UstadLinkify.create {
            + (props.message?.messageText ?: "")
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

    Stack {
        direction = responsive(StackDirection.row)
        spacing = responsive(20.px)

        UstadTextField {
            id = "newComment"
//            disabled = !props.fieldsEnabled
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