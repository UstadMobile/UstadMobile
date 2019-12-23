package com.ustadmobile.sharedse.network

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.lib.db.entities.NetworkNode
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.discardRemaining
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.io.IOException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class BleHttpProxyTest {

    private lateinit var httpdServer: NanoHttpdWithSessionSource

    private lateinit var httpClient: HttpClient

    private lateinit var networkManager: NetworkManagerBle

    data class MockBleDevice(var bluetoothMacAddr: String,
                             var bleClientResponding: Boolean = true,
                             var bleMessageReturnsError: Boolean = false)

    private var nearbyDevices = listOf<MockBleDevice>()

    @Before
    fun setup() {
        httpdServer = object : NanoHttpdWithSessionSource(8095) {
            override fun serve(session: IHTTPSession?): Response {
                return newFixedLengthResponse("OK")
            }
        }
        httpClient = HttpClient()

        networkManager = mock<NetworkManagerBle> {
            on { sendMessage(any(), any(), any(), any()) }.thenAnswer {invocation ->
                val destNode = invocation.arguments[2] as NetworkNode
                val nearbyDevice = nearbyDevices.firstOrNull { it.bluetoothMacAddr == destNode.bluetoothMacAddress!! }
                if(nearbyDevice == null || !nearbyDevice.bleClientResponding) {
                    println ("Nearby device = ${nearbyDevice} : does not exist or is set to not respond")
                    return@thenAnswer Unit //there is no such device around for this scenario
                }



                val responseListener  = invocation.arguments[3] as BleMessageResponseListener
                val messageIn = invocation.arguments[1] as BleMessage


                if(!nearbyDevice.bleMessageReturnsError) {
                    val responseBuffer = ByteArrayOutputStream()
                    val httpSession = httpdServer.sessionSource(ByteArrayInputStream(messageIn.payload!!),
                            responseBuffer)
                    httpSession.execute()
                    responseBuffer.flush()

                    val responseMessage = BleMessage(BleMessage.MESSAGE_TYPE_HTTP,
                            BleMessage.getNextMessageIdForReceiver(destNode.bluetoothMacAddress!!),
                            responseBuffer.toByteArray())
                    responseListener.onResponseReceived(destNode.bluetoothMacAddress!!,
                            responseMessage, null)
                }else {
                    responseListener.onResponseReceived(destNode.bluetoothMacAddress!!,
                            null, IOException("Mock network manager sendMessage exception"))
                }

                Unit
            }
        }
    }

    @After
    fun tearDown() {
        httpClient.close()
    }

    @Test
    fun givenResponsivePeer_whenRequestMade_thenShouldSendResponseOverHttp() {
        val proxy = BleHttpProxy(networkManager, Any())
        proxy.start(8090)
        nearbyDevices = listOf(MockBleDevice(TEST_NEARBY_MAC1, true, false))

        runBlocking {
            repeat(10) {
                val response = httpClient.get<String>("http://localhost:8090/${BleHttpProxy.PREFIX}/$TEST_NEARBY_MAC1/ContentEntryList")
                Assert.assertEquals("Response is as expected", "OK",
                        response)
            }
        }
    }

    @Test
    fun givenPeerConnectionThrowsError_whenRequestMade_thenShouldReturnBadGatewayStatus() {
        val proxy = BleHttpProxy(networkManager, Any())
        proxy.start(8090)

        nearbyDevices = listOf(MockBleDevice(TEST_NEARBY_MAC1, true, true))

        runBlocking {
            val response = httpClient.get<HttpResponse>("http://localhost:8090/${BleHttpProxy.PREFIX}/$TEST_NEARBY_MAC1/ContentEntryList")
            Assert.assertEquals("When send BLE message calls back with an error, then http response status is 502",
                    response.status, HttpStatusCode.BadGateway)
            response.discardRemaining()
        }
        proxy.stop()
    }

    companion object {

        const val TEST_NEARBY_MAC1 = "aa:bb:cc:dd"
    }

}