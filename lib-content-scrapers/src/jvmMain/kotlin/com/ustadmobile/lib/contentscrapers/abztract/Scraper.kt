package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETPYE_MPEG
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_EPUB
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_KHAN
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_MKV
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_MP4
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_PDF
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_WEBM
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_WEB_CHUNK
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@ExperimentalStdlibApi
abstract class Scraper(val containerDir: File, val db: UmAppDatabase, var contentEntryUid: Long, val sqiUid: Int) {

    val mimeTypeToContentFlag: Map<String, Int> = mapOf(
            MIMETYPE_PDF to ContentEntry.TYPE_DOCUMENT,
            MIMETPYE_MPEG to ContentEntry.TYPE_AUDIO,
            MIMETYPE_WEB_CHUNK to ContentEntry.TYPE_INTERACTIVE_EXERCISE,
            MIMETYPE_KHAN to ContentEntry.TYPE_VIDEO,
            MIMETYPE_MP4 to ContentEntry.TYPE_VIDEO,
            MIMETYPE_MKV to ContentEntry.TYPE_VIDEO,
            MIMETYPE_WEBM to ContentEntry.TYPE_VIDEO,
            MIMETYPE_EPUB to ContentEntry.TYPE_EBOOK
    )

    val contentEntryParentChildJoinDao = db.contentEntryParentChildJoinDao
    val contentEntryDao = db.contentEntryDao
    val containerDao = db.containerDao
    val scrapeQueueDao = db.scrapeQueueItemDao

    abstract fun scrapeUrl(sourceUrl: String)

    abstract fun close()

    fun createBaseContainer(containerMimeType: String): Container {
        val container = Container().apply {
            containerContentEntryUid = contentEntryUid
            mimeType = containerMimeType
            mobileOptimized = true
            cntLastModified = System.currentTimeMillis()
        }
        container.containerUid = containerDao.insert(container)
        return container
    }

    fun hideContentEntry() {
        contentEntryDao.updateContentEntryInActive(contentEntryUid, true)
    }

    fun showContentEntry() {
        contentEntryDao.updateContentEntryInActive(contentEntryUid, false)
    }

    fun setScrapeDone(successful: Boolean, errorCode: Int){
        scrapeQueueDao.updateSetStatusById(sqiUid, if (successful) ScrapeQueueItemDao.STATUS_DONE else ScrapeQueueItemDao.STATUS_FAILED, errorCode)
    }

    fun isUrlContentUpdated(url: URL, container: Container): Boolean {
        val conn = (url.openConnection() as HttpURLConnection)
        val lastModified = conn.lastModified
        val eTagHeaderValue = conn.getHeaderField(ETAG.toLowerCase())

        if (lastModified != 0L) {
            return lastModified > container.cntLastModified
        }

        if (eTagHeaderValue != null) {
            val eTag = db.containerETagDao.getEtagOfContainer(container.containerUid)
            return eTag != eTag
        }
        conn.disconnect()
        return true
    }

    companion object {

        const val LAST_MODIFIED = "Last-Modified"

        const val ETAG = "ETag"
        
        const val FILE_SIZE_LIMIT = 440401920

        const val ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED = 100
        const val ERROR_TYPE_INVALID_LICENSE = 101
        const val ERROR_TYPE_NO_FILE_AVAILABLE = 102
        const val ERROR_TYPE_FILE_SIZE_LIMIT_EXCEEDED = 103
        const val ERROR_TYPE_ENTRY_NOT_CREATED = 104


        const val ERROR_TYPE_CONTENT_NOT_FOUND = 200
        const val ERROR_TYPE_NO_SOURCE_URL_FOUND = 201
        const val ERROR_TYPE_LINK_NOT_FOUND = 203
        const val ERROR_TYPE_YOUTUBE_ERROR = 210
        const val ERROR_TYPE_UNKNOWN_YOUTUBE = 220
        const val ERROR_TYPE_MISSING_EXECUTABLE = 230
        const val ERROR_TYPE_MISSING_QUESTIONS = 240
        const val ERROR_TYPE_ILLEGAL_STATE = 250
        const val ERROR_TYPE_PRACTICE_CONTENT_NOT_FOUND = 260
        const val ERROR_TYPE_TIMEOUT = 300
        const val ERROR_TYPE_KHAN_QUESTION_SOLVER = 350

    }

}