package com.ustadmobile.libuicompose.view.deleteditem.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.compose.collectAsLazyPagingItems
import com.ustadmobile.core.viewmodel.deleteditem.DeletedItemListUiState
import com.ustadmobile.core.viewmodel.deleteditem.DeletedItemListViewModel
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.lib.db.entities.DeletedItem
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@Composable
fun DeletedItemListScreen(
    viewModel: DeletedItemListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(DeletedItemListUiState())

    if(uiState.confirmDialogVisible) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissConfirmDialog,
            text = {
                Text(uiState.deleteConfirmText ?: "")
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onConfirmDeletePermanently,
                    modifier = Modifier.testTag("confirm_delete_permanently_button")
                ) {
                    Text(stringResource(MR.strings.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::onDismissConfirmDialog,
                    modifier = Modifier.testTag("cancel_delete_button")
                ) {
                    Text(stringResource(MR.strings.cancel))
                }
            }
        )
    }

    DeletedItemListScreen(
        uiState = uiState,
        onClickRestore = viewModel::onClickRestore,
        onClickDeletePermanently = viewModel::onClickDeletePermanently,
    )
}

@Composable
fun DeletedItemListScreen(
    uiState: DeletedItemListUiState,
    onClickRestore: (DeletedItem) -> Unit = { },
    onClickDeletePermanently: (DeletedItem) -> Unit = { },
) {
    val pager = remember(uiState.deletedItemsList) {
        Pager(
            pagingSourceFactory = uiState.deletedItemsList,
            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { it.delItemUid }
        ) { deletedItem ->
            DeletedItemListItem(
                deletedItem = deletedItem,
                onClickRestore = onClickRestore,
                onClickDeletePermanently = onClickDeletePermanently,
            )
        }
    }
}
