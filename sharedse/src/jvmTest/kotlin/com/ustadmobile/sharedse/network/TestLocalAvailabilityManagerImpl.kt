package com.ustadmobile.sharedse.network

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.lib.db.entities.EntryStatusResponse
import com.ustadmobile.lib.db.entities.NetworkNode
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class TestLocalAvailabilityManagerImpl  {

    val TEST_ENTRY_UID1  = 42L

    val TEST_NODE1_ADDR = "aa:bb:cc:dd:ee:ff"

    val TEST_NODE2_ADDR = "00:12:12:13:aa:bb"

    @Test
    fun givenEntryMonitorActiveWithNodeThatHasEntry_whenNodeDisocvered_shouldCreateAndSendBleEntryStatusTask() {
        runBlocking {
            val tasksMade = mutableListOf<BleEntryStatusTask>()
            val taskChannel = Channel<BleEntryStatusTask>()

            val statusTaskMaker: StatusTaskMakerFn = {context: Any, containerUidsToCheck: List<Long>, networkNode: NetworkNode ->
                val task = spy<BleEntryStatusTask>() {
                    on { sendRequest() } doAnswer {
                        val that = (it.mock as BleEntryStatusTask)
                        that.statusResponseListener?.invoke(mutableListOf(EntryStatusResponse(TEST_ENTRY_UID1, true)), that)
                        Unit
                    }
                }
                task.networkNode = networkNode
                taskChannel.offer(task)
                tasksMade.add(task)
                task
            }

            val managerImpl = LocalAvailabilityManagerImpl(Any(), statusTaskMaker)
            managerImpl.addMonitoringRequest(AvailabilityMonitorRequest(listOf(TEST_ENTRY_UID1)))
            managerImpl.handleNodeDiscovered(TEST_NODE1_ADDR)


            val task1 = taskChannel.receive()
            verify(task1, timeout(10000)).sendRequest()
            Assert.assertEquals("Create one task", 1, tasksMade.size)

            val availableMap = managerImpl.areContentEntriesLocallyAvailable(listOf(TEST_ENTRY_UID1, -1))
            Assert.assertTrue("Entry that responded as available is marked as available", availableMap[TEST_ENTRY_UID1] ?: false)
            Assert.assertFalse("Other unknown entry is marked as not available", availableMap[-1] ?: true)
        }
    }

    @Test
    fun givenNodesAlreadyDiscovered_whenAvailabilityStatusRequested_shouldCreateStatusTasks() {
        val tasksMade = mutableListOf<BleEntryStatusTask>()
        val taskChannel = Channel<BleEntryStatusTask>(capacity = Channel.UNLIMITED)
        runBlocking {
            val statusTaskMaker: StatusTaskMakerFn = {context: Any, containerUidsToCheck: List<Long>, networkNode: NetworkNode ->
                val task = spy<BleEntryStatusTask>() {
                    on { sendRequest() } doAnswer {
                        val that = (it.mock as BleEntryStatusTask)
                        that.statusResponseListener?.invoke(mutableListOf(EntryStatusResponse(TEST_ENTRY_UID1, true)), that)

                        Unit
                    }
                }
                task.networkNode = networkNode
                tasksMade.add(task)
                taskChannel.offer(task)
                task
            }

            val managerImpl = LocalAvailabilityManagerImpl(Any(), statusTaskMaker)
            managerImpl.handleNodeDiscovered(TEST_NODE1_ADDR)
            managerImpl.handleNodeDiscovered(TEST_NODE2_ADDR)

            managerImpl.addMonitoringRequest(AvailabilityMonitorRequest(listOf(TEST_ENTRY_UID1)))

            val task1 = taskChannel.receive()
            val task2 = taskChannel.receive()
            verify(task1, timeout(5000)).sendRequest()
            verify(task2, timeout(5000)).sendRequest()
            Assert.assertEquals("Made two tasks", 2, tasksMade.size)

            val availableMap = managerImpl.areContentEntriesLocallyAvailable(listOf(TEST_ENTRY_UID1, -1))
            Assert.assertTrue("Entry that responded as available is marked as available", availableMap[TEST_ENTRY_UID1] ?: false)
            Assert.assertFalse("Other unknown entry is marked as not available", availableMap[-1] ?: true)
        }


    }

    @Test
    fun givenStatusAlreadyKnown_whenAvailabilityStatusRequested_noTasksAreCreated() {
        runBlocking {
            val tasksMade = mutableListOf<BleEntryStatusTask>()

            runBlocking {
                val statusTaskMaker: StatusTaskMakerFn = { context: Any, containerUidsToCheck: List<Long>, networkNode: NetworkNode ->
                    val task = spy<BleEntryStatusTask>() {
                        on { sendRequest() } doAnswer {
                            val that = (it.mock as BleEntryStatusTask)
                            that.statusResponseListener?.invoke(mutableListOf(EntryStatusResponse(TEST_ENTRY_UID1, true)), that)
                            tasksMade.add(that)
                            Unit
                        }
                    }
                    task.networkNode = networkNode
                    task
                }

                val managerImpl = LocalAvailabilityManagerImpl(Any(), statusTaskMaker)
                managerImpl.handleNodeDiscovered(TEST_NODE1_ADDR)
                managerImpl.handleNodeDiscovered(TEST_NODE2_ADDR)
                managerImpl.addMonitoringRequest(AvailabilityMonitorRequest(listOf(TEST_ENTRY_UID1)))

                val numTasksMadeBeforeSecondRequest = tasksMade.size

                managerImpl.addMonitoringRequest(AvailabilityMonitorRequest(listOf(TEST_ENTRY_UID1)))

                Assert.assertEquals("No new tasks made after creating an availability monitor with the same UID",
                        numTasksMadeBeforeSecondRequest, tasksMade.size)

            }
        }
    }

}