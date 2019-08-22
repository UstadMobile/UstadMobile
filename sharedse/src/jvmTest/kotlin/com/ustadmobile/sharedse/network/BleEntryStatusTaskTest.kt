package com.ustadmobile.sharedse.network


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.EntryStatusResponseDao
import com.ustadmobile.core.db.dao.NetworkNodeDao
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.sharedse.network.BleMessageUtil.bleMessageLongToBytes
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_RESPONSE
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.spy
import java.util.*

/**
 * Test class which tests [BleEntryStatusTask] to make sure it behaves as expected
 * under different circumstances when response received.
 *
 * @author kileha3
 */
class BleEntryStatusTaskTest {

    private val containerUids = listOf(1056289670L, 9076137860L, 4590875612L, 2912543894L)

    private val localAvailabilityCheckResponse = listOf(0L, 9076137860000L, 0L, 2912543894000L)

    private var mockedEntryStatusTask: BleEntryStatusTask? = null

    private var entryStatusResponseDao: EntryStatusResponseDao? = null

    private var managerBle: NetworkManagerBle? = null

    private var networkNodeDao: NetworkNodeDao? = null

    private lateinit var networkNode: NetworkNode

    private var context = Any()

    @Before
    fun setUp() {
        val umAppDatabase = UmAppDatabase.getInstance(context)
        umAppDatabase.clearAllTables()

        managerBle = com.nhaarman.mockitokotlin2.spy {

        }
        managerBle!!.onCreate()

        networkNode = NetworkNode()
        networkNode.bluetoothMacAddress = "00:3F:2F:64:C6:4F"
        networkNode.nodeId = 1
        networkNodeDao = umAppDatabase.networkNodeDao
        networkNodeDao!!.replace(networkNode)

        entryStatusResponseDao = umAppDatabase.entryStatusResponseDao

        mockedEntryStatusTask = spy(BleEntryStatusTask::class.java)
        mockedEntryStatusTask!!.context = context
        mockedEntryStatusTask!!.setManagerBle(spy(NetworkManagerBle::class.java))
        mockedEntryStatusTask!!.setEntryUidsToCheck(containerUids)
    }

    @Test
    fun givenBleMessageWithRequest_whenResponseReceived_thenShouldUpdateEntryStatusResponseInDatabase() {

        val responseMessage = BleMessage(ENTRY_STATUS_RESPONSE, 42.toByte(),
                bleMessageLongToBytes(localAvailabilityCheckResponse))
        mockedEntryStatusTask!!.onResponseReceived(networkNode.bluetoothMacAddress!!, responseMessage, null)

        assertNotNull("entry check status response will be saved to the database",
                entryStatusResponseDao!!.findByContainerUidAndNetworkNode(containerUids[0],
                        networkNode.nodeId))
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
