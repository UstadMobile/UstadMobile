package com.ustadmobile.hooks

import com.ustadmobile.door.paging.*
import com.ustadmobile.lib.util.randomString
import js.core.jso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import react.useMemo
import tanstack.query.core.QueryKey
import tanstack.react.query.UseInfiniteQueryResult
import tanstack.react.query.useInfiniteQuery

fun <Key: Any, Value: Any> usePagingSource(
    pagingSource: PagingSource<Key, Value>,
    coroutineScope: CoroutineScope,
    placeholdersEnabled: Boolean,
    loadSize: Int = 50,
) : UseInfiniteQueryResult<LoadResult<Key, Value>, Throwable> {
    val instanceQueryKey: String = useMemo(pagingSource) {
        randomString(12)
    }

    val infiniteQueryResult: UseInfiniteQueryResult<LoadResult<Key, Value>, Throwable> = useInfiniteQuery(
        queryKey = QueryKey(instanceQueryKey),
        queryFn = { queryCtx ->
            coroutineScope.promise {
                val loadParams = when(queryCtx.pageParam) {
                    is LoadParams.Append<*> -> queryCtx.pageParam.unsafeCast<LoadParams.Append<Key>>()
                    is LoadParams.Prepend<*> -> queryCtx.pageParam.unsafeCast<LoadParams.Prepend<Key>>()
                    is LoadParams.Refresh<*> -> queryCtx.pageParam.unsafeCast<LoadParams.Refresh<Key>>()
                    else -> LoadParams.Refresh<Key>(null, loadSize, placeholdersEnabled)
                }

                pagingSource.load(loadParams)
            }
        },
        options = jso {
            getNextPageParam = { lastPage: LoadResult<Key, Value>, allPages ->
                val nextKey = (lastPage as? LoadResult.Page<Key, Value>)?.nextKey
                nextKey?.let { LoadParams.Append(it, loadSize, placeholdersEnabled) }
                    ?: undefined.unsafeCast<Any>()
            }
            getPreviousPageParam = { firstPage, allPages ->
                val prevKey = (firstPage as? LoadResult.Page<Key, Value>)?.prevKey
                prevKey?.let { LoadParams.Prepend(it, loadSize, placeholdersEnabled)}
                    ?: undefined.unsafeCast<Any>()
            }
        }
    )

    return infiniteQueryResult
}
