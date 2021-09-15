package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.contentjob.ProcessResult
import com.ustadmobile.core.contentjob.ext.processMetadata
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addFileToContainer
import com.ustadmobile.core.io.ext.addTorrentFileFromContainer
import com.ustadmobile.core.io.ext.getLocalUri
import com.ustadmobile.core.torrent.UstadTorrentManager
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ShrinkUtils
import com.ustadmobile.core.util.ext.fitWithin
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.db.entities.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.lang.IllegalArgumentException
import com.ustadmobile.door.ext.toDoorUri

class VideoTypePluginJvm(private var context: Any, private val endpoint: Endpoint, override val di: DI): VideoTypePlugin() {

    private val VIDEO_JVM = "VideoPluginJVM"

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    val torrentDir: File by di.on(endpoint).instance(tag = DiTag.TAG_TORRENT_DIR)

    private val ustadTorrentManager: UstadTorrentManager by di.on(endpoint).instance()

    override suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): MetadataResult? {
        return getEntry(uri, process)
    }

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ProcessContext, progress: ContentJobProgressListener): ProcessResult {
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")
        withContext(Dispatchers.Default) {

            val uri = contentJobItem.sourceUri ?: throw IllegalStateException("missing uri")
            val videoUri = DoorUri.parse(uri)
            val videoFile = process.getLocalUri(videoUri, context, di).toFile()
            val contentEntryUid = processMetadata(jobItem, process, context, endpoint)
            var newVideo = File(videoFile.parentFile, "new${videoFile.nameWithoutExtension}.mp4")
            val trackerUrl = db.siteDao.getSiteAsync()?.torrentAnnounceUrl
                    ?: throw IllegalArgumentException("missing tracker url")

            val compressVideo: Boolean = process.params["compress"]?.toBoolean() ?: false

            Napier.d(tag = VIDEO_JVM, message = "conversion Params compress video is $compressVideo")

            if(compressVideo) {
                val fileVideoDimensionsAndAspectRatio = ShrinkUtils.getVideoResolutionMetadata(videoFile)
                val newVideoDimensions = Pair(fileVideoDimensionsAndAspectRatio.first, fileVideoDimensionsAndAspectRatio.second).fitWithin()
                ShrinkUtils.optimiseVideo(videoFile, newVideo, newVideoDimensions, fileVideoDimensionsAndAspectRatio.third)
            }else{
                newVideo = videoFile
            }

            val container = Container().apply {
                containerContentEntryUid = contentEntryUid
                cntLastModified = System.currentTimeMillis()
                fileSize = newVideo.length()
                this.mimeType = supportedMimeTypes.first()
                containerUid = repo.containerDao.insert(this)
            }

            val containerFolder = jobItem.contentJob?.toUri ?: defaultContainerDir.toURI().toString()
            val containerFolderUri = DoorUri.parse(containerFolder)

            repo.addFileToContainer(container.containerUid, newVideo.toDoorUri(), newVideo.name,
                    context,
                    di,
                    ContainerAddOptions(containerFolderUri))

            repo.addTorrentFileFromContainer(
                    container.containerUid,
                    DoorUri.parse(torrentDir.toURI().toString()),
                    trackerUrl, containerFolderUri
            )

            val containerUidFolder = File(containerFolderUri.toFile(), container.containerUid.toString())
            containerUidFolder.mkdirs()
            ustadTorrentManager.addTorrent(container.containerUid, containerUidFolder.path)

            container
        }


        return ProcessResult(JobStatus.COMPLETE)
    }

    suspend fun getEntry(uri: DoorUri, process: ProcessContext): MetadataResult? {
        return withContext(Dispatchers.Default){
            val file = uri.toFile()

            if(!supportedFileExtensions.any { file.name.endsWith(it, true) }) {
                return@withContext null
            }

            val fileVideoDimensions = ShrinkUtils.getVideoResolutionMetadata(file)

            if(fileVideoDimensions.first == 0 || fileVideoDimensions.second == 0){
                return@withContext null
            }

            val entry = ContentEntryWithLanguage().apply {
                this.title = file.nameWithoutExtension
                this.leaf = true
                this.contentTypeFlag = ContentEntry.TYPE_VIDEO
            }
            MetadataResult(entry, PLUGIN_ID)
        }
    }

}