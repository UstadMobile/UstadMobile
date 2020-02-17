package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.core.util.MimeType
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

abstract class Scraper(val containerDir: File, val db: UmAppDatabase, var contentEntryUid: Long, val sqiUid: Int) {

    val mimeTypeToContentFlag: Map<String, Int> = mapOf(
            MimeType.PDF to ContentEntry.DOCUMENT_TYPE,
            MimeType.MPEG to ContentEntry.AUDIO_TYPE,
            MimeType.WEB_CHUNK to ContentEntry.INTERACTIVE_EXERICSE_TYPE,
            MimeType.KHAN_VIDEO to ContentEntry.VIDEO_TYPE,
            MimeType.MP4 to ContentEntry.VIDEO_TYPE,
            MimeType.MKV to ContentEntry.VIDEO_TYPE,
            MimeType.WEBM to ContentEntry.VIDEO_TYPE,
            MimeType.EPUB to ContentEntry.EBOOK_TYPE
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

        const val ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED = 100
        const val ERROR_TYPE_INVALID_LICENSE = 101
        const val ERROR_TYPE_NO_FILE_AVAILABLE = 102


        const val ERROR_TYPE_FILE_NOT_LOADED = 200
        const val ERROR_TYPE_NO_SOURCE_URL_FOUND = 201
        const val ERROR_TYPE_LINK_NOT_FOUND = 203
        const val ERROR_TYPE_YOUTUBE_ERROR = 210
        const val ERROR_TYPE_MISSING_EXE = 230

    }

}