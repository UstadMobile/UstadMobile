package com.ustadmobile.core.catalog.contenttype

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaFormat
import androidx.core.net.toUri
import com.linkedin.android.litr.MediaTransformer
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VideoTypePluginAndroid : VideoTypePlugin() {

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


            val file = File(filePath)
            val newVideo = File(file.parentFile, "${file.nameWithoutExtension}.mp4")

            val videoTarget = MediaFormat.createVideoFormat("video/avc", 1080, 1080)
            videoTarget.setInteger(MediaFormat.KEY_BIT_RATE, 128000)
            videoTarget.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
            videoTarget.setInteger(MediaFormat.KEY_FRAME_RATE, 25)
            videoTarget.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)

            val audioTarget = MediaFormat.createAudioFormat("audio/raw", 8000, 2)
            audioTarget.setInteger(MediaFormat.KEY_BIT_RATE, 128000)

            val mediaTransformer = MediaTransformer(context as Context)
            mediaTransformer.transform(contentEntryUid.toString(), file.toUri(), newVideo.absolutePath,
                    videoTarget, null, object : TransformationListener {
                override fun onStarted(id: String) {
                }

                override fun onProgress(id: String, progress: Float) {
                    progressListener.invoke(progress.toInt())
                }

                override fun onCompleted(id: String, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                    videoCompleted.complete(true)
                }

                override fun onCancelled(id: String, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                    videoCompleted.complete(false)
                }

                override fun onError(id: String, cause: Throwable?, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                    videoCompleted.completeExceptionally(cause ?: throw Exception("error on video id: $id"))
                }

            }, MediaTransformer.GRANULARITY_DEFAULT, null)


            videoCompleted.await()
            mediaTransformer.release()

            val container = Container().apply {
                containerContentEntryUid = contentEntryUid
                cntLastModified = System.currentTimeMillis()
                fileSize = newVideo.length()
                this.mimeType = mimeType
                containerUid = repo.containerDao.insert(this)
            }

            val containerManager = ContainerManager(container, db, repo, containerBaseDir)

            containerManager.addEntries(ContainerManager.FileEntrySource(newVideo, newVideo.name))

            container
        }
    }
}