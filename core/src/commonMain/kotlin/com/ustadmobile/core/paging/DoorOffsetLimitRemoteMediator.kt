package com.ustadmobile.door.paging

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadParamsRefresh
import com.ustadmobile.door.ext.concurrentSafeListOf
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

/**
 *
 */
class DoorOffsetLimitRemoteMediator<Value: Any>(
    private val pagingSource: () -> PagingSource<Int, Value>
) {

    data class LoadedRange(
        val offset: Int,
        val limit: Int,
        val time: Long,
    )

    private val loadedRanges = concurrentSafeListOf<LoadedRange>()

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val prefetchDistance = 100

    private val defaultTtl = 5_000

    @Volatile
    private var endOfPaginationReached: Boolean? = null

    /**
     * Must be invoked when the underlying PagingSource is invoked
     */
    @Suppress("CAST_NEVER_SUCCEEDS")
    fun onLoad(params: PagingSourceLoadParams<Int>) {
        val pagingOffset = getOffset(params, (params.key ?: 0), Int.MAX_VALUE)
        val pagingLimit: Int = getLimit(params, (params.key ?: 0))

        /*
         * Set a range for the items that we want to have in the local database, expanding the
         * paging source load params by prefetchDistance
         */
        val rangeOffset = maxOf(0,pagingOffset - prefetchDistance)

        //The range offset may (or may not if it is already zero) be lower than the paging
        val rangeLimit = pagingLimit + (pagingOffset - rangeOffset) + prefetchDistance

        //Improvement to be made - trim the load range based on items already recently loaded.

        val pagingSourceInstance = pagingSource()
        if(pagingSourceInstance is DoorRepositoryReplicatePullPagingSource<*>) {
            scope.launch {
                Napier.d { "DoorOffsetLimitRemoteMediator: loadHttp from offset=$rangeOffset limit=$rangeLimit" }
                pagingSourceInstance.loadHttp(
                    PagingSourceLoadParamsRefresh(rangeOffset, rangeLimit, false) as PagingSourceLoadParams<Int>
                )
            }
        }else {
            Napier.d { "DoorOffsetLimitRemoteMediator: not a DoorRepositoryReplicatePullPagingSource" }
        }
    }

    fun cancel() {

    }

}