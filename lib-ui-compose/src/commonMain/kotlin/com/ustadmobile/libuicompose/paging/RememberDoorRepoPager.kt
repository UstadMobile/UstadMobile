package com.ustadmobile.libuicompose.paging

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.Pager
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import app.cash.paging.PagingConfig
import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParamsRefresh
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.door.paging.DoorOffsetLimitRemoteMediator
import com.ustadmobile.door.paging.PagingSourceInterceptor
import com.ustadmobile.door.paging.PagingSourceWithHttpLoader
import com.ustadmobile.door.util.systemTimeInMillis
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import java.util.concurrent.atomic.AtomicReference

class DoorRepositoryPagerResult<T : Any>(
    val pager: Pager<Int, T>,
    val lazyPagingItems: LazyPagingItems<T>,
)

/**
 * Use DoorOffsetLimitRemoteMediator to trigger remote paged loads as required
 */
@Composable
fun <T: Any> rememberDoorRepositoryPager(
    pagingSourceFactory: () -> PagingSource<Int, T>,
    refreshCommandFlow: Flow<RefreshCommand>,
    config: PagingConfig = PagingConfig(20, maxSize = 200),
    refreshCommandTimeout: Long = 2_000,
): DoorRepositoryPagerResult<T> {

    /**
     * When mutableState is used, then the state is set asynchronously. This could (in theory)
     * be a problem because if the currentPagingSource is set by the interceptor, but not yet
     * taken effect when the onRemoteLoad function is called, then the onRemoteLoad will missed
     * or possibly invoked on the wrong object.
     *
     * currentPagingSource is not used as part of the layout. It is used only by events that happen
     * outside the composition, so there is no safety issue with setting it immediately.
     */
    val currentPagingSource: AtomicReference<PagingSource<*, *>?> = remember {
        AtomicReference(null)
    }

    var pagingSourceFactoryState by remember {
        mutableStateOf(pagingSourceFactory)
    }

    //Unchecked cast is unavoidable here, and will always be correct.
    @Suppress("UNCHECKED_CAST")
    val offsetLimitMediator = remember {
        DoorOffsetLimitRemoteMediator(
            onRemoteLoad = { offset, limit ->
                Napier.v { "rememberDoorRepositoryPager: fetch remote offset=$offset limit=$limit" }
                (currentPagingSource.get() as? PagingSourceWithHttpLoader<Int>)?.loadHttp(
                    PagingSourceLoadParamsRefresh(offset, limit, false)
                )
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            offsetLimitMediator.cancel()
        }
    }

    /**
     * Detect when PagingSourceFactory has been changed. If that happens, then invalidate the
     * offsetLimitMediator.
     */
    LaunchedEffect(pagingSourceFactory) {
        if(pagingSourceFactoryState !== pagingSourceFactory) {
            Napier.v {
                "rememberDoorRepositoryPager: new pagingSourceFactory set, invalidating offset limit mediator"
            }
            offsetLimitMediator.invalidate()
            pagingSourceFactoryState = pagingSourceFactory
        }
    }


    val pager = remember(pagingSourceFactory) {
        Pager(
            config = config,
            pagingSourceFactory = {
                PagingSourceInterceptor(
                    src = pagingSourceFactory().also { pagingSource ->
                        currentPagingSource.set(pagingSource)
                    },
                    onLoad = {
                        offsetLimitMediator.onLoad(it)
                    }
                )
            },
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LaunchedEffect(refreshCommandFlow) {
        refreshCommandFlow.filter {
            systemTimeInMillis() - it.time < refreshCommandTimeout
        }.collect {
            Napier.d("rememberDoorRepositoryPager: refresh")
            //Normally, this would use lazyPagingItems.refresh, but that doesn't actually work.

            offsetLimitMediator.invalidate()
            currentPagingSource.get()?.invalidate()
        }
    }

    return DoorRepositoryPagerResult(pager, lazyPagingItems)
}
