package com.ustadmobile.libuicompose.view.deleteditem.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.MR
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.deleteditem.DeletedItemListUiState
import com.ustadmobile.core.viewmodel.deleteditem.DeletedItemListViewModel
import com.ustadmobile.lib.db.entities.DeletedItem
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.Flow

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
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onClickRestore: (DeletedItem) -> Unit = { },
    onClickDeletePermanently: (DeletedItem) -> Unit = { },
) {
    val repositoryResult = rememberDoorRepositoryPager(
        uiState.deletedItemsList, refreshCommandFlow
    )

    val lazyPagingItems = repositoryResult.lazyPagingItems

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
