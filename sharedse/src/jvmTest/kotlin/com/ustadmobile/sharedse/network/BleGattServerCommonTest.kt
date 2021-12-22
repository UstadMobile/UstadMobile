package com.ustadmobile.sharedse.network

import org.mockito.kotlin.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_REQUEST
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_RESPONSE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_REQUEST
import com.ustadmobile.sharedse.util.UstadTestRule
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.*
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.CALLS_REAL_METHODS
import java.io.IOException
import java.util.*


/**
 * Test class which tests [com.ustadmobile.port.sharedse.networkmanager.BleGattServer]
 * to make sure it behaves as expected when given different message with different request types.
 *
 * @author kileha3
 */

class BleGattServerCommonTest {

    private lateinit var mockedNetworkManager: NetworkManagerBle

    private val containerUids = ArrayList<Long>()

    private lateinit var gattServer: BleGattServerCommon

    private val containerList = ArrayList<Container>()

    private lateinit var wiFiDirectGroupBle: WiFiDirectGroupBle

    private val clientBtAddr = "aa:bb:cc:dd:ee"


    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    lateinit var umAppDatabase: UmAppDatabase

    lateinit var accountManager: UstadAccountManager

    lateinit var entryStatusRequest : EntryStatusRequest

    @Before
    @Throws(IOException::class)
    fun setUp() {

        mockedNetworkManager = mock { }
        di = DI {
            import(ustadTestRule.diModule)
            bind<NetworkManagerBle>() with singleton { mockedNetworkManager }
        }

        accountManager = di.direct.instance()

        umAppDatabase = di.on(accountManager.activeAccount).direct.instance(tag = TAG_DB)

        gattServer = mock(useConstructor = UseConstructor.withArguments(di),
            defaultAnswer = CALLS_REAL_METHODS) {  }

        wiFiDirectGroupBle = WiFiDirectGroupBle("NetworkSsId", "@@@1234").apply {
            ipAddress = "127.0.0.1"
            port = 0
        }


        for (i in 0 until MAX_ENTITIES_NUMBER) {
            val currentTimeStamp = Calendar.getInstance().timeInMillis
            val entryFile = Container()
            entryFile.cntLastModified = currentTimeStamp
            containerList.add(entryFile)
        }

        //containerUids.addAll(listOf(*umAppDatabase.containerDao.insertListAsync(containerList)))
        entryStatusRequest = EntryStatusRequest(accountManager.activeAccount.endpointUrl, containerUids.toLongArray())
    }

    //@Test
    fun givenRequestMessageWithCorrectRequestHeader_whenHandlingIt_thenShouldReturnResponseMessage() {
        val messageToSend = BleMessage(ENTRY_STATUS_REQUEST, 42.toByte(), entryStatusRequest.toBytes())

        val responseMessage = gattServer.handleRequest(messageToSend, clientBtAddr)

        assertEquals("Should return the right response request type",
                ENTRY_STATUS_RESPONSE, responseMessage!!.requestType)
    }


    //@Test
    fun givenRequestMessageWithWrongRequestHeader_whenHandlingIt_thenShouldNotReturnResponseMessage() {
        val messageToSend = BleMessage(0.toByte(), 42.toByte(), entryStatusRequest.toBytes())
        val responseMessage = gattServer.handleRequest(messageToSend, clientBtAddr)

        assertNull("Response message should be null", responseMessage)
    }


    //@Test
    fun givenNoWifiDirectGroupExisting_whenWifiDirectGroupRequested_thenShouldCreateAGroupAndPassGroupDetails() {
        mockedNetworkManager.stub {
            on { awaitWifiDirectGroupReady(anyLong()) }.thenAnswer {
                Thread.sleep(200)
                wiFiDirectGroupBle
            }
        }

        val messageToSend = BleMessage(WIFI_GROUP_REQUEST, 42.toByte(), byteArrayOf())

        val responseMessage = gattServer.handleRequest(messageToSend, clientBtAddr)

        val groupBle = WiFiDirectGroupBle(responseMessage!!.payload!!)

        //Verify that wifi direct group creation was initiated
        verify(mockedNetworkManager, timeout(5000)).awaitWifiDirectGroupReady(anyLong())

        assertTrue("Returned the right Wifi direct group information",
                wiFiDirectGroupBle.passphrase == groupBle.passphrase && wiFiDirectGroupBle.ssid == groupBle.ssid)

    }


    //@Test
    fun givenRequestWithAvailableEntries_whenHandlingIt_thenShouldReplyTheyAreAvailable() {

        val messageToSend = BleMessage(ENTRY_STATUS_REQUEST, 42.toByte(),
                entryStatusRequest.toBytes())

        val responseMessage = gattServer.handleRequest(messageToSend, clientBtAddr)
        val responseList = BleMessageUtil.bleMessageBytesToLong(responseMessage!!.payload!!)
        var availabilityCounter = 0
        for (response in responseList) {
            if (response != 0L) {
                availabilityCounter++
            }
        }

        assertEquals("All requested entry uuids status are available",
                containerUids.size.toLong(), availabilityCounter.toLong())

    }

    //@Test
    fun givenRequestWithUnAvailableEntries_whenHandlingIt_thenShouldReplyTheyAreNotAvailable() {
        val messageToSend = BleMessage(ENTRY_STATUS_REQUEST, 42.toByte(),
                entryStatusRequest.toBytes())
        umAppDatabase.clearAllTables()
        val responseMessage = gattServer.handleRequest(messageToSend, clientBtAddr)
        val responseList = BleMessageUtil.bleMessageBytesToLong(responseMessage!!.payload!!)
        var availabilityCounter = 0
        for (response in responseList) {
            if (response != 0L) {
                availabilityCounter++
            }
        }

        assertEquals("All requested entry uuids status are not available",
                0, availabilityCounter.toLong())
    }


    companion object {

        internal const val MAX_ENTITIES_NUMBER = 4
    }

}

