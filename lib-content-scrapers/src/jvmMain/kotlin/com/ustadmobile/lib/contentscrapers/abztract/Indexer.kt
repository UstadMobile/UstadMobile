package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.LanguageList
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import com.ustadmobile.lib.db.entities.ScrapeQueueItemWithScrapeRun
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

@ExperimentalStdlibApi
abstract class Indexer(val parentContentEntryUid: Long, val runUid: Int, val sqiUid: Int, val contentEntryUid: Long, endpoint: Endpoint, di: DI) {

    val db: UmAppDatabase by di.on(endpoint).instance(tag = UmAppDatabase.TAG_DB)

    val repo: UmAppDatabase by di.on(endpoint).instance(tag = UmAppDatabase.TAG_REPO)

    var parentContentEntry: ContentEntry? = null
    var contentEntry: ContentEntry? = null
    var scrapeQueueItem: ScrapeQueueItemWithScrapeRun? = null
    val englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(repo.languageDao, "English")

    init {
        runBlocking {
            parentContentEntry = db.contentEntryDao.findByUidAsync(parentContentEntryUid)
            contentEntry = db.contentEntryDao.findByUidAsync(contentEntryUid)
            scrapeQueueItem = db.scrapeQueueItemDao.findByUid(sqiUid)
        }
        LanguageList().addAllLanguages()
    }

    fun createQueueItem(queueUrl: String, contentEntry: ContentEntry?, contentType: String, scraperType: Int, parentContentEntryUid: Long , priority: Int = 1) {
        var item = db.scrapeQueueItemDao.getExistingQueueItem(runUid, queueUrl)
        if (item == null) {
            item = ScrapeQueueItem()
            item.scrapeUrl = queueUrl
            item.sqiContentEntryParentUid = parentContentEntryUid
            item.sqiContentEntryUid = contentEntry?.contentEntryUid ?: 0
            item.status = ScrapeQueueItemDao.STATUS_PENDING
            item.contentType = contentType
            item.runId = runUid
            item.overrideEntry = false
            item.itemType = scraperType
            item.timeAdded = System.currentTimeMillis()
            item.priority = priority
            db.scrapeQueueItemDao.insert(item)
        }
    }

    fun setIndexerDone(successful: Boolean, errorCode: Int) {
        db.scrapeQueueItemDao.updateSetStatusById(sqiUid, if (successful) ScrapeQueueItemDao.STATUS_DONE else ScrapeQueueItemDao.STATUS_FAILED, errorCode)
    }

    fun hideContentEntry(contentEntryUid: Long) {
        repo.contentEntryDao.updateContentEntryInActive(contentEntryUid, true)
    }

    abstract fun indexUrl(sourceUrl: String)

    abstract fun close()

}