package com.ustadmobile.sharedse.util

import org.mockito.kotlin.mock
import com.ustadmobile.core.util.LiveDataWorkQueue
import com.ustadmobile.door.lifecycle.LiveData
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
    fun givenListOfWorkItems_whenOnChangeCalled_thenAllItemsRunOnce() {
        var mockLiveData = mock<LiveData<List<TestWorkItem>>> {}
        runBlocking {
            val onStartCounter = AtomicInteger(0)
            val onStartFn: (TestWorkItem) -> Unit = { onStartCounter.incrementAndGet() }
            val onFinishCounter = AtomicInteger(0)
            val onEndFn: (TestWorkItem) -> Unit = { onFinishCounter.incrementAndGet() }
            val queue = LiveDataWorkQueue<TestWorkItem>(mockLiveData,
                    {item1, item2 ->  item1.uid == item2.uid},
                    numProcessors = 3, onItemStarted =  onStartFn, onItemFinished = onEndFn) {
                it.runMe()
            }

            queue.start()
            val firstListVals = (1..10).map { TestWorkItem(it) }
            queue.onChanged(firstListVals)
            withTimeout(10000) {
                while(!(firstListVals.all { it.runCount.get() == 1 } && onFinishCounter.get() == firstListVals.size))
                    delay(200)
            }

            Assert.assertTrue("All items have been run once",
                    firstListVals.all { it.runCount.get() == 1 })

            Assert.assertEquals("On start fn called for each item in list",
                    firstListVals.size, onStartCounter.get())

            Assert.assertEquals("On finish fn called for each item in list",
                    firstListVals.size, onFinishCounter.get())
        }
    }

    @Test
    fun givenWorkItemAlreadyRunning_whenOnChangeCalled_itemWillNotRunTwice() {
        var mockLiveData = mock<LiveData<List<TestWorkItem>>> {}
        runBlocking {
            val queue = LiveDataWorkQueue<TestWorkItem>(mockLiveData, {item1, item2 ->  item1.uid == item2.uid}
                    ,numProcessors = 3) {
                it.runMe()
                delay(200)
            }
            queue.start()

            val workItemList = listOf(TestWorkItem(1))
            queue.onChanged(workItemList)
            delay(30)
            queue.onChanged(workItemList)
            delay(250)

            Assert.assertEquals("Item ran once", 1, workItemList[0].runCount.get())
        }
    }

    @Test
    fun givenWorkItemFinished_whenONChangeCalled_itemWillRunAgain() {
        var mockLiveData = mock<LiveData<List<TestWorkItem>>> {}
        runBlocking {
            val queue = LiveDataWorkQueue<TestWorkItem>(mockLiveData, {item1, item2 ->  item1.uid == item2.uid}
                    ,numProcessors = 3) {
                it.runMe()
            }
            queue.start()

            val workItemList = listOf(TestWorkItem(1))
            queue.onChanged(workItemList)
            delay(30)
            queue.onChanged(workItemList)
            withTimeout(1000) {
                while(workItemList[0].runCount.get() != 2)
                    delay(50)
            }

            Assert.assertEquals("Item ran twice", 2, workItemList[0].runCount.get())
        }
    }


}