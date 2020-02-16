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
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.pow

abstract class YoutubeScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long, sqiUid: Int) : Scraper(containerDir, db, contentEntryUid, sqiUid) {

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

        if (youtubeLocker.tryLock(2, TimeUnit.MINUTES)) {
            var process: Process? = null
            try {
                Thread.sleep(10000)
                UMLogUtil.logTrace("starting youtube lock")
                val builder = ProcessBuilder(ytPath, "--retries", "1", "--limit-rate", "2M", "-f", videoQualityOption, "-o", "${tempDir!!.absolutePath}/%(id)s.%(ext)s", sourceUrl)
                process = builder.start()
                process.waitFor()
                val exitValue = process.exitValue()
                if (exitValue != 0) {
                    UMLogUtil.logError("Error Stream for src $sourceUrl with error code  ${UMIOUtils.readStreamToString(process.errorStream)}")
                    failureAttempts.add(System.currentTimeMillis())
                    println(UMIOUtils.readStreamToString(process.errorStream))
                    val numberOfFailures = 1 + failureAttempts.count {
                        it >= (System.currentTimeMillis() - THRESHOLD_TIMEOUT)
                    }
                    lockedUntil = baseRetry.pow(numberOfFailures) + System.currentTimeMillis()
                    setScrapeQueueDelay(lockedUntil.toLong())
                    setScrapeDone(false, ERROR_TYPE_YOUTUBE_ERROR)

                    throw IOException("failed with youtube with ytUrl $sourceUrl")
                }
            } catch (e: Exception) {
                hideContentEntry()
                close()
                setScrapeDone(false, ERROR_TYPE_YOUTUBE_ERROR)
                throw e
            } finally {
                process?.destroy()
                youtubeLocker.unlock()
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
                return null
            }
        }


        val containerManager = ContainerManager(createBaseContainer(mimetype), db, db, containerDir.absolutePath)
        runBlocking {
            containerManager.addEntries(ContainerManager.FileEntrySource(videoFile, videoFile.name))
        }

        close()

        return containerManager


    }

    override fun close() {
        tempDir?.deleteRecursively()
    }

    companion object {

        const val THRESHOLD_TIMEOUT = 1800000

        var failureAttempts = mutableListOf<Long>()

        var lockedUntil: Float = 0f

        const val baseRetry: Float = 10f

        private val youtubeLocker = ReentrantLock()

    }
}