package com.ustadmobile.core.catalog.contenttype

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import com.github.aakira.napier.Napier
import com.linkedin.android.litr.MediaTransformer
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.fitWithin
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class VideoTypePluginAndroid : VideoTypePlugin() {

    private val VIDEO_ANDROID = "VideoPluginAndroid"

    private val videoCompleted = CompletableDeferred<Boolean>()

    override suspend fun extractMetadata(filePath: String): ContentEntryWithLanguage? {
        return withContext(Dispatchers.Default) {
            val file = File(filePath)

            if (!fileExtensions.any { file.name.endsWith(it, true) }) {
                return@withContext null
            }

            ContentEntryWithLanguage().apply {
                this.title = file.nameWithoutExtension
                this.leaf = true
                this.contentTypeFlag = ContentEntry.TYPE_VIDEO
            }
        }
    }

    override suspend fun importToContainer(filePath: String, conversionParams: Map<String, String>,
                                           contentEntryUid: Long, mimeType: String, containerBaseDir: String,
                                           context: Any, db: UmAppDatabase, repo: UmAppDatabase, progressListener: (Int) -> Unit): Container {
        return withContext(Dispatchers.Default) {

            val file = File(filePath.removePrefix("file://"))
      /*      val newVideo = File(file.parentFile, "new${file.nameWithoutExtension}.mp4")

            Napier.d(tag = VIDEO_ANDROID, message = "start import for new video file $newVideo.name")

            val metaRetriever = MediaMetadataRetriever()
            metaRetriever.setDataSource(file.path)
            val originalHeight = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
            val originalWidth = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()

            val pairDimensions = Pair(originalWidth, originalHeight).fitWithin()

            Napier.d(tag = VIDEO_ANDROID, message = "width of old video is $originalWidth, height of old video is $originalHeight")
            Napier.d(tag = VIDEO_ANDROID, message = "width of new video is ${pairDimensions.first}, height of new video is ${pairDimensions.second}")

            val videoTarget = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, pairDimensions.first, pairDimensions.second).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, 128000)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
                setInteger(MediaFormat.KEY_FRAME_RATE, 25)
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            }

            val audioTarget = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 8000, 2).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, 128000)
            }

            val mediaTransformer = MediaTransformer(context as Context)
            mediaTransformer.transform(contentEntryUid.toString(), file.toUri(), newVideo.absolutePath,
                    videoTarget, audioTarget, object : TransformationListener {
                override fun onStarted(id: String) {
                    Napier.d(tag = VIDEO_ANDROID, message = "started transform")
                }

                override fun onProgress(id: String, progress: Float) {
                    Napier.d(tag = VIDEO_ANDROID, message = "progress at value ${progress * 100}")
                    progressListener.invoke((progress * 100).toInt())
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
                            ?: throw Exception("error on video id: $id"))
                }

            }, MediaTransformer.GRANULARITY_DEFAULT, null)


            videoCompleted.await()
            mediaTransformer.release()

            Napier.d(tag = VIDEO_ANDROID, message = "released transform with new file size " +
                    "at ${newVideo.length()} with old size at ${file.length()}")*/

            val container = Container().apply {
                containerContentEntryUid = contentEntryUid
                cntLastModified = System.currentTimeMillis()
                fileSize = file.length()
                this.mimeType = mimeType
                containerUid = repo.containerDao.insert(this)
            }

            val containerManager = ContainerManager(container, db, repo, containerBaseDir)

            containerManager.addEntries(ContainerManager.FileEntrySource(file, file.name))

            container
        }
    }
}