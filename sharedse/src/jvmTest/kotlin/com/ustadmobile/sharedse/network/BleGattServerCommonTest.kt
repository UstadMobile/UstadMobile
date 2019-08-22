package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.sharedse.network.BleMessageUtil.bleMessageLongToBytes
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_REQUEST
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.ENTRY_STATUS_RESPONSE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_REQUEST
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Test class which tests [com.ustadmobile.port.sharedse.networkmanager.BleGattServer]
 * to make sure it behaves as expected when given different message with different request types.
 *
 * @author kileha3
 */

class BleGattServerCommonTest {

    private var mockedNetworkManager: NetworkManagerBleCommon? = null

    private val containerUids = ArrayList<Long>()

    private var gattServer: BleGattServerCommon? = null

    private val containerList = ArrayList<Container>()

    private var wiFiDirectGroupBle: WiFiDirectGroupBle? = null

    private var umAppDatabase: UmAppDatabase? = null

    private var context: Any = Any()


    @Before
    @Throws(IOException::class)
    fun setUp() {
        mockedNetworkManager = com.nhaarman.mockitokotlin2.spy {

        }
        mockedNetworkManager!!.onCreate()
        val httpd = EmbeddedHTTPD(0, context)
        httpd.start()
       //`when`(mockedNetworkManager!!.httpd).thenReturn(httpd)

        umAppDatabase = UmAppDatabase.getInstance(context)
        umAppDatabase!!.clearAllTables()

        gattServer = spy(BleGattServerCommon::class.java)
        gattServer!!.setContext(context)
        wiFiDirectGroupBle = WiFiDirectGroupBle("NetworkSsId", "@@@1234")
        wiFiDirectGroupBle!!.ipAddress = "127.0.0.1"
        wiFiDirectGroupBle!!.port = 0
        gattServer!!.setNetworkManager(mockedNetworkManager!!)



        for (i in 0 until MAX_ENTITIES_NUMBER) {
            val currentTimeStamp = Calendar.getInstance().timeInMillis
            val entryFile = Container()
            entryFile.lastModified = currentTimeStamp
            containerList.add(entryFile)
        }
        containerUids.addAll(listOf(*umAppDatabase!!.containerDao.insertListAndReturnIds(containerList)))

    }

    @Test
    fun givenRequestMessageWithCorrectRequestHeader_whenHandlingIt_thenShouldReturnResponseMessage() {
        val messageToSend = BleMessage(ENTRY_STATUS_REQUEST, 42.toByte(),
                bleMessageLongToBytes(containerUids))

        val responseMessage = gattServer!!.handleRequest(messageToSend)

        assertEquals("Should return the right response request type",
                ENTRY_STATUS_RESPONSE, responseMessage!!.requestType)
    }


    @Test
    fun givenRequestMessageWithWrongRequestHeader_whenHandlingIt_thenShouldNotReturnResponseMessage() {
        val messageToSend = BleMessage(0.toByte(), 42.toByte(), bleMessageLongToBytes(containerUids))

        val responseMessage = gattServer!!.handleRequest(messageToSend)

        assertNull("Response message should be null", responseMessage)
    }


    @Test
    fun givenNoWifiDirectGroupExisting_whenWifiDirectGroupRequested_thenShouldCreateAGroupAndPassGroupDetails() {

        doAnswer { invocation ->
            Thread.sleep(TimeUnit.SECONDS.toMillis(3))
            wiFiDirectGroupBle
        }.`when`<NetworkManagerBleCommon>(mockedNetworkManager).awaitWifiDirectGroupReady(anyLong())

        val messageToSend = BleMessage(WIFI_GROUP_REQUEST, 42.toByte(), bleMessageLongToBytes(containerUids))

        val responseMessage = gattServer!!.handleRequest(messageToSend)

        val groupBle = WiFiDirectGroupBle(responseMessage!!.payload!!)

        //Verify that wifi direct group creation was initiated
        verify<NetworkManagerBleCommon>(mockedNetworkManager).awaitWifiDirectGroupReady(anyLong())

        assertTrue("Returned the right Wifi direct group information",
                wiFiDirectGroupBle!!.passphrase == groupBle.passphrase && wiFiDirectGroupBle!!.ssid == groupBle.ssid)

    }


    @Test
    fun givenRequestWithAvailableEntries_whenHandlingIt_thenShouldReplyTheyAreAvailable() {

        val messageToSend = BleMessage(ENTRY_STATUS_REQUEST, 42.toByte(),
                bleMessageLongToBytes(containerUids))

        val responseMessage = gattServer!!.handleRequest(messageToSend)
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

    @Test
    fun givenRequestWithUnAvailableEntries_whenHandlingIt_thenShouldReplyTheyAreNotAvailable() {

        val messageToSend = BleMessage(ENTRY_STATUS_REQUEST, 42.toByte(), bleMessageLongToBytes(containerUids))
        umAppDatabase!!.clearAllTables()
        val responseMessage = gattServer!!.handleRequest(messageToSend)
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

