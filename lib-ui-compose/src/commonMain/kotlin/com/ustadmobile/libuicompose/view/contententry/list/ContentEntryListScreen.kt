package com.ustadmobile.libuicompose.view.contententry.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListUiState
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.libuicompose.view.contententry.UstadContentEntryListItem

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

    // TODO error
//    val pager = remember(uiState.contentEntryList) {
//        Pager(
//            pagingSourceFactory = uiState.contentEntryList,
//            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
//        )
//    }
//    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()


    LazyColumn(
        modifier = Modifier.fillMaxSize()
    )  {

//        items(
//            items = lazyPagingItems,
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