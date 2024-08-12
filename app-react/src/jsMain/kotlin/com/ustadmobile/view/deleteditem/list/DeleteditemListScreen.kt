package com.ustadmobile.view.deleteditem.list

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.deleteditem.DeletedItemListUiState
import com.ustadmobile.core.viewmodel.deleteditem.DeletedItemListViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.DeletedItem
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import mui.material.Button
import mui.material.Container
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogContentText
import react.FC
import react.Props
import react.create
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useEmptyFlow
import kotlinx.coroutines.flow.Flow

external interface DeletedItemListProps: Props {
    var uiState: DeletedItemListUiState

    var refreshCommandFlow: Flow<RefreshCommand>?

    var onClickRestore: (DeletedItem) -> Unit

    var onClickDeletePermanently: (DeletedItem) -> Unit

}

val DeletedItemListComponent = FC<DeletedItemListProps> { props ->
    val muiAppState = useMuiAppState()

    val emptyRefreshCommandFlow = useEmptyFlow<RefreshCommand>()

    val mediatorResult = useDoorRemoteMediator(
        props.uiState.deletedItemsList, props.refreshCommandFlow ?: emptyRefreshCommandFlow
    )

    val infiniteQueryResult = usePagingSource(
        pagingSourceFactory = mediatorResult.pagingSourceFactory,
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
                    onClickRestore = props.onClickRestore
                    onClickDeletePermanently = props.onClickDeletePermanently
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
    val strings = useStringProvider()
    val uiStateVal by viewModel.uiState.collectAsState(DeletedItemListUiState())

    Dialog {
        open = uiStateVal.confirmDialogVisible
        onClose = { _, _ ->
            viewModel.onDismissConfirmDialog()
        }

        DialogContent {
            DialogContentText {
                + (uiStateVal.deleteConfirmText ?: "")
            }
        }

        DialogActions {
            Button {
                id = "cancel_delete_button"
                onClick = {
                    viewModel.onDismissConfirmDialog()
                }

                + strings[MR.strings.cancel]
            }

            Button {
                id = "confirm_delete_button"
                onClick = {
                    viewModel.onConfirmDeletePermanently()
                }

                + strings[MR.strings.confirm]
            }
        }
    }

    DeletedItemListComponent {
        uiState = uiStateVal
        onClickRestore = viewModel::onClickRestore
        onClickDeletePermanently = viewModel::onClickDeletePermanently
    }
}

