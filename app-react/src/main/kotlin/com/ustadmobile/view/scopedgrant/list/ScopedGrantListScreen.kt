package com.ustadmobile.view.scopedgrant.list

import com.ustadmobile.core.controller.ScopedGrantEditPresenter.Companion.PERMISSION_LIST_MAP
import com.ustadmobile.core.controller.ScopedGrantEditPresenter.Companion.PERMISSION_MESSAGE_ID_LIST
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.core.view.ScopedGrantListView
import com.ustadmobile.core.viewmodel.scopedgrant.list.ScopedGrantListUiState
import com.ustadmobile.core.viewmodel.scopedgrant.list.ScopedGrantListViewModel
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.ScopedGrantEntityAndName
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.mui.components.UstadAddListItem
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.Contain
import csstype.Height
import csstype.Overflow
import csstype.pct
import js.core.jso
import mui.icons.material.Add
import mui.icons.material.Lock
import mui.material.Container
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.router.useLocation

external interface ScopedGrantListProps: Props {
    var uiState: ScopedGrantListUiState
    var onSortOrderChanged: (SortOrderOption) -> Unit
    var onListItemClick: (ScopedGrantEntityAndName) -> Unit
    var onClickAddItem: () -> Unit
}

val ScopedGrantListComponent = FC<ScopedGrantListProps> { props ->

    val strings = useStringsXml()

    val infiniteQueryResult = usePagingSource(
        props.uiState.scopeGrantList, true, 50
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
                UstadListSortHeader.create {
                    activeSortOrderOption = props.uiState.sortOption
                    sortOptions = props.uiState.sortOptions
                    enabled = true
                    onClickSort = {
                        props.onSortOrderChanged(it)
                    }
                }
            }

            if (props.uiState.showAddItem) {
                item {
                    UstadAddListItem.create {
                        text = strings[MessageID.add_new]
                        onClickAdd = props.onClickAddItem
                    }
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.scopedGrant?.sgUid.toString() }
            ) { scopedGrant ->
                ListItem.create {
                    ListItemButton {
                        onClick = {
                            scopedGrant?.also { props.onListItemClick(it) }
                        }

                        //TODO: Figure icon
                        ListItemIcon {
                            +Lock.create()
                        }

                        ListItemText {
                            primary = ReactNode(scopedGrant?.name ?: "")
                            secondary = ReactNode(
                                getPermissionAsText(
                                    scopedGrant?.scopedGrant?.sgPermissions?:0L))
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

fun getPermissionAsText(permission: Long): String{

    val enabledPermissions = MutableLiveData(PERMISSION_MESSAGE_ID_LIST.map{
        it.toBitmaskFlag(permission)
    }.filter { it.enabled })

    val permissionList = enabledPermissions.getValue()

    val strings = useStringsXml()
    val messageIds = permissionList?.map { it.messageId }
    val messageIdsStrings: List<String>? = permissionList?.map { strings[it.messageId] }

    val text = messageIdsStrings?.joinToString(" ")

    return text?:""

}

val ScopedGrantListScreen = FC<Props> {
    val location = useLocation()
    val viewModel = useUstadViewModel{di, savedStateHandle ->
        ScopedGrantListViewModel(di, savedStateHandle, location.ustadViewName)
    }

    val uiState: ScopedGrantListUiState by viewModel.uiState.collectAsState(ScopedGrantListUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab{
        fabState = appState.fabState
    }

    ScopedGrantListComponent{
        this.uiState = uiState
        onListItemClick = viewModel::onClickEntry
        onSortOrderChanged = viewModel::onSortOrderChanged
        onClickAddItem = viewModel::onClickAdd
    }
}


val testList = (0..150).map {
    ScopedGrantEntityAndName().apply {
        name = "Person $it"
        scopedGrant = ScopedGrant().apply {

        }
    }
}

val ScopedGrantListScreenPreview = FC<Props> { props ->
    ScopedGrantListComponent{
        + props
        uiState = ScopedGrantListUiState(
            scopeGrantList = { ListPagingSource(testList) }
        )
    }
}
