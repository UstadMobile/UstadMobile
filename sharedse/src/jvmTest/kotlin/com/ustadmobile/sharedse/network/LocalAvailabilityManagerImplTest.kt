package com.ustadmobile.sharedse.network

import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.timeout
import com.nhaarman.mockitokotlin2.verify
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest
import com.ustadmobile.lib.db.entities.EntryStatusResponse
import com.ustadmobile.sharedse.util.UstadTestRule
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LocalAvailabilityManagerImplTest  {

    val TEST_ENTRY_UID1  = 42L

    val TEST_NODE1_ADDR = "aa:bb:cc:dd:ee:ff"

    val TEST_NODE2_ADDR = "00:12:12:13:aa:bb"

    private lateinit var db: UmAppDatabase

    private val context = Any()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    lateinit var di: DI

    private val tasksMade = mutableListOf<BleEntryStatusTask>()

    private lateinit var taskChannel: Channel<BleEntryStatusTask>

    lateinit var activeEndpoint: Endpoint

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
            bind<BleEntryStatusTask>() with factory { args: BleEntryStatusTaskArgs ->
                spy <BleEntryStatusTask>{
                    on { sendRequest()} doAnswer {
                        val that = (it.mock as BleEntryStatusTask)
                        that.statusResponseListener?.invoke(mutableListOf(EntryStatusResponse(TEST_ENTRY_UID1, true)), that)
                        Unit
                    }
                }.also {
                    it.networkNode = args.networkNode
                    tasksMade.add(it)
                    taskChannel.offer(it)
                }
            }
        }

        tasksMade.clear()
        taskChannel = Channel<BleEntryStatusTask>(capacity = Channel.UNLIMITED)

        val accountManager: UstadAccountManager = di.direct.instance()
        activeEndpoint = Endpoint(accountManager.activeAccount.endpointUrl)
        db = di.on(accountManager.activeAccount).direct.instance(tag = TAG_DB)
    }

    @Test
    fun givenEntryMonitorActiveWithNodeThatHasEntry_whenNodeDisocvered_shouldCreateAndSendBleEntryStatusTask() {
        runBlocking {
            val countdownLatch = CountDownLatch(1)
            val coroutineContext = newSingleThreadContext("LocalAvailabilityTest")
            val managerImpl = LocalAvailabilityManagerImpl(di, activeEndpoint)
            val monitorRequest = AvailabilityMonitorRequest(listOf(TEST_ENTRY_UID1), {
                if(it[TEST_ENTRY_UID1] ?: false)
                    countdownLatch.countDown()
            })

            managerImpl.addMonitoringRequest(monitorRequest)
            managerImpl.handleNodeDiscovered(TEST_NODE1_ADDR)

            countdownLatch.await(10, TimeUnit.SECONDS)

            verify(tasksMade[0]).sendRequest()
            Assert.assertEquals("Create one task", 1, tasksMade.size)

            val availableMap = managerImpl.areContentEntriesLocallyAvailable(listOf(TEST_ENTRY_UID1, -1))
            Assert.assertEquals("Entry that responded as available is marked as available",
                    true, availableMap[TEST_ENTRY_UID1])
            Assert.assertFalse("Other unknown entry is marked as not available", availableMap[-1] ?: true)
            coroutineContext.close()
        }
    }

    @Test
    fun givenNodesAlreadyDiscovered_whenAvailabilityStatusRequested_shouldCreateStatusTasks() {
        runBlocking {
            val managerImpl = LocalAvailabilityManagerImpl(di, activeEndpoint)
            managerImpl.handleNodeDiscovered(TEST_NODE1_ADDR)
            managerImpl.handleNodeDiscovered(TEST_NODE2_ADDR)

            val countDownLatch = CountDownLatch(1)
            val availabilityRequest = AvailabilityMonitorRequest(listOf(TEST_ENTRY_UID1), {
                if(it[TEST_ENTRY_UID1] ?: false)
                    countDownLatch.countDown()
            })

            managerImpl.addMonitoringRequest(availabilityRequest)

            val task1 = withTimeout(5000) { taskChannel.receive() }
            val task2 = withTimeout(5000) { taskChannel.receive() }
            verify(task1, timeout(5000)).sendRequest()
            verify(task2, timeout(5000)).sendRequest()
            Assert.assertEquals("Made two tasks", 2, tasksMade.size)

            countDownLatch.await(5, TimeUnit.SECONDS)
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
                val managerImpl = LocalAvailabilityManagerImpl(di, activeEndpoint)
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