package com.ustadmobile.lib.contentscrapers.apache

import ScraperTypes
import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants.SCRAPER_TAG
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import com.ustadmobile.port.sharedse.contentformats.mimeTypeSupported
import org.apache.commons.lang3.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.kodein.di.DI
import java.net.HttpURLConnection
import java.net.URL

@ExperimentalStdlibApi
class ApacheIndexer(parentContentEntryUid: Long, runUid: Int, sqiUid: Int, contentEntryUid: Long, endpoint: Endpoint, di: DI) : Indexer(parentContentEntryUid, runUid, sqiUid, contentEntryUid, endpoint, di) {

    private val logPrefix = "[ApacheIndexer SQI ID #$sqiUid] "

    override fun indexUrl(sourceUrl: String) {
        
        val srcUrl = sourceUrl.requirePostfix()
        val url = URL(srcUrl)

        val huc: HttpURLConnection = url.openConnection() as HttpURLConnection

        val data = String(huc.inputStream.readBytes())
        huc.disconnect()

        val document = Jsoup.parse(data)

        val folderTitle = document.title().substringAfterLast("/")

        Napier.i("$logPrefix found folder title $folderTitle", tag = SCRAPER_TAG)

        val folderEntry: ContentEntry
        val dbEntry = contentEntry
        folderEntry = if (dbEntry != null && scrapeQueueItem?.overrideEntry == true) {
            dbEntry
        }else{
            val entry = ContentScraperUtil.createOrUpdateContentEntry(folderTitle, folderTitle,
                    srcUrl, parentContentEntry?.publisher ?: "",
                    ContentEntry.LICENSE_TYPE_OTHER, englishLang.langUid, null,
                    "", false, "",
                    "", "",
                    "", ContentEntry.TYPE_COLLECTION, contentEntryDao)
            Napier.d("$logPrefix new entry created/updated with entryUid ${entry.contentEntryUid} with title $folderTitle", tag = SCRAPER_TAG)
            ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentEntryParentChildJoinDao, parentContentEntry, entry, 0)
            entry
        }


        document.select("tr:has([alt])").forEachIndexed { counter, it ->

            val alt = it.select("td [alt]").attr("alt")
            var conn: HttpURLConnection? = null
            try {
                val element = it.select("td a")
                val href = element.attr("href")
                val title = element.text()
                val hrefUrl = URL(url, href)

                if (alt == "[DIR]") {

                    Napier.i("$logPrefix found new directory with title $title for parent folder $folderTitle", tag = SCRAPER_TAG)

                    val childEntry = ContentScraperUtil.insertTempContentEntry(contentEntryDao, hrefUrl.toString(), folderEntry.primaryLanguageUid, title)
                    ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentEntryParentChildJoinDao, folderEntry, childEntry, counter)
                    createQueueItem(hrefUrl.toString(), childEntry, ScraperTypes.APACHE_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX, folderEntry.contentEntryUid)

                } else if (alt == "[   ]") {

                    Napier.i("$logPrefix found new file with title $title for parent folder $folderTitle", tag = SCRAPER_TAG)

                    conn = hrefUrl.openConnection() as HttpURLConnection
                    conn.requestMethod = "HEAD"
                    val mimeType = conn.contentType

                    val supported = mimeTypeSupported.find { fileMimeType -> fileMimeType == mimeType }

                    if (supported != null) {
                        Napier.d("$logPrefix file $title with $mimeType found", tag = SCRAPER_TAG)

                        val childEntry = ContentScraperUtil.insertTempContentEntry(contentEntryDao, hrefUrl.toString(), folderEntry.primaryLanguageUid, title)
                        ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentEntryParentChildJoinDao, folderEntry, childEntry, counter)
                        createQueueItem(hrefUrl.toString(), childEntry, ScraperTypes.URL_SCRAPER, ScrapeQueueItem.ITEM_TYPE_SCRAPE, folderEntry.contentEntryUid)
                    } else {
                        Napier.i("$logPrefix file: $title not supported with mimeType: $mimeType", tag = SCRAPER_TAG)
                    }
                } else {
                    Napier.i("$logPrefix found unknown apache: $title not supported with alt: $alt", tag = SCRAPER_TAG)
                }
            } catch (e: Exception) {
                Napier.e("$logPrefix Error during directory search on $srcUrl", tag = SCRAPER_TAG)
                Napier.e("$logPrefix ${ExceptionUtils.getStackTrace(e)}", tag = SCRAPER_TAG)
            } finally {
                conn?.disconnect()
            }
        }

        Napier.d("$logPrefix finished Indexing", tag = SCRAPER_TAG)
        setIndexerDone(true, 0)
        close()
    }

    override fun close() {

    }


}