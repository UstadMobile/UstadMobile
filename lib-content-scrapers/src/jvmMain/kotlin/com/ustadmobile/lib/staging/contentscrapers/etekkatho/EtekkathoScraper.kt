package com.ustadmobile.lib.contentscrapers.etekkatho

import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * The etekkatho website has a single url link for download
 * This can be found by using Jsoup css selector query with th:contains(Download) ~td a[href].btn and getting the attribute value of href
 *
 *
 * The url requires a request property header with user agent for the download to be successful
 */

class EtekkathoScraper @Throws(IOException::class)
constructor(url: String, private val destinationDir: File) {

    private val scrapUrl: URL = URL(url)
    private val etekDirectory: File = File(destinationDir, url.substring(url.indexOf("=") + 1))
    var isUpdated: Boolean = false
        private set
    var mimeType: String? = null
        private set

    init {
        etekDirectory.mkdirs()
    }

    fun scrapeContent() {
        var conn: HttpURLConnection? = null
        try {
            val document = Jsoup.connect(scrapUrl.toString()).get()

            var hrefLink = document.selectFirst("th:contains(Download) ~td a[href].btn")?.attr("href")
            hrefLink = hrefLink?.replace(" ".toRegex(), "_")

            val contentUrl = URL(scrapUrl, hrefLink)
            conn = contentUrl.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Mobile Safari/537.36")

            val content = File(etekDirectory, etekDirectory.name)

            isUpdated = ContentScraperUtil.isFileModified(conn, etekDirectory, etekDirectory.name)

            if (ContentScraperUtil.fileHasContent(content)) {
                isUpdated = false
                ContentScraperUtil.deleteFile(content)
                return
            }

            if (!isUpdated) {
                return
            }

            mimeType = conn.contentType


            FileUtils.copyInputStreamToFile(conn.inputStream, content)
        } catch (e: IOException) {
            UMLogUtil.logError("Unable to download content for etekkatho for url $scrapUrl")
            ContentScraperUtil.deleteETagOrModified(etekDirectory, etekDirectory.name)
        } finally {
            conn?.disconnect()
        }

    }
}
