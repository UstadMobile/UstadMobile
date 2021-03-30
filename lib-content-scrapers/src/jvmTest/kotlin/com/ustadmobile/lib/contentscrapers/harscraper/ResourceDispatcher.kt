package com.ustadmobile.lib.contentscrapers.harscraper

import com.ustadmobile.core.util.UMFileUtil
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.buffer
import okio.source
import java.io.InputStream
import java.net.URLConnection


class ResourceDispatcher(private val basePath: String): Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse {
        try {
            val requestPath = request.path ?: ""
            val path = UMFileUtil.joinPaths(basePath, requestPath)
            val contentStream: InputStream = javaClass.getResourceAsStream(path)
                    ?: return MockResponse().setResponseCode(404)

            val source = contentStream.source().buffer()
            val buffer = Buffer()
            source.readAll(buffer)

            val contentLength = buffer.size

            val mimetype = URLConnection.guessContentTypeFromName(path)

            val response = MockResponse().setResponseCode(200)
            response.setHeader("Content-Length", contentLength)
            response.setHeader("Content-Type", mimetype)
            response.setBody(buffer)
            return response

        } catch (e: Exception) {
            e.printStackTrace()
            System.err.println(request.path)
        }

        return MockResponse().setResponseCode(404)
    }
}