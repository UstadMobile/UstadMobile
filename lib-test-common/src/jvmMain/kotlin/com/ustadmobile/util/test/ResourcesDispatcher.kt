package com.ustadmobile.util.test

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.buffer
import okio.source

/**
 * Simple MockWebServer dispatcher that will serve responses from the resources.
 */
class ResourcesDispatcher(
    private val clazz: Class<*>,
    private val responseTransform: (MockResponse) -> MockResponse = { it },
): Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val buffer = Buffer()
        val path = request.path ?: return MockResponse().setResponseCode(500)
        val contentInStream = clazz.getResourceAsStream(path)
            ?: return MockResponse().setResponseCode(404)
        val contentSource = contentInStream.source().buffer()
        contentSource.readAll(buffer)
        val contentLength = buffer.size

        return responseTransform(
            MockResponse()
                .setBody(buffer)
                .addHeader("content-length", contentLength)
        )
    }
}
