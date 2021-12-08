package com.ustadmobile.core.catalog.contenttype

import io.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.contentjob.ContentPluginIds.CONTAINER_DOWNLOAD_PLUGIN
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.core.io.ConcatenatedEntry
import com.ustadmobile.core.io.ConcatenatedInputStream2
import com.ustadmobile.core.io.ext.readAndSaveToDir
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherOkHttp.Companion.SUFFIX_HEADER
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherOkHttp.Companion.SUFFIX_PART
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.base64EncodedToHexString
import com.ustadmobile.core.util.ext.distinctMd5sSortedAsJoinedQueryParam
import com.ustadmobile.core.util.ext.distinctMds5sSorted
import com.ustadmobile.core.util.ext.linkExistingContainerEntries
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorIdentityHashCode
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.ustadmobile.lib.db.entities.ContentJobItem
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import kotlinx.coroutines.*
import io.ktor.client.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import io.ktor.client.request.get
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.closeQuietly
import okhttp3.internal.headersContentLength
import java.io.File
import java.io.FileInputStream
import java.io.SequenceInputStream
import java.net.URI
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class ContainerDownloadContentJob(
        private var context: Any,
        private val endpoint: Endpoint,
        override val di: DI
) : ContentPlugin {

    override val pluginId: Int
        get() = CONTAINER_DOWNLOAD_PLUGIN
    override val supportedMimeTypes: List<String>
        get() = listOf()
    override val supportedFileExtensions: List<String>
        get() = listOf()

    private val totalDownloadSize = AtomicLong(0L)

    private val bytesSoFar = AtomicLong(0L)

    private val logPrefix: String by lazy {
        "ContainerDownloaderJobOkHttp @${this.doorIdentityHashCode}"
    }

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val containerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val httpClient: HttpClient = di.direct.instance()

    suspend fun progressUpdater(
            listener: ContentJobProgressListener,
            contentJobItem: ContentJobItem
    ) = coroutineScope {
        while(isActive) {
            contentJobItem.cjiItemTotal = totalDownloadSize.get()
            contentJobItem.cjiItemProgress = bytesSoFar.get()
            listener.onProgress(contentJobItem)
            delay(500L)
        }
    }



    override suspend fun extractMetadata(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        TODO("Not yet implemented")
    }

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ContentJobProcessContext, progress: ContentJobProgressListener): ProcessResult {
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")

        return withContext(Dispatchers.Default){

            val progressUpdaterJob = async(Dispatchers.Default) {
                progressUpdater(progress, contentJobItem)
            }

            val downloadFolderUri: String = jobItem.contentJob?.toUri
                    ?: containerDir.toURI().toString()
            val containerFolderDoorUri = DoorUri.parse(downloadFolderUri)
            val containerFolderFile = containerFolderDoorUri.toFile()

            val startTime = System.currentTimeMillis()
            var httpResponse: Response? = null
            var inStream: ConcatenatedInputStream2? = null

            try {

                val containerEntryListUrl = UMFileUtil.joinPaths(endpoint.url,
                        "$CONTAINER_ENTRY_LIST_PATH?containerUid=${contentJobItem.cjiContainerUid}")
                val containerEntryListVal: List<ContainerEntryWithMd5> = httpClient.get(
                        containerEntryListUrl)
                val containerEntriesList = mutableListOf<ContainerEntryWithMd5>()
                containerEntriesList.addAll(containerEntryListVal)

                val containerEntriesPartition = db.linkExistingContainerEntries(contentJobItem.cjiContainerUid,
                        containerEntryListVal)


                //We always download in md5sum (hex) alphabetical order, such that a partial download will
                //be resumed as expected.
                val md5sToDownload = containerEntriesPartition.entriesWithoutMatchingFile.distinctMds5sSorted()

                val md5ExpectedList = md5sToDownload.toMutableList()
                val firstMd5 = md5sToDownload.first().base64EncodedToHexString()

                val firstFile = File(containerFolderFile, "$firstMd5$SUFFIX_PART")
                val firstFileHeader = File(containerFolderFile, "$firstMd5$SUFFIX_HEADER")
                val firstFilePartPresent = firstFile.exists() && firstFileHeader.exists()


                //check and see if the first file is already here
                val inputUrl = "${endpoint.url}/${ContainerEntryFileDao.ENDPOINT_CONCATENATEDFILES2}/download"
                Napier.d("$logPrefix Download ${md5sToDownload.size} container files $inputUrl -> $downloadFolderUri")

                val requestBuilder = Request.Builder()
                        .url(inputUrl)

                if(firstFilePartPresent) {
                    val startFrom = firstFile.length() + firstFileHeader.length()
                    Napier.d("$logPrefix partial download from $startFrom")
                    requestBuilder.addHeader("range", "bytes=${startFrom}-")
                }

                requestBuilder.method("POST",
                        containerEntriesPartition.entriesWithoutMatchingFile.distinctMd5sSortedAsJoinedQueryParam().toRequestBody(
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

                val readAndSaveResult = inStream.readAndSaveToDir(containerFolderFile, containerFolderFile, db,
                        bytesSoFar, containerEntriesPartition.entriesWithoutMatchingFile, md5ExpectedList, logPrefix)
                val totalBytesRead= readAndSaveResult.totalBytesRead
                val payloadExpected = (totalDownloadSize.get() - (md5sToDownload.size * ConcatenatedEntry.SIZE))

                Napier.d("$logPrefix done downloaded ${bytesSoFar.get() - bytesToSkipWriting}/expected ${payloadExpected} bytes" +
                        " in ${System.currentTimeMillis() - startTime}ms")

                 return@withContext if(totalBytesRead == payloadExpected) {
                        ProcessResult(JobStatus.COMPLETE)
                    }else {
                        ProcessResult(JobStatus.FAILED)
                    }

            }finally {
                progressUpdaterJob.cancel()
                httpResponse?.closeQuietly()
                inStream?.closeQuietly()
                contentJobItem.cjiItemTotal = totalDownloadSize.get()
                contentJobItem.cjiItemProgress = bytesSoFar.get()
                progress.onProgress(contentJobItem)
            }

        }

    }

    companion object {

        internal const val CONTAINER_ENTRY_LIST_PATH = "ContainerEntryList/findByContainerWithMd5"


    }


}