package com.ustadmobile.sharedse.impl.http

import com.ustadmobile.port.sharedse.impl.http.parseRequestBody
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.post
import io.ktor.client.request.put
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader

class IHTTPSessionExtTest  {

    private lateinit var embeddedHttpd: RouterNanoHTTPD

    private lateinit var httpClient: HttpClient

    class BodyEchoResponder: RouterNanoHTTPD.UriResponder {
        override fun put(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
            return NanoHTTPD.newFixedLengthResponse(session.parseRequestBody())
        }

        override fun get(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
            TODO("Not yet implemented")
        }

        override fun other(method: String?, uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
            TODO("Not yet implemented")
        }

        override fun post(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
            return NanoHTTPD.newFixedLengthResponse(session.parseRequestBody())
        }

        override fun delete(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
            TODO("Not yet implemented")
        }
    }


    @Before
    fun setup() {
        embeddedHttpd = RouterNanoHTTPD(0)
        embeddedHttpd.start()
        httpClient = HttpClient(OkHttp) {
            install(JsonFeature)
        }
    }

    @After
    fun tearDown() {
        embeddedHttpd.stop()
        httpClient.close()
    }

    @Test
    fun givenPutRequestWithBody_whenParseRequestBodyCalled_thenShouldReturnContent() {
        embeddedHttpd.addRoute("/body", BodyEchoResponder::class.java)
        runBlocking {
            val response = httpClient.put<String>("http://localhost:${embeddedHttpd.listeningPort}/body") {
                body = "Hello World"
            }

            Assert.assertEquals("Got body back in response", "Hello World",
                response)
        }
    }

    @Test
    fun givenSmallPostRequestWithBody_whenParseRequestCalled_thenShouldReturnContent() {
        embeddedHttpd.addRoute("/body", BodyEchoResponder::class.java)
        runBlocking {
            val response = httpClient.post<String>("http://localhost:${embeddedHttpd.listeningPort}/body") {
                body = "Hello World"
            }

            Assert.assertEquals("Got body back in response", "Hello World",
                    response)
        }
    }

    @Test
    fun givenLargerPostRequestWithBody_whenParseRequestCalled_thenShouldReturnContent() {
        embeddedHttpd.addRoute("/body", BodyEchoResponder::class.java)
        val textContent = BufferedReader(InputStreamReader(this::class.java.getResourceAsStream("/com/ustadmobile/port/sharedse/xapi/statementWithProgress.json"))).use {
            it.readText()
        }
        runBlocking {
            val response = httpClient.post<String>("http://localhost:${embeddedHttpd.listeningPort}/body") {
                body = textContent
            }

            Assert.assertEquals("Got body back in response", textContent,
                    response)
        }
    }

}