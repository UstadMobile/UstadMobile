package com.ustadmobile.view.leavingreason.list

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.leavingreason.list.LeavingReasonListUiState
import com.ustadmobile.core.viewmodel.leavingreason.list.LeavingReasonListViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.mui.components.UstadAddListItem
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.Contain
import csstype.Height
import csstype.Overflow
import csstype.pct
import js.core.jso
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import mui.system.Container
import react.FC
import react.Props
import react.ReactNode
import react.useState
import react.create

external interface LeavingReasonListScreenProps: Props {

    var uiState : LeavingReasonListUiState

    var onClickLeavingReason: (LeavingReason) -> Unit

    var onClickAddLeavingReason: () -> Unit

}

val LeavingReasonListScreenComponent = FC<LeavingReasonListScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    val infiniteQueryResult = usePagingSource(
        props.uiState.leavingReasonList, true, 50
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


            item {
                UstadAddListItem.create {
                    text = strings[MessageID.add_leaving_reason]
                    onClickAdd = props.onClickAddLeavingReason
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.leavingReasonUid.toString() }
            ) { leavingReason ->
                ListItem.create {
                    ListItemButton{
                        onClick = {
                            leavingReason?.also { props.onClickLeavingReason(it) }
                        }

                        ListItemText {
                            primary = ReactNode(leavingReason?.leavingReasonTitle)
                        }
                    }
                }
            }
        }

        Container {
            VirtualListOutlet()
        }
    }

}

val LeavingReasonListScreenPreview = FC<Props> {
    var uiStateVar by useState {
        LeavingReasonListUiState()
    }
    LeavingReasonListScreenComponent {
        uiState = uiStateVar
        onClickLeavingReason = {}
    }
}

val LeavingReasonListScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        LeavingReasonListViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(LeavingReasonListUiState())

    LeavingReasonListScreenComponent {
        uiState = uiStateVal
        onClickLeavingReason = viewModel::onClickLeavingReason
        onClickAddLeavingReason = viewModel::onClickAdd
    }
}