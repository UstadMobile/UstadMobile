package com.ustadmobile.util.test

import okhttp3.*
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import okio.Buffer
import okio.BufferedSink
import okio.Okio
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

// Based on https://gist.github.com/paour/bf58afa8969640e36e9bd87f85a6c5df
class ReverseProxyDispatcher(private val serverUrl: HttpUrl) : Dispatcher() {
    private val client: OkHttpClient

    val numTimesToFail = AtomicInteger(0)

    var throttleBytesPerPeriod = 0L

    var throttlePeriod = 0L

    var throttlePeriodUnit: TimeUnit = TimeUnit.MILLISECONDS

    init {
        client = OkHttpClient.Builder().build()
    }

    @Throws(InterruptedException::class)
    override fun dispatch(request: RecordedRequest): MockResponse {
        val proxiedUri = HttpUrl.parse("http://example.com" + request.path)!!
                .newBuilder()
                .scheme(serverUrl.scheme())
                .host(serverUrl.host())
                .port(serverUrl.port())
                .build()

        val requestBuilder = Request.Builder()
                .url(proxiedUri)
                .headers(request.headers)

        if (request.bodySize != 0L) {
            requestBuilder.method(request.method, object : RequestBody() {
                override fun contentType(): MediaType {
                    return MediaType.parse(request.getHeader("Content-Type"))!!
                }

                @Throws(IOException::class)
                override fun writeTo(sink: BufferedSink) {
                    request.body.readAll(sink)
                }
            })
        }

        var response: Response? = null
        try {
            response = client.newCall(requestBuilder.build()).execute()
        } catch (e: IOException) {
            response = null
        }

        if(response == null) {
            return MockResponse()
                    .setStatus("Reverse proxy error")
                    .setResponseCode(500)
        }else {
            val responseBytes = response.body()!!.bytes()
            val fileBuffer = Okio.buffer(Okio.source(ByteArrayInputStream(responseBytes)))
            val outBuffer = Buffer()
            fileBuffer.readFully(outBuffer, responseBytes.size.toLong())

            val mockResponse = MockResponse()
                    .setBody(outBuffer)
                    .setResponseCode(response!!.code())

            //Chunked transfer encoding won't work here.
            //This was related to ktor not handling partial response... may or may not be needed..
            response.headers().names().filter { !it.equals("transfer-encoding", ignoreCase = true) }
                    .forEach { mockResponse.addHeader(it,response.header(it)) }

            if(numTimesToFail.decrementAndGet() >= 0) {
                mockResponse.setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY)
            }

            if(throttleBytesPerPeriod != 0L) {
                mockResponse.throttleBody(throttleBytesPerPeriod, throttlePeriod, throttlePeriodUnit)
            }

            return mockResponse
        }

    }
}
