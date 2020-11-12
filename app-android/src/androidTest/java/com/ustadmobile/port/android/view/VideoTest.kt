package com.ustadmobile.port.android.view

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import com.github.aakira.napier.Napier
import com.linkedin.android.litr.MediaTransformer
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import com.toughra.ustadmobile.R
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.util.ext.fitWithin
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
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@ExperimentalStdlibApi
@RunWith(Parameterized::class)
class VideoTest(val fileLocation: String) {

    private val videoCompleted = CompletableDeferred<Boolean>()

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    lateinit var container: Container

    @Test
    fun test(){

        val tmpDir = UmFileUtilSe.makeTempDir("testVideoPlayer",
                "" + System.currentTimeMillis())

        val videoFile = File(tmpDir, "${fileLocation.substringAfterLast("/").substringBefore(".")}.mp4")
        val newVideo = File(tmpDir, "new${fileLocation.substringAfterLast("/").substringBefore(".")}.mp4")

        Napier.d(tag = "VIDEO_ANDROID", message = "starting transform for file ${fileLocation.substringAfterLast("/")}")

        FileUtils.copyInputStreamToFile(
                javaClass.getResourceAsStream(fileLocation)!!,
                videoFile)
        val context = ApplicationProvider.getApplicationContext<Context>()

        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(videoFile.path)
        val originalHeight = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
        val originalWidth = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()

        val pairDimensions = Pair(originalWidth, originalHeight).fitWithin()

        Napier.d(tag = "VIDEO_ANDROID", message = "width of old video is $originalWidth, height of old video is $originalHeight")
        Napier.d(tag = "VIDEO_ANDROID", message = "width of new video is ${pairDimensions.first}, height of new video is ${pairDimensions.second}")

        val videoTarget = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, pairDimensions.first, pairDimensions.second).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, 128000)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
            setInteger(MediaFormat.KEY_FRAME_RATE, 25)
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        }

        val audioTarget = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AMR_NB, 8000, 2).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, 128000)
        }

        val mediaTransformer = MediaTransformer(context)
        mediaTransformer.transform("1", videoFile.toUri(), newVideo.absolutePath,
                videoTarget, null, object: TransformationListener {
            override fun onStarted(id: String) {
                Napier.d(tag = "VIDEO_ANDROID", message = "started transform")
            }

            override fun onProgress(id: String, progress: Float) {
                //Napier.d(tag = "VIDEO_ANDROID", message = "progress at value ${progress * 100}")
            }

            override fun onCompleted(id: String, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                Napier.d(tag = "VIDEO_ANDROID", message = "completed transform")
                videoCompleted.complete(true)
            }

            override fun onCancelled(id: String, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                Napier.d(tag = "VIDEO_ANDROID", message = "cancelled transform")
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

        Napier.d(tag = "VIDEO_ANDROID", message = "released transform with new file size " +
                "at ${newVideo.length()} with old size at ${videoFile.length()}")


        Napier.d(tag = "VIDEO_ANDROID", message = "ending transform for file ${fileLocation.substringAfterLast("/")}")

        runBlocking {
            container = dbRule.db.insertVideoContent()
            val manager = ContainerManager(container!!, dbRule.db,
                    dbRule.db, tmpDir.absolutePath)
            manager.addEntries(ContainerManager.FileEntrySource(newVideo, "newVideo.mp4"))
        }


       /* val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_CONTENT_ENTRY_UID to container!!.containerContentEntryUid, UstadView.ARG_CONTAINER_UID to container!!.containerUid)) {
            VideoContentFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }*/

    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun fileLocations() = listOf(
                "/com/ustadmobile/app/android/Poems.mp4",
                "/com/ustadmobile/app/android/KeepOurSurroundingsClean.mp4",
                "/com/ustadmobile/app/android/Local Poems.mp4",
                "/com/ustadmobile/app/android/Local Songs.mp4"
        )
    }



}