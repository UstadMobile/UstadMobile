package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.port.sharedse.impl.http.RangeInputStream
import com.ustadmobile.sharedse.network.containerfetcher.ConnectionOpener
import com.ustadmobile.sharedse.network.containeruploader.ContainerUploaderListener
import com.ustadmobile.sharedse.network.containeruploader.ContainerUploaderRequest
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

@Serializable
data class UploadStatus(val sessionId: String, val uploadedTo: Long)

class ContainerUploader(val request: ContainerUploaderRequest,
                        val listener: ContainerUploaderListener?,
                        val chunkSize: Int = CHUNK_SIZE,
                        override val di: DI) : DIAware {

    private val contentLength = AtomicLong(0L)

    private val bytesSoFar = AtomicLong(0L)

    private val networkManager: NetworkManagerBle by instance()

    suspend fun progressUpdater() = coroutineScope {
        while (isActive) {
            listener?.onProgress(request, bytesSoFar.get(), contentLength.get())
            delay(500L)
        }
    }

    suspend fun upload(): Int {
        return coroutineScope {
            val progressUpdaterJob = async { progressUpdater() }
            var downloadStatus = 0
            val uploadFile = File(request.fromFile)
            val jsonFile = File(uploadFile.parentFile, "${uploadFile.name}.uploadInfo")

            var urlConnection: HttpURLConnection? = null
            var inStream: FileInputStream? = null
            var rangeStream: RangeInputStream? = null
            try {
                val localConnectionOpenerVal =
                        (networkManager as NetworkManagerWithConnectionOpener).localConnectionOpener

                val connectionOpener: ConnectionOpener = if (localConnectionOpenerVal != null) {
                    localConnectionOpenerVal
                } else {
                    { url -> url.openConnection() as HttpURLConnection }
                }

                urlConnection = connectionOpener(URL(request.uploadToUrl))

                if (!jsonFile.exists()) {
                    urlConnection.requestMethod = "GET"
                    urlConnection.connect()

                    val sessionId = String(UMIOUtils.readStreamToByteArray(urlConnection.inputStream))
                    val status = UploadStatus(sessionId, 0)
                    val jsonStatus = Json.stringify(UploadStatus.serializer(), status)
                    jsonFile.writeText(jsonStatus)
                    urlConnection.disconnect()
                }

                // read from file and into uploadStatus obj
                val jsonContent = jsonFile.readText()
                var uploadStatus = Json.parse(UploadStatus.serializer(), jsonContent)

                val fileToUpload = File(request.fromFile)
                val fileSize = fileToUpload.length()
                val start = uploadStatus.uploadedTo

                bytesSoFar.set(start)
                contentLength.set(fileSize)

                for (uploadedTo in start..fileSize step chunkSize.toLong()) {

                    var readRange = 0
                    var errorMessage: String? = null
                    do {


                        if (errorMessage?.isNotEmpty() == true) {
                            // reset the bytes if the server is more ahead than recorded
                            if (errorMessage.startsWith("Range should start from:")) {
                                errorMessage = errorMessage.substringAfter(":")
                                bytesSoFar.set(errorMessage.toLong())
                            }
                        }

                        inStream = FileInputStream(fileToUpload)
                        rangeStream = RangeInputStream(inStream, bytesSoFar.get(), bytesSoFar.get() + chunkSize)

                        val remaining = fileSize - bytesSoFar.get()
                        val sizeToRead = min(chunkSize, remaining.toInt())
                        val buffer = ByteArray(sizeToRead)

                        while (readRange < sizeToRead) {
                            readRange += rangeStream.read(buffer)
                        }

                        val end = bytesSoFar.get() + readRange - 1

                        urlConnection = connectionOpener(URL(request.uploadToUrl))
                        urlConnection.doOutput = true
                        urlConnection.requestMethod = "PUT"
                        urlConnection.setRequestProperty("Content-Length", readRange.toString())
                        urlConnection.setRequestProperty("Range", "bytes=${bytesSoFar.get()}-$end")
                        urlConnection.setRequestProperty("SessionId", uploadStatus.sessionId)
                        urlConnection.outputStream.write(buffer)
                        urlConnection.outputStream.flush()
                        urlConnection.outputStream.close()
                        urlConnection.connect()

                        val responseCode = urlConnection.responseCode

                        if (responseCode == 400) {
                            errorMessage = String(urlConnection.errorStream.readBytes())
                        }

                        urlConnection.disconnect()

                    } while (responseCode != HttpStatusCode.NoContent.value)

                    val endedAt = bytesSoFar.get() + readRange
                    bytesSoFar.set(endedAt)

                    uploadStatus = UploadStatus(uploadStatus.sessionId, endedAt)
                    val statusJson = Json.stringify(UploadStatus.serializer(), uploadStatus)
                    jsonFile.writeText(statusJson)

                }

                downloadStatus = if (isActive) {
                    JobStatus.COMPLETE
                } else {
                    JobStatus.PAUSED
                }


            } finally {
                progressUpdaterJob.cancel()
                listener?.onProgress(request, bytesSoFar.get(), contentLength.get())
                inStream?.close()
                inStream?.close()
                rangeStream?.close()
                urlConnection?.disconnect()

            }
            downloadStatus
        }

    }

    companion object {

        const val CHUNK_SIZE = 1024 * 8

    }

}