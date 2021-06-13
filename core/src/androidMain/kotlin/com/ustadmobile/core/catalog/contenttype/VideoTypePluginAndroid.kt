package com.ustadmobile.core.catalog.contenttype

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import androidx.core.net.toUri
import io.github.aakira.napier.Napier
import com.linkedin.android.litr.MediaTransformer
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addFileToContainer
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class VideoTypePluginAndroid : VideoTypePlugin() {

    private val VIDEO_ANDROID = "VideoPluginAndroid"

    override suspend fun extractMetadata(uri: String, context: Any): ContentEntryWithLanguage? {
        return withContext(Dispatchers.Default) {
            val file = File(uri)

            if (!fileExtensions.any { file.name.endsWith(it, true) }) {
                return@withContext null
            }

            val fileVideoDimensions = file.extractVideoResolutionMetadata()

            if(fileVideoDimensions.first == 0 || fileVideoDimensions.second == 0){
                return@withContext null
            }

            ContentEntryWithLanguage().apply {
                this.title = file.nameWithoutExtension
                this.leaf = true
                this.contentTypeFlag = ContentEntry.TYPE_VIDEO
            }
        }
    }

    override suspend fun importToContainer(uri: String, conversionParams: Map<String, String>,
                                           contentEntryUid: Long, mimeType: String, containerBaseDir: String,
                                           context: Any, db: UmAppDatabase, repo: UmAppDatabase, progressListener: (Int) -> Unit): Container {
        return withContext(Dispatchers.Default) {

            val videoFile = File(uri.removePrefix("file://"))
            var newVideo = File(videoFile.parentFile, "new${videoFile.nameWithoutExtension}.mp4")
            val compressVideo: Boolean = conversionParams["compress"]?.toBoolean() ?: false

            Napier.d(tag = VIDEO_ANDROID, message = "conversion Params compress video is $compressVideo")

            if(compressVideo) {

                Napier.d(tag = VIDEO_ANDROID, message = "start import for new video file $newVideo.name")

                val dimensionsArray = conversionParams["dimensions"]?.split("x") ?: listOf()
                val storageDimensions = videoFile.extractVideoResolutionMetadata()
                val originalVideoDimensions = if(dimensionsArray.isEmpty()){
                    storageDimensions
                }else{
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
                    it.setDataSource(videoFile.path)
                    it.getFirstAudioCodecInfo()
                }

                val audioTarget = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                    audioCodecInfo.sampleRate, audioCodecInfo.channelCount).apply {
                    setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE)
                }

                val videoCompleted = CompletableDeferred<Boolean>()

                val mediaTransformer = MediaTransformer(context as Context)
                mediaTransformer.transform(contentEntryUid.toString(), videoFile.toUri(), newVideo.absolutePath,
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

                Napier.d(tag = VIDEO_ANDROID, message = "released transform with new file size " +
                        "at ${newVideo.length()} with old size at ${videoFile.length()}")
            }else{
                newVideo = videoFile
            }


            val container = Container().apply {
                containerContentEntryUid = contentEntryUid
                cntLastModified = System.currentTimeMillis()
                fileSize = newVideo.length()
                this.mimeType = mimeType
                containerUid = repo.containerDao.insert(this)
            }

            repo.addFileToContainer(container.containerUid, newVideo.toDoorUri(), newVideo.name,
                ContainerAddOptions(File(containerBaseDir).toDoorUri()))

            videoFile.delete()

            container
        }
    }
}