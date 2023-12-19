package com.ustadmobile.view.message.list

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
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
import kotlinx.css.px
import mui.icons.material.Send
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.aria.ariaLabel
import react.useRequiredContext
import react.useState
import web.cssom.BackgroundClip
import web.cssom.Display
import web.cssom.ShapeOutside


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

            item {
                NewMessageBox.create {
                    text = props.uiState.newMessageText
                    onChangeNewMessageText = props.onChangeNewMessageText
                    onClickSend = props.onClickSend
                }
            }
        }

        Container {
            VirtualListOutlet()
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
            //color = MaterialTheme.colorScheme.onPrimaryContainer,
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
    val theme by useRequiredContext(ThemeContext)

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
           sx {
               shapeOutside = ShapeOutside.contentBox
               background = theme.palette.secondary.main
           }
            color = IconButtonColor.info

            ariaLabel = strings[MR.strings.message]
            onClick = {
                props.onClickSend()
            }

            Send()
        }
    }
}