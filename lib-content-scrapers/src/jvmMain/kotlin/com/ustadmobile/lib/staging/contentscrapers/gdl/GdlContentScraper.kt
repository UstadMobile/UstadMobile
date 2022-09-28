package com.ustadmobile.lib.staging.contentscrapers.gdl

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.core.db.dao.ScrapeQueueItemDaoCommon
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_EPUB
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_PDF
import com.ustadmobile.lib.contentscrapers.ShrinkerUtil
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.staging.contentscrapers.replaceMeWithDi
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.exception.ExceptionUtils
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class GdlContentScraper(var scrapeUrl: URL, var destLocation: File, var containerDir: File, var parentEntry: ContentEntry, var contentType: String, var sqiUid: Int) : Runnable {


    private var isContentUpdated: Boolean = true

    override fun run() {
        //replace with DI
        lateinit var db: UmAppDatabase
        //val db = UmAppDatabase.getInstance(Any(), replaceMeWithDi())
        val repository = db// db.getRepository("https://localhost", "")
        val containerDao = repository.containerDao
        val queueDao = db.scrapeQueueItemDao


        val startTime = System.currentTimeMillis()
        UMLogUtil.logInfo("Started scraper url $scrapeUrl at start time: $startTime with squUid $sqiUid")
        queueDao.setTimeStarted(sqiUid, startTime)
        var successful = false
        try {
            var content: File

            when {
                MIMETYPE_EPUB == contentType -> {
                    content = File(destLocation, destLocation.name)
                    scrapeEpubContent(scrapeUrl.toString())
                    successful = true
                }
                MIMETYPE_PDF == contentType -> {
                    content = File(destLocation, destLocation.name)
                    content = File(content, FilenameUtils.getName(scrapeUrl.path))
                    scrapePdfContent(scrapeUrl.toString())
                    successful = true
                }
                else -> {
                    UMLogUtil.logError("unsupported kind = $contentType at url = $scrapeUrl")
                    throw IllegalArgumentException("unsupported kind = $contentType at url = $scrapeUrl")
                }
            }

            if (isContentUpdated) {
                ContentScraperUtil.insertContainer(containerDao, parentEntry, true,
                        contentType, content.lastModified(), content, db, repository,
                        containerDir)

            }

        } catch (e: Exception) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Unable to scrape content from url $scrapeUrl")
            ContentScraperUtil.deleteETagOrModified(destLocation, destLocation.name)
        }

        queueDao.updateSetStatusById(sqiUid, if (successful) ScrapeQueueItemDaoCommon.STATUS_DONE else ScrapeQueueItemDaoCommon.STATUS_FAILED, 0)
        queueDao.setTimeFinished(sqiUid, System.currentTimeMillis())
        val duration = System.currentTimeMillis() - startTime
        UMLogUtil.logInfo("Ended scrape for url $scrapeUrl in duration: $duration squUid  $sqiUid")
    }

    private fun scrapeEpubContent(scrapeUrl: String) {

        val folder = File(destLocation, destLocation.name)
        folder.mkdirs()

        val url = URL(scrapeUrl)
        var conn: HttpURLConnection? = null
        try {
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "HEAD"

            isContentUpdated = ContentScraperUtil.isFileModified(conn, destLocation, destLocation.name)

            if (!isContentUpdated) {
                return
            }

            val contentFile = File(destLocation, FilenameUtils.getName(url.path))
            FileUtils.copyURLToFile(url, contentFile)
            ShrinkerUtil.shrinkEpub(contentFile)


        } catch (e: Exception) {
            throw e
        } finally {
            conn?.disconnect()
        }

    }


    private fun scrapePdfContent(scrapeUrl: String) {
        val folder = File(destLocation, destLocation.name)
        folder.mkdirs()

        val url = URL(scrapeUrl)
        var conn: HttpURLConnection? = null
        try {
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "HEAD"

            isContentUpdated = ContentScraperUtil.isFileModified(conn, destLocation, destLocation.name)

            if (!isContentUpdated) {
                return
            }

            val contentFile = File(folder, FilenameUtils.getName(url.path))
            FileUtils.copyURLToFile(url, contentFile)


        } catch (e: Exception) {
            throw e
        } finally {
            conn?.disconnect()
        }
    }


}