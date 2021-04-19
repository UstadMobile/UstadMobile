package com.ustadmobile.lib.contentscrapers.ddl

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.contentscrapers.abztract.Indexer
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import org.jsoup.Jsoup
import org.kodein.di.DI


class DdlPageIndexer(parentContentEntryUid: Long, runId: Int, sqiUid: Int, contentEntryUid: Long, endpoint: Endpoint, di: DI) : Indexer(parentContentEntryUid, runId, sqiUid, contentEntryUid, endpoint, di ) {

    override fun indexUrl(sourceUrl: String) {

        val langEntry = db.languageDao.findByUid(parentContentEntry?.primaryLanguageUid!!)
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
            createQueueItem(url, parentContentEntry!!, ScraperTypes.DDL_LIST_INDEXER, ScrapeQueueItem.ITEM_TYPE_INDEX, parentContentEntryUid)
        }

        setIndexerDone(true, 0)
    }

    override fun close() {

    }


}