package com.ustadmobile.port.android.view

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaFormat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import com.linkedin.android.litr.MediaTransformer
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import com.toughra.ustadmobile.R
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertVideoContent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.Rule
import org.junit.Test
import java.io.File

@ExperimentalStdlibApi
class VideoTest {

    private val videoCompleted = CompletableDeferred<Boolean>()

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    lateinit var container: Container

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

        val videoTarget = MediaFormat.createVideoFormat("video/avc", 1080,1080)
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
                videoCompleted.complete(true)
            }

            override fun onCancelled(id: String, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                videoCompleted.complete(false)
            }

            override fun onError(id: String, cause: Throwable?, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                videoCompleted.completeExceptionally(cause ?: throw Exception("error on video id: $id"))
            }

        }, MediaTransformer.GRANULARITY_DEFAULT, null)


        runBlocking {
            videoCompleted.await()
        }
        mediaTransformer.release()

        runBlocking {
            container = dbRule.db.insertVideoContent()
            val manager = ContainerManager(container!!, dbRule.db,
                    dbRule.db, tmpDir.absolutePath)
            manager.addEntries(ContainerManager.FileEntrySource(newVideo, "newVideo.mp4"))
        }


        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_CONTENT_ENTRY_UID to container!!.containerContentEntryUid, UstadView.ARG_CONTAINER_UID to container!!.containerUid)) {
            VideoContentFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

    }


}