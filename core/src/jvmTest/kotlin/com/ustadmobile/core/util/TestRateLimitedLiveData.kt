package com.ustadmobile.core.util

import com.ustadmobile.door.ChangeListenerRequest
import com.ustadmobile.door.room.RoomDatabase
import com.ustadmobile.door.lifecycle.Observer
import com.ustadmobile.door.ext.concurrentSafeListOf
import com.ustadmobile.door.room.InvalidationTracker
import com.ustadmobile.door.room.InvalidationTrackerObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import org.mockito.kotlin.*
import org.junit.Assert

class TestRateLimitedLiveData {


    @Test
    fun givenActiveObserver_whenTableChanges_thenShouldRefreshOncePerInterval() {
        val invalidationTrackerObservers = mutableListOf<InvalidationTrackerObserver>()
        val mockInvalidationTracker = mock<InvalidationTracker> {
            on { addObserver(any()) }.thenAnswer {
                invalidationTrackerObservers += (it.arguments.first() as InvalidationTrackerObserver)
                it.mock
            }
        }

        val mockDb = mock<RoomDatabase> {
            on { getInvalidationTracker() }.thenReturn(mockInvalidationTracker)
        }

        val callTimes = concurrentSafeListOf<Long>()
        val rateLimitedLiveData = RateLimitedLiveData<String>(mockDb, listOf("Test"), 1000) {
            callTimes += System.currentTimeMillis()
            "Hello World ${System.currentTimeMillis()}"
        }

        val observer = mock<Observer<String>> {  }

        rateLimitedLiveData.observeForever(observer)

        runBlocking {
            GlobalScope.launch {
                repeat(5) {
                    delay(100)
                    invalidationTrackerObservers.forEach {
                        it.onInvalidated(setOf("Test"))
                    }
                }
            }

            verify(observer, timeout(1500).times(2)).onChanged(any())
            Assert.assertEquals("Got expected number of call times", 2, callTimes.size)
            val callTimeWait = callTimes[1] - callTimes[0]
            Assert.assertTrue("Difference in call time is at least 1000ms",
                callTimeWait >= 1000)
        }
    }

}