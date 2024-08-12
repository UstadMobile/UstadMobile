package com.ustadmobile.hooks

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParamsRefresh
import com.ustadmobile.core.hooks.useLaunchedEffect
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.door.paging.DoorOffsetLimitRemoteMediator
import com.ustadmobile.door.paging.PagingSourceInterceptor
import kotlinx.coroutines.flow.Flow
import react.useMemo
import react.useState
import com.ustadmobile.door.paging.PagingSourceWithHttpLoader
import com.ustadmobile.door.util.systemTimeInMillis
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.filter
import react.useEffect

data class DoorRemoteMediatorResult<T: Any>(
    val pagingSourceFactory: ListPagingSourceFactory<T>,
    val mediatorState: DoorOffsetLimitRemoteMediator.OffsetLimitMediatorState,
)
/**
 * Use DoorOffsetLimitMediator with a given pagingSourceFactory to trigger loading remote data as
 * required.
 *
 * This returns a result with new ListPagingSourceFactory that returns the original PagingSource
 * wrapped with a PagingSourceInterceptor, which will invoke the OffsetLimitMediator to load remote
 * data as required.
 *
 * @param pagingSourceFactory ListPagingSourceFactory (e.g. as provided by the ViewModel)
 * @param refreshCommandFlow a flow of refresh commands that will be collected (triggers
 *        invalidation of both the pagingsource and mediator).
 */
fun <T:Any> useDoorRemoteMediator(
    pagingSourceFactory: ListPagingSourceFactory<T>,
    refreshCommandFlow: Flow<RefreshCommand>,
    refreshCommandTimeout: Long = 2_000,
): DoorRemoteMediatorResult<T> {
    /* This can't be done via useState because it would be set asynchronously. That would cause a
     * problem because:
     *  a) PagingSourceInterceptor would try and set the pagingSource state variable
     *  b) It might not be set when offsetLimitMediator.onRemoteLoad is called, so then the required
     *     http request would not be made.
     * This is safe because the PagingSource is not used when rendering and emitting elements. It is
     * only accessed by the offsetLimitMediator, which would be invoked by TanStack.
     */
    val pagingSourceRef = useMemo(dependencies = emptyArray()) {
        mutableListOf<PagingSource<Int, T>?>(null)
    }

    val offsetLimitMediator = useMemo(dependencies = emptyArray()) {
        DoorOffsetLimitRemoteMediator(
            onRemoteLoad = { offset, limit ->
                val currentPagingSourceVal = pagingSourceRef.first()
                val pagingSourceType = currentPagingSourceVal?.let {
                    it::class.simpleName
                }

                Napier.v { "useDoorRemoteMediator: fetch remote offset=$offset limit=$limit pagingSourceType=$pagingSourceType" }
                (currentPagingSourceVal as? PagingSourceWithHttpLoader<Int>)?.loadHttp(
                    PagingSourceLoadParamsRefresh(offset, limit, false)
                )
            }
        )
    }

    useEffect(dependencies = emptyArray()) {
        cleanup {
            offsetLimitMediator.cancel()
        }
    }

    val pagingSourceWithIntercept: ListPagingSourceFactory<T> = useMemo(pagingSourceFactory) {
        {
            PagingSourceInterceptor(
                src = pagingSourceFactory().also {
                    Napier.v { "useDoorRemoteMediator: set paging source to ${it::class.simpleName}" }
                    pagingSourceRef[0] = it
                    offsetLimitMediator.invalidate()
                },
                onLoad = {
                    Napier.v { "useDoorRemoteMediator: load" }
                    offsetLimitMediator.onLoad(it)
                }
            )
        }
    }

    useLaunchedEffect(refreshCommandFlow) {
        refreshCommandFlow.filter {
            systemTimeInMillis() - it.time < refreshCommandTimeout
        }.collect {
            Napier.v { "useDoorRemoteMediator: refresh" }
            offsetLimitMediator.invalidate()
            pagingSourceRef.first()?.invalidate()
        }
    }

    var mediatorState by useState { DoorOffsetLimitRemoteMediator.OffsetLimitMediatorState() }

    useLaunchedEffect(dependencies = emptyArray()) {
        offsetLimitMediator.state.collect {
            mediatorState = it
        }
    }

    return DoorRemoteMediatorResult(
        pagingSourceFactory = pagingSourceWithIntercept,
        mediatorState = mediatorState,
    )
}
