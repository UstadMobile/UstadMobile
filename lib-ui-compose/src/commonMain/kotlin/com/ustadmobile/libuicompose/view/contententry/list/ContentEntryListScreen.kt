package com.ustadmobile.libuicompose.view.contententry.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListUiState
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.libuicompose.view.contententry.UstadContentEntryListItem
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.libuicompose.components.ustadPagedItems
import androidx.compose.runtime.remember
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun ContentEntryListScreenForViewModel(
    viewModel: ContentEntryListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ContentEntryListUiState())

    ContentEntryListScreen(
        uiState = uiState,
        onClickContentEntry = viewModel::onClickEntry
    )

}

@Composable
fun ContentEntryListScreen(
    uiState: ContentEntryListUiState = ContentEntryListUiState(),
    onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
    ) -> Unit = {},
    onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
    ) -> Unit = {},
) {

    val pager = remember(uiState.contentEntryList) {
        Pager(
            pagingSourceFactory = uiState.contentEntryList,
            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
        )
    }
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()


    LazyColumn(
        modifier = Modifier.fillMaxSize()
    )  {

//        ustadPagedItems(
//            pagingItems = lazyPagingItems,
//            key = { contentEntry -> contentEntry.contentEntryUid }
//        ){ contentEntry ->
//
//            UstadContentEntryListItem(
//                onClick = {
//                    @Suppress("UNNECESSARY_SAFE_CALL")
//                    contentEntry?.also { onClickContentEntry(it) }
//                },
//                onClickDownload = {
//                    @Suppress("UNNECESSARY_SAFE_CALL")
//                    contentEntry?.also { onClickDownloadContentEntry(it) }
//                },
//                contentEntry = contentEntry
//            )
//        }
    }
}