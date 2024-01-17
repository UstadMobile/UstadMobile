package com.ustadmobile.libuicompose.util.downloadurl

import com.ustadmobile.door.ext.toDoorUri
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicLong


data class DownloadUrlState(
    val fileUri: String? = null,
    val error: String? = null,
    val totalBytes: Long = 0,
    val bytesTransferred: Long = 0,
    val status: Status = Status.IN_PROGRESS,
) {

    enum class Status {
        IN_PROGRESS, COMPLETED, FAILED
    }

}

/**
 * This is a simple function that can be used within an LaunchedEffect to access a remote url as a
 * file. Because lib-cache will store any cacheable content as a file, we can simply request the
 * file using the X-Request-Storage-Path header.
 *
 * It provides determinative progress during the download for progress indicators.
 */
suspend fun downloadUrlViaCacheAndGetLocalUri(
    url: String,
    httpClient: HttpClient,
    progressInterval: Int = 500,
    onStateChange: (DownloadUrlState) -> Unit,
) {
    withContext(Dispatchers.IO) {
        val totalBytes = AtomicLong(1)
        val bytesTransferred = AtomicLong(0)
        val progressUpdateJob = launch {
            onStateChange(
                DownloadUrlState(
                    fileUri = null,
                    error = null,
                    totalBytes= totalBytes.get(),
                    bytesTransferred = bytesTransferred.get(),
                    status = DownloadUrlState.Status.IN_PROGRESS,
                )
            )

            delay(progressInterval.toLong())
        }

        try {
            val response = httpClient.get(url) {
                header("X-Request-Storage-Path", "true")
            }

            totalBytes.set(response.headers["content-length"]?.toLong() ?: 1)

            val buffer = ByteArray(8192)

            var bytesRead = 0
            response.bodyAsChannel().toInputStream().use { inStream ->
                while (isActive && inStream.read(buffer).also { bytesRead = it } != -1) {
                    bytesTransferred.set(bytesTransferred.get() + bytesRead)
                }
            }
            progressUpdateJob.cancel()

            /*
             * There won't be a storage path if it was the first time the url was retrieved, so we
             * need to go and ask again using the head method
             */
            val filePath = response.headers["X-Storage-Path"]
                ?: httpClient.head(url) {
                    header("X-Request-Storage-Path", "true")
                    header("cache-control", "only-if-cached")
                }.headers["X-Storage-Path"]
                ?: throw IllegalStateException("DownloadUrl: No x-storage-path for $url")

            onStateChange(
                DownloadUrlState(
                    fileUri = File(filePath).toDoorUri().toString(),
                    bytesTransferred = bytesTransferred.get(),
                    totalBytes = totalBytes.get(),
                    status = DownloadUrlState.Status.COMPLETED,
                )
            )
        }catch(e: Throwable) {
            Napier.w(throwable = e) { "DownloadUrl: $url Fail" }
            onStateChange(
                DownloadUrlState(
                    error = e.message ?: "Other error",
                    bytesTransferred = bytesTransferred.get(),
                    totalBytes = totalBytes.get(),
                    status = DownloadUrlState.Status.COMPLETED,
                )
            )
        }finally {
            progressUpdateJob.cancel()
        }
    }

}
