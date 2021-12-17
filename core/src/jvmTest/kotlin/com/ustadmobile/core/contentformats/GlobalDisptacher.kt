package com.ustadmobile.core.contentformats

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.Okio
import okio.buffer
import okio.source
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.IOException
import java.nio.file.Files


val globalDisptacher: Dispatcher = object : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {

        try {
            val requestPath = request.path ?: ""
            if (requestPath.contains("json")) {

                val fileName = requestPath.substringAfter("json")

                val data = javaClass.getResourceAsStream(fileName)
                        ?: return MockResponse().setResponseCode(404)
                val body = IOUtils.toString(data, "UTF-8")
                val response = MockResponse().setResponseCode(200)
                response.addHeader("ETag", "UTF-8".hashCode())
                if (!request.method.equals("HEAD", ignoreCase = true))
                    response.setBody(body)

                return response

            } else if (requestPath.contains("content")) {

                val fileLocation = requestPath.substringAfter("content")
                val videoIn = javaClass.getResourceAsStream(fileLocation)
                        ?: return MockResponse().setResponseCode(404)
                val source =  videoIn.source().buffer()
                val buffer = Buffer()
                source.readAll(buffer)

                val mimeType = Files.probeContentType(File(fileLocation).toPath())

                val response = MockResponse().setResponseCode(200)
                response.setHeader("ETag", (buffer.size.toString() + "ABC").hashCode())
                response.setHeader("Content-Type", mimeType)
                if (!request.method.equals("HEAD", ignoreCase = true))
                    response.setBody(buffer)

                return response

            }


        } catch (e: IOException) {
            e.printStackTrace()
        }

        return MockResponse().setResponseCode(404)
    }
}
