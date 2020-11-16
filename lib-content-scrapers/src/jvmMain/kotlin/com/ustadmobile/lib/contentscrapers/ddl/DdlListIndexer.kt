package com.ustadmobile.lib.contentscrapers.ddl

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.contentscrapers.ddl.IndexDdlContent.Companion.DDL
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import org.jsoup.Jsoup
import org.kodein.di.DI

@ExperimentalStdlibApi
// TOOO check ddl
class DdlListIndexer(parentEntryUid: Long, runUid: Int, sqiUid: Int, contentEntryUid: Long, endpoint: Endpoint, di: DI) : Indexer(parentEntryUid, runUid, sqiUid, contentEntryUid, endpoint, di) {

    override fun indexUrl(sourceUrl: String) {

        val document = Jsoup.connect(sourceUrl)
                .header("X-Requested-With", "XMLHttpRequest").get()

        val langCode = db.languageDao.findByUid(parentContentEntry?.primaryLanguageUid!!)?.iso_639_1_standard

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

                val title = resource.selectFirst("div.resource-title")?.text()?: ""

                val entry = ContentScraperUtil.insertTempContentEntry(repo.contentEntryDao, url, parentContentEntry!!.primaryLanguageUid, title)

                ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(repo.contentEntryParentChildJoinDao, parentContentEntry!!, entry, counter++)

                createQueueItem(url,  entry, ScraperTypes.DDL_ARTICLE_SCRAPER, ScrapeQueueItem.ITEM_TYPE_SCRAPE, parentContentEntryUid)
            }

        }

        setIndexerDone(true, 0)

    }

    override fun close() {


    }
}