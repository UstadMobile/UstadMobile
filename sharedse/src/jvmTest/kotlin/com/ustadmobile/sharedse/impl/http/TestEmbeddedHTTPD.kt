package com.ustadmobile.sharedse.impl.http

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.sharedse.test.util.bindDbForActiveContext
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.io.IOException

/**
 * Created by mike on 12/25/17.
 */
class TestEmbeddedHTTPD {

    private var httpd: EmbeddedHTTPD? = null

    private var context: Any = Any()

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
        UmAccountManager.bindDbForActiveContext(context)
        var db = UmAccountManager.getActiveAccount(context)
        httpd = EmbeddedHTTPD(0, context)
        httpd!!.start()
    }

    @After
    fun stopServer() {
        httpd!!.stop()
        httpd = null
    }

    @Test
    @Throws(IOException::class)
    fun givenResponseListenerAdded_whenRequestMade_shouldReceiveResponseStartAndFinishedEvent() {

        httpd!!.addRoute(".*", EmbeddeHttpdResponder::class.java)

        val responseListener = mock(EmbeddedHTTPD.ResponseListener::class.java)
        httpd!!.addResponseListener(responseListener)

        val client = HttpClient()

        GlobalScope.launch{
            client.get<String>(httpd!!.localHttpUrl + "dir/filename.txt")

            //val response = UstadMobileSystemImpl.instance.makeRequestSync(
           //         UmHttpRequest(context!!, httpd!!.localHttpUrl + "dir/filename.txt"))
            val sessionArgumentCaptor = ArgumentCaptor.forClass(
                    NanoHTTPD.IHTTPSession::class.java)
            val responseArgumentCaptor = ArgumentCaptor.forClass(
                    NanoHTTPD.Response::class.java)

            verify(responseListener).responseStarted(sessionArgumentCaptor.capture(),
                    responseArgumentCaptor.capture())
            Assert.assertEquals("Received expected request on response started",
                    "/dir/filename.txt", sessionArgumentCaptor.value.uri)

            verify(responseListener, timeout(10000)).responseFinished(sessionArgumentCaptor.capture(),
                    responseArgumentCaptor.capture())
            Assert.assertEquals("Received expected request on response finished",
                    "/dir/filename.txt", sessionArgumentCaptor.value.uri)

            httpd!!.removeResponseListener(responseListener)

        }





    }


}
