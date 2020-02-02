package com.ustadmobile.lib.contentscrapers.khanacademy

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_KHAN
import com.ustadmobile.lib.contentscrapers.ShrinkerUtil
import com.ustadmobile.lib.contentscrapers.abztract.Scraper
import com.ustadmobile.lib.contentscrapers.abztract.ScraperException
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanContentIndexer.Companion.KHAN_PREFIX
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.util.HashMap


class KhanLiteVideoScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long) : Scraper(containerDir, db, contentEntryUid) {


    override fun scrapeUrl(sourceUrl: String) {

        var entry: ContentEntry? = null
        runBlocking {
            entry = contentEntryDao.findByUidAsync(contentEntryUid)
        }

        if (entry == null) {
            throw ScraperException(0, "Content Entry was not found for url $sourceUrl")
        }

        val khanId = entry!!.sourceUrl!!.substringAfter(KHAN_PREFIX)

        val tempDir = Files.createTempDirectory(khanId).toFile()
        val tempFile = File(tempDir, khanId)

        val url = getValidUrl(khanId)

        FileUtils.copyURLToFile(url, tempFile)
        val webMFile = File(tempDir, FilenameUtils.getName(url.path))
        ShrinkerUtil.convertKhanVideoToWebMAndCodec2(tempFile, webMFile)


        val fileMap = HashMap<File, String>()
        ContentScraperUtil.createContainerFromDirectory(tempDir, fileMap)

        val containerManager = ContainerManager(createBaseContainer(MIMETYPE_KHAN), db, db, containerDir.absolutePath)
        runBlocking {
            fileMap.forEach {
                containerManager.addEntries(ContainerManager.FileEntrySource(it.component1(), it.component2()))
            }
        }

        tempDir.deleteRecursively()
    }

    private fun getValidUrl(khanId: String): URL {
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
                throw ScraperException(ERROR_TYPE_NO_URL_FOUND, "no valid url for khan id $khanId)")
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

    companion object {

        const val ERROR_TYPE_NO_URL_FOUND = 200

    }

}