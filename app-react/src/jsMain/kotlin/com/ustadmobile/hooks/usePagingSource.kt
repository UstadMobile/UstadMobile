package com.ustadmobile.hooks

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParamsAppend
import app.cash.paging.PagingSourceLoadParamsPrepend
import app.cash.paging.PagingSourceLoadParamsRefresh
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultPage
import com.ustadmobile.core.hooks.useCoroutineScope
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
 * Approach: The TanStack query key is the hashcode of the PagingSourceFactory. If the QueryKey
 * changes this will cause flickering. Changing the PagingSourceFactory forces the query to run
 * again by changing the QueryKey.
 *
 * An invalidation callback will be registered whenever a PagingSource is created through
 * PagingSourceFactory. If invalidation occurs, we will call TanStack's refetch function to refresh
 * data.
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
) : UseInfiniteQueryResult<PagingSourceLoadResult<Key, Value>, Throwable> {
    console.log("PagingSource : usePagingSource key=${pagingSourceFactory.hashCode()}")

    var lastInvalidationTime: Long by useState { 0 }
    var lastRefreshedTime: Long by useState { 0 }

    val coroutineScope = useCoroutineScope(dependencies = emptyArray())
    var pagingSource: PagingSource<Key, Value>? by useState {
        null
    }

    val invalidationCallback: () -> Unit = useMemo(pagingSourceFactory) {
        {
            val timeNow = systemTimeInMillis()
            console.log("PagingSource: set last invalidation time = $timeNow")
            lastInvalidationTime = timeNow
        }
    }

    /*
     * Unregister the invalidation callback when
     * a) There is a new PagingSource, so we need to unregister the invalidation callback from the
     *    old PagingSource
     * b) Component is being unmounted
     */
    useEffect(pagingSource) {
        cleanup {
            console.log("PagingSource: remove invalidation listener for old source")
            pagingSource?.unregisterInvalidatedCallback(invalidationCallback)
        }
    }

    val infiniteQueryResult = useInfiniteQuery<PagingSourceLoadResult<Key, Value>, Throwable, PagingSourceLoadResult<Key, Value>, QueryKey>(
        options = jso {
            console.log("PagingSource jso key=${pagingSourceFactory.hashCode()}")
            queryKey = QueryKey(""+pagingSourceFactory.hashCode())
            queryFn = { queryContext: QueryFunctionContext<QueryKey, *> ->
                console.log("PagingSource(): running QueryFn return promise key=${pagingSourceFactory.hashCode()}")
                val loadParams = when(queryContext.pageParam) {
                    is PagingSourceLoadParamsAppend<*> -> queryContext.pageParam.unsafeCast<PagingSourceLoadParamsAppend<Key>>()
                    is PagingSourceLoadParamsPrepend<*> -> queryContext.pageParam.unsafeCast<PagingSourceLoadParamsPrepend<Key>>()
                    is PagingSourceLoadParamsRefresh<*> -> queryContext.pageParam.unsafeCast<PagingSourceLoadParamsRefresh<Key>>()
                    else -> PagingSourceLoadParamsRefresh<Key>(null, loadSize, placeholdersEnabled)
                }

                //Must use PagingSourceFactory itself here: TanStack query will remember this function
                //according to the QueryKey, so references would be to old data.
                coroutineScope.promise {
                    pagingSourceFactory().also {
                        pagingSource = it
                        it.registerInvalidatedCallback(invalidationCallback)
                    }.load(loadParams)
                }.unsafeCast<Promise<PagingSourceLoadResult<Key, Value>>>()
            }
            getNextPageParam = { lastPage: PagingSourceLoadResult<Key, Value>, allPages ->
                val nextKey = (lastPage as? PagingSourceLoadResultPage<Key, Value>)?.nextKey
                nextKey?.let { PagingSourceLoadParamsAppend(it, loadSize, placeholdersEnabled) }
                    ?: undefined.unsafeCast<Any>()
            }
            getPreviousPageParam = { firstPage, allPages ->
                val prevKey = (firstPage as? PagingSourceLoadResultPage<Key, Value>)?.prevKey
                prevKey?.let { PagingSourceLoadParamsPrepend(it, loadSize, placeholdersEnabled)}
                    ?: undefined.unsafeCast<Any>()
            }
        }
    )


    /*
     * Check if the PagingSource has been invalidated after the lastRefreshTime. If so, wait until
     * any current fetch is done, (e.g. infiniteQueryResult.isFetching is false), and then call
     * refetch.
     */
    useEffect(dependencies = arrayOf(lastInvalidationTime, infiniteQueryResult.isFetching, lastRefreshedTime)) {
        val dateUpdatedAt = infiniteQueryResult.dataUpdatedAt.toLong()
        console.log("PagingSource: isFetching = ${infiniteQueryResult.isFetching} lastInvalidationTime=$lastInvalidationTime updatedAt=$dateUpdatedAt refreshTime=$lastRefreshedTime")
        if(lastRefreshedTime < lastInvalidationTime && !infiniteQueryResult.isFetching) {
            lastRefreshedTime = systemTimeInMillis()
            console.log("PagingSource: refetch: isFetching = ${infiniteQueryResult.isFetching} lastInvalidationTime=$lastInvalidationTime")
            infiniteQueryResult.refetch(
                jso {

                }
            )
        }
    }

    return infiniteQueryResult
}
