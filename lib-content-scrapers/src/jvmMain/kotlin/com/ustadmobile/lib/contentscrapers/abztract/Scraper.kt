package com.ustadmobile.lib.contentscrapers.abztract

import io.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.core.db.dao.ScrapeQueueItemDaoCommon
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.systemTimeInMillis
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
import com.ustadmobile.lib.db.entities.ScrapeQueueItemWithScrapeRun
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

abstract class Scraper(var contentEntryUid: Long, val sqiUid: Int, var parentContentEntryUid: Long, endpoint: Endpoint, di: DI) {

    val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    val containerFolder: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    protected val okHttpClient: OkHttpClient by di.instance()

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

    var contentEntry: ContentEntry? = null
    var parentContentEntry: ContentEntry? = null
    var scrapeQueueItem: ScrapeQueueItemWithScrapeRun? = null

    init {
        runBlocking {
            contentEntry = db.contentEntryDao.findByUid(contentEntryUid)
            parentContentEntry = db.contentEntryDao.findByUid(parentContentEntryUid)
            scrapeQueueItem = db.scrapeQueueItemDao.findByUid(sqiUid)
        }
    }

    abstract fun scrapeUrl(sourceUrl: String)

    abstract fun close()

    fun createBaseContainer(containerMimeType: String): Container {
        val container = Container().apply {
            containerContentEntryUid = contentEntryUid
            mimeType = containerMimeType
            mobileOptimized = true
            cntLastModified = System.currentTimeMillis()
        }
        container.containerUid = repo.containerDao.insert(container)
        return container
    }

    fun hideContentEntry() {
        repo.contentEntryDao.updateContentEntryInActive(contentEntryUid, true,
            systemTimeInMillis())
    }

    fun showContentEntry() {
        repo.contentEntryDao.updateContentEntryInActive(contentEntryUid, false,
            systemTimeInMillis())
    }

    fun setScrapeDone(successful: Boolean, errorCode: Int) {
        db.scrapeQueueItemDao.updateSetStatusById(sqiUid, if (successful) ScrapeQueueItemDaoCommon.STATUS_DONE else ScrapeQueueItemDaoCommon.STATUS_FAILED, errorCode)
    }

    data class HeadRequestValues(val isUpdated: Boolean, val etag: String, val mimeType: String, val lastModified: Long)

    fun isUrlContentUpdated(url: URL, container: Container?): HeadRequestValues {
        var response: Response? = null
        try {
            val headRequest = Request.Builder()
                .url(url.toString())
                .head()
                .build()
            response = okHttpClient.newCall(headRequest).execute()

            val mimeType = response.header("content-type") ?: ""
            val lastModified = response.headers.getDate("last-modified")?.time ?: 0L
            val eTagHeaderValue = response.header(ETAG.toLowerCase())

            var isUpdated = true
            if (lastModified != 0L) {
                isUpdated = lastModified > container?.cntLastModified ?: 0
            }

            if (isUpdated && eTagHeaderValue != null) {
                val eTag = db.containerETagDao.getEtagOfContainer(container?.containerUid ?: 0)
                if (!eTag.isNullOrEmpty()) {
                    isUpdated = eTagHeaderValue != eTag
                }
            }

            return HeadRequestValues(isUpdated, eTagHeaderValue ?: "", mimeType, lastModified)
        }catch(e: Exception) {
            Napier.e("Exception attempting to check $url updated", e)
            throw e
        }finally {
            response?.closeQuietly()
        }
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