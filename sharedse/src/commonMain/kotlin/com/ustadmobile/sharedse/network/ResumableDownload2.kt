package com.ustadmobile.sharedse.network

import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.sharedse.io.*
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.*
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.discardRemaining
import io.ktor.http.HttpStatusCode
import io.ktor.util.filter
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.io.ByteBuffer
import kotlinx.io.IOException
import kotlinx.io.InputStream
import kotlinx.io.core.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.content
import kotlin.coroutines.coroutineContext

/**
 * This class will manage downloading and resuming an interrupted download. It will write a small
 * json text file next to the download itself (.dlinfo). This file stores http headers that are
 * required for validation.
 *
 * The download method will retry the given number of times. The resume can be from a previous
 * run, as long as the partial download file and dlinfo file are present a resume will be attempted.
 *
 * If the server responds with 206 partial content, output will be appended. If the server does not
 * support validation (e.g. no etag and no last modified), the download attempt will start from the
 * beginning.
 */
class ResumableDownload2(val httpUrl: String, val destinationFile: String, val retryDelay: Int = 1000,
                         private val calcMd5: Boolean = true, val httpClient: HttpClient = defaultHttpClient()) {

    var onDownloadProgress: (Long) -> Unit = {}

    private val bytesDownloaded = atomic(0L)

    suspend fun download(maxAttempts: Int = 3) : Boolean {
        try {
            UMLog.l(UMLog.INFO, 0, "ResumableDownload2: $httpUrl - starting download")
            val dlInfoFile = FileSe(destinationFile + DLINFO_EXTENSION)
            val dlPartFile = FileSe(destinationFile + DLPART_EXTENSION)

            val dlInfoMap = mutableMapOf<String, String?>()

            var responseTime = 0L
            var copyTime = 0L
            for(i in 0..maxAttempts) {
                var httpIn = null as Input?
                var fileOutput = null as Output?
                var httpResponse = null as HttpResponse?
                var headResponse = null as HttpResponse?
                val buffer = IoBuffer.Pool.borrow()
                try {
                    var startFrom = 0L
                    val dlPartFileExists = dlPartFile.exists()
                    val dlPartFileSize = if (dlPartFileExists) dlPartFile.length() else 0L
                    var appendOutput = false

                    val requestBuilder = HttpRequestBuilder()
                    if (dlPartFile.exists() && dlInfoFile.exists()) {
                        Json.parse(JsonObject.serializer(), dlInfoFile.readText()).forEach {
                            dlInfoMap[it.key.toLowerCase()] = it.value.content
                        }
                    }

                    if (dlInfoMap.any { it.key in VALIDATION_HEADERS }) {
                        headResponse = httpClient.head<HttpResponse>(httpUrl)
                        val validated = VALIDATION_HEADERS.filter { headResponse!!.headers[it] != null
                                && it.toLowerCase() in dlInfoMap.keys }
                                .any { dlInfoMap[it.toLowerCase()] == headResponse!!.headers[it] }

                        headResponse.discardRemaining()


                        if(validated) {
                            startFrom = dlPartFile.length()
                            requestBuilder.header("Range", "bytes=$startFrom-")
                            UMLog.l(UMLog.DEBUG, 0, " validated to start from $startFrom bytes")
                        }else {
                            UMLog.l(UMLog.DEBUG, 0, " file exists but not validated")
                        }
                        headResponse.close()
                        headResponse = null
                    }

                    requestBuilder.url(httpUrl)
                    val requestStart = getSystemTimeInMillis()
                    httpResponse = httpClient.get<HttpResponse>(requestBuilder)
                    responseTime = getSystemTimeInMillis() - requestStart


                    if(httpResponse.status !in listOf(HttpStatusCode.OK, HttpStatusCode.PartialContent)) {
                        httpResponse.discardRemaining()
                        throw IOException("Unsuccessful http request: response code was: ${httpResponse.status}")
                    }

                    appendOutput = (httpResponse.status == HttpStatusCode.PartialContent)
                    if(appendOutput)
                        bytesDownloaded.value = startFrom


                    //save the etag and last modified info (if known)
                    dlInfoMap.clear()
                    httpResponse.headers.filter { key, value ->  key.toLowerCase() in VALIDATION_HEADERS}.forEach { key, values ->
                        dlInfoMap[key.toLowerCase()] = values[0]
                    }

                    val jsonObj = JsonObject(dlInfoMap.map { entry -> Pair(entry.key, JsonPrimitive(entry.value)) }.toMap())
                    dlInfoFile.writeText(Json.stringify(JsonObject.serializer(), jsonObj))

                    val copyStartTime = getSystemTimeInMillis()

                    httpIn = inputStreamAsInput(httpResponse.receive<InputStream>())
                    fileOutput = createFileOutputWritableChannel(dlPartFile.getAbsolutePath(), appendOutput)


                    //This copy procedure is as per the implementation of Input.copyTo,
                    // with logic inserted to support cancellation
                    do {
                        buffer.resetForWrite()
                        val rc = httpIn.readAvailable(buffer)
                        if(!coroutineContext.isActive) {
                            throw CancellationException("coroutine canceled - not reading anymore")
                        }
                        if (rc == -1) break
                        bytesDownloaded.addAndGet(rc.toLong())
                        onDownloadProgress(bytesDownloaded.value)
                        fileOutput.writeFully(buffer)
                    } while (true)

                    copyTime = getSystemTimeInMillis() - copyStartTime

                    //Can be added for checking performance
                    //println("Response time: $responseTime ms | Copy time: $copyTime")

                    fileOutput.flush()
                    fileOutput.close()
                    fileOutput = null

                    val byteCount = bytesDownloaded.value
                    UMLog.l(UMLog.INFO, 0, "ResumableDownload2: $httpUrl - completed " +
                            "downloaded ${byteCount} . Response time = $responseTime ms, " +
                            "download time = $copyTime ms")

                    return dlPartFile.renameFile(FileSe(destinationFile))
                }catch(e: Exception) {
                    UMLog.l(UMLog.INFO, 0, "ResumableDownload2: $httpUrl - exception " +
                            " $e : ${e.message}")
                    if(e is CancellationException)
                        throw e

                    delay(retryDelay.toLong())
                }finally {
                    withContext(NonCancellable) {
                        //println("Cleaning up resumabledownload of $httpUrl")
                        httpIn?.close()
                        httpResponse?.close()
                        fileOutput?.flush()
                        fileOutput?.close()
                        headResponse?.close()
                        buffer.release(IoBuffer.Pool)
                    }
                }
            }

        }catch (e: CancellationException) {
            println("ResumableDownload2: $httpUrl - cancellation exception")
            throw e
        }

        return false
    }




    companion object {

        private val SUBLOGTAG = "ResumableHttpDownload"

        /**
         * Extension of the file which carry file information
         */
        const val DLINFO_EXTENSION = ".dlinfo"

        /**
         * Extension of the partially downloaded file.
         */
        const val DLPART_EXTENSION = ".dlpart"

        private const val HTTP_HEADER_LAST_MODIFIED = "last-modified"

        private const val HTTP_HEADER_ETAG = "etag"

        private const val HTTP_HEADER_CONTENT_RANGE = "content-range"

        /**
         * HTTP header accepted encoding type.
         */
        val HTTP_HEADER_ACCEPT_ENCODING = "Accept-Encoding"

        /**
         * HTTP encoding identity
         */
        val HTTP_ENCODING_IDENTITY = "identity"

        /**
         * The timeout to read data. The HttpUrlConnection client on Android by default seems to leave
         * this as being infinite
         */
        private val HTTP_READ_TIMEOUT = 5000

        /**
         * The timeout to connect to an http server. The HttpUrlConnection client on Android by default
         * seems to leave this as being infinite
         */
        private val HTTP_CONNECT_TIMEOUT = 10000

        val VALIDATION_HEADERS = listOf(HTTP_HEADER_ETAG, HTTP_HEADER_LAST_MODIFIED)
    }
}