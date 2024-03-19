package com.ustadmobile.core.domain.cachestoragepath

import com.ustadmobile.core.domain.compress.CompressionType
import com.ustadmobile.core.io.await
import com.ustadmobile.core.io.ext.bodyAsDecodedByteStream
import com.ustadmobile.core.io.ext.uncompress
import com.ustadmobile.core.util.ext.displayFilename
import com.ustadmobile.core.util.retryAsync
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.libcache.UstadCache
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicLong


class GetStoragePathForUrlUseCaseCommonJvm (
    private val okHttpClient: OkHttpClient,
    private val cache: UstadCache,
    private val tmpDir: File,
): GetStoragePathForUrlUseCase{

    override suspend fun invoke(
        url: String,
        progressInterval: Int,
        onStateChange: (GetStoragePathForUrlUseCase.GetStoragePathForUrlState) -> Unit,
        inflateToTmpFileIfCompressed: Boolean,
    ): GetStoragePathForUrlUseCase.GetStoragePathResult = withContext(Dispatchers.IO) {
        val totalBytes = AtomicLong(1)
        val bytesTransferred = AtomicLong(0)
        val progressUpdateJob = launch {
            while(isActive) {
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
        }

        try {
            val call = okHttpClient.newCall(Request.Builder()
                .addHeader("accept-encoding", "gzip")
                .url(url)
                .build())
            val response = call.await()

            totalBytes.set(response.headers["content-length"]?.toLong() ?: -1)

            val buffer = ByteArray(8192)

            var bytesRead = 0
            response.bodyAsDecodedByteStream()?.use { inStream ->
                while (isActive && inStream.read(buffer).also { bytesRead = it } != -1) {
                    bytesTransferred.set(bytesTransferred.get() + bytesRead)
                }
            }
            progressUpdateJob.cancel()

            val responseFile = retryAsync(maxAttempts = 10, interval = 200) {
                val cacheEntry = cache.getCacheEntry(url)
                val filePath = cacheEntry?.storageUri
                    ?: throw IllegalStateException("no filepath for $url")
                val currentSize = File(filePath).length()
                val expectedFileSize = cacheEntry.storageSize

                if(expectedFileSize > 0 && currentSize != expectedFileSize)
                    throw IllegalStateException("File $filePath not ready")

                File(filePath)
            }

            val responseCompression = CompressionType.byHeaderVal(
                response.header("content-encoding")
            )

            val (file, compressionType)  = if(
                responseCompression != CompressionType.NONE && inflateToTmpFileIfCompressed
            ) {
                val uncompressedTmpFile = File(tmpDir, url.displayFilename(false))

                FileInputStream(responseFile).uncompress(responseCompression).use { inStream ->
                    FileOutputStream(uncompressedTmpFile).use { outStream ->
                        inStream.copyTo(outStream)
                    }
                }

                uncompressedTmpFile to CompressionType.NONE
            }else {
                responseFile to responseCompression
            }

            Napier.v { "DownloadUrl: $url is now accessible on $responseFile" }

            val fileUriStr = file.toDoorUri().toString()
            onStateChange(
                GetStoragePathForUrlUseCase.GetStoragePathForUrlState(
                    fileUri = fileUriStr,
                    bytesTransferred = bytesTransferred.get(),
                    totalBytes = totalBytes.get(),
                    status = GetStoragePathForUrlUseCase.GetStoragePathForUrlState.Status.COMPLETED,
                )
            )

            GetStoragePathForUrlUseCase.GetStoragePathResult(fileUriStr, compressionType)
        }catch(e: Throwable) {
            if(e !is CancellationException) {
                Napier.w(throwable = e) { "DownloadUrl: $url Fail" }
                onStateChange(
                    GetStoragePathForUrlUseCase.GetStoragePathForUrlState(
                        error = e.message ?: "Other error",
                        bytesTransferred = bytesTransferred.get(),
                        totalBytes = totalBytes.get(),
                        status = GetStoragePathForUrlUseCase.GetStoragePathForUrlState.Status.FAILED,
                    )
                )
            }

            throw e
        }finally {
            progressUpdateJob.cancel()
        }
    }
}