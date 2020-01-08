package com.ustadmobile.sharedse.network


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.NetworkNodeDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.lib.db.entities.EntryStatusResponse
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.sharedse.network.BleMessageUtil.bleMessageLongToBytes
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_RESPONSE
import com.ustadmobile.sharedse.test.util.bindDbForActiveContext
import junit.framework.TestCase.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.spy
import java.util.*
import java.util.concurrent.atomic.AtomicReference

/**
 * Test class which tests [BleEntryStatusTask] to make sure it behaves as expected
 * under different circumstances when response received.
 *
 * @author kileha3
 */
class BleEntryStatusTaskTest {

    private val containerUids = listOf(1056289670L, 9076137860L, 4590875612L, 2912543894L)

    private val localAvailabilityCheckResponse = listOf(0L, 9076137860000L, 0L, 2912543894000L)

    private lateinit var mockedEntryStatusTask: BleEntryStatusTask

    private var managerBle: NetworkManagerBle? = null

    private var networkNodeDao: NetworkNodeDao? = null

    private lateinit var networkNode: NetworkNode

    private var context = Any()

    @Before
    fun setUp() {
        UmAccountManager.bindDbForActiveContext(context)
        val umAppDatabase = UmAccountManager.getActiveDatabase(context)
        umAppDatabase.clearAllTables()

        managerBle = com.nhaarman.mockitokotlin2.spy {

        }
        managerBle!!.onCreate()

        networkNode = NetworkNode()
        networkNode.bluetoothMacAddress = "00:3F:2F:64:C6:4F"
        networkNode.nodeId = 1
        networkNodeDao = umAppDatabase.networkNodeDao
        networkNodeDao!!.replace(networkNode)

        mockedEntryStatusTask = spy(BleEntryStatusTask::class.java)
        mockedEntryStatusTask.context = context
        mockedEntryStatusTask.setManagerBle(spy(NetworkManagerBle::class.java))
        mockedEntryStatusTask.setEntryUidsToCheck(containerUids)
    }

    @Test
    fun givenBleMessageWithRequest_whenResponseReceived_thenShouldUpdateEntryStatusResponseInDatabase() {

        val responseMessage = BleMessage(ENTRY_STATUS_RESPONSE, 42.toByte(),
                bleMessageLongToBytes(localAvailabilityCheckResponse))

        val responseReceived = AtomicReference<List<EntryStatusResponse>?>()
        mockedEntryStatusTask.statusResponseListener = {response, task ->
            responseReceived.set(response)
        }

        mockedEntryStatusTask.onResponseReceived(networkNode.bluetoothMacAddress!!, responseMessage, null)
        Assert.assertNotNull("Received response callback", responseReceived.get())
        responseReceived.get()!!.forEachIndexed { index, response ->
            Assert.assertEquals("ContainerUid matches item requested", containerUids[index],
                    response.erContainerUid)
            Assert.assertEquals("Availability matches as expected", localAvailabilityCheckResponse[index] != 0L,
                    response.available)
        }
    }


    //19/July/2019 - temporarily disabled
    //@Test
    fun givenNode_whenTryToConnectAndFailedMoreThanThreshold_shouldBeDeletedFromDb() {

        for (i in 0..5) {
            managerBle!!.handleNodeConnectionHistory(networkNode.bluetoothMacAddress!!, false)
        }

        assertNull("The node was deleted from the db",
                networkNodeDao!!.findNodeByBluetoothAddress(networkNode.bluetoothMacAddress!!))
    }

/*    @Test
    fun givenNodeWithFailureBelowThreshold_whenSucceed_shouldResetTheFailureCounterToZero() {

        for (i in 0..2) {
            managerBle!!.handleNodeConnectionHistory(networkNode.bluetoothMacAddress!!, false)
        }

        managerBle!!.handleNodeConnectionHistory(networkNode.bluetoothMacAddress!!, true)

        assertNotNull("The node was not deleted from the db and counter was reset to 0",
                networkNodeDao!!.findNodeByBluetoothAddress(networkNode.bluetoothMacAddress!!))

//        TODO: this has been disabled because it is causing a compilation error, and will be updated in
//         the dev kotlin multi download branch
//

        assertEquals("Counter was reset to 0", 0,
              managerBle!!.getBadNodeTracker(networkNode.bluetoothMacAddress!!)!!.value)
    }*/
}
