package com.ustadmobile.view.message.conversationlist

import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListUiState
import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListViewModel
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.MessageAndOtherPerson
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.core.jso
import mui.material.Container
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useState
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import com.ustadmobile.hooks.useFormattedTime
import emotion.react.css
import react.dom.html.ReactHTML.span
import web.cssom.Display
import web.cssom.TextOverflow
import web.cssom.WhiteSpace

external interface ConversationListScreenProps : Props {

    var uiState: ConversationListUiState

    var onClickEntry: (MessageAndOtherPerson) -> Unit

}


private val ConversationListScreenComponent2 = FC<ConversationListScreenProps> { props ->

    val infiniteQueryResult = usePagingSource(
        props.uiState.conversations, true, 50
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
                key = { (it.message?.messageUid).toString() }
            ) { messageItem ->
                ConversationItem.create {
                    message = messageItem
                    onListItemClick = props.onClickEntry
                }
            }
        }

        Container {
            VirtualListOutlet()
        }
    }
}

val demoConversationList = (0..150).map {
    MessageAndOtherPerson().apply {
        message = Message().apply {
            messageText = "The Conversation number $it"
        }
        otherPerson = Person().apply {
            firstNames = "Person $it"
            lastName = "$it"
            personUid = it.toLong()
        }
    }
}

val ConversationListScreenPreview = FC<Props> {


    val conversationListUiState by useState {
        ConversationListUiState(
            conversations = { ListPagingSource(demoConversationList) }
        )
    }

    ConversationListScreenComponent2 {
        uiState = conversationListUiState
    }
}

val ConversationListScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ConversationListViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(ConversationListUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab {
        fabState = appState.fabState
    }

    ConversationListScreenComponent2 {
        uiState = uiStateVal
        onClickEntry = viewModel::onClickEntry
    }
}

external interface ConversationItemProps: Props {

    var message: MessageAndOtherPerson?

    var onListItemClick: (MessageAndOtherPerson) -> Unit

}

val ConversationItem = FC<ConversationItemProps> { props ->

    val formattedTime = useFormattedTime(
        timeInMillisSinceMidnight = (props.message?.message?.messageTimestamp ?: 0).toInt(),
    )

    ListItem {
        ListItemButton{
            onClick = {
                props.message?.also { props.onListItemClick(it) }
            }

            ListItemIcon {
                UstadPersonAvatar {
                    pictureUri = props.message?.personPicture?.personPictureThumbnailUri
                    personName = props.message?.otherPerson?.fullName()
                }
            }

            ListItemText {
                primary = ReactNode("${props.message?.otherPerson?.fullName()}")
                secondary = span.create {
                    css {
                        display = Display.inlineBlock
                        whiteSpace = WhiteSpace.nowrap
                        overflow = Overflow.hidden
                        width = 100.pct
                        textOverflow = TextOverflow.ellipsis
                    }
                    + "${props.message?.message?.messageText}"
                }
            }
        }
        secondaryAction = ReactNode(formattedTime)
    }
}
