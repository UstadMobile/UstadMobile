package com.ustadmobile.core.torrent

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.createSymLink
import com.ustadmobile.core.util.ext.linkExistingContainerEntries
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItem
import io.ktor.client.*
import io.ktor.client.request.*
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

    private val ustadTorrentManager: UstadTorrentManagerImpl = di.direct.instance<UstadTorrentManager>() as UstadTorrentManagerImpl

    override val pluginId: Int
        get() = TODO("Not yet implemented")

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

        return MetadataResult(contentEntry as ContentEntryWithLanguage)
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

        // find all containerEntryFilesWithMd5
        val containerEntryListUrl = UMFileUtil.joinPaths(endpoint.url,
                "$CONTAINER_ENTRY_LIST_PATH?containerUid=${containerUid}")
        val containerEntryListVal = httpClient.get<List<ContainerEntryWithMd5>>(
                containerEntryListUrl)

        //find any files that might have been downloaded before
        val containerEntriesPartition = db.linkExistingContainerEntries(containerUid,
                containerEntryListVal)

        //link them to the new container path
        containerEntriesPartition.entriesWithMatchingFile.forEach {
            val oldPath = it.containerEntryFile?.cefPath ?: return@forEach
            val target = File(containerFilesFolder, File(oldPath).name)
            createSymLink(oldPath, target.path)
        }

        ustadTorrentManager.addTorrent(containerUid)


        return ProcessResult(200)

    }

    companion object {

        internal const val CONTAINER_ENTRY_LIST_PATH = "ContainerEntryList/findByContainerWithMd5"

    }

}

