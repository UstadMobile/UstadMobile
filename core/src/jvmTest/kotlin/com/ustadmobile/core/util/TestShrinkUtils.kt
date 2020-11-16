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
                Pair(1920, 1080), videoDimensions)

    }

    @Test
    fun testVideoConversion(){

        val videoFile = temporaryFolder.newFile("video.mp4")
        val newVideo = temporaryFolder.newFile("newVideo.mp4")
        UmFileUtilSe.extractResourceToFile(
                "/com/ustadmobile/core/container/BigBuckBunny.mp4",
                videoFile)

        val fileVideoDimensions = ShrinkUtils.getVideoResolutionMetadata(videoFile)
        val newVideoDimensions = fileVideoDimensions.fitWithin()

        ShrinkUtils.optimiseVideo(videoFile, newVideo, newVideoDimensions)

        Assert.assertTrue("optimzed file", newVideo.length() < videoFile.length())

    }



}