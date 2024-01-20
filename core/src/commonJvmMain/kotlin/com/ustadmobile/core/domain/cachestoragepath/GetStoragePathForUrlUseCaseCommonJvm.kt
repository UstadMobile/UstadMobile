package com.ustadmobile.core.domain.cachestoragepath

import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.libcache.UstadCache
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicLong


class GetStoragePathForUrlUseCaseCommonJvm (
    private val httpClient: HttpClient,
    private val cache: UstadCache,
): GetStoragePathForUrlUseCase{

    override suspend fun invoke(
        url: String,
        progressInterval: Int,
        onStateChange: (GetStoragePathForUrlUseCase.GetStoragePathForUrlState) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val totalBytes = AtomicLong(1)
        val bytesTransferred = AtomicLong(0)
        val progressUpdateJob = launch {
            onStateChange(
                GetStoragePathForUrlUseCase.GetStoragePathForUrlState(
                    fileUri = null,
                    error = null,
                    totalBytes = totalBytes.get(),
                    bytesTransferred = bytesTransferred.get(),
                    status = GetStoragePathForUrlUseCase.GetStoragePathForUrlState.Status.IN_PROGRESS,
                )
            )

            delay(progressInterval.toLong())
        }

        try {
            val response = httpClient.get(url)

            totalBytes.set(response.headers["content-length"]?.toLong() ?: 1)

            val buffer = ByteArray(8192)

            var bytesRead = 0
            response.bodyAsChannel().toInputStream().use { inStream ->
                while (isActive && inStream.read(buffer).also { bytesRead = it } != -1) {
                    bytesTransferred.set(bytesTransferred.get() + bytesRead)
                }
            }
            progressUpdateJob.cancel()

            val filePath = cache.getCacheEntry(url)?.storageUri
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
                GetStoragePathForUrlUseCase.GetStoragePathForUrlState(
                    fileUri = fileUri,
                    bytesTransferred = bytesTransferred.get(),
                    totalBytes = totalBytes.get(),
                    status = GetStoragePathForUrlUseCase.GetStoragePathForUrlState.Status.COMPLETED,
                )
            )

            fileUri
        }catch(e: Throwable) {
            Napier.w(throwable = e) { "DownloadUrl: $url Fail" }
            onStateChange(
                GetStoragePathForUrlUseCase.GetStoragePathForUrlState(
                    error = e.message ?: "Other error",
                    bytesTransferred = bytesTransferred.get(),
                    totalBytes = totalBytes.get(),
                    status = GetStoragePathForUrlUseCase.GetStoragePathForUrlState.Status.FAILED,
                )
            )
            throw e
        }finally {
            progressUpdateJob.cancel()
        }
    }
}