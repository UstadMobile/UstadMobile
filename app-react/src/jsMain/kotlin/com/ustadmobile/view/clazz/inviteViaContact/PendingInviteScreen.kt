package com.ustadmobile.view.clazz.inviteViaContact

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.PendingInviteUiState
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.PendingInviteViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import kotlinx.coroutines.flow.emptyFlow
import mui.material.List
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useMemo
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct

val PendingInviteScreen = FC<Props> {

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        PendingInviteViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(PendingInviteUiState())


    PendingInviteComponent {
        uiState = uiStateVal
        onClickRevokeInvite = viewModel::onClickRevokeInvite
    }

}

external interface PendingInviteProps: Props {

    var uiState: PendingInviteUiState

    var onClickRevokeInvite: (String) -> Unit

}

val PendingInviteComponent = FC<PendingInviteProps> { props ->

    val refreshFlow = useMemo(dependencies = emptyArray()) {
        emptyFlow<RefreshCommand>()
    }

    val mediatorResult = useDoorRemoteMediator(
        props.uiState.pendingInviteList, refreshFlow
    )

    val infiniteQueryResult = usePagingSource(
        mediatorResult.pagingSourceFactory, true
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
                key = { "${it.ciUid}" }
            ) { item ->
                ListItem.create{
                    disablePadding = true
                    key = "1"
                    ListItemButton {
                        ListItemText {
                            primary = ReactNode(item?.inviteContact)
                        }
                    }

                }
            }
        }

        UstadStandardContainer {
            List {
                VirtualListOutlet()
            }
        }
    }

}
