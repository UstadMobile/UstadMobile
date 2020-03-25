package com.ustadmobile.lib.contentscrapers.abztract

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.checkIfPathsToDriversExist
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.HAB
import com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.util.YoutubeData
import org.apache.commons.io.FileUtils.readFileToString
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.nio.file.Files
import kotlin.concurrent.withLock
import kotlin.math.pow
import kotlin.random.Random
import kotlin.system.exitProcess


typealias ModifyYoutubeJson = (jsonFile: YoutubeData) -> YoutubeData

abstract class YoutubePlaylistIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase, sqiUid: Int) : Indexer(parentContentEntry, runUid, db, sqiUid) {

    private val ytPath: String
    private val gson: Gson

    init {
        checkIfPathsToDriversExist()
        ytPath = System.getProperty(ContentScraperUtil.YOUTUBE_DL_PATH_KEY)
        gson = GsonBuilder().disableHtmlEscaping().create()
    }

    fun startPlayListIndexer(sourceUrl: String, modify: ModifyYoutubeJson? = null) {

        val ytExeFile = File(ytPath)
        if (!ytExeFile.exists()) {
            close()
            setIndexerDone(false, Scraper.ERROR_TYPE_MISSING_EXECUTABLE)
            throw IOException("Webp executable does not exist: $ytPath")
        }

        val tempDir = Files.createTempDirectory(sourceUrl.substringAfter("=")).toFile()

        val builder = ProcessBuilder(ytPath, "--limit-rate", "1M", "--retries", "1", "--write-info-json", "--skip-download",
                "-o", "${tempDir.absolutePath}/%(playlist_index)s", sourceUrl)

        YoutubeScraper.youtubeLocker.withLock {

            var retryFlag = true
            var numberOfFailures = 1
            while (retryFlag) {

                var process: Process? = null
                try {
                    Thread.sleep(Random.nextLong(10000, 30000))
                    process = builder.start()
                    process!!.waitFor()
                    val exitValue = process.exitValue()
                    if (exitValue != 0) {
                        val error = UMIOUtils.readStreamToString(process.errorStream)
                        UMLogUtil.logError("Error Stream for src $sourceUrl with error code  $error")
                        if (!error.contains("429")) {
                            throw ScraperException(Scraper.ERROR_TYPE_UNKNOWN_YOUTUBE, "unknown error: $error")
                        }
                        throw IOException("Failed $numberOfFailures for  $sourceUrl")
                    }
                    retryFlag = false
                } catch (s: ScraperException) {
                    setIndexerDone(false, Scraper.ERROR_TYPE_UNKNOWN_YOUTUBE)
                    close()
                    throw s
                }catch (e: Exception) {
                    if (numberOfFailures > 5) {
                        setIndexerDone(false, Scraper.ERROR_TYPE_YOUTUBE_ERROR)
                        exitProcess(1)
                    }

                    YoutubeScraper.lockedUntil = YoutubeScraper.baseRetry.pow(numberOfFailures) * 1000
                    UMLogUtil.logError("caught youtube exception with lockedUntil value of ${YoutubeScraper.lockedUntil.toLong()}")
                    Thread.sleep(YoutubeScraper.lockedUntil.toLong())

                    numberOfFailures++
                } finally {
                    process?.destroy()
                }

            }

        }
        UMLogUtil.logTrace("ending youtube lock")



        tempDir.listFiles()?.forEachIndexed { i, file ->

            try {

                val jsonString = readFileToString(file, ScraperConstants.UTF_ENCODING)
                val youtubeData = gson.fromJson(jsonString, YoutubeData::class.java)

                if (youtubeData.webpage_url == null) {
                    return@forEachIndexed
                }

                modify?.invoke(youtubeData)

            } catch (e: Exception) {
                UMLogUtil.logError("$KHAN Exception - Error with data for index $i in playlist $sourceUrl")
            }

        }

        tempDir.deleteRecursively()
    }

}