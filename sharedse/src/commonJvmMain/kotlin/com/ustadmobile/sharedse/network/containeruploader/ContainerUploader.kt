package com.ustadmobile.sharedse.network.containeruploader

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.ContainerUploadJob
import com.ustadmobile.port.sharedse.ext.generateConcatenatedFilesResponse
import com.ustadmobile.port.sharedse.impl.http.RangeInputStream
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
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min

class ContainerUploader(val request: ContainerUploaderRequest,
                        val listener: ContainerUploaderListener?,
                        val chunkSize: Int = DEFAULT_CHUNK_SIZE,
                        override val di: DI) : DIAware {

    private val contentLength = AtomicLong(0L)

    private val bytesSoFar = AtomicLong(0L)

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

                urlConnection = URL(UMFileUtil.joinPaths(request.uploadToUrl, "/createSession/")).openConnection() as HttpURLConnection

                urlConnection.requestMethod = "GET"
                urlConnection.connect()

                val uploadJob = db.containerUploadJobDao.findByUid(request.uploadJobUid)
                        ?: ContainerUploadJob()
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
                    var error = false
                    var errorMessage: String? = null
                        do {

                            if (error) {
                                // reset the bytes if the server is more ahead than recorded
                                if (errorMessage?.startsWith("Range should start from:") == true) {
                                    errorMessage = errorMessage.substringAfter(":")
                                    bytesSoFar.set(errorMessage.toLong())
                                } else {
                                    throw Exception()
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


                            urlConnection = URL(UMFileUtil.joinPaths(request.uploadToUrl, "/receiveData/")).openConnection() as HttpURLConnection
                            urlConnection.connectTimeout = 5000
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

                            if (urlConnection.errorStream != null) {
                                error = true
                                errorMessage = String(urlConnection.errorStream.readBytes())
                            }

                            urlConnection.disconnect()


                        } while (responseCode != HttpStatusCode.NoContent.value)

                    val endedAt = bytesSoFar.get() + readRange
                    bytesSoFar.set(endedAt)

                    uploadJob.bytesSoFar = endedAt
                    db.containerUploadJobDao.updateProgress(endedAt, uploadJob.cujUid)

                }

                downloadStatus = if (isActive) {
                    JobStatus.COMPLETE
                } else {
                    JobStatus.PAUSED
                }

                uploadJob.jobStatus = downloadStatus
                db.containerUploadJobDao.updateStatus(downloadStatus, uploadJob.cujUid)

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

        const val DEFAULT_CHUNK_SIZE = 1024 * 200

    }

}