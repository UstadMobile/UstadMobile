package com.ustadmobile.lib.contentscrapers.abztract

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.controller.VideoContentPresenterCommon.Companion.VIDEO_MIME_MAP
import com.ustadmobile.core.io.ext.addFileToContainer
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.util.YoutubeData
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.pow
import kotlin.random.Random
import kotlin.system.exitProcess

open class YoutubeScraper(contentEntryUid: Long, sqiUid: Int, parentContentEntryUid: Long, endpoint: Endpoint, open val di: DI) : Scraper(contentEntryUid, sqiUid, parentContentEntryUid, endpoint, di) {

    private val ytPath: String
    private val gson: Gson
    private var tempDir: File? = null

    init {
        ContentScraperUtil.checkIfPathsToDriversExist()
        ytPath = System.getProperty(ContentScraperUtil.YOUTUBE_DL_PATH_KEY)
        gson = GsonBuilder().disableHtmlEscaping().create()
    }

    protected fun scrapeYoutubeVideo(sourceUrl: String, videoQualityOption: String = "worst[ext=webm]/worst") {

        UMLogUtil.logTrace("starting youtube scrape for $sourceUrl")

        val ytExeFile = File(ytPath)
        if (!ytExeFile.exists()) {
            hideContentEntry()
            close()
            throw ScraperException(ERROR_TYPE_MISSING_EXECUTABLE, "Webp executable does not exist: $ytPath")
        }

        tempDir = Files.createTempDirectory(sourceUrl.substringAfter("=")).toFile()

        youtubeLocker.withLock {
            UMLogUtil.logTrace("starting youtube lock scraper")
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
                        val error = process.errorStream.readString()
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
        UMLogUtil.logTrace("ending youtube lock scraper")

        val videoFile = tempDir!!.listFiles()[0]
        val mimetype = Files.probeContentType(videoFile.toPath())

        if (!VIDEO_MIME_MAP.keys.contains(mimetype)) {
            hideContentEntry()
            close()
            setScrapeDone(false, ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED)
            throw ScraperException(ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED, "Video type not supported for $mimetype")
        }

        val recentContainer = db.containerDao.getMostRecentContainerForContentEntry(contentEntryUid)

        if (recentContainer != null) {
            val isUpdated = videoFile.lastModified() > recentContainer.cntLastModified
            if (!isUpdated) {
                showContentEntry()
                setScrapeDone(true, 0)
                close()
                return
            }
        }

        val container = createBaseContainer(mimetype)
        val containerAddOptions = ContainerAddOptions(storageDirUri = containerFolder.toDoorUri())
        runBlocking {
            repo.addFileToContainer(container.containerUid, videoFile.toDoorUri(),
                    videoFile.name, Any(), di, containerAddOptions)
        }

        showContentEntry()
        setScrapeDone(true, 0)
        close()

    }

    override fun scrapeUrl(sourceUrl: String) {

        var entry: ContentEntry? = null
        runBlocking {
            entry = db.contentEntryDao.findByUidAsync(contentEntryUid)
        }

        if (entry == null) {
            close()
            hideContentEntry()
            setScrapeDone(false, ERROR_TYPE_ENTRY_NOT_CREATED)
            throw ScraperException(ERROR_TYPE_ENTRY_NOT_CREATED, "entry was not created $sourceUrl")
        }

        val data = getJsonInfo(sourceUrl)

        if (data == null) {
            hideContentEntry()
            setScrapeDone(false, 0)
            close()
            throw ScraperException(0, "No Data Found after running youtube-dl")
        }

        ContentScraperUtil.createOrUpdateContentEntry(data.id!!, data.title, sourceUrl,
                entry?.publisher ?: "", entry?.licenseType ?: 0,
                entry?.primaryLanguageUid ?: 0, entry?.languageVariantUid,
                data.description, true, "", data.thumbnail, "", "",
                ContentEntry.TYPE_VIDEO, repo.contentEntryDao)

        scrapeYoutubeVideo(sourceUrl)


        setScrapeDone(true, 0)
        showContentEntry()
        UMLogUtil.logError("end of scrape")


    }

    private fun getJsonInfo(sourceUrl: String): YoutubeData? {

        val ytExeFile = File(ytPath)
        if (!ytExeFile.exists()) {
            hideContentEntry()
            close()
            throw ScraperException(ERROR_TYPE_MISSING_EXECUTABLE, "Webp executable does not exist: $ytPath")
        }

        youtubeLocker.withLock {
            UMLogUtil.logTrace("starting youtube lock json")
            var retryFlag = true
            var numberOfFailures = 1
            while (retryFlag) {

                var process: Process? = null
                try {
                    Thread.sleep(Random.nextLong(10000, 30000))
                    val builder = ProcessBuilder(ytPath, "--retries", "1", "-i", "-J","--flat-playlist",  sourceUrl)
                    process = builder.start()
                    process.waitFor(30, TimeUnit.SECONDS)
                    val data = process.inputStream.readString()
                    val exitValue = process.exitValue()
                    if (exitValue != 0) {
                        val error =  process.errorStream.readString()
                        UMLogUtil.logError("Error Stream for src $sourceUrl with error code  $error")
                        if (!error.contains("429")) {
                            throw ScraperException(ERROR_TYPE_UNKNOWN_YOUTUBE, "unknown error: $error")
                        }
                        throw IOException("Failed $numberOfFailures for  $sourceUrl")
                    }
                    retryFlag = false
                    return gson.fromJson(data, YoutubeData::class.java)
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
                    UMLogUtil.logTrace("ending youtube lock json")
                }
            }
        }
        UMLogUtil.logTrace("ending youtube lock json")

        return null
    }


    override fun close() {
        val deleted = tempDir?.deleteRecursively() ?: false
        UMLogUtil.logError("did it delete: $deleted for ${tempDir?.name} ")
    }

    companion object {

        var lockedUntil: Float = 0f

        const val baseRetry: Float = 10f

        val youtubeLocker = ReentrantLock()

    }
}