package com.ustadmobile.lib.contentscrapers.ddl

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import org.jsoup.Jsoup
import java.io.File

class DdlPageIndexer(contentEntryUid: Long, runId: Int, db: UmAppDatabase) : Indexer(contentEntryUid, runId, db) {

    override fun indexUrl(sourceUrl: String) {

        val langEntry = db.languageDao.findByUid(contentEntry?.primaryLanguageUid!!)
        val twoCodeLang = langEntry?.iso_639_1_standard

        val document = Jsoup.connect(sourceUrl)
                .header("X-Requested-With", "XMLHttpRequest").get()

        val pageList = document.select("a.page-link")

        var maxNumber = 1
        for (page in pageList) {

            val num = page.text()
            try {
                val number = Integer.parseInt(num)
                if (number > maxNumber) {
                    maxNumber = number
                }
            } catch (ignored: NumberFormatException) {
            }

        }

        val subjectId = sourceUrl.substringAfterLast("=")

        for (i in 1..maxNumber) {

            val url = "https://www.ddl.af/$twoCodeLang/resources/list?subject_area=${subjectId}&page=$i"
            createQueueItem(url, contentEntry!!, ScraperTypes.DDL_LIST_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX)
        }
    }

    override fun close() {

    }


}