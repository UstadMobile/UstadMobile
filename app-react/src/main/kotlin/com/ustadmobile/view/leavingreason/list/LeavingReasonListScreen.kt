package com.ustadmobile.view.leavingreason.list

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.leavingreason.list.LeavingReasonListUiState
import com.ustadmobile.core.viewmodel.leavingreason.list.LeavingReasonListViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.mui.components.UstadAddListItem
import com.ustadmobile.view.components.UstadBlankIcon
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.Contain
import csstype.Height
import csstype.Margin
import csstype.Overflow
import csstype.pct
import csstype.px
import js.core.jso
import mui.material.IconButton
import mui.material.ListItem
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.system.Container
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.useState
import react.create
import mui.icons.material.Edit as EditIcon

external interface LeavingReasonListScreenProps: Props {

    var uiState : LeavingReasonListUiState

    var onEditLeavingReason: (LeavingReason) -> Unit

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

                    ListItemIcon{
                        sx {
                            margin = Margin(
                                left = 12.px,
                                right = 0.px,
                                top = 0.px,
                                bottom = 0.px,
                            )
                        }
                        + UstadBlankIcon.create()
                    }

                    ListItemText {
                        primary = ReactNode(leavingReason?.leavingReasonTitle)
                    }

                    secondaryAction = IconButton.create {
                        onClick = {_ ->
                            leavingReason?.also { props.onEditLeavingReason(it) }
                        }
                        + EditIcon.create()
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
    val uiStateVar by useState {
        LeavingReasonListUiState(
            leavingReasonList = { ListPagingSource(listOf(
                LeavingReason().apply {
                    leavingReasonUid = 0
                    leavingReasonTitle = "Moved"
                },
                LeavingReason().apply {
                    leavingReasonUid = 1
                    leavingReasonTitle = "Medical"
                },
                LeavingReason().apply {
                    leavingReasonUid = 2
                    leavingReasonTitle = "Transportation problem"
                }
            )) }
        )
    }
    LeavingReasonListScreenComponent {
        uiState = uiStateVar
    }
}

val LeavingReasonListScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        LeavingReasonListViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(LeavingReasonListUiState())

    LeavingReasonListScreenComponent {
        uiState = uiStateVal
        onEditLeavingReason = viewModel::onEditLeavingReason
        onClickAddLeavingReason = viewModel::onClickAdd
    }
}