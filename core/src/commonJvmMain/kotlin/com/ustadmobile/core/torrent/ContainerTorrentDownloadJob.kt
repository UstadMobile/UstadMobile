package com.ustadmobile.core.torrent

import com.turn.ttorrent.client.FileMetadataProvider
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.ConnectivityException
import com.ustadmobile.core.io.ext.getUnCompressedSize
import com.ustadmobile.core.io.ext.isGzipped
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.createSymLink
import com.ustadmobile.core.util.ext.base64EncodedToHexString
import com.ustadmobile.core.util.ext.maxQueryParamListSize
import com.ustadmobile.core.util.ext.withWifiLock
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_NONE
import kotlinx.serialization.builtins.serializer
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.io.InputStream
import java.net.URI
import kotlin.coroutines.cancellation.CancellationException

class ContainerTorrentDownloadJob(
        private var context: Any,
        private val endpoint: Endpoint,
        override val di: DI
) : ContentPlugin {

    private val httpClient: HttpClient = di.direct.instance()

    val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val containerDir = di.direct.instance<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val torrentDir by di.on(endpoint).instance<File>(tag = DiTag.TAG_TORRENT_DIR)

    private val ustadTorrentManager: UstadTorrentManager = di.direct.instance<UstadTorrentManager>()

    override val pluginId: Int
        get() = PLUGIN_ID

    override val supportedMimeTypes: List<String>
        get() = listOf("application/ustad-container")
    override val supportedFileExtensions: List<String>
        get() = listOf(".container")

    override suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): MetadataResult? {

        // check valid uri format, valid endpoint, valid container
        val containerUid = uri.uri.toString().substringAfterLast("/").toLongOrNull() ?: return null

        val container = repo.containerDao.findByUid(containerUid) ?: return null

        val contentEntry = repo.contentEntryDao.findByUid(container.containerContentEntryUid)
                ?: throw IllegalArgumentException("no entry found from container")

        return MetadataResult(contentEntry as ContentEntryWithLanguage, PLUGIN_ID)
    }

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ProcessContext, progress: ContentJobProgressListener): ProcessResult {
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")
        val containerUid = contentJobItem.cjiContainerUid

        // check if torrent file already exists
        val torrentFile = File(torrentDir, "$containerUid.torrent")
        return withContext(Dispatchers.Default) {

            try {

                if (!torrentFile.exists()) {
                    val path = UMFileUtil.joinPaths(endpoint.url, "containers/$containerUid")
                    val torrentFileStream = httpClient.get<InputStream>(path)
                    torrentFile.writeBytes(torrentFileStream.readBytes())
                }

                val downloadFolderPath = jobItem.contentJob?.toUri?.let { URI(it).path }
                        ?: containerDir.path

                val containerFilesFolder = File(downloadFolderPath, containerUid.toString())
                containerFilesFolder.mkdirs()

                val metadataProvider = FileMetadataProvider(torrentFile.absolutePath)
                val fileNameList = metadataProvider
                        .torrentMetadata
                        .files
                        .map { File(it.relativePathAsString).name }

                val existingFiles = db.containerEntryFileDao.findEntriesByMd5SumsSafeAsync(fileNameList,
                        db.maxQueryParamListSize)

                //link them to the new container path
                existingFiles.forEach {
                    val existingPath = it.cefPath ?: return@forEach
                    val existingFile = File(existingPath)
                    if (existingFile.name == MANIFEST_FILE_NAME) {
                        return@forEach
                    }
                    val target = File(containerFilesFolder, existingFile.name)
                    if (!target.exists()) {
                        createSymLink(existingPath, target.path)
                    }
                }

                withWifiLock(context) {

                    val startTime = System.currentTimeMillis()
                    ustadTorrentManager.addTorrent(containerUid, containerFilesFolder.path)

                    val torrentDeferred = CompletableDeferred<Boolean>()
                    val torrentListener = object : TorrentDownloadListener {
                        override fun onComplete() {
                            contentJobItem.cjiItemProgress = (contentJobItem.cjiItemTotal * 0.75).toLong()
                            progress.onProgress(contentJobItem)
                            println("downloaded torrent in ${System.currentTimeMillis() - startTime} ms")
                            torrentDeferred.complete(true)
                        }

                        override fun onProgress(progressSize: Int) {
                            contentJobItem.cjiItemProgress = (progressSize * 0.75).toLong()
                            progress.onProgress(contentJobItem)
                        }
                    }
                    GlobalScope.launch(Dispatchers.Default) {
                        while (true) {
                            delay(100)
                            if (!isActive) {
                                torrentDeferred.completeExceptionally(CancellationException())
                            }
                        }
                    }

                    ustadTorrentManager.addDownloadListener(containerUid, torrentListener as TorrentDownloadListener)
                    torrentDeferred.await()
                    ustadTorrentManager.removeDownloadListener(containerUid)

                }


                val existingMd5s = existingFiles.mapNotNull { it.cefMd5 }.toSet()
                val entriesThatGotDownloaded = fileNameList
                        .filter { it !in existingMd5s || it.equals(MANIFEST_FILE_NAME) }

                val newContainerEntryFiles = mutableListOf<ContainerEntryFile>()
                entriesThatGotDownloaded.forEach {
                    val entryFile = ContainerEntryFile().apply {
                        cefMd5 = it
                        val cefFile = File(containerFilesFolder, it)
                        cefPath = cefFile.path
                        ceTotalSize = cefFile.getUnCompressedSize()
                        ceCompressedSize = cefFile.length()
                        compression = if (cefFile.isGzipped()) COMPRESSION_GZIP else COMPRESSION_NONE
                    }
                    newContainerEntryFiles.add(entryFile)
                }
                db.containerEntryFileDao.insertList(newContainerEntryFiles)

                val manifestFile = File(containerFilesFolder, MANIFEST_FILE_NAME)
                if (!manifestFile.exists()) throw IllegalArgumentException("no manifest file found")

                val manifestJson = manifestFile.readText()
                val manifest: ContainerManifest = Json.decodeFromString(
                        ContainerManifest.serializer(), manifestJson)
                val newContainerEntry = mutableListOf<ContainerEntry>()
                manifest.entryMap?.entries?.forEach { entry ->
                    if (entry.key == MANIFEST_FILE_NAME) {
                        return@forEach
                    }
                    entry.value.forEach { path ->
                        val cefUid = db.containerEntryFileDao.findEntryByMd5Sum(entry.key.base64EncodedToHexString())?.cefUid
                                ?: throw IllegalArgumentException("missed a file during download ${entry.key} with path $path")
                        val containerEntry = ContainerEntry().apply {
                            ceContainerUid = containerUid
                            cePath = path
                            ceCefUid = cefUid
                        }
                        newContainerEntry.add(containerEntry)
                    }
                }
                db.containerEntryDao.insertListAsync(newContainerEntry)

                contentJobItem.cjiItemProgress = contentJobItem.cjiItemTotal
                progress.onProgress(contentJobItem)

            } catch (c: CancellationException) {
                throw c
            }

            return@withContext ProcessResult(JobStatus.COMPLETE)
        }
    }



    companion object {

        internal const val MANIFEST_FILE_NAME = "USTAD-MANIFEST.json"

        const val PLUGIN_ID = 10
    }

}

