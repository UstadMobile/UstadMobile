package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.container.ContainerAddOptions.Companion.NEVER_COMPRESS_FILTER
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.controller.VideoContentPresenterCommon.Companion.VIDEO_MIME_MAP
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addFileToContainer
import com.ustadmobile.core.io.ext.guessMimeType
import com.ustadmobile.core.io.ext.isRemote
import com.ustadmobile.core.network.NetworkProgressListenerAdapter
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ShrinkUtils
import com.ustadmobile.core.util.ext.fitWithin
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.ext.updateTotalFromContainerSize
import com.ustadmobile.core.util.ext.updateTotalFromLocalUriIfNeeded
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.db.entities.*
import io.github.aakira.napier.Napier
import io.ktor.client.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File

class VideoTypePluginJvm(
        private var context: Any,
        private val endpoint: Endpoint,
        override val di: DI,
        private val uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
): VideoTypePlugin() {

    private val VIDEO_JVM = "VideoPluginJVM"

    private val httpClient: HttpClient = di.direct.instance()

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val ffmpegFile: File by di.instance(tag = DiTag.TAG_FILE_FFMPEG)

    private val ffprobeFile: File by di.instance(tag = DiTag.TAG_FILE_FFPROBE)

    override suspend fun extractMetadata(
        uri: DoorUri,
        process: ContentJobProcessContext
    ): MetadataResult? {
        return getEntry(uri, process)
    }

    override suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progress: ContentJobProgressListener
    ): ProcessResult {
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")
        return withContext(Dispatchers.Default) {
            val uri = contentJobItem.sourceUri ?: throw IllegalStateException("missing uri")
            val videoUri = DoorUri.parse(uri)
            val localVideoUri = process.getLocalOrCachedUri()
            val videoFile = localVideoUri.toFile()
            var pathInContainer = if(!videoUri.isRemote()) {
                videoFile.name
            }else {
                val extension = videoUri.guessMimeType(context, di)?.let { mimeType ->
                    VIDEO_MIME_MAP[mimeType]
                } ?: throw IllegalArgumentException("Unknown mime type for $videoUri")

                videoUri.getFileName(context).requirePostfix(extension)
            }

            var videoFileToAddToContainer = videoFile
            val contentNeedUpload = !videoUri.isRemote()
            contentJobItem.updateTotalFromLocalUriIfNeeded(localVideoUri, contentNeedUpload,
                progress, context, di)

            try {

                if(!contentJobItem.cjiContainerProcessed) {

                    val compressVideo: Boolean = process.params["compress"]?.toBoolean() ?: false

                    Napier.d(tag = VIDEO_JVM, message = "conversion Params compress video is $compressVideo")

                    if (compressVideo) {
                        videoFileToAddToContainer = File(videoFile.parentFile,
                            "new${videoFile.nameWithoutExtension}.mp4")
                        val fileVideoDimensionsAndAspectRatio = ShrinkUtils.getVideoResolutionMetadata(
                            videoFile, ffprobeFile)
                        val newVideoDimensions = Pair(fileVideoDimensionsAndAspectRatio.first, fileVideoDimensionsAndAspectRatio.second).fitWithin()
                        ShrinkUtils.optimiseVideo(videoFile, videoFileToAddToContainer, ffmpegFile,
                            newVideoDimensions, fileVideoDimensionsAndAspectRatio.third)
                        pathInContainer = "new${videoFile.nameWithoutExtension}.mp4"
                    }

                    val container = db.containerDao.findByUid(contentJobItem.cjiContainerUid)
                            ?: Container().apply {
                                containerContentEntryUid = contentJobItem.cjiContentEntryUid
                                cntLastModified = System.currentTimeMillis()
                                mimeType = supportedMimeTypes.first()
                                containerUid = repo.containerDao.insertAsync(this)

                            }

                    contentJobItem.cjiContainerUid = container.containerUid
                    process.withContentJobItemTransactionMutex { txDb ->
                        txDb.contentJobItemDao.updateContentJobItemContainer(contentJobItem.cjiUid,
                            container.containerUid)
                    }

                    val containerFolder = jobItem.contentJob?.toUri
                            ?: defaultContainerDir.toURI().toString()
                    val containerFolderUri = DoorUri.parse(containerFolder)

                    repo.addFileToContainer(container.containerUid, videoFileToAddToContainer.toDoorUri(),
                        pathInContainer, context, di,
                        ContainerAddOptions(containerFolderUri, compressionFilter = NEVER_COMPRESS_FILTER))

                    contentJobItem.updateTotalFromContainerSize(contentNeedUpload, db,
                        progress)

                    val haveConnectivityToContinueJob = process.withContentJobItemTransactionMutex { txDb ->
                        txDb.contentJobItemDao.updateContainerProcessed(contentJobItem.cjiUid, true)

                        contentJobItem.cjiConnectivityNeeded = true
                        txDb.contentJobItemDao.updateConnectivityNeeded(contentJobItem.cjiUid, true)

                        txDb.contentJobDao.isConnectivityAcceptableForJob(jobItem.contentJob?.cjUid
                            ?: 0)
                    }

                    if (!haveConnectivityToContinueJob) {
                        return@withContext ProcessResult(JobStatus.WAITING_FOR_CONNECTION)
                    }
                }


                if(contentNeedUpload) {
                    return@withContext ProcessResult(uploader.upload(contentJobItem,
                        NetworkProgressListenerAdapter(progress, contentJobItem), httpClient,
                        endpoint, process
                    ))
                }

                return@withContext ProcessResult(JobStatus.COMPLETE)
            }catch (c: CancellationException){

                withContext(NonCancellable){
                    videoFile.delete()
                    videoFileToAddToContainer.delete()

                }

                throw c

            }
        }
    }

    suspend fun getEntry(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        return withContext(Dispatchers.Default){

            val localUri = process.getLocalOrCachedUri()

            val fileName = uri.getFileName(context)

            if(!supportedFileExtensions.any { fileName.endsWith(it, true) }) {
                return@withContext null
            }

            val file = localUri.toFile()

            val fileVideoDimensions = ShrinkUtils.getVideoResolutionMetadata(file, ffprobeFile)

            if(fileVideoDimensions.first == 0 || fileVideoDimensions.second == 0){
                return@withContext null
            }

            val entry = ContentEntryWithLanguage().apply {
                this.title = fileName
                this.leaf = true
                sourceUrl = uri.uri.toString()
                this.contentTypeFlag = ContentEntry.TYPE_VIDEO
            }
            MetadataResult(entry, PLUGIN_ID)
        }
    }

}