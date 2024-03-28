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
import com.ustadmobile.door.paging.DoorRepositoryReplicatePullPagingSource
import com.ustadmobile.door.paging.PagingSourceInterceptor
import com.ustadmobile.door.util.systemTimeInMillis
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

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
    var currentPagingSource: PagingSource<*, *>? by remember {
        mutableStateOf(null)
    }

    var pagingSourceFactoryState by remember {
        mutableStateOf(pagingSourceFactory)
    }

    val offsetLimitMediator = remember {
        DoorOffsetLimitRemoteMediator(
            onRemoteLoad = { offset, limit ->
                Napier.v { "rememberDoorRepositoryPager: fetch remote offset=$offset limit=$limit" }
                (currentPagingSource as? DoorRepositoryReplicatePullPagingSource)?.loadHttp(
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
    LaunchedEffect(currentPagingSource) {
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
                        currentPagingSource = pagingSource
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
            currentPagingSource?.invalidate()
        }
    }

    return DoorRepositoryPagerResult(pager, lazyPagingItems)
}
