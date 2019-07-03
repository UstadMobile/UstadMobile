package com.ustadmobile.sharedse.util

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.door.DoorLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class TestLiveDataWorkQueue {


     class TestWorkItem constructor(val uid: Int) {

        val runCount = AtomicInteger(0)

        suspend fun runMe(){
            runCount.incrementAndGet()
        }

    }

    @Test
    fun testIt() {
        var mockLiveData = mock<DoorLiveData<List<TestWorkItem>>> {}
        runBlocking {
            val queue = LiveDataWorkQueue<TestWorkItem>(mockLiveData, {item1, item2 ->  item1.uid == item2.uid}
            ,numProcessors = 3) {
                it.runMe()
            }

            queue.start()
            val firstListVals = (1..10).map { TestWorkItem(it) }
            queue.onChanged(firstListVals)
            withTimeout(10000) {
                while(!firstListVals.all { it.runCount.get() == 1 })
                    delay(200)
            }

            Assert.assertTrue("All items have been run once",
                    firstListVals.all { it.runCount.get() == 1 })
        }
    }


}