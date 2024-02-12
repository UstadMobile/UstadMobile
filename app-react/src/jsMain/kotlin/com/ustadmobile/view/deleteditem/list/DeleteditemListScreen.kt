package com.ustadmobile.view.deleteditem.list

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.deleteditem.DeletedItemListUiState
import com.ustadmobile.core.viewmodel.deleteditem.DeletedItemListViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.core.jso
import mui.material.Container
import react.FC
import react.Props
import react.create
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct

external interface DeletedItemListProps: Props {
    var uiState: DeletedItemListUiState
}

val DeletedItemListComponent = FC<DeletedItemListProps> { props ->
    val muiAppState = useMuiAppState()

    val infiniteQueryResult = usePagingSource(
        pagingSourceFactory = props.uiState.deletedItemsList,
        placeholdersEnabled = true
    )

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
                key = { it.delItemUid.toString() }
            ) {
                DeletedItemListItem.create {
                    item = it
                }
            }
        }

        Container {
            mui.material.List {
                VirtualListOutlet()
            }
        }
    }
}



val DeletedItemListScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        DeletedItemListViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(DeletedItemListUiState())

    DeletedItemListComponent {
        uiState = uiStateVal
    }
}

