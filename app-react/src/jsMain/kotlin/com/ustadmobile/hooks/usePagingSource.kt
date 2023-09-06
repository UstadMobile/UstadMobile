package com.ustadmobile.hooks

import com.ustadmobile.core.hooks.useCoroutineScope
import com.ustadmobile.door.paging.*
import com.ustadmobile.door.util.systemTimeInMillis
import js.core.jso
import kotlinx.coroutines.promise
import react.useEffect
import react.useMemo
import react.useState
import tanstack.query.core.QueryFunctionContext
import tanstack.query.core.QueryKey
import tanstack.react.query.UseInfiniteQueryResult
import tanstack.react.query.useInfiniteQuery
import js.promise.Promise


/**
 * Use a Paging Source through using TanStack's useInfiniteQuery. Returns InfiniteQueryResult.
 *
 * Automatically updates when the PagingSourceFactory changes and when the PagingSource is
 * invalidated.
 *
 * @param pagingSourceFactory Factory that should provide a paging source.
 * @param placeholdersEnabled as per Tanstack
 * @param loadSize initial load size
 *
 * @return UseInfiniteQueryResult
 */
fun <Key: Any, Value: Any> usePagingSource(
    pagingSourceFactory: () -> PagingSource<Key, Value>,
    placeholdersEnabled: Boolean,
    loadSize: Int = 50,
) : UseInfiniteQueryResult<LoadResult<Key, Value>, Throwable> {
    //If the factory itself changes, that will change the TanStack Query Key, forces reload
    val factoryQueryKey: String = useMemo(pagingSourceFactory) {
        ""+pagingSourceFactory.hashCode()
    }

    var invalidationCount: Int by useState { 0 }

    val (pagingSourceTimestamp, pagingSource) = useMemo(factoryQueryKey, invalidationCount) {
        systemTimeInMillis() to pagingSourceFactory()
    }

    var infiniteQueryTimeStamp by useState { pagingSourceTimestamp }

    //Listen for invalidation
    useEffect(pagingSource) {
        val invalidationListener: () -> Unit = {
            invalidationCount++
        }

        pagingSource.registerInvalidatedCallback(invalidationListener)

        cleanup {
            pagingSource.unregisterInvalidatedCallback(invalidationListener)
        }
    }

    val coroutineScope = useCoroutineScope(dependencies = emptyArray())

    val infiniteQueryResult = useInfiniteQuery<LoadResult<Key, Value>, Throwable, LoadResult<Key, Value>, QueryKey>(
        options = jso {
            queryKey = QueryKey(factoryQueryKey)
            queryFn = { queryContext: QueryFunctionContext<QueryKey, *> ->
                val loadParams = when(queryContext.pageParam) {
                    is LoadParams.Append<*> -> queryContext.pageParam.unsafeCast<LoadParams.Append<Key>>()
                    is LoadParams.Prepend<*> -> queryContext.pageParam.unsafeCast<LoadParams.Prepend<Key>>()
                    is LoadParams.Refresh<*> -> queryContext.pageParam.unsafeCast<LoadParams.Refresh<Key>>()
                    else -> LoadParams.Refresh<Key>(null, loadSize, placeholdersEnabled)
                }

                coroutineScope.promise {
                    pagingSource.load(loadParams)
                }.unsafeCast<Promise<LoadResult<Key, Value>>>()
            }
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

    useEffect(pagingSourceTimestamp) {
        if(pagingSourceTimestamp != infiniteQueryTimeStamp) {
            infiniteQueryTimeStamp = pagingSourceTimestamp
            infiniteQueryResult.refetch(jso {  })
        }
    }

    return infiniteQueryResult
}
