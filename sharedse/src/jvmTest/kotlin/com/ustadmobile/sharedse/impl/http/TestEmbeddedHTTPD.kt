package com.ustadmobile.sharedse.impl.http

import org.mockito.kotlin.*
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.sharedse.network.NetworkManagerBle
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import java.io.IOException

/**
 * Created by mike on 12/25/17.
 */
class TestEmbeddedHTTPD {

    private lateinit var httpd: EmbeddedHTTPD

    private var context: Any = Any()

    private lateinit var di: DI

    internal class EmbeddeHttpdResponder : RouterNanoHTTPD.UriResponder {

        override fun get(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
            return NanoHTTPD.newFixedLengthResponse("Hello world")
        }

        override fun put(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
            return NanoHTTPD.newFixedLengthResponse("Hello world")
        }

        override fun post(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
            return NanoHTTPD.newFixedLengthResponse("Hello world")
        }

        override fun delete(uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
            return NanoHTTPD.newFixedLengthResponse("Hello world")
        }

        override fun other(method: String, uriResource: RouterNanoHTTPD.UriResource, urlParams: Map<String, String>, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
            return NanoHTTPD.newFixedLengthResponse("Hello world")
        }
    }

    @Before
    @Throws(IOException::class)
    fun startServer() {
        di = DI {
            bind<NetworkManagerBle>() with singleton { mock<NetworkManagerBle> {} }
        }
        httpd = EmbeddedHTTPD(0, di)
        httpd.start()
    }

    @After
    fun stopServer() {
        httpd.stop()
    }

    @Test
    @Throws(IOException::class)
    fun givenResponseListenerAdded_whenRequestMade_shouldReceiveResponseStartAndFinishedEvent() {

        httpd.addRoute(".*", EmbeddeHttpdResponder::class.java)

        val responseListener = mock<EmbeddedHTTPD.ResponseListener>()
        httpd.addResponseListener(responseListener)

        val client = HttpClient()

        runBlocking {
            client.get(httpd.localHttpUrl + "dir/filename.txt").body<String>()

            argumentCaptor<NanoHTTPD.IHTTPSession> {
                verify(responseListener).responseStarted(capture(), any())
                Assert.assertEquals("Received expected request on response started",
                        "/dir/filename.txt", firstValue.uri)
            }

            argumentCaptor<NanoHTTPD.IHTTPSession>() {
                verify(responseListener, timeout(10000)).responseFinished(capture(), any())
                Assert.assertEquals("Received expected request on response finished",
                        "/dir/filename.txt", firstValue.uri)
            }

            httpd.removeResponseListener(responseListener)

        }
    }


}
