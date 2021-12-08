package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.contentjob.ContentPluginIds.CONTAINER_DOWNLOAD_PLUGIN
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherOkHttp
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherRequest2
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UMFileUtil
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
import io.ktor.client.request.get
import java.io.File
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

            try {
                val containerEntryListUrl = UMFileUtil.joinPaths(endpoint.url,
                        "$CONTAINER_ENTRY_LIST_PATH?containerUid=${contentJobItem.cjiContainerUid}")
                val containerEntryListVal: List<ContainerEntryWithMd5> = httpClient.get(
                        containerEntryListUrl)
                val containerEntriesList = mutableListOf<ContainerEntryWithMd5>()
                containerEntriesList.addAll(containerEntryListVal)

                val containerEntriesPartition = db.linkExistingContainerEntries(
                    contentJobItem.cjiContainerUid, containerEntryListVal)


                //We always download in md5sum (hex) alphabetical order, such that a partial download will
                //be resumed as expected.
                val containerFetchRequest = ContainerFetcherRequest2(
                    containerEntriesPartition.entriesWithoutMatchingFile, endpoint.url, endpoint.url,
                    downloadFolderUri)
                val status = ContainerFetcherOkHttp(containerFetchRequest, null, di).download()
                ProcessResult(status)
            }finally {
                progressUpdaterJob.cancel()
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