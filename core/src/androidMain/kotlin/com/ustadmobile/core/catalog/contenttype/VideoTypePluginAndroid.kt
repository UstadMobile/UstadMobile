package com.ustadmobile.core.catalog.contenttype

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import com.google.gson.Gson
import io.github.aakira.napier.Napier
import com.linkedin.android.litr.MediaTransformer
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import com.turn.ttorrent.tracker.Tracker
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.contentjob.ProcessResult
import com.ustadmobile.core.contentjob.ext.processMetadata
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.torrent.UstadTorrentManager
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.door.ext.toDoorUri
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File


class VideoTypePluginAndroid(private var context: Any, private val endpoint: Endpoint, override val di: DI) : VideoTypePlugin() {

    private val VIDEO_ANDROID = "VideoPluginAndroid"

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val gson: Gson = di.direct.instance()

    private val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val torrentDir: File by di.on(endpoint).instance(tag = DiTag.TAG_TORRENT_DIR)

    private val tracker: Tracker = di.direct.instance()

    private val ustadTorrentManager: UstadTorrentManager by di.on(endpoint).instance()

    override suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): MetadataResult? {
        return getEntry(uri, process)
    }

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ProcessContext, jobProgress: ContentJobProgressListener): ProcessResult {
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")
        withContext(Dispatchers.Default) {

            val uri = contentJobItem.sourceUri ?: throw IllegalStateException("missing uri")
            val videoUri = DoorUri.parse(uri)
            val localUri = process.getLocalUri(videoUri, context, di)
            val contentEntryUid = processMetadata(jobItem, process,context, endpoint)


            val videoTempDir = makeTempDir(prefix = "tmp")
            val newVideo = File(videoTempDir,
                    localUri.getFileName(context))
            val compressVideo: Boolean = process.params["compress"]?.toBoolean() ?: false

            Napier.d(tag = VIDEO_ANDROID, message = "conversion Params compress video is $compressVideo")

            if (compressVideo) {

                Napier.d(tag = VIDEO_ANDROID, message = "start import for new video file $newVideo.name")

                val dimensionsArray = process.params["dimensions"]?.split("x") ?: listOf()
                val storageDimensions = localUri.extractVideoResolutionMetadata(context as Context)
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
                    it.setDataSource(context as Context, localUri.uri, null)
                    it.getFirstAudioCodecInfo()
                }

                val audioTarget = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                        audioCodecInfo.sampleRate, audioCodecInfo.channelCount).apply {
                    setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE)
                }

                val videoCompleted = CompletableDeferred<Boolean>()

                val mediaTransformer = MediaTransformer(context as Context)

                mediaTransformer.transform(contentJobItem.cjiContentEntryUid.toString(), localUri.uri, newVideo.path,
                        videoTarget, audioTarget, object : TransformationListener {
                    override fun onStarted(id: String) {
                        Napier.d(tag = VIDEO_ANDROID, message = "started transform")
                    }

                    override fun onProgress(id: String, progress: Float) {
                        Napier.d(tag = VIDEO_ANDROID, message = "progress at value ${progress * 100}")
                        contentJobItem.cjiProgress = (progress * 100).toLong()
                        jobProgress.onProgress(contentJobItem)
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
                containerContentEntryUid = contentEntryUid
                cntLastModified = System.currentTimeMillis()
                mimeType = supportedMimeTypes.first()
                containerUid = repo.containerDao.insert(this)
            }

            contentJobItem.cjiContainerUid = container.containerUid

            val containerFolder = jobItem.contentJob?.toUri ?: defaultContainerDir.toURI().toString()
            val containerFolderUri = DoorUri.parse(containerFolder)

            if (compressVideo) {
                repo.addFileToContainer(container.containerUid, newVideo.toDoorUri(), newVideo.name,
                        context,
                        di,
                        ContainerAddOptions(containerFolderUri))
            } else {
                repo.addContainerFromUri(container.containerUid, localUri, context, di,
                        localUri.getFileName(context),
                        ContainerAddOptions(containerFolderUri))
            }
            videoTempDir.delete()

            repo.addTorrentFileFromContainer(
                    container.containerUid,
                    DoorUri.parse(torrentDir.toURI().toString()),
                    tracker.announceUrl, containerFolderUri
            )

            ustadTorrentManager.addTorrent(container.containerUid)

            repo.containerDao.findByUid(container.containerUid) ?: container
        }
        return ProcessResult(200)
    }

    suspend fun getEntry(doorUri: DoorUri, process: ProcessContext): MetadataResult? {
        return withContext(Dispatchers.Default) {

            val localUri = process.getLocalUri(doorUri, context, di)

            val fileName = localUri.getFileName(context)

            if (!supportedFileExtensions.any { fileName.endsWith(it, true) }) {
                return@withContext null
            }

            val fileVideoDimensions = localUri.extractVideoResolutionMetadata(context as Context)

            if(fileVideoDimensions.first == 0 || fileVideoDimensions.second == 0){
                return@withContext null
            }

            val entry =ContentEntryWithLanguage().apply {
                this.title = fileName
                this.leaf = true
                this.contentTypeFlag = ContentEntry.TYPE_VIDEO
            }
            MetadataResult(entry, pluginId)
        }
    }

}