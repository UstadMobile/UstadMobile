package com.ustadmobile.core.paging

import com.ustadmobile.door.paging.*
import kotlin.math.max
import kotlin.math.min
import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState

/**
 * Simple PagingSource based on a list. Useful for preview data and testing purposes.
 */
class ListPagingSource<Value: Any>(
    private val list: List<Value>,
): PagingSource<Int, Value>() {

    override fun getRefreshKey(state: PagingState<Int, Value>): Int? {
        return state.anchorPosition
    }

    @Suppress("CAST_NEVER_SUCCEEDS") //required as per the library docs
    override suspend fun load(params: PagingSourceLoadParams<Int>): PagingSourceLoadResult<Int, Value> {
        val startFrom = params.key ?: 0
        val loadSize = params.loadSize
        val subList = list.subList(params.key ?: 0,
            min(list.size, startFrom + loadSize))

        return PagingSourceLoadResultPage(
            data = subList,
            prevKey = if(startFrom > 0) {
                max(0, startFrom - loadSize)
            }else {
                null
            },
            nextKey = if((startFrom + loadSize) < list.size) {
                startFrom + loadSize
            }else {
                null
            },
        ) as PagingSourceLoadResult<Int, Value>
    }
}
