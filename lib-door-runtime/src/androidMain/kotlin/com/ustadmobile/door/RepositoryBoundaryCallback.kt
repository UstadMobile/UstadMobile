package com.ustadmobile.door

import androidx.paging.PagedList
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * This is a PagedList.boundaryCallback that automatically calls a given LoadHelper to load more data.
 */
class RepositoryBoundaryCallback<T>(val loadHelper: RepositoryLoadHelper<List<T>>): PagedList.BoundaryCallback<T>() {

    fun loadMore() {
        GlobalScope.launch {
            try {
                loadHelper.doRequest()
            }catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onZeroItemsLoaded() {
        loadMore()
    }

    override fun onItemAtEndLoaded(itemAtEnd: T) {
        loadMore()
    }
}