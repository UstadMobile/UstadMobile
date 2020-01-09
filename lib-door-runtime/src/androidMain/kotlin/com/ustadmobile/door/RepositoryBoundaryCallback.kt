package com.ustadmobile.door

import androidx.paging.PagedList
import com.github.aakira.napier.Napier
import kotlinx.coroutines.*

/**
 * This is a PagedList.boundaryCallback that automatically calls a given LoadHelper to load more data.
 */
class RepositoryBoundaryCallback<T>(val loadHelper: RepositoryLoadHelper<List<T>>): PagedList.BoundaryCallback<T>() {

    fun loadMore() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                loadHelper.doRequest()
            }catch(e: Exception) {
                Napier.e("Exception running loadHelper", e)
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