package com.ustadmobile.lib.contentscrapers.khanacademy

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.controller.VideoPlayerPresenterCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.abztract.ScraperException
import com.ustadmobile.lib.contentscrapers.abztract.YoutubeScraper
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanContentIndexer.Companion.KHAN_PREFIX
import com.ustadmobile.lib.db.entities.ContainerETag
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files


class KhanLiteVideoScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long, sqiUid: Int) : YoutubeScraper(containerDir, db, contentEntryUid, sqiUid) {


    override fun scrapeUrl(sourceUrl: String) {

        var entry: ContentEntry? = null
        runBlocking {
            entry = contentEntryDao.findByUidAsync(contentEntryUid)
        }

        if (entry == null) {
            throw ScraperException(ERROR_TYPE_NO_SOURCE_URL_FOUND, "Content Entry was not found for url $sourceUrl")
        }

        val khanId = entry!!.sourceUrl!!.substringAfter(KHAN_PREFIX)


        val url = getValidUrl(khanId)

        if (url == null) {

           /* hideContentEntry()
            throw ScraperException(ERROR_TYPE_YOUTUBE_ERROR, "stopped youtube $sourceUrl")*/

            val ytUrl = getYoutubeUrl(khanId)
            try {
                scrapeYoutubeLink(ytUrl)
            } catch (e: Exception) {
                hideContentEntry()
                throw e
            }

        } else {

            val conn = (url.openConnection() as HttpURLConnection)
            val eTag = conn.getHeaderField("etag")
            val mimetype = conn.contentType
            conn.disconnect()

            if (!VideoPlayerPresenterCommon.VIDEO_MIME_MAP.keys.contains(mimetype)) {
                hideContentEntry()
                throw ScraperException(ERROR_TYPE_MIME_TYPE_NOT_SUPPORTED, "Video type not supported for $mimetype for url $url")
            }

            val ext = VideoPlayerPresenterCommon.VIDEO_MIME_MAP[mimetype]

            val recentContainer = containerDao.getMostRecentContainerForContentEntry(contentEntryUid)

            if (recentContainer != null) {
                val isUpdated = isUrlContentUpdated(url, recentContainer)
                if (!isUpdated) {
                    showContentEntry()
                    return
                }
            }

            val tempDir = Files.createTempDirectory(khanId).toFile()
            val tempFile = File(tempDir, khanId + ext)
            FileUtils.copyURLToFile(url, tempFile)

            val container = createBaseContainer(mimetype)
            val containerManager = ContainerManager(container, db, db, containerDir.absolutePath)
            runBlocking {
                containerManager.addEntries(ContainerManager.FileEntrySource(tempFile, tempFile.name))
            }
            if (!eTag.isNullOrEmpty()) {
                val etagContainer = ContainerETag(container.containerUid, eTag)
                db.containerETagDao.insert(etagContainer)
            }

            tempDir.deleteRecursively()

        }

    }

    private fun getValidUrl(khanId: String): URL? {
        val lowUrl = URL(getMp4LowUrl(khanId))
        val mp4Url = URL(getMp4Url(khanId))
        return when {
            isUrlValid(lowUrl) -> {
                lowUrl
            }
            isUrlValid(mp4Url) -> {
                mp4Url
            }
            else -> {
                null
            }
        }
    }

    private fun isUrlValid(url: URL): Boolean {
        val huc: HttpURLConnection = url.openConnection() as HttpURLConnection
        huc.requestMethod = "HEAD"

        val responseCode: Int = huc.responseCode

        return responseCode == 200
    }


    override fun close() {
    }

    fun getMp4LowUrl(videoId: String): String {
        return "https://cdn.kastatic.org/ka-youtube-converted/$videoId.mp4-low/$videoId-low.mp4"
    }

    fun getMp4Url(videoId: String): String {
        return "https://cdn.kastatic.org/ka-youtube-converted/$videoId.mp4/$videoId.mp4"
    }

    fun getYoutubeUrl(videoId: String): String {
        return "https://www.youtube.com/watch?v=$videoId"
    }

}