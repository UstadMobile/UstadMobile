package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.contentjob.ContentPluginIds.CONTAINER_DOWNLOAD_PLUGIN
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherListener2
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherOkHttp
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherRequest2
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorIdentityHashCode
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.sumByLong
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
import kotlinx.serialization.json.Json
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.io.ContainerManifest
import com.ustadmobile.core.io.ext.FILE_EXTENSION_CE_JSON
import com.ustadmobile.core.io.ext.toContainerEntryWithMd5
import com.ustadmobile.retriever.Retriever
import com.ustadmobile.retriever.RetrieverRequest
import com.ustadmobile.retriever.fetcher.RetrieverListener
import com.ustadmobile.retriever.fetcher.RetrieverProgressEvent
import com.ustadmobile.retriever.RetrieverStatusUpdateEvent
import java.io.BufferedReader
import java.io.FileReader
import io.github.aakira.napier.Napier
import com.ustadmobile.door.ext.concurrentSafeListOf
import java.io.IOException

class ContainerDownloadPlugin(
    context: Any,
    endpoint: Endpoint,
    di: DI
) : AbstractContentEntryPlugin(context, endpoint, di) /*, RetrieverListener */{

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

    private val containerStorageManager: ContainerStorageManager by di.on(endpoint).instance()

    private val httpClient: HttpClient = di.direct.instance()

    private val json: Json by di.instance()

    private val retriever: Retriever by di.instance()

    override suspend fun extractMetadata(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        return extractMetadata(ContentEntryDetailView.VIEW_NAME, uri)
    }

    private class ContainerFetcherProgressListenerAdapter(
        private val contentJobProgressListener: ContentJobProgressListener,
        private val jobItem: ContentJobItem
    ): ContainerFetcherListener2 {
        override fun onStart(request: ContainerFetcherRequest2) {

        }

        override fun onProgress(
            request: ContainerFetcherRequest2,
            bytesDownloaded: Long,
            contentLength: Long
        ) {
            jobItem.cjiItemProgress = bytesDownloaded
            jobItem.cjiItemTotal = contentLength
            contentJobProgressListener.onProgress(jobItem)
        }

        override fun onDone(request: ContainerFetcherRequest2, status: Int) {

        }
    }

    override suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progress: ContentJobProgressListener
    ): ProcessResult {
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")

        updateContentEntryUidsFromSourceUrlIfNeeded(contentJobItem)
        if(contentJobItem.cjiContainerUid == 0L) {
            contentJobItem.cjiContainerUid = db.containerDao
                .getMostRecentContainerUidForContentEntryAsync(contentJobItem.cjiContentEntryUid)
            db.contentJobItemDao.updateContentJobItemContainer(contentJobItem.cjiUid,
                contentJobItem.cjiContainerUid)
        }

        val containerSize = db.containerDao.findSizeByUid(contentJobItem.cjiContainerUid)
        if(containerSize <= 0L) {
            //if this is still 0, it means there is no recent container
            return ProcessResult(JobStatus.FAILED, "Refusing to download an empty container")
        }

        return withContext(Dispatchers.Default){
            val downloadFolderUri: String = jobItem.contentJob?.toUri
                    ?: containerStorageManager.storageList.first().dirUri

            val downloadFolderDir = DoorUri.parse(downloadFolderUri).toFile()

            //This download will go into a subdirectory. This avoids potential concurrency
            // problems if other downloads are running simultaneously getting an entry with the
            // same md5.
            val containerDownloadDir: File = File(downloadFolderDir,
                contentJobItem.cjiContainerUid.toString())
            containerDownloadDir.takeIf { !it.exists() }?.mkdirs()

            try {
                val manifestUrl = endpoint
                    .url("/Container/Manifest/${contentJobItem.cjiContainerUid}")
                val manifestFileDest: File = File(containerDownloadDir, "MANIFEST")

                val manifestRetrieverListener = object: RetrieverListener {
                    override suspend fun onRetrieverProgress(
                        retrieverProgressEvent: RetrieverProgressEvent
                    ) {

                    }

                    override suspend fun onRetrieverStatusUpdate(
                        retrieverStatusEvent: RetrieverStatusUpdateEvent
                    ) {

                    }
                }

                retriever.retrieve(listOf(RetrieverRequest(manifestUrl,
                    manifestFileDest.absolutePath, null)), manifestRetrieverListener)
                val bufferedReader = BufferedReader(FileReader(manifestFileDest))
                val manifest = bufferedReader.use { reader ->
                    ContainerManifest.parseFromLines {
                        reader.readLine()
                    }
                }
                Napier.d("Downloaded manifest for ${contentJobItem.cjiContainerUid}")

                val entriesWithChecksums = manifest.toContainerEntryList()

                //TODO: filter out completed files already saved into the directory.
                val containerEntriesPartition: ContainerEntryWithFilePartition =
                    db.partitionContainerEntries(entriesWithChecksums)

                val entriesToDownload = containerEntriesPartition.entriesWithoutMatchingFile
                    .filterContainerEntryNotInDirectory(containerDownloadDir)
                val retrieverRequests = entriesToDownload.map {
                    it.toRetrieverRequest(endpoint, containerDownloadDir)
                }.distinctBy { it.originUrl }

                val downloadedEntries : MutableList<ContainerEntryWithContainerEntryFile> =
                    concurrentSafeListOf()

                //When Retriever tells us a particular entry was completed,
                // write the ContainerEntryFile to disk
                val retrieverListener: RetrieverListener = object: RetrieverListener {
                    override suspend fun onRetrieverProgress(retrieverProgressEvent: RetrieverProgressEvent) {

                    }

                    override suspend fun onRetrieverStatusUpdate(
                        retrieverStatusEvent: RetrieverStatusUpdateEvent
                    ) {
                        //HERE - when an entry is successful, save json
                        if(retrieverStatusEvent.status == Retriever.STATUS_SUCCESSFUL) {
                            val completedIntegrity = "sha256-${retrieverStatusEvent.checksums?.sha256?.encodeBase64()}"
                            val completedEntry = containerEntriesPartition
                                .entriesWithoutMatchingFile.firstOrNull {
                                    it.containerEntryFile?.cefIntegrity == completedIntegrity
                                }

                            val completedEntryFile = completedEntry?.containerEntryFile
                            val completedMd5 = completedEntryFile?.cefMd5
                            if (completedEntryFile == null || completedMd5 == null) {
                                Napier.w("$logPrefix Retriever completed an entry, but can't find it or md5")
                                return
                            }


                            completedEntryFile.cefPath = File(containerDownloadDir,
                                completedMd5.base64EncodedToHexString()).absolutePath
                            //write to disk
                            val jsonFileName = completedEntryFile.cefMd5
                                ?.base64EncodedToHexString() + FILE_EXTENSION_CE_JSON
                            val entryFileJson = json.encodeToString(ContainerEntryFile.serializer(),
                                completedEntryFile)
                            File(containerDownloadDir, jsonFileName).writeText(entryFileJson)
                            downloadedEntries += completedEntry
                        }else if(retrieverStatusEvent.status == Retriever.STATUS_FAILED) {
                            Napier.e("$logPrefix Retriever failure on $retrieverStatusEvent.url",
                                retrieverStatusEvent.exception)
                        }

                    }
                }

                val status= if(containerEntriesPartition.entriesWithoutMatchingFile.isEmpty()) {
                    JobStatus.COMPLETE
                }else {
                    retriever.retrieve(retrieverRequests, retrieverListener)

                    val downloadedMd5s = downloadedEntries.mapNotNull { it.containerEntryFile?.cefMd5 }
                    val pendingEntries = entriesToDownload.filter {
                        it.containerEntryFile?.cefMd5 !in downloadedMd5s
                    }

                    if(pendingEntries.isNotEmpty())
                        throw IOException("${logPrefix }retriever.retrieve was called, " +
                            "but not everything was downloaded. Pending = " +
                            pendingEntries.joinToString { "${it.cePath} (${it.containerEntryFile?.cefMd5})" } )

                    JobStatus.COMPLETE
                }

                //Add the entries to the database
                if(status == JobStatus.COMPLETE) {
                    val containerEntryFileEntities = containerDownloadDir
                        .getContentEntryJsonFilesFromDir(json)

                    val containerEntryListVal = entriesWithChecksums.map {
                        it.toContainerEntryWithMd5()
                    }

                    db.withDoorTransactionAsync(UmAppDatabase::class) { txDb ->
                        txDb.containerEntryFileDao.insertListAsync(containerEntryFileEntities)
                        txDb.linkExistingContainerEntries(contentJobItem.cjiContainerUid,
                            containerEntryListVal)
                    }

                    containerDownloadDir.deleteAllContentEntryJsonFiles()
                }

                ProcessResult(status)
            }finally {
                //progress
            }



//            val progressAdapter = ContainerFetcherProgressListenerAdapter(progress, contentJobItem)

//            try {
//                val containerEntryListUrl = UMFileUtil.joinPaths(endpoint.url,
//                        "$CONTAINER_ENTRY_LIST_PATH?containerUid=${contentJobItem.cjiContainerUid}")
//                val containerEntryListVal: List<ContainerEntryWithMd5> = httpClient.get(
//                        containerEntryListUrl)
//                val containerEntriesList = mutableListOf<ContainerEntryWithMd5>()
//                containerEntriesList.addAll(containerEntryListVal)
//
//
//                val containerEntriesPartition = db.partitionContainerEntriesWithMd5(
//                    containerEntryListVal)
//
//                val entriesToDownload = containerEntriesPartition.entriesWithoutMatchingFile.
//                    filterNotInDirectory(containerDownloadDir)
//
//                contentJobItem.cjiItemProgress = containerEntriesPartition.existingFiles.sumByLong {
//                    it.ceCompressedSize
//                }
//                contentJobItem.cjiItemTotal = containerSize
//                progress.onProgress(contentJobItem)
//
//                //We always download in md5sum (hex) alphabetical order, such that a partial download will
//                //be resumed as expected.
//                val containerFetchRequest = ContainerFetcherRequest2(
//                    entriesToDownload, endpoint.url, endpoint.url,
//                    containerDownloadUri.toString())
//                val status = if(entriesToDownload.isNotEmpty()) {
//                    ContainerFetcherOkHttp(containerFetchRequest, progressAdapter,
//                        di).download()
//                }else {
//                    JobStatus.COMPLETE
//                }
//
//                val containerEntryFileEntities = containerDownloadDir
//                    .getContentEntryJsonFilesFromDir(json)
//
//                db.withDoorTransactionAsync(UmAppDatabase::class) { txDb ->
//                    txDb.containerEntryFileDao.insertListAsync(containerEntryFileEntities)
//
//                    //now everything is downloaded, link it
//                    txDb.linkExistingContainerEntries(
//                        contentJobItem.cjiContainerUid, containerEntryListVal)
//                }
//
//                containerDownloadDir.deleteAllContentEntryJsonFiles()
//
//                ProcessResult(status)
//            }finally {
//                contentJobItem.cjiItemTotal = totalDownloadSize.get()
//                contentJobItem.cjiItemProgress = bytesSoFar.get()
//                progress.onProgress(contentJobItem)
//            }
//
        }

    }

    companion object {

        internal const val CONTAINER_ENTRY_LIST_PATH = "ContainerEntryList/findByContainerWithMd5"


    }


}