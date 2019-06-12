package com.ustadmobile.sharedse.network

import com.ustadmobile.core.container.ContainerManagerCommon
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.sharedse.io.FileOutputStreamSe
import com.ustadmobile.sharedse.io.FileSe
import com.ustadmobile.sharedse.io.readText
import com.ustadmobile.sharedse.io.writeText
import com.ustadmobile.sharedse.security.MessageDigestSe
import com.ustadmobile.sharedse.security.getMessageDigestInstance
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.*
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.discardRemaining
import io.ktor.http.HttpStatusCode
import io.ktor.util.filter
import kotlinx.coroutines.delay
import kotlinx.io.IOException
import kotlinx.io.InputStream
import kotlinx.io.OutputStream
import kotlinx.serialization.json.*

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
                         private val calcMd5: Boolean = true) {

    private var md5SumBytes: ByteArray? = null

    val md5Sum: ByteArray
        get() = if(md5SumBytes == null) {
                throw IllegalStateException("Download not complete: cannot provide md5")
            } else {
                md5SumBytes!!
            }





    suspend fun download(maxAttempts: Int = 3) : Boolean {

        val httpClient = HttpClient()
        val dlInfoFile = FileSe(destinationFile + DLINFO_EXTENSION)
        val dlPartFile = FileSe(destinationFile + DLPART_EXTENSION)

        val dlInfoMap = mutableMapOf<String, String?>()

        for(i in 0..maxAttempts) {
            var inputStream = null as InputStream?
            var outStream = null as OutputStream?
            var httpResponse = null as HttpResponse?
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
                    val headResponse = httpClient.head<HttpResponse>(httpUrl)
                    val validated = VALIDATION_HEADERS.filter { headResponse.headers[it] != null
                            && it.toLowerCase() in dlInfoMap.keys }
                            .any { dlInfoMap[it.toLowerCase()] == headResponse.headers[it] }

                    headResponse.discardRemaining()


                    if(validated) {
                        startFrom = dlPartFile.length()
                        requestBuilder.header("Range", "bytes=$startFrom-")
                        UMLog.l(UMLog.DEBUG, 0, " validated to start from $startFrom bytes")
                    }else {
                        UMLog.l(UMLog.DEBUG, 0, " file exists but not validated")
                    }
                }

                requestBuilder.url(httpUrl)

                httpResponse = httpClient.get<HttpResponse>(requestBuilder)

                if(httpResponse.status !in listOf(HttpStatusCode.OK, HttpStatusCode.PartialContent)) {
                    httpResponse.discardRemaining()
                    throw IOException("Unsuccessful http request: response code was: ${httpResponse.status}")
                }

                //save the etag and last modified info (if known)
                dlInfoMap.clear()
                httpResponse.headers.filter { key, value ->  key.toLowerCase() in VALIDATION_HEADERS}.forEach { key, values ->
                    dlInfoMap[key.toLowerCase()] = values[0]
                }

                val jsonObj = JsonObject(dlInfoMap.map { entry -> Pair(entry.key, JsonPrimitive(entry.value)) }.toMap())
                dlInfoFile.writeText(Json.stringify(JsonObject.serializer(), jsonObj))

                inputStream = httpResponse.receive<InputStream>()

                if(httpResponse.status == HttpStatusCode.PartialContent) {
                    appendOutput = true
                }

                outStream = FileOutputStreamSe(dlPartFile, appendOutput)

                val messageDigestSe = getMessageDigestInstance("MD5")

                val buf = ByteArray(8 * 1024)
                var bytesRead = 0
                while (inputStream.read(buf).also { bytesRead = it } != -1) {
                    outStream.write(buf, 0, bytesRead)
                    messageDigestSe.update(buf, 0, bytesRead)
                }
                outStream.flush()
                md5SumBytes = messageDigestSe.digest()

                //now move the file to the destination
                if (dlPartFile.renameTo(FileSe(destinationFile))) {
                    return true
                } else {
                    return false
                }
            }catch(e: Exception) {
                delay(retryDelay.toLong())
            }finally {
                httpResponse?.close()
                outStream?.close()
                inputStream?.close()
            }


        }


        return false
    }




    companion object {

        private val SUBLOGTAG = "ResumableHttpDownload"

        /**
         * Extension of the file which carry file information
         */
        val DLINFO_EXTENSION = ".dlinfo"

        /**
         * Extension of the partially downloaded file.
         */
        val DLPART_EXTENSION = ".dlpart"

        private val HTTP_HEADER_LAST_MODIFIED = "last-modified"

        private val HTTP_HEADER_ETAG = "etag"

        private val HTTP_HEADER_CONTENT_RANGE = "content-range"

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