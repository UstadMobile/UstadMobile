package com.ustadmobile.core.catalog.contenttype

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import io.github.aakira.napier.Napier
import com.linkedin.android.litr.MediaTransformer
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.contentjob.ProcessResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addContainerFromUri
import com.ustadmobile.core.io.ext.addFileToContainer
import com.ustadmobile.core.io.ext.extractVideoResolutionMetadata
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentJobItem
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.nio.file.Files


class VideoTypePluginAndroid(private var context: Any, private val endpoint: Endpoint, override val di: DI) : VideoTypePlugin() {

    private val VIDEO_ANDROID = "VideoPluginAndroid"

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)


    override suspend fun canProcess(doorUri: DoorUri, process: ProcessContext): Boolean {
        return getEntry(doorUri, process) != null
    }

    override suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): MetadataResult? {
        return getEntry(uri, process)
    }

    override suspend fun processJob(jobItem: ContentJobItem, process: ProcessContext, progress: ContentJobProgressListener): ProcessResult {
        withContext(Dispatchers.Default) {

            val uri = jobItem.fromUri ?: throw IllegalStateException("missing uri")
            val videoUri = DoorUri.parse(uri)
            val newVideo = File(Files.createTempDirectory("tmp").toFile(),
                    videoUri.getFileName(context))
            val compressVideo: Boolean = process.params["compress"]?.toBoolean() ?: false

            Napier.d(tag = VIDEO_ANDROID, message = "conversion Params compress video is $compressVideo")

            if (compressVideo) {

                Napier.d(tag = VIDEO_ANDROID, message = "start import for new video file $newVideo.name")

                val dimensionsArray = process.params["dimensions"]?.split("x") ?: listOf()
                val storageDimensions = videoUri.extractVideoResolutionMetadata(context as Context)
                val originalVideoDimensions = if (dimensionsArray.isEmpty()) {
                    storageDimensions
                } else {
                    val aspectRatio = dimensionsArray[0].toFloat() / dimensionsArray[1].toFloat()
                    Pair(storageDimensions.first, (storageDimensions.first / aspectRatio).toInt())
                }
                val newVideoDimensions = originalVideoDimensions.fitWithin()

                Napier.d(tag = VIDEO_ANDROID, message = "width of old video is ${originalVideoDimensions.first}, height of old video is ${originalVideoDimensions.second}")
                Napier.d(tag = VIDEO_ANDROID, message = "width of new video is ${newVideoDimensions.first}, height of new video is ${newVideoDimensions.second}")

                val videoTarget = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, newVideoDimensions.first, newVideoDimensions.second).apply {
                    setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BIT_RATE)
                    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_FRAME_INTERVAL)
                    setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE)
                    setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                }

                val audioCodecInfo = MediaExtractor().useThenRelease {
                    it.setDataSource(context as Context, videoUri.uri, null)
                    it.getFirstAudioCodecInfo()
                }

                val audioTarget = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                        audioCodecInfo.sampleRate, audioCodecInfo.channelCount).apply {
                    setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE)
                }

                val videoCompleted = CompletableDeferred<Boolean>()

                val mediaTransformer = MediaTransformer(context as Context)

                mediaTransformer.transform(jobItem.cjiContentEntryUid.toString(), videoUri.uri, newVideo.path,
                        videoTarget, audioTarget, object : TransformationListener {
                    override fun onStarted(id: String) {
                        Napier.d(tag = VIDEO_ANDROID, message = "started transform")
                    }

                    override fun onProgress(id: String, progress: Float) {
                        Napier.d(tag = VIDEO_ANDROID, message = "progress at value ${progress * 100}")
                        jobItem.cjiProgress = (progress * 100).toLong()
                    }

                    override fun onCompleted(id: String, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                        Napier.d(tag = VIDEO_ANDROID, message = "completed transform")
                        videoCompleted.complete(true)
                    }

                    override fun onCancelled(id: String, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                        Napier.d(tag = VIDEO_ANDROID, message = "cancelled transform")
                        videoCompleted.complete(false)
                    }

                    override fun onError(id: String, cause: Throwable?, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                        videoCompleted.completeExceptionally(cause
                                ?: RuntimeException("error on video id: $id"))
                    }

                }, MediaTransformer.GRANULARITY_DEFAULT, null)


                try {
                    videoCompleted.await()
                } catch (e: Exception) {
                    Napier.e(tag = VIDEO_ANDROID, throwable = e, message = e.message ?: "")
                } finally {
                    mediaTransformer.release()
                }

            }


            val container = Container().apply {
                containerContentEntryUid = jobItem.cjiContentEntryUid
                cntLastModified = System.currentTimeMillis()
                mimeType = supportedMimeTypes.first()
                containerUid = repo.containerDao.insert(this)
            }

            jobItem.cjiContainerUid = container.containerUid

            val containerFolder = jobItem.toUri ?: defaultContainerDir.toURI().toString()
            val containerFolderUri = DoorUri.parse(containerFolder)

            if (compressVideo) {
                repo.addFileToContainer(container.containerUid, newVideo.toDoorUri(), newVideo.name,
                        ContainerAddOptions(containerFolderUri),
                        di)
            } else {
                repo.addContainerFromUri(container.containerUid, videoUri, context, di,
                        videoUri.getFileName(context),
                        ContainerAddOptions(containerFolderUri))
            }
            newVideo.delete()

            repo.containerDao.findByUid(container.containerUid) ?: container
        }
        return ProcessResult(200)
    }

    suspend fun getEntry(doorUri: DoorUri, process: ProcessContext): MetadataResult? {
        return withContext(Dispatchers.Default) {

            val fileName = doorUri.getFileName(context)

            if (!supportedFileExtensions.any { fileName.endsWith(it, true) }) {
                return@withContext null
            }

            val fileVideoDimensions = doorUri.extractVideoResolutionMetadata(context as Context)

            if(fileVideoDimensions.first == 0 || fileVideoDimensions.second == 0){
                return@withContext null
            }

            val entry =ContentEntryWithLanguage().apply {
                this.title = fileName
                this.leaf = true
                this.contentTypeFlag = ContentEntry.TYPE_VIDEO
            }
            MetadataResult(entry)
        }
    }

}