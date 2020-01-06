package com.ustadmobile.door

import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class DoorLiveDataJdbcImplTest {

    @Test
    fun givenEmptyLiveData_whenActive_shouldCallFetchFnAndAddChangeListener() {
        val mockDb = mock<DoorDatabase>()
        val fetchFnCount = AtomicInteger()
        val fetchCountdownLatch = CountDownLatch(1)
        val liveDataJdbc = DoorLiveDataJdbcImpl<Int>(mockDb, listOf("magic")) {
            fetchFnCount.incrementAndGet()
            fetchCountdownLatch.countDown()
            42
        }

        liveDataJdbc.observeForever(mock())
        verify(mockDb, timeout(5000)).addChangeListener(argThat { tableNames.contains("magic") })
        fetchCountdownLatch.await(5, TimeUnit.SECONDS)
        Assert.assertEquals("FetchFn called once", 1, fetchFnCount.get())
    }

    @Test
    fun givenEmptyLiveData_whenInactive_shouldRemoveChangeListener() {
        val mockDb = mock<DoorDatabase>()
        val fetchFnCount = AtomicInteger()
        val liveDataJdbc = DoorLiveDataJdbcImpl<Int>(mockDb, listOf("magic")) {
            fetchFnCount.incrementAndGet()
            42
        }

        val mockObserver = mock<DoorObserver<Int>>()
        liveDataJdbc.observeForever(mockObserver)
        liveDataJdbc.removeObserver(mockObserver)

        verify(mockDb, timeout(5000)).addChangeListener(argThat { tableNames.contains("magic") })
        verify(mockDb, timeout(5000)).removeChangeListener(argThat { tableNames.contains("magic") })
    }

    @Test
    fun givenEmptyLiveData_whenDbChanges_shouldCallFetchFnAgain() {
        val mockDb = mock<DoorDatabase>() {
            on {addChangeListener(any())}.thenAnswer {invocation ->
                GlobalScope.launch {
                    delay(100)
                    val changeListenerRequest = invocation.arguments[0] as DoorDatabase.ChangeListenerRequest
                    changeListenerRequest.onChange(listOf("magic"))
                }
            }
        }

        val countdownLatch = CountDownLatch(2)
        val liveDataJdbc = DoorLiveDataJdbcImpl<Int>(mockDb, listOf("magic")) {
            countdownLatch.countDown()
            42
        }

        liveDataJdbc.observeForever(mock())
        countdownLatch.await(5, TimeUnit.SECONDS)
        Assert.assertEquals("Fetch function was called again after db callback to indicate table change",
                0, countdownLatch.count)
    }

}