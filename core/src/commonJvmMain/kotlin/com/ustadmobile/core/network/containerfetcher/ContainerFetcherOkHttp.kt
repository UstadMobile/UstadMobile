package com.ustadmobile.core.network.containerfetcher

import io.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerEntryFileCommon
import com.ustadmobile.core.io.ConcatenatedEntry
import com.ustadmobile.core.io.ConcatenatedInputStream2
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorIdentityHashCode
import kotlinx.coroutines.*
import org.kodein.di.*
import java.io.*
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import com.ustadmobile.core.io.ext.parseKmpUriStringToFile
import com.ustadmobile.core.io.ext.readAndSaveToDir
import com.ustadmobile.core.util.ext.base64EncodedToHexString
import com.ustadmobile.core.util.ext.distinctMd5sSortedAsJoinedQueryParam
import com.ustadmobile.core.util.ext.distinctMds5sSorted
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.closeQuietly
import okhttp3.internal.headersContentLength

class ContainerFetcherOkHttp(
    val request: ContainerFetcherRequest2,
    val listener: ContainerFetcherListener2?,
    override val di: DI
): DIAware {

    private val totalDownloadSize = AtomicLong(0L)

    private val bytesSoFar = AtomicLong(0L)

    private val db: UmAppDatabase by di.on(Endpoint(request.siteUrl)).instance(tag = DoorTag.TAG_DB)

    private var startTime: Long = 0

    private val logPrefix: String by lazy {
        "ContainerDownloaderJobOkHttp @${this.doorIdentityHashCode}"
    }

    suspend fun download(): Int = coroutineScope {
        val progressUpdaterJob = launch {
            while(isActive) {
                listener?.onProgress(request, bytesSoFar.get(), totalDownloadSize.get())
                delay(500L)
            }
        }


        startTime = System.currentTimeMillis()
        var downloadStatus = 0
        var httpResponse: Response? = null
        var inStream: ConcatenatedInputStream2? = null

        //We always download in md5sum (hex) alphabetical order, such that a partial download will
        //be resumed as expected.
        val md5sToDownload = request.entriesToDownload.distinctMds5sSorted()

        val md5ExpectedList = md5sToDownload.toMutableList()
        val firstMd5 = md5sToDownload.first().base64EncodedToHexString()
        val destDirFile = request.destDirUri.parseKmpUriStringToFile()


        //TODO: Avoid duplicating this
        val firstFile = File(destDirFile, "$firstMd5$SUFFIX_PART")
        val firstFileHeader = File(destDirFile, "$firstMd5$SUFFIX_HEADER")
        val firstFilePartPresent = firstFile.exists() && firstFileHeader.exists()

        try {
            //check and see if the first file is already here
            val inputUrl = "${request.mirrorUrl}/${ContainerEntryFileCommon.ENDPOINT_CONCATENATEDFILES2}/download"
            Napier.d("$logPrefix Download ${md5sToDownload.size} container files $inputUrl -> ${request.destDirUri}")

            val requestBuilder = Request.Builder()
                .url(inputUrl)

            if(firstFilePartPresent) {
                val startFrom = firstFile.length() + firstFileHeader.length()
                Napier.d("$logPrefix partial download from $startFrom")
                requestBuilder.addHeader("range", "bytes=${startFrom}-")
            }


            requestBuilder.method("POST",
                request.entriesToDownload.distinctMd5sSortedAsJoinedQueryParam().toRequestBody(
                    "application/json".toMediaType()))

            val okHttpClient: OkHttpClient = di.direct.instance()
            httpResponse = okHttpClient.newCall(requestBuilder.build()).execute()
            val httpBody = httpResponse.body ?: throw IllegalStateException("HTTP response has no body!")
            inStream = ConcatenatedInputStream2(if(firstFilePartPresent) {
                //If the first file exists, we must read the contents of it's header, then the payload,
                //so that the checuksum will match

                //checking if this might be causing an issue due to reading from a file that is appended to...
                val inputStreamList = listOf(FileInputStream(firstFileHeader),
                    FileInputStream(firstFile), httpBody.byteStream())

                SequenceInputStream(Collections.enumeration(inputStreamList))
            }else {
                httpBody.byteStream()
            })

            val bytesToSkipWriting = firstFile.length() + firstFileHeader.length()
            totalDownloadSize.set((firstFile.length() + firstFileHeader.length()) + httpResponse.headersContentLength())

            val readAndSaveResult = inStream.readAndSaveToDir(
                destDirFile, destDirFile, bytesSoFar,
                md5ExpectedList, logPrefix, di.direct.instance()
            )
            val totalBytesRead= readAndSaveResult.totalBytesRead
            val payloadExpected = (totalDownloadSize.get() - (md5sToDownload.size * ConcatenatedEntry.SIZE))

            downloadStatus = if(totalBytesRead == payloadExpected) {
                JobStatus.COMPLETE
            }else {
                JobStatus.QUEUED
            }
            Napier.d("$logPrefix done downloaded ${bytesSoFar.get() - bytesToSkipWriting}/expected ${payloadExpected} bytes" +
                " in ${System.currentTimeMillis() - startTime}ms")
        }catch(e: Exception) {
            Napier.e("$logPrefix exception downloading", e)
            throw e
        }finally {
            progressUpdaterJob.cancel()
            httpResponse?.closeQuietly()
            inStream?.closeQuietly()
            listener?.onProgress(request, bytesSoFar.get(), totalDownloadSize.get())
        }

        downloadStatus
    }

    companion object {

        const val SUFFIX_PART = ".part"

        const val SUFFIX_HEADER = ".header"

    }

}