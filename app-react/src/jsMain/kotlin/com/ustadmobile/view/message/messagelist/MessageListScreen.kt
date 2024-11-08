package com.ustadmobile.view.message.messagelist

import com.ustadmobile.core.MR.strings.add
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListUiState
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListViewModel
import com.ustadmobile.hooks.useDateFormatter
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useEmptyFlow
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTimeFormatter
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.mui.components.SocialWarningListItem
import com.ustadmobile.mui.components.UstadSendTextField
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import mui.material.Container
import react.FC
import react.Fragment
import react.Props
import react.ReactNode
import react.create
import react.useEffect
import react.useRef
import react.useState
import web.animations.CompositeOperation.Companion.add
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import web.html.HTMLElement


external interface MessageListScreenProps : Props {

    var uiState: MessageListUiState

    var onChangeNewMessageText: (String) -> Unit

    var onClickSend: () -> Unit

    var onWarningDismiss: () -> Unit

}


private val MessageListScreenComponent2 = FC<MessageListScreenProps> { props ->

    val emptyRefreshFlow = useEmptyFlow<RefreshCommand>()

    val mediatorResult = useDoorRemoteMediator(
        props.uiState.messages, emptyRefreshFlow
    )

    val infiniteQueryResult = usePagingSource(
        mediatorResult.pagingSourceFactory, true, 50
    )
    val muiAppState = useMuiAppState()

    val newMessageBoxRef = useRef<HTMLElement>(null)
    var newMessageBoxHeight: Int by useState(0)
    val newMessageBoxPaddingPx = 8
    val timeFormatterVal = useTimeFormatter()
    val dateFormatterVal = useDateFormatter()

    useEffect(newMessageBoxRef.current?.clientHeight) {
        newMessageBoxHeight =
            (newMessageBoxRef.current?.clientHeight ?: 0) + (newMessageBoxPaddingPx * 2)
    }

    VirtualList {
        reverseLayout = true
        style = jso {
            height = "calc(100vh - ${(muiAppState.appBarHeight + newMessageBoxHeight)}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            reverseLayout = false

            if (props.uiState.showSocialWarning) {
                Fragment.create {
                    SocialWarningListItem {
                        cautions = listOf(
                            "Avoid sharing personal information",
                            "Report any inappropriate behavior",
                            "Be respectful in interactions",
                            "Stay cautious when engaging with others",
                            "Think twice before sharing sensitive details"
                        )
                        onDismiss = props.onWarningDismiss
                        onLearnMore = { /* Navigate to guidelines */ }
                    }
                }
            }

            infiniteQueryPagingItemsList(
                items = infiniteQueryResult,
                key = { items, index -> items[index]?.messageUid?.toString() ?: "${items}_$index" }
            ) { list, index ->
                MessageListItem.create {
                    message = list[index]
                    previousMessage = list.getOrNull(index + 1)
                    activeUserUid = props.uiState.activePersonUid
                    timeFormatter = timeFormatterVal
                    dateFormatter = dateFormatterVal
                    listUiState = props.uiState
                }
            }
        }

        Container {
            VirtualListOutlet()
        }
    }

    Container {
        ref = newMessageBoxRef

        UstadSendTextField {
            value = props.uiState.newMessageText
            fullWidth = true
            onTextChange = props.onChangeNewMessageText
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
        onWarningDismiss = viewModel::onWarningDismiss
    }
}
