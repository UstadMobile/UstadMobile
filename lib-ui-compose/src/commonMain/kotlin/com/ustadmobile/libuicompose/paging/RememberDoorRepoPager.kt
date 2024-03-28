package com.ustadmobile.libuicompose.paging

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import app.cash.paging.PagingConfig
import app.cash.paging.PagingSource
import com.ustadmobile.core.paging.DoorOffsetLimitRemoteMediator
import com.ustadmobile.door.paging.PagingSourceInterceptor
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow

class DoorRepositoryPagerResult<T : Any>(
    val pager: Pager<Int, T>,
    val lazyPagingItems: LazyPagingItems<T>,
)

/**
 * Workaround
 */
@OptIn(ExperimentalPagingApi::class)
@Composable
fun <T: Any> rememberDoorRepositoryPager(
    pagingSourceFactory: () -> PagingSource<Int, T>,
    config: PagingConfig,
    refreshCommandFlow: Flow<Boolean>,
): DoorRepositoryPagerResult<T> {
    var currentPagingSource: PagingSource<*, *>? by remember {
        mutableStateOf(null)
    }

//    val offsetLimitMediator = remember(pagingSourceFactory) {
//        DoorOffsetLimitRemoteMediator(pagingSourceFactory)
//    }

    val pager = remember(pagingSourceFactory) {
        Pager(
            config = config,
            pagingSourceFactory = {
                PagingSourceInterceptor(
                    src = pagingSourceFactory(),
                    onLoad = {
                        //offsetLimitMediator.onLoad(it)
                    }
                ).also {
                    currentPagingSource = it
                }
            },
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    var refreshMediatorPending by remember {
        mutableStateOf(false)//User100
    }

    LaunchedEffect(refreshCommandFlow) {
        refreshCommandFlow.collect {
            Napier.d("rememberDoorRepositoryPager: refresh")
            //Normally, this would use lazyPagingItems.refresh, but that doesn't actually work.
            //TODO: Invalidate the offsetLimitMediator first e.g. its next request will not consider previous loads as cached.
            currentPagingSource?.invalidate()
            refreshMediatorPending = true
        }
    }

    return DoorRepositoryPagerResult(pager, lazyPagingItems)
}
