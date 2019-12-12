package com.ustadmobile.sharedse.network

import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.impl.http.BleHttpRequest
import com.ustadmobile.port.sharedse.impl.http.BleHttpResponse
import com.ustadmobile.port.sharedse.impl.http.BleProxyResponder
import com.ustadmobile.port.sharedse.impl.http.asBleHttpResponse
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.toUtf8Bytes
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.*

class BleProxyResponderTest {

    private lateinit var httpClient: HttpClient

    private lateinit var networkManager: NetworkManagerBle

    data class MockBleDevice(var bluetoothMacAddr: String,
                             var bleClientResponding: Boolean = true,
                             var bleMessageReturnsError: Boolean = false)

    private var nearbyDevices = listOf<MockBleDevice>()

    private lateinit var destNanoHttpdServer :NanoHTTPD


    @Before
    fun setup() {
        Napier.base(DebugAntilog())
        httpClient = HttpClient() {
            install(JsonFeature)
        }

        val nanoHttpd = NanoHTTPD.Response::class.java
        val headerField = nanoHttpd.getDeclaredField("header")
        println(headerField)

        networkManager = mock<NetworkManagerBle> {
            onBlocking { sendBleMessage(any(), any(), any())}.thenAnswer { invocation ->
                val destNodeAddr = invocation.arguments[2] as String
                val nearbyDevice = nearbyDevices.firstOrNull { it.bluetoothMacAddr == destNodeAddr }
                if(nearbyDevice == null || !nearbyDevice.bleClientResponding) {
                    println ("Nearby device = ${nearbyDevice} : does not exist or is set to not respond")
                    return@thenAnswer Unit //there is no such device around for this scenario
                }

                val messageIn = invocation.arguments[1] as BleMessage

                if(!nearbyDevice.bleMessageReturnsError) {
                    val bleRequest = Json.parse(BleHttpRequest.serializer(), String(messageIn.payload!!))
                    val response = destNanoHttpdServer.serve(bleRequest)
                    val bleResponse = response.asBleHttpResponse()
                    val payload = Json.stringify(BleHttpResponse.serializer(), bleResponse).toUtf8Bytes()

                    val responseMessage = BleMessage(BleMessage.MESSAGE_TYPE_HTTP,
                            BleMessage.getNextMessageIdForReceiver(destNodeAddr),
                            payload)
                    val responsePackets = responseMessage.getPackets(512)
                    val receivedMessage = BleMessage()
                    responsePackets.forEach {
                        receivedMessage.onPackageReceived(it)
                    }

                    receivedMessage
                }else {
                    null
                }
            }

            on { context}.thenReturn(Any())
        }
    }

    @After
    fun tearDown() {
        httpClient.close()
    }

    @Test
    fun givenValidRequest_whenReceived_thenShouldForwardAndRespond() {
        nearbyDevices = listOf(MockBleDevice(TEST_NEARBY_MAC1, true, false))
        destNanoHttpdServer = object: NanoHTTPD(9200) {

            override fun serve(session: IHTTPSession?): Response {
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html",
                        "<html><body>OK</body></html>")
            }

        }

        val responder = BleProxyResponder()
        val mockUriResource = mock<RouterNanoHTTPD.UriResource> {
            on { initParameter(0, NetworkManagerBleCommon::class.java)}.thenReturn(networkManager)
        }

        val mockSession = mock<NanoHTTPD.IHTTPSession> {
            on { uri }.thenReturn("/")
            on { method }.thenReturn(NanoHTTPD.Method.GET)
            on { headers }.thenReturn(mutableMapOf())
        }

        val response = responder.get(mockUriResource, mutableMapOf("bleaddr" to TEST_NEARBY_MAC1),
                mockSession)

        Assert.assertEquals("Response is OK", NanoHTTPD.Response.Status.OK, response.status)
    }

    @Test
    fun givenRequestWithParameters_whenReceived_thenParametersShouldMatch() {
        nearbyDevices = listOf(MockBleDevice(TEST_NEARBY_MAC1, true, false))
        destNanoHttpdServer = object: NanoHTTPD(9200) {

            override fun serve(session: IHTTPSession?): Response {
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/html",
                        "<html><body>OK</body></html>")
            }

        }
        destNanoHttpdServer = spy(destNanoHttpdServer)

        val responder = BleProxyResponder()
        val mockUriResource = mock<RouterNanoHTTPD.UriResource> {
            on { initParameter(0, NetworkManagerBleCommon::class.java)}.thenReturn(networkManager)
        }

        val mockSession = mock<NanoHTTPD.IHTTPSession> {
            on { uri }.thenReturn("/bleproxy/$TEST_NEARBY_MAC1/rest/endpoint")
            on { parameters }.thenReturn(mutableMapOf("arg1" to mutableListOf("value1")))
            on { method }.thenReturn(NanoHTTPD.Method.GET)
            on { headers }.thenReturn(mutableMapOf())
        }

        val response = responder.get(mockUriResource, mutableMapOf("bleaddr" to TEST_NEARBY_MAC1),
                mockSession)
        argumentCaptor<NanoHTTPD.IHTTPSession> {
            verify(destNanoHttpdServer).serve(capture())
            Assert.assertEquals(listOf("value1"), firstValue.parameters["arg1"])
            Assert.assertEquals("/rest/endpoint", firstValue.uri)

        }

        Assert.assertEquals("Response is OK", NanoHTTPD.Response.Status.OK, response.status)
        Assert.assertEquals("Mime type is text/html as provided by original server", "text/html",
                response.mimeType)
    }

    @Test
    fun givenServerProvidingJsonResponse_whenKtorGetRequestCalled_thenShouldParseOK() {
        nearbyDevices = listOf(MockBleDevice(TEST_NEARBY_MAC1, true, false))
        val contentEntryList = (0..10000).map { ContentEntry(UUID.randomUUID().toString(), "desc", true, true) }
        destNanoHttpdServer = object: NanoHTTPD(0) {
            override fun serve(session: IHTTPSession?): Response {
                return newFixedLengthResponse(Response.Status.OK, "application/json",
                        Json.stringify(ContentEntry.serializer().list, contentEntryList))
            }
        }

        destNanoHttpdServer.start()

        runBlocking {
            val contentEntryListReceived = httpClient.get<List<ContentEntry>>("http://localhost:${destNanoHttpdServer.listeningPort}/")
            Assert.assertEquals(contentEntryList, contentEntryListReceived)
        }

    }

    class TestParamHandler: RouterNanoHTTPD.UriResponder {
        override fun put(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
            println(urlParams)
            return NanoHTTPD.newFixedLengthResponse("OK")
        }

        override fun get(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
            println(urlParams)
            return NanoHTTPD.newFixedLengthResponse(urlParams["bleaddr"])
        }

        override fun other(method: String?, uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun post(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun delete(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    companion object {

        const val TEST_NEARBY_MAC1 = "aa:bb:cc:dd"
    }

}