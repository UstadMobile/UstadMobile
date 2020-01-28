package com.ustadmobile.lib.contentscrapers.abztract

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.checkIfPathsToDriversExist
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.HAB
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.util.YoutubeData
import org.apache.commons.io.FileUtils.readFileToString
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.nio.file.Files


typealias ModifyYoutubeJson = (jsonFile: YoutubeData) -> YoutubeData

abstract class YoutubePlaylistIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase) : Indexer(parentContentEntry, runUid, db) {

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
            throw IOException("Webp executable does not exist: $ytPath")
        }

        val tempDir = Files.createTempDirectory(sourceUrl.substringAfter("=")).toFile()

        val builder = ProcessBuilder(ytPath, "--write-info-json", "--skip-download",
                "-o", "${tempDir.absolutePath}/%(playlist_index)s", sourceUrl)

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
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            process?.destroy()
        }


        tempDir.listFiles()?.forEachIndexed { i, file ->

            try {

                val jsonString = readFileToString(file, ScraperConstants.UTF_ENCODING)
                var youtubeData = gson.fromJson(jsonString, YoutubeData::class.java)

                if (youtubeData.webpage_url == null) {
                    return@forEachIndexed
                }

                modify?.invoke(youtubeData)

            } catch (e: Exception) {
                UMLogUtil.logError("${HAB} Exception - Error with data for index $i in playlist $sourceUrl")
            }

        }

        tempDir.deleteRecursively()


        Thread.sleep(4000)

    }

}