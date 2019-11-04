package com.ustadmobile.sharedse.util

import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.sharedse.network.BleEntryStatusTask
import com.ustadmobile.sharedse.network.BleEntryStatusTask.Companion.STATUS_COMPLETED
import com.ustadmobile.sharedse.network.BleEntryStatusTask.Companion.STATUS_FAILURE
import com.ustadmobile.sharedse.network.BleEntryStatusTask.Companion.STATUS_NONE
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.Exception
import java.util.concurrent.TimeUnit

//class TestEntryTaskExecutor {
//
//    private val mockedNetworkManager = spy<NetworkManagerBle>()
//
//    private val executor = EntryTaskExecutor(2)
//
//    private val DEFAULT_DELAY = TimeUnit.SECONDS.toMillis(1)
//
//    class TestBleEntryStatusTask (context: Any,managerBle:NetworkManagerBle, entryUidsToCheck: List<Long>, peerToCheck: NetworkNode)
//        : BleEntryStatusTask(context,managerBle, entryUidsToCheck,peerToCheck){
//
//        override fun run() {
//            println("Running task with node adress = ${this.networkNode.bluetoothMacAddress}")
//        }
//
//    }
//
//    private val taskList = listOf(
//            TestBleEntryStatusTask(Any(),mockedNetworkManager,listOf(89L,100L),
//                    NetworkNode("00:11:22:33:FF:E1","127.0.0.1")),
//            TestBleEntryStatusTask(Any(),mockedNetworkManager,listOf(89L,100L),
//                    NetworkNode("00:11:22:33:FF:E2","127.0.0.1")),
//            TestBleEntryStatusTask(Any(),mockedNetworkManager,listOf(89L,100L),
//                    NetworkNode("00:11:22:33:FF:E3","127.0.0.1")),
//            TestBleEntryStatusTask(Any(),mockedNetworkManager,listOf(89L,100L),
//                    NetworkNode("00:11:22:33:FF:E4","127.0.0.1"))
//
//    )
//
//    @Test
//    fun givenListOfTasksToBeExecuted_whenExecuteCalled_thenAllShouldBeExecuted() = runBlocking {
//        for(task in taskList){
//            executor.execute(task)
//        }
//
//        delay(DEFAULT_DELAY)
//
//        assertEquals("All pending task were executed ", taskList.size,
//                executor.runningOrCompletedTasks.filter { task -> task.status != STATUS_NONE }.size)
//    }
//
//
//    @Test
//    fun givenListOfTaskToBeExecuted_whenOnResponseReceivedCalled_thenShouldMarkTaskAsCompleted() = runBlocking{
//        for(task in taskList){
//            executor.execute(task)
//        }
//
//        delay(DEFAULT_DELAY)
//
//        taskList[0].onResponseReceived(taskList[0].networkNode.bluetoothMacAddress!!,null,null)
//
//        assertEquals("Only one task are marked as completed",1,
//                executor.runningOrCompletedTasks.filter { task -> task.status == STATUS_COMPLETED }.size)
//    }
//
//
//    @Test
//    fun givenListOfTaskToBeExecuted_whenOnResponseReceivedCalled_shouldRemoveCompletedTaskFromQueue() = runBlocking {
//        for(task in taskList){
//            executor.execute(task)
//        }
//
//        delay(DEFAULT_DELAY)
//
//        taskList[0].onResponseReceived(taskList[0].networkNode.bluetoothMacAddress!!,null,null)
//
//        assertEquals("Only one task was removed from the queue",
//                executor.taskQueue.size, taskList.size - 1)
//    }
//
//    @Test
//    fun givenListOfTaskToBeExecuted_whenOnResponseReceivedCalledAndTaskFailed_shouldRemoveCompletedTaskFromQueue() = runBlocking {
//        for(task in taskList){
//            executor.execute(task)
//        }
//
//        delay(DEFAULT_DELAY)
//
//        taskList[0].onResponseReceived(taskList[0].networkNode.bluetoothMacAddress!!,null, Exception("Failed to connect"))
//
//        assertEquals("Only one task was was marked as failure",
//                1, executor.runningOrCompletedTasks.filter { task -> task.status == STATUS_FAILURE }.size)
//    }
//}