package com.ustadmobile.libuicompose.view.deleteditem.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.compose.collectAsLazyPagingItems
import com.ustadmobile.core.viewmodel.deleteditem.DeletedItemListUiState
import com.ustadmobile.core.viewmodel.deleteditem.DeletedItemListViewModel
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.lib.db.entities.DeletedItem

@Composable
fun DeletedItemListScreen(
    viewModel: DeletedItemListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(DeletedItemListUiState())

    DeletedItemListScreen(
        uiState = uiState
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
