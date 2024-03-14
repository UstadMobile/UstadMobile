package com.ustadmobile.util.test

import com.ustadmobile.lib.util.parseRangeRequestHeader
import com.ustadmobile.util.test.ext.gzipped
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream

/**
 * Simple MockWebServer dispatcher that will serve responses from the resources.
 */
open class ResourcesDispatcher(
    private val clazz: Class<*>,
    private val contentEncoding: String? = null,
    private val responseTransform: (MockResponse) -> MockResponse = { it },
): Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val buffer = Buffer()
        val path = request.path ?: return MockResponse().setResponseCode(500)
        val contentBytes = clazz.getResourceAsStream(path)!!.readAllBytes().let {
            when(contentEncoding) {
                null -> it
                "gzip" -> it.gzipped()
                else -> throw IllegalArgumentException("unsupported encoding: $contentEncoding")
            }
        } ?: return MockResponse().setResponseCode(404)
        val contentInStream = ByteArrayInputStream(contentBytes)

        val rangeResponse = request.getHeader("range")?.let {
            parseRangeRequestHeader(it, contentBytes.size.toLong())
        }

        val contentSource = if(rangeResponse != null) {
            RangeInputStream(contentInStream, rangeResponse.fromByte, rangeResponse.toByte)
                .source().buffer()
        }else {
            contentInStream.source().buffer()
        }

        contentSource.readAll(buffer)
        val contentLength = rangeResponse?.actualContentLength ?: contentBytes.size.toLong()

        return responseTransform(
            MockResponse()
                .setResponseCode(rangeResponse?.statusCode ?: 200)
                .setBody(buffer)
                .addHeader("content-length", contentLength)
                .apply {
                    if(contentEncoding != null)
                        addHeader("content-encoding", contentEncoding)
                }
        )
    }
}
