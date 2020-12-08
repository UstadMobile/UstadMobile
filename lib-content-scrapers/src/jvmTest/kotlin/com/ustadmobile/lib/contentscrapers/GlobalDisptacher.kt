package com.ustadmobile.lib.contentscrapers

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.Okio
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.IOException
import java.nio.file.Files


val globalDisptacher: Dispatcher = object : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {

        try {

            if (request.path.contains("json")) {

                val fileName = request.path.substringAfter("json")

                val data = javaClass.getResourceAsStream(fileName)
                        ?: return MockResponse().setResponseCode(404)
                val body = IOUtils.toString(data, ScraperConstants.UTF_ENCODING)
                val response = MockResponse().setResponseCode(200)
                response.setHeader("ETag", ScraperConstants.UTF_ENCODING.hashCode())
                if (!request.method.equals("HEAD", ignoreCase = true))
                    response.setBody(body)

                return response

            } else if (request.path.contains("content")) {

                val fileLocation = request.path.substringAfter("content")
                val videoIn = javaClass.getResourceAsStream(fileLocation)
                        ?: return MockResponse().setResponseCode(404)
                val source = Okio.buffer(Okio.source(videoIn))
                val buffer = Buffer()
                source.readAll(buffer)

                val mimeType = Files.probeContentType(File(fileLocation).toPath())

                val response = MockResponse().setResponseCode(200)
                response.setHeader("ETag", (buffer.size().toString() + "ABC").hashCode())
                response.setHeader("Content-Type", mimeType)
                if (!request.method.equals("HEAD", ignoreCase = true))
                    response.body = buffer

                return response

            }


        } catch (e: IOException) {
            e.printStackTrace()
        }

        return MockResponse().setResponseCode(404)
    }
}
