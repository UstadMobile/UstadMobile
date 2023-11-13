package com.ustadmobile.core.contentformats

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.buffer
import okio.source
import java.io.InputStream
import java.net.URLConnection

class ContentDispatcher() : Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse {
        try {

            val contentStream: InputStream = javaClass.getResourceAsStream(request.path!!)
                    ?: return MockResponse().setResponseCode(404)

            val source = contentStream.source().buffer()
            val buffer = Buffer()
            source.readAll(buffer)

            val contentLength = buffer.size

            val mimetype = URLConnection.guessContentTypeFromName(request.path!!)

            val response = MockResponse().setResponseCode(200)
            response.setHeader("Content-Length", contentLength)
            if(mimetype != null){
                response.setHeader("Content-Type", mimetype)
            }
            if (!request.method.equals("HEAD", ignoreCase = true))
                response.setBody(buffer)

            return response

        } catch (e: Exception) {
            e.printStackTrace()
            MockResponse().setResponseCode(500)
            System.err.println(request.path)
        }

        return MockResponse().setResponseCode(404)
    }


}