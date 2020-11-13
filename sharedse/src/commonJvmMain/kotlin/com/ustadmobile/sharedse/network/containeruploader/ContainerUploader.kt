package com.ustadmobile.sharedse.network.containeruploader

import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.ContainerUploaderRequest
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.ContainerImportJob
import com.ustadmobile.port.sharedse.ext.generateConcatenatedFilesResponse
import com.ustadmobile.port.sharedse.impl.http.RangeInputStream
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.*
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min
import java.lang.System
import kotlin.coroutines.coroutineContext

class ContainerUploader(val request: ContainerUploaderRequest,
                        val chunkSize: Int = DEFAULT_CHUNK_SIZE,
                        override val di: DI) : DIAware {

    private val contentLength = AtomicLong(0L)

    private val bytesSoFar = AtomicLong(0L)

    private val db: UmAppDatabase by di.on(Endpoint(request.endpointUrl)).instance(tag = UmAppDatabase.TAG_DB)

    private val logPrefix: String
        get() = "[ContainerUploader #${request.uploadJobUid} - ${System.identityHashCode(this)}]"

    private val UPLOADER_JOB_TAG = "ContainerUploader"

    suspend fun progressUpdater() = coroutineScope {
        while (isActive) {
            Napier.d(tag = UPLOADER_JOB_TAG, message = "upload progress updating at value ${bytesSoFar.get()}")
            db.containerImportJobDao.updateProgress(bytesSoFar.get(), contentLength.get(), request.uploadJobUid)
            delay(500L)
        }
    }

    suspend fun upload(): Int {
        val progressUpdaterJob = GlobalScope.async { progressUpdater() }
        var downloadStatus = JobStatus.FAILED

        var urlConnection: HttpURLConnection? = null
        var inStream: InputStream? = null
        var rangeStream: RangeInputStream? = null
        try {

            urlConnection = URL(UMFileUtil.joinPaths(request.uploadToUrl, "/createSession/")).openConnection() as HttpURLConnection

            urlConnection.requestMethod = "GET"
            urlConnection.connect()

            val uploadJob = db.containerImportJobDao.findByUid(request.uploadJobUid)
                    ?: ContainerImportJob()

            val fileSize = db.containerEntryFileDao.generateConcatenatedFilesResponse(request.fileList).contentLength

            if (uploadJob.cijSessionId.isNullOrEmpty()) {
                val sessionId = String(UMIOUtils.readStreamToByteArray(urlConnection.inputStream))
                uploadJob.cijSessionId = sessionId
                uploadJob.cijBytesSoFar = 0
                uploadJob.cijContentLength = fileSize
                db.containerImportJobDao.update(uploadJob)
            }

            urlConnection.disconnect()

            Napier.i( message = { "$logPrefix Start upload URL=${request.uploadToUrl} " +
                    "SessionID=${uploadJob.cijSessionId} FileList=${request.fileList}" } )

            val start = uploadJob.cijBytesSoFar

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
                            readRange += inStream.read(buffer, readRange, sizeToRead - readRange)
                        }


                        val end = bytesSoFar.get() + readRange - 1


                        urlConnection = URL(UMFileUtil.joinPaths(request.uploadToUrl, "/receiveData/")).openConnection() as HttpURLConnection
                        urlConnection.connectTimeout = 5000
                        urlConnection.doOutput = true
                        urlConnection.requestMethod = "PUT"
                        urlConnection.setRequestProperty("Content-Length", readRange.toString())
                        urlConnection.setRequestProperty("Range", "bytes=${bytesSoFar.get()}-$end")
                        urlConnection.setRequestProperty("SessionId", uploadJob.cijSessionId)
                        urlConnection.outputStream.write(buffer)
                        urlConnection.outputStream.flush()
                        urlConnection.outputStream.close()
                        urlConnection.connect()

                        val responseCode = urlConnection.responseCode
                        Napier.d({"$logPrefix sent bytes=${bytesSoFar.get()}-$end"})

                        if (urlConnection.errorStream != null) {
                            error = true
                            errorMessage = String(urlConnection.errorStream.readBytes())
                        }

                        urlConnection.disconnect()


                    } while (responseCode != HttpStatusCode.NoContent.value)

                val endedAt = bytesSoFar.get() + readRange
                Napier.d(tag = UPLOADER_JOB_TAG, message = "uploading, last ended at $endedAt")
                uploadJob.cijBytesSoFar = endedAt
                bytesSoFar.set(endedAt)

            }

            downloadStatus = if (fileSize == bytesSoFar.get()) {
                JobStatus.COMPLETE
            } else if(!coroutineContext.isActive) {
                JobStatus.PAUSED
            }else {
                JobStatus.FAILED
            }

            uploadJob.cijJobStatus = downloadStatus
            db.containerImportJobDao.updateStatus(downloadStatus, uploadJob.cijUid)

        }catch(e: Exception) {
            Napier.e("$logPrefix exception", e)
        } finally {
            progressUpdaterJob.cancel()
            inStream?.close()
            rangeStream?.close()
            urlConnection?.disconnect()
        }

        return downloadStatus
    }

    companion object {

        const val DEFAULT_CHUNK_SIZE = 1024 * 200

    }

}