package com.ustadmobile.lib.contentscrapers.ddl

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.contentscrapers.ddl.IndexDdlContent.Companion.DDL
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import org.jsoup.Jsoup
import java.io.File

class DdlListIndexer(contentEntryUid: Long, runUid: Int, db: UmAppDatabase) : Indexer(contentEntryUid, runUid, db) {

    override fun indexUrl(sourceUrl: String) {

        val document = Jsoup.connect(sourceUrl)
                .header("X-Requested-With", "XMLHttpRequest").get()

        val langCode = languageDao.findByUid(contentEntry?.primaryLanguageUid!!)?.iso_639_1_standard

        val resourceList = document.select("article a[href]")

        var counter = (sourceUrl.substringAfter("page=").toInt() - 1) * 32 + 500
        for (resource in resourceList) {

            val href = resource.attr("href")
            if (href.contains("resource/")) {

                val index = href.indexOf("af/")
                if(href.indexOf("af/") == -1){
                    UMLogUtil.logError("$DDL did not give full url as expected for href $href")
                    continue
                }

                val url = StringBuilder(href).insert(index + 3, "$langCode/").toString()

                val entry = ContentScraperUtil.insertTempContentEntry(contentEntryDao, url, contentEntry!!.primaryLanguageUid)

                ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentEntryParentChildJoinDao, contentEntry!!, entry, counter++)

                createQueueItem(url,  entry, ScraperTypes.DDL_ARTICLE_SCRAPER, ScrapeQueueItem.ITEM_TYPE_SCRAPE)
            }

        }


    }

    override fun close() {


    }
}