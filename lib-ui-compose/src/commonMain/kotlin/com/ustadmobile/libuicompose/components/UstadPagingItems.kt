package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.itemKey
import app.cash.paging.compose.LazyPagingItems


fun <T: Any> LazyGridScope.ustadPagedItems(
    pagingItems: LazyPagingItems<T>,
    key: (T) -> Any,
    itemContent: @Composable (T?) -> Unit
) {
    items(
        count = pagingItems.itemCount,
        key = pagingItems.itemKey {
            key(it)
        }
    ) { index ->
        itemContent(pagingItems[index])
    }

}

fun  <T: Any> LazyListScope.ustadPagedItems(
    pagingItems: LazyPagingItems<T>,
    key: (T) -> Any,
    itemContent: @Composable (T?) -> Unit
) {
    items(
        count = pagingItems.itemCount,
        key = pagingItems.itemKey {
            key(it)
        }
    ) { index ->
        itemContent(pagingItems[index])
    }
}