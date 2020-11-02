package com.ustadmobile.port.android.view

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaFormat
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import com.linkedin.android.litr.MediaTransformer
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import org.apache.commons.io.FileUtils
import org.junit.Test
import java.io.File

class VideoTest {

    @Test
    fun test(){

        val tmpDir = UmFileUtilSe.makeTempDir("testVideoPlayer",
                "" + System.currentTimeMillis())

        val videoFile = File(tmpDir, "video.mp4")
        val newVideo = File(tmpDir, "newVideo.mp4")

        FileUtils.copyInputStreamToFile(
                javaClass.getResourceAsStream("/com/ustadmobile/app/android/Poems.mp4")!!,
                videoFile)
        val context = ApplicationProvider.getApplicationContext<Context>()

        val videoTarget = MediaFormat.createVideoFormat("video/avc", 720,720)
        videoTarget.setInteger(MediaFormat.KEY_BIT_RATE, 128000)
        videoTarget.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
        videoTarget.setInteger(MediaFormat.KEY_FRAME_RATE, 25)
        videoTarget.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)

        val audioTarget = MediaFormat.createAudioFormat("audio/raw",8000, 2)
        audioTarget.setInteger(MediaFormat.KEY_BIT_RATE, 128000)

        val mediaTransformer = MediaTransformer(context)
        mediaTransformer.transform("1", videoFile.toUri(), newVideo.absolutePath,
                videoTarget, null, object: TransformationListener {
            override fun onStarted(id: String) {
                println("started")
            }

            override fun onProgress(id: String, progress: Float) {
                println("$progress")
            }

            override fun onCompleted(id: String, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                println("completed video")
            }

            override fun onCancelled(id: String, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                println("cancelled")
            }

            override fun onError(id: String, cause: Throwable?, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                println("error")
            }

        }, MediaTransformer.GRANULARITY_DEFAULT, null)


        mediaTransformer.release()

    }


}