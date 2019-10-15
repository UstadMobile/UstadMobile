package com.ustadmobile.lib.contentscrapers.gdl

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao.Companion.STATUS_RUNNING
import com.ustadmobile.core.db.dao.ScrapeRunDao
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING
import com.ustadmobile.lib.contentscrapers.ScraperConstants.GDL
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY_NC
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import com.ustadmobile.lib.db.entities.ScrapeRun
import com.ustadmobile.sharedse.util.LiveDataWorkQueue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.lang.exception.ExceptionUtils
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import kotlin.system.exitProcess

class GdlContentIndexer(val queueUrl: URL, val parentEntry: ContentEntry, val destLocation: File,
                        val contentType: String, val scrapeQueueItemUid: Int, val runId: Int): Runnable {

    override fun run() {
        System.gc()
        queueDao.setTimeStarted(scrapeQueueItemUid, System.currentTimeMillis())
        var successful = false
        if (ScraperConstants.GDLContentType.ROOT.type == contentType) {
            try {
                browseLanguages(parentEntry, queueUrl, destLocation)
                successful = true
            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Error creating topics for url $queueUrl")
            }

        } else if (ScraperConstants.GDLContentType.LANGPAGE.type == contentType) {
            try {
                browsePages(parentEntry, queueUrl, destLocation)
                successful = true
            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Error creating subjects for url $queueUrl")
            }

        }

        queueDao.updateSetStatusById(scrapeQueueItemUid, if (successful) ScrapeQueueItemDao.STATUS_DONE else ScrapeQueueItemDao.STATUS_FAILED)
        queueDao.setTimeFinished(scrapeQueueItemUid, System.currentTimeMillis())
    }

    private fun browsePages(parentEntry: ContentEntry, queueUrl: URL, destLocation: File) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.


    }

    private fun browseLanguages(parentEntry: ContentEntry, queueUrl: URL, destLocation: File) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    companion object {

        val ROOT_URL = "https://opds.staging.digitallibrary.io/v1/en/root.xml"

        private lateinit var contentEntryDao: ContentEntryDao
        private lateinit var contentParentChildJoinDao: ContentEntryParentChildJoinDao
        private lateinit var englishLang: Language
        private lateinit var queueDao: ScrapeQueueItemDao
        private lateinit var gdlEntry: ContentEntry
        private lateinit var scrapeWorkQueue: LiveDataWorkQueue<ScrapeQueueItem>

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                System.err.println("Usage: <file destination><container destination><optional log{trace, debug, info, warn, error, fatal}>")
                exitProcess(1)
            }
            UMLogUtil.setLevel(if (args.size == 4) args[3] else "")
            UMLogUtil.logInfo(args[0])
            UMLogUtil.logInfo(args[1])
            try {
                val runDao = UmAppDatabase.getInstance(Any()).scrapeRunDao

                var runId = runDao.findPendingRunIdByScraperType(ScrapeRunDao.SCRAPE_TYPE_GDL)
                if (runId == 0) {
                    runId = runDao.insert(ScrapeRun(ScrapeRunDao.SCRAPE_TYPE_GDL,
                            ScrapeQueueItemDao.STATUS_PENDING)).toInt()
                }

                scrapeFromRoot(File(args[0]), File(args[1]), runId)
            } catch (e: Exception) {
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Main method exception catch khan")
            }

        }

        fun scrapeFromRoot(destination: File, container: File, runId: Int) {
            startScrape(ROOT_URL, destination, container, runId)
        }

        fun startScrape(startUrl: String, destinationDir: File, containerDir: File, runId: Int) {

            val url: URL
            try {
                url = URL(startUrl)
            } catch (e: MalformedURLException) {
                UMLogUtil.logFatal("Index Malformed url$startUrl")
                throw IllegalArgumentException("Malformed url$startUrl", e)
            }

            val db = UmAppDatabase.getInstance(Any())
            val repository = db // db.getRepository("https://localhost", "")

            destinationDir.mkdirs()
            containerDir.mkdirs()
            contentEntryDao = repository.contentEntryDao
            contentParentChildJoinDao = repository.contentEntryParentChildJoinDao
            val languageDao = repository.languageDao
            queueDao = db.scrapeQueueItemDao

            englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English")

            val masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, ScraperConstants.USTAD_MOBILE,
                    ROOT, ScraperConstants.USTAD_MOBILE, ContentEntry.LICENSE_TYPE_CC_BY, englishLang.langUid, null,
                    EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao)

            gdlEntry = ContentScraperUtil.createOrUpdateContentEntry("https://digitallibrary.io/", GDL,
                    "https://opds.staging.digitallibrary.io/v1/en/root.xml/", GDL, LICENSE_TYPE_CC_BY_NC, englishLang.langUid, null,
                    "bringing books to every child in the world by 2030", false, EMPTY_STRING,
                    "https://cdn.kastatic.org/images/khan-logo-dark-background.new.png",
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, gdlEntry, 8)

            val englishFolder = File(destinationDir, "en")
            englishFolder.mkdirs()

            ContentScraperUtil.createQueueItem(queueDao, url, gdlEntry, englishFolder, ScraperConstants.GDLContentType.ROOT.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)

            val indexProcessor = 4
            val indexWorkQueue = LiveDataWorkQueue(queueDao.findNextQueueItems(runId, ScrapeQueueItem.ITEM_TYPE_INDEX),
                    { item1, item2 -> item1.sqiUid == item2.sqiUid },
                    indexProcessor) {
                queueDao.updateSetStatusById(it.sqiUid, STATUS_RUNNING)
                val parent = contentEntryDao.findByUidAsync(it.sqiContentEntryParentUid)
                val queueUrl: URL

                try {
                    queueUrl = URL(it.scrapeUrl!!)
                    GdlContentIndexer(queueUrl, parent!!, File(it.destDir!!),
                            it.contentType!!, it.sqiUid, runId).run()
                } catch (ignored: Exception) {
                    //Must never happen
                    throw RuntimeException("SEVERE: invalid URL to index: should not be in queue:" + it.scrapeUrl!!)
                }
            }

            GlobalScope.launch {
                indexWorkQueue.start()
            }
        }


    }


}