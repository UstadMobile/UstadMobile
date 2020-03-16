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
import kotlin.math.pow
import kotlin.random.Random
import kotlin.system.exitProcess

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
            throw ScraperException(ERROR_TYPE_MISSING_EXECUTABLE, "Webp executable does not exist: $ytPath")
        }

        tempDir = Files.createTempDirectory(sourceUrl.substringAfter("=")).toFile()

        youtubeLocker.withLock {
            UMLogUtil.logTrace("starting youtube lock")
            var retryFlag = true
            var numberOfFailures = 1
            while (retryFlag) {

                var process: Process? = null
                try {
                    Thread.sleep(Random.nextLong(10000, 30000))
                    val builder = ProcessBuilder(ytPath, "--retries", "1", "--limit-rate", "1M", "-f", videoQualityOption, "-o", "${tempDir!!.absolutePath}/%(id)s.%(ext)s", sourceUrl)
                    process = builder.start()
                    process.waitFor()
                    val exitValue = process.exitValue()
                    if (exitValue != 0) {
                        val error = UMIOUtils.readStreamToString(process.errorStream)
                        UMLogUtil.logError("Error Stream for src $sourceUrl with error code  $error")
                        if (!error.contains("429")) {
                            throw ScraperException(ERROR_TYPE_UNKNOWN_YOUTUBE, "unknown error: $error")
                        }
                        throw IOException("Failed $numberOfFailures for  $sourceUrl")
                    }
                    retryFlag = false
                } catch (s: ScraperException) {
                    setScrapeDone(false, ERROR_TYPE_UNKNOWN_YOUTUBE)
                    hideContentEntry()
                    close()
                    throw s
                } catch (e: Exception) {

                    if (numberOfFailures > 5) {
                        setScrapeDone(false, ERROR_TYPE_YOUTUBE_ERROR)
                        hideContentEntry()
                        close()
                        exitProcess(1)
                    }

                    lockedUntil = baseRetry.pow(numberOfFailures) * 1000
                    UMLogUtil.logError("caught youtube exception with lockedUntil value of ${lockedUntil.toLong()}")
                    Thread.sleep(lockedUntil.toLong())

                    numberOfFailures++

                } finally {
                    process?.destroy()
                }
            }
        }
        UMLogUtil.logTrace("ending youtube lock")

        val videoFile = tempDir!!.listFiles()[0]
        val mimetype = Files.probeContentType(videoFile.toPath())

        if (!VIDEO_MIME_MAP.keys.contains(mimetype)) {
            hideContentEntry()
            close()
            setScrapeDone(false, ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED)
            throw ScraperException(ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED, "Video type not supported for $mimetype")
        }

        val recentContainer = containerDao.getMostRecentContainerForContentEntry(contentEntryUid)

        if (recentContainer != null) {
            val isUpdated = videoFile.lastModified() > recentContainer.cntLastModified
            if (!isUpdated) {
                showContentEntry()
                setScrapeDone(true, 0)
                close()
                return null
            }
        }


        val containerManager = ContainerManager(createBaseContainer(mimetype), db, db, containerDir.absolutePath)
        runBlocking {
            containerManager.addEntries(ContainerManager.FileEntrySource(videoFile, videoFile.name))
        }


        showContentEntry()
        setScrapeDone(true, 0)
        close()

        UMLogUtil.logError("end of scrape")

        return containerManager


    }

    override fun close() {
        val deleted = tempDir?.deleteRecursively() ?: false
        UMLogUtil.logError("did it delete: $deleted for ${tempDir?.name} ")
    }

    companion object {

        var lockedUntil: Float = 0f

        const val baseRetry: Float = 10f

        private val youtubeLocker = ReentrantLock()

    }
}