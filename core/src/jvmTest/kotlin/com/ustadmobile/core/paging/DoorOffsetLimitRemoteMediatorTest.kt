package com.ustadmobile.core.paging

import app.cash.paging.PagingSourceLoadParamsRefresh
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.mockito.kotlin.mock
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoMoreInteractions
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DoorOffsetLimitRemoteMediatorTest {

    private lateinit var scope: CoroutineScope

    @BeforeTest
    fun setup() {
        scope = CoroutineScope(Dispatchers.Default + Job())
    }

    @AfterTest
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun givenNothingLoadedBefore_whenInvoked_thenWillFetchRangePlusPrefetch() {
        val onRemoteLoadMock: DoorOffsetLimitRemoteMediator.OnRemoteLoad = mock {  }
        val mediator = DoorOffsetLimitRemoteMediator(
            prefetchDistance = 100,
            scope =scope,
            onRemoteLoad = onRemoteLoadMock
        )

        mediator.onLoad(PagingSourceLoadParamsRefresh(0, 50, false))
        verifyBlocking(onRemoteLoadMock, timeout(1000)) {
            invoke(0, 150)
        }
    }

    @Test
    fun givenInitialRangeLoadedBefore_whenInvoked_thenWillFetchAdditional() {
        val onRemoteLoadMock: DoorOffsetLimitRemoteMediator.OnRemoteLoad = mock {  }
        val mediator = DoorOffsetLimitRemoteMediator(
            prefetchDistance = 100,
            scope =scope,
            onRemoteLoad = onRemoteLoadMock
        )

        mediator.onLoad(PagingSourceLoadParamsRefresh(0, 50, false))
        verifyBlocking(onRemoteLoadMock, timeout(1000)) {
            invoke(0, 150)
        }

        //Will now load the remaining items e.g. items 150-209 (inclusive)
        mediator.onLoad(PagingSourceLoadParamsRefresh(60, 50, false))
        verifyBlocking(onRemoteLoadMock, timeout(1000)) {
            invoke(150, 60)
        }
    }

    @Test
    fun givenEndOfRangeLoadedBefore_whenInvoked_thenWillFetchAdditional() {
        val onRemoteLoadMock: DoorOffsetLimitRemoteMediator.OnRemoteLoad = mock {  }
        val mediator = DoorOffsetLimitRemoteMediator(
            prefetchDistance = 100,
            scope = scope,
            onRemoteLoad = onRemoteLoadMock
        )

        //Will load from 500-649
        mediator.onLoad(PagingSourceLoadParamsRefresh(600, 50, false))
        verifyBlocking(onRemoteLoadMock, timeout(1000)) {
            invoke(500, 250)
        }

        //Should load from 350 to 499
        mediator.onLoad(PagingSourceLoadParamsRefresh(450, 50, false))
        verifyBlocking(onRemoteLoadMock, timeout(1000)) {
            invoke(350, 150)
        }
    }

    @Test
    fun givenRangeLoadedBefore_whenPrefetchDoesNotExceedThreshold_thenWillNotLoadAgain() {
        val onRemoteLoadMock: DoorOffsetLimitRemoteMediator.OnRemoteLoad = mock {  }
        val mediator = DoorOffsetLimitRemoteMediator(
            prefetchDistance = 100,
            prefetchThreshold = 50,
            scope =scope,
            onRemoteLoad = onRemoteLoadMock
        )

        mediator.onLoad(PagingSourceLoadParamsRefresh(0, 50, false))
        verifyBlocking(onRemoteLoadMock, timeout(1000)) {
            invoke(0, 150)
        }

        mediator.onLoad(PagingSourceLoadParamsRefresh(20, 50, false))
        Thread.sleep(100)
        verifyNoMoreInteractions(onRemoteLoadMock)
    }




}