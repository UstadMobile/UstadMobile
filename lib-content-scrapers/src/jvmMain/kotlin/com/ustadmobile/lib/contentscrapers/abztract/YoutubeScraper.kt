package com.ustadmobile.lib.contentscrapers.abztract

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.controller.VideoPlayerPresenterCommon.Companion.VIDEO_MIME_MAP
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.nio.file.Files

abstract class YoutubeScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long) : Scraper(containerDir, db, contentEntryUid) {

    private val ytPath: String
    private val gson: Gson

    init {
        ContentScraperUtil.checkIfPathsToDriversExist()
        ytPath = System.getProperty(ContentScraperUtil.YOUTUBE_DL_PATH_KEY)
        gson = GsonBuilder().disableHtmlEscaping().create()
    }

    fun scrapeYoutubeLink(sourceUrl: String, videoQualityOption: String = "worst[ext=webm]/worst"): ContainerManager {

        val ytExeFile = File(ytPath)
        if (!ytExeFile.exists()) {
            throw IOException("Webp executable does not exist: $ytPath")
        }

        val tempDir = Files.createTempDirectory(sourceUrl.substringAfter("=")).toFile()

        val builder = ProcessBuilder(ytPath, "-f", videoQualityOption, "-o", "${tempDir.absolutePath}/%(id)s.%(ext)s", sourceUrl)
        var process: Process? = null
        try {
            process = builder.start()
            process!!.waitFor()
            val exitValue = process.exitValue()
            if (exitValue != 0) {
                UMLogUtil.logError("Error Stream for src $sourceUrl with error code  ${UMIOUtils.readStreamToString(process.errorStream)}")
                println(UMIOUtils.readStreamToString(process.errorStream))
                Thread.sleep(60000)
                throw IOException()
            }
        } catch (e: Exception) {
            throw e
        } finally {
            process?.destroy()
        }

        val videoFile = tempDir.listFiles()[0]
        val mimetype = Files.probeContentType(videoFile.toPath())

        if (!VIDEO_MIME_MAP.keys.contains(mimetype)) {
            throw IllegalStateException("Video type not supported for $mimetype")
        }

        val containerManager = ContainerManager(createBaseContainer(mimetype), db, db, containerDir.absolutePath)
        runBlocking {
            containerManager.addEntries(ContainerManager.FileEntrySource(videoFile, videoFile.name))
        }

        tempDir.deleteRecursively()

        Thread.sleep(4000)

        return containerManager


    }

    override fun close() {
    }
}