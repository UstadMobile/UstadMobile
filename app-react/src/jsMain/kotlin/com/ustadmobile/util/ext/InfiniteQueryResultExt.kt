package com.ustadmobile.util.ext

import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultPage
import com.ustadmobile.hooks.DoorRemoteMediatorResult
import com.ustadmobile.view.components.virtuallist.pages
import tanstack.react.query.UseInfiniteQueryResult

fun <T: Any> UseInfiniteQueryResult<PagingSourceLoadResult<Int, T>, Throwable>.isSettledEmpty(
    mediatorResult: DoorRemoteMediatorResult<*>
): Boolean {
    return !isLoading && mediatorResult.mediatorState.loadingStarted
            && !pages().any {
                (it as? PagingSourceLoadResultPage<*, *>)?.data?.isNotEmpty() == true
            }
            && mediatorResult.mediatorState.loadingRangesInProgress.isEmpty()

}
