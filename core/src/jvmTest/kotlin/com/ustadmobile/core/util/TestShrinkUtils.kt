package com.ustadmobile.core.util

import com.ustadmobile.core.util.ext.fitWithin
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.sharedse.io.extractResourceToFile
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TestShrinkUtils {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun testgetVideoResolutionMetadata(){

        val fileToCheck = temporaryFolder.newFile("newVideo.mp4")
        javaClass.getResourceAsStream("/com/ustadmobile/core/container/BigBuckBunny.mp4")!!
            .writeToFile(fileToCheck)

        val videoDimensions = ShrinkUtils.getVideoResolutionMetadata(fileToCheck,
            SysPathUtil.findCommandInPath("ffprobe")!!)
        Assert.assertEquals("ffprobe found same dimensions in video",
                Triple(1920, 1080, "16:9"), videoDimensions)

    }

    @Test
    fun testVideoConversion(){

        val videoFile = temporaryFolder.newFile("video.mp4")
        val newVideo = temporaryFolder.newFile("newVideo.mp4")
        javaClass.getResourceAsStream("/com/ustadmobile/core/container/BigBuckBunny.mp4",)!!
            .writeToFile(videoFile)

        val fileVideoDimensions = ShrinkUtils.getVideoResolutionMetadata(videoFile,
            SysPathUtil.findCommandInPath("ffprobe")!!)
        val newVideoDimensions = Pair(fileVideoDimensions.first, fileVideoDimensions.second).fitWithin()

        ShrinkUtils.optimiseVideo(videoFile, newVideo,
            SysPathUtil.findCommandInPath("ffmpeg")!!,
            newVideoDimensions, fileVideoDimensions.third)

        Assert.assertTrue("New video is smaller than old video", newVideo.length() < videoFile.length())
        Assert.assertTrue("New video file exists", newVideo.exists())
        Assert.assertTrue("New video has a size > 0", newVideo.length() > 0)

    }



}