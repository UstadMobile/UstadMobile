package com.ustadmobile.core.torrent

import com.turn.ttorrent.client.FileMetadataProvider
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.getUnCompressedSize
import com.ustadmobile.core.io.ext.isGzipped
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.createSymLink
import com.ustadmobile.core.util.ext.base64EncodedToHexString
import com.ustadmobile.core.util.ext.maxQueryParamListSize
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContainerEntry
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_NONE
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItem
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
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

class ContainerTorrentDownloadJob(private val endpoint: Endpoint, override val di: DI) : ContentPlugin {

    private val httpClient: HttpClient = di.direct.instance()

    val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val containerDir = di.direct.instance<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val torrentDir = di.direct.instance<File>(tag = DiTag.TAG_TORRENT_DIR)

    private val ustadTorrentManager: UstadTorrentManager = di.direct.instance<UstadTorrentManager>()

    override val pluginId: Int
        get() = PLUGIN_ID

    override val supportedMimeTypes: List<String>
        get() = listOf("application/ustad-container")
    override val supportedFileExtensions: List<String>
        get() = listOf(".container")

    override suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): MetadataResult? {

        // check valid uri format, valid endpoint, valid container
        val containerUid = uri.uri.toString().substringAfterLast("/").toLong()

        val container = repo.containerDao.findByUid(containerUid) ?: return null

        val contentEntry = repo.contentEntryDao.findByUid(container.containerContentEntryUid)
                ?: throw IllegalArgumentException("no entry found from container")

        return MetadataResult(contentEntry as ContentEntryWithLanguage, PLUGIN_ID)
    }

    override suspend fun processJob(jobItem: ContentJobItem, process: ProcessContext, progress: ContentJobProgressListener): ProcessResult {

        val containerUid = jobItem.cjiContainerUid

        // check if torrent file already exists
        val torrentFile = File(torrentDir, "$containerUid.torrent")
        if(!torrentFile.exists()){
            val path = UMFileUtil.joinPaths(endpoint.url, "containers/$containerUid")
            val torrentFileStream = httpClient.get<InputStream>(path)
            torrentFile.writeBytes(torrentFileStream.readBytes())
        }

        val downloadFolderPath = jobItem.toUri ?: containerDir.path

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
            val oldPath = it.cefPath ?: return@forEach
            val target = File(containerFilesFolder, File(oldPath).name)
            if(!target.exists()){
                createSymLink(oldPath, target.path)
            }
        }

        ustadTorrentManager.addTorrent(containerUid)

        val torrentDeferred = CompletableDeferred<Boolean>()
        val torrentListener = object: TorrentDownloadListener{
            override fun onComplete() {
                torrentDeferred.complete(true)
            }
        }
        GlobalScope.launch(Dispatchers.Default){
            while(true){
                delay(100)
                if(!isActive){
                    torrentDeferred.completeExceptionally(CancellationException())
                }
            }
        }

        ustadTorrentManager.addDownloadListener(containerUid, torrentListener)
        torrentDeferred.await()
        ustadTorrentManager.removeDownloadListener(containerUid, torrentListener)

        val existingMd5s = existingFiles.mapNotNull { it.cefMd5 }.toSet()
        val entriesThatGotDownloaded = fileNameList
                .filter { it !in existingMd5s || it.equals(MANIFEST_FILE_NAME) }


        entriesThatGotDownloaded.forEach {
            ContainerEntryFile().apply {
                cefMd5 = it
                val cefFile = File(containerFilesFolder, it)
                cefPath = cefFile.path
                ceTotalSize = cefFile.getUnCompressedSize()
                ceCompressedSize = cefFile.length()
                compression = if(cefFile.isGzipped()) COMPRESSION_GZIP else COMPRESSION_NONE
                cefUid = db.containerEntryFileDao.insert(this)
            }
        }

        val manifestFile = File(containerFilesFolder, MANIFEST_FILE_NAME)
        if(!manifestFile.exists()) throw IllegalArgumentException("no manifest file found")

        val manifestJson = manifestFile.readText()
        val manifest: Map<String, List<String>> = Json.decodeFromString(MapSerializer(String.serializer(),
                ListSerializer(String.serializer())), manifestJson)
        manifest.entries.forEach { entry ->
            if(entry.key == MANIFEST_FILE_NAME){
                return@forEach
            }
            entry.value.forEach {  path ->
                ContainerEntry().apply {
                    ceContainerUid = containerUid
                    cePath = path
                    ceCefUid = db.containerEntryFileDao.findEntryByMd5Sum(entry.key.base64EncodedToHexString())?.cefUid
                            ?: throw IllegalArgumentException("missed a file during download ${entry.key} with path $path")
                    ceUid = db.containerEntryDao.insert(this)
                }
            }
        }


        return ProcessResult(200)
    }



    companion object {

        internal const val MANIFEST_FILE_NAME = "USTAD-MANIFEST.json"



        const val PLUGIN_ID = 10
    }

}

