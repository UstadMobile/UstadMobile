package com.ustadmobile.sharedse.network

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.port.sharedse.ext.generateConcatenatedFilesResponse
import com.ustadmobile.port.sharedse.impl.http.RangeInputStream
import com.ustadmobile.sharedse.network.containerfetcher.ConnectionOpener
import com.ustadmobile.sharedse.network.containeruploader.ContainerUploaderListener
import com.ustadmobile.sharedse.network.containeruploader.ContainerUploaderRequest
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

class ContainerUploader(val request: ContainerUploaderRequest,
                        val listener: ContainerUploaderListener?,
                        val chunkSize: Int = CHUNK_SIZE,
                        override val di: DI) : DIAware {

    private val contentLength = AtomicLong(0L)

    private val bytesSoFar = AtomicLong(0L)

    private val networkManager: NetworkManagerBle by instance()

    private val db: UmAppDatabase by di.on(Endpoint(request.endpointUrl)).instance(tag = UmAppDatabase.TAG_DB)

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

            var urlConnection: HttpURLConnection? = null
            var inStream: InputStream? = null
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

                urlConnection.requestMethod = "GET"
                urlConnection.connect()

                val uploadJob = db.containerUploadJobDao.findByUid(request.uploadJobUid)
                if (uploadJob.sessionId.isNullOrEmpty()) {
                    val sessionId = String(UMIOUtils.readStreamToByteArray(urlConnection.inputStream))
                    uploadJob.sessionId = sessionId
                    uploadJob.bytesSoFar = 0
                    db.containerUploadJobDao.update(uploadJob)
                }

                urlConnection.disconnect()

                val start = uploadJob.bytesSoFar

                val fileSize = db.containerEntryFileDao.generateConcatenatedFilesResponse(request.fileList).contentLength

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

                        val response = db.containerEntryFileDao.generateConcatenatedFilesResponse(request.fileList)
                        inStream = response.dataSrc ?: throw Exception()
                        rangeStream = RangeInputStream(inStream, bytesSoFar.get(), bytesSoFar.get() + chunkSize)

                        val remaining = fileSize - bytesSoFar.get()
                        val sizeToRead = min(chunkSize, remaining.toInt())
                        val buffer = ByteArray(sizeToRead)

                        while (readRange < sizeToRead) {
                            readRange += inStream.read(buffer)
                        }

                        val end = bytesSoFar.get() + readRange - 1

                        urlConnection = connectionOpener(URL(request.uploadToUrl))
                        urlConnection.doOutput = true
                        urlConnection.requestMethod = "PUT"
                        urlConnection.setRequestProperty("Content-Length", readRange.toString())
                        urlConnection.setRequestProperty("Range", "bytes=${bytesSoFar.get()}-$end")
                        urlConnection.setRequestProperty("SessionId", uploadJob.sessionId)
                        urlConnection.outputStream.write(buffer)
                        urlConnection.outputStream.flush()
                        urlConnection.outputStream.close()
                        urlConnection.connect()

                        val responseCode = urlConnection.responseCode

                        if (responseCode == 400) {
                            errorMessage = String(urlConnection.errorStream.readBytes())
                        }

                        urlConnection.disconnect()

                    } while (responseCode != HttpStatusCode.NoContent.value && errorMessage?.isNotEmpty() == true)

                    val endedAt = bytesSoFar.get() + readRange
                    bytesSoFar.set(endedAt)

                    db.containerUploadJobDao.updateBytesSoFarForSessionWithId(uploadJob.sessionId
                            ?: "", endedAt)

                }

                downloadStatus = if (isActive) {
                    JobStatus.COMPLETE
                } else {
                    JobStatus.PAUSED
                }

                db.containerUploadJobDao.setJobStatus(downloadStatus, uploadJob.sessionId ?: "")

            } finally {
                progressUpdaterJob.cancel()
                listener?.onProgress(request, bytesSoFar.get(), contentLength.get())
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