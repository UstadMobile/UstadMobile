package com.ustadmobile.hooks

import com.ustadmobile.door.paging.*
import js.core.jso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import react.useEffect
import react.useMemo
import react.useState
import tanstack.query.core.QueryKey
import tanstack.react.query.UseInfiniteQueryResult
import tanstack.react.query.useInfiniteQuery

fun <Key: Any, Value: Any> usePagingSource(
    pagingSourceFactory: () -> PagingSource<Key, Value>,
    queryKey: QueryKey,
    coroutineScope: CoroutineScope,
    placeholdersEnabled: Boolean,
    loadSize: Int = 50,
) : UseInfiniteQueryResult<LoadResult<Key, Value>, Throwable> {

    val pagingSource = useMemo(pagingSourceFactory) {
        pagingSourceFactory()
    }

    val infiniteQueryResult: UseInfiniteQueryResult<LoadResult<Key, Value>, Throwable> = useInfiniteQuery(
        queryKey = queryKey,
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

    var loadedPagingSource by useState { pagingSourceFactory }
    useEffect(pagingSourceFactory) {
        if(loadedPagingSource !== pagingSourceFactory) {
            loadedPagingSource = pagingSourceFactory
            infiniteQueryResult.refetch(jso {  })
        }
    }

    return infiniteQueryResult
}
