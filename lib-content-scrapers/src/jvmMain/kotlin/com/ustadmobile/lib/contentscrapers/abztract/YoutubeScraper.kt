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
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

abstract class YoutubeScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long) : Scraper(containerDir, db, contentEntryUid) {

    private val ytPath: String
    private val gson: Gson
    private var tempDir: File? = null

    init {
        ContentScraperUtil.checkIfPathsToDriversExist()
        ytPath = System.getProperty(ContentScraperUtil.YOUTUBE_DL_PATH_KEY)
        gson = GsonBuilder().disableHtmlEscaping().create()
    }

    fun scrapeYoutubeLink(sourceUrl: String, videoQualityOption: String = "worst[ext=webm]/worst"): ContainerManager? {

        UMLogUtil.logTrace("starting youtube scrape for $sourceUrl")

        val ytExeFile = File(ytPath)
        if (!ytExeFile.exists()) {
            hideContentEntry()
            close()
            throw ScraperException(ERROR_TYPE_MISSING_EXE, "Webp executable does not exist: $ytPath")
        }

        tempDir = Files.createTempDirectory(sourceUrl.substringAfter("=")).toFile()

        youtubeLocker.withLock {

            UMLogUtil.logTrace("starting youtube lock")

            val builder = ProcessBuilder(ytPath, "-f", videoQualityOption, "-o", "${tempDir!!.absolutePath}/%(id)s.%(ext)s", sourceUrl)
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
                hideContentEntry()
                close()
                throw ScraperException(ERROR_TYPE_YOUTUBE_ERROR, "${e.message} failed with youtube with ytUrl $sourceUrl")
            } finally {
                process?.destroy()
            }
        }
        UMLogUtil.logTrace("ending youtube lock")

        val videoFile = tempDir!!.listFiles()[0]
        val mimetype = Files.probeContentType(videoFile.toPath())

        if (!VIDEO_MIME_MAP.keys.contains(mimetype)) {
            hideContentEntry()
            close()
            throw ScraperException(ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED, "Video type not supported for $mimetype")
        }

        val recentContainer = containerDao.getMostRecentContainerForContentEntry(contentEntryUid)

        if (recentContainer != null) {
            val isUpdated = videoFile.lastModified() > recentContainer.cntLastModified
            if (!isUpdated) {
                showContentEntry()
                close()
                Thread.sleep(4000)

                return null
            }
        }


        val containerManager = ContainerManager(createBaseContainer(mimetype), db, db, containerDir.absolutePath)
        runBlocking {
            containerManager.addEntries(ContainerManager.FileEntrySource(videoFile, videoFile.name))
        }

        Thread.sleep(4000)

        close()

        return containerManager


    }

    override fun close() {
        tempDir?.deleteRecursively()
    }

    companion object {

        private val youtubeLocker = ReentrantLock()

    }
}