package com.ustadmobile.libuicompose.util.downloadurl

import com.ustadmobile.core.domain.cachestoragepath.GetCacheStoragePathUseCase
import com.ustadmobile.door.ext.toDoorUri
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.get
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
    getCacheStoragePathUseCase: GetCacheStoragePathUseCase,
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

            val filePath = getCacheStoragePathUseCase(url)
                ?: throw IllegalStateException("no filepath for $url")

            val expectedFileSize = totalBytes.get()

            /*
             * Wait for file to settle if required. If the request just completed, the file might not
             * be ready.
             */
            for(i in 0 until 10 ) {
                val currentSize = File(filePath).length()
                if(currentSize == expectedFileSize)
                    break

                delay(200)
            }

            val fileUri = File(filePath).toDoorUri().toString()
            Napier.v { "DownloadUrl: $url is now accessible on $fileUri" }

            onStateChange(
                DownloadUrlState(
                    fileUri = fileUri,
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
