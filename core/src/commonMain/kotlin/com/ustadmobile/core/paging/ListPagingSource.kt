package com.ustadmobile.core.paging

import com.ustadmobile.door.paging.*
import kotlin.math.max
import kotlin.math.min

/**
 * Simple PagingSource based on a list. Useful for preview data and testing purposes.
 */
class ListPagingSource<Value: Any>(
    private val list: List<Value>,
): PagingSource<Int, Value>() {
    override fun getRefreshKey(state: PagingState<Int, Value>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Value> {
        val startFrom = params.key ?: 0
        val loadSize = params.toDoorLoadParams().loadSize
        val subList = list.subList(params.key ?: 0,
            min(list.size, startFrom + loadSize))

        return DoorLoadResult.Page(
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
        ).toLoadResult()
    }
}
