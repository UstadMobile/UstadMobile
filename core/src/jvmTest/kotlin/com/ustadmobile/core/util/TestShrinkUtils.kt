package com.ustadmobile.core.util

import com.github.aakira.napier.Napier
import com.ustadmobile.core.util.ext.fitWithin
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TestShrinkUtils {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun testFindProbeInPath(){

        val probePath = ShrinkUtils.findInPath("ffprobe")

        Assert.assertEquals("found path to executable",
                "/usr/bin/ffprobe", probePath)

        val ffmpegPath = ShrinkUtils.findInPath("ffmpeg")

        Assert.assertEquals("found path to executable",
                "/usr/bin/ffmpeg", ffmpegPath)

    }

    @Test
    fun testgetVideoResolutionMetadata(){

        val fileToCheck = temporaryFolder.newFile("newVideo.mp4")
        UmFileUtilSe.extractResourceToFile(
                "/com/ustadmobile/core/container/BigBuckBunny.mp4",
                fileToCheck)

        val videoDimensions = ShrinkUtils.getVideoResolutionMetadata(fileToCheck)
        Assert.assertEquals("ffprobe found same dimensions in video",
                Triple(1920, 1080, "16:9"), videoDimensions)

    }

    @Test
    fun testVideoConversion(){

        val videoFile = temporaryFolder.newFile("video.mp4")
        val newVideo = temporaryFolder.newFile("newVideo.mp4")
        UmFileUtilSe.extractResourceToFile(
                "/com/ustadmobile/core/container/BigBuckBunny.mp4",
                videoFile)

        val fileVideoDimensions = ShrinkUtils.getVideoResolutionMetadata(videoFile)
        val newVideoDimensions = Pair(fileVideoDimensions.first, fileVideoDimensions.second).fitWithin()

        ShrinkUtils.optimiseVideo(videoFile, newVideo, newVideoDimensions, fileVideoDimensions.third)

        Assert.assertTrue("New video is smaller than old video", newVideo.length() < videoFile.length())
        Assert.assertTrue("New video file exists", newVideo.exists())
        Assert.assertTrue("New video has a size > 0", newVideo.length() > 0)

    }



}