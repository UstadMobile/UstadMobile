package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.contentjob.ProcessResult
import com.ustadmobile.core.util.ext.uploadContentIfNeeded
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addFileToContainer
import com.ustadmobile.core.io.ext.addTorrentFileFromContainer
import com.ustadmobile.core.io.ext.getLocalUri
import com.ustadmobile.core.io.ext.isRemote
import com.ustadmobile.core.torrent.UstadTorrentManager
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ShrinkUtils
import com.ustadmobile.core.util.ext.checkConnectivityToDoJob
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
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import org.kodein.di.direct

class VideoTypePluginJvm(private var context: Any, private val endpoint: Endpoint, override val di: DI): VideoTypePlugin() {

    private val VIDEO_JVM = "VideoPluginJVM"

    private val httpClient: HttpClient = di.direct.instance()

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
        return withContext(Dispatchers.Default) {

            val uri = contentJobItem.sourceUri ?: throw IllegalStateException("missing uri")
            val videoUri = DoorUri.parse(uri)
            val videoFile = process.getLocalUri(videoUri, context, di).toFile()
            var newVideo = File(videoFile.parentFile, "new${videoFile.nameWithoutExtension}.mp4")

            try {

                val trackerUrl = db.siteDao.getSiteAsync()?.torrentAnnounceUrl
                        ?: throw IllegalArgumentException("missing tracker url")
                val contentNeedUpload = !videoUri.isRemote()

                val compressVideo: Boolean = process.params["compress"]?.toBoolean() ?: false

                Napier.d(tag = VIDEO_JVM, message = "conversion Params compress video is $compressVideo")

                if (compressVideo) {
                    val fileVideoDimensionsAndAspectRatio = ShrinkUtils.getVideoResolutionMetadata(videoFile)
                    val newVideoDimensions = Pair(fileVideoDimensionsAndAspectRatio.first, fileVideoDimensionsAndAspectRatio.second).fitWithin()
                    ShrinkUtils.optimiseVideo(videoFile, newVideo, newVideoDimensions, fileVideoDimensionsAndAspectRatio.third)
                } else {
                    newVideo = videoFile
                }

                val container = db.containerDao.findByUid(contentJobItem.cjiContainerUid)
                        ?: Container().apply {
                            containerContentEntryUid = contentJobItem.cjiContentEntryUid
                            cntLastModified = System.currentTimeMillis()
                            mimeType = supportedMimeTypes.first()
                            containerUid = repo.containerDao.insertAsync(this)
                            contentJobItem.cjiContainerUid = containerUid
                        }

                db.contentJobItemDao.updateContainer(contentJobItem.cjiUid, container.containerUid)


                val containerFolder = jobItem.contentJob?.toUri
                        ?: defaultContainerDir.toURI().toString()
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


                contentJobItem.cjiConnectivityNeeded = true
                db.contentJobItemDao.updateConnectivityNeeded(contentJobItem.cjiUid, true)

                val haveConnectivityToContinueJob = checkConnectivityToDoJob(db, jobItem)
                if (!haveConnectivityToContinueJob) {
                    return@withContext ProcessResult(JobStatus.QUEUED)
                }

                val torrentFileBytes = File(torrentDir, "${container.containerUid}.torrent").readBytes()
                uploadContentIfNeeded(contentNeedUpload, contentJobItem, progress, httpClient, torrentFileBytes, endpoint)

                repo.containerDao.findByUid(container.containerUid)

                return@withContext ProcessResult(JobStatus.COMPLETE)
            }catch (c: CancellationException){

                withContext(NonCancellable){
                    videoFile.delete()
                    newVideo.delete()

                }

                throw c

            }
        }
    }

    suspend fun getEntry(uri: DoorUri, process: ProcessContext): MetadataResult? {
        return withContext(Dispatchers.Default){

            val localUri = process.getLocalUri(uri, context, di)

            val fileName = localUri.getFileName(context)

            if(!supportedFileExtensions.any { fileName.endsWith(it, true) }) {
                return@withContext null
            }

            val file = localUri.toFile()

            val fileVideoDimensions = ShrinkUtils.getVideoResolutionMetadata(file)

            if(fileVideoDimensions.first == 0 || fileVideoDimensions.second == 0){
                return@withContext null
            }

            val entry = ContentEntryWithLanguage().apply {
                this.title = file.nameWithoutExtension
                this.leaf = true
                sourceUrl = uri.uri.toString()
                this.contentTypeFlag = ContentEntry.TYPE_VIDEO
            }
            MetadataResult(entry, PLUGIN_ID)
        }
    }

}