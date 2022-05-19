package com.ustadmobile.lib.staging.contentscrapers.voa

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao.Companion.STATUS_RUNNING
import com.ustadmobile.core.db.dao.ScrapeRunDao
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants

import com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.voa.VoaScraper
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_PUBLIC_DOMAIN
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import com.ustadmobile.core.util.LiveDataWorkQueue
import com.ustadmobile.lib.staging.contentscrapers.replaceMeWithDi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import kotlin.system.exitProcess

/**
 * The VOA website can be scraped at https://learningenglish.voanews.com/
 * They are 4 categories in the website we are interested in:
 * beginning level, intermediate level, advanced level and us history
 *
 *
 * Each of these categories have sub categories to scrape
 * The subcategory can be found using css selector h2.section-head a to find the href link
 * Each subcategory has a list of lessons that can have multiple video and audio.
 * These lessons can be found using css selector: div.container div.media-block-wrap div.media-block a.img-wrap
 */

class IndexVoaScraper internal constructor(private val indexerUrl: URL, private val parentEntry: ContentEntry, private val indexLocation: File,
                                           private val contentType: String, private val scrapeQueueItemUid: Int, private val runId: Int) : Runnable {

    override fun run() {
        System.gc()
        queueDao!!.setTimeStarted(scrapeQueueItemUid, System.currentTimeMillis())
        var successful = false
        if (ScraperConstants.VoaContentType.LEVELS.type == contentType) {
            try {
                findContentInCategories(parentEntry, indexerUrl, indexLocation)
                successful = true
            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Error creating topics for url $indexerUrl")
            }

        } else if (ScraperConstants.VoaContentType.LESSONS.type == contentType) {

            try {
                findLessons(parentEntry, indexLocation, indexerUrl)
                successful = true
            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Error creating subjects for url $indexerUrl")
            }

        }

        queueDao!!.updateSetStatusById(scrapeQueueItemUid, if (successful) ScrapeQueueItemDao.STATUS_DONE else ScrapeQueueItemDao.STATUS_FAILED, 0)
        queueDao!!.setTimeFinished(scrapeQueueItemUid, System.currentTimeMillis())
    }


    @Throws(IOException::class)
    private fun findContentInCategories(parentEntry: ContentEntry, urlString: URL, destinationDirectory: File) {

        val categoryDocument = Jsoup.connect(urlString.toString()).get()

        val categoryList = categoryDocument.select("h2.section-head a")

        var categoryCount = 0
        for (category in categoryList) {

            val title = category.text()

            if (Arrays.stream(CATEGORY).parallel().noneMatch { title.contains(it) }) {

                val hrefLink = category.attr("href")
                try {

                    val categoryFolder = File(destinationDirectory, title)
                    categoryFolder.mkdirs()

                    val lessonListUrl = URL(urlString, hrefLink)

                    val categoryEntry = ContentScraperUtil.createOrUpdateContentEntry(FilenameUtils.getBaseName(hrefLink),
                            title, lessonListUrl.toString(), VOA, LICENSE_TYPE_PUBLIC_DOMAIN, englishLang!!.langUid,
                            null, "", false, "", "",
                            "", "", 0, contentEntryDao!!)

                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parentEntry, categoryEntry, categoryCount++)

                    ContentScraperUtil.createQueueItem(queueDao!!, lessonListUrl, categoryEntry, categoryFolder,
                            ScraperConstants.VoaContentType.LESSONS.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)

                } catch (e: IOException) {
                    UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                    UMLogUtil.logError("Error with voa category = $hrefLink with title $title")
                }

            }


        }


    }

    @Throws(IOException::class)
    private fun findLessons(categoryEntry: ContentEntry, categoryFolder: File, lessonUrl: URL) {

        val lessonListDoc = Jsoup.connect(lessonUrl.toString()).get()

        val elementList = lessonListDoc.select("div.container div.media-block-wrap div.media-block a.img-wrap")

        var lessonCount = 0
        for (lessonElement in elementList) {

            val lessonHref = lessonElement.attr("href")
            val lesson = URL(url, lessonHref)

            val title = lessonElement.attr("title")

            val lessonEntry = ContentScraperUtil.createOrUpdateContentEntry(FilenameUtils.getBaseName(lessonHref),
                    title, lesson.toString(), VOA, LICENSE_TYPE_PUBLIC_DOMAIN, englishLang!!.langUid, null, "", true, "", "",
                    "", "", 0, contentEntryDao!!)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, categoryEntry, lessonEntry, lessonCount++)

            ContentScraperUtil.createQueueItem(queueDao!!, lesson, lessonEntry, categoryFolder,
                    "", runId, ScrapeQueueItem.ITEM_TYPE_SCRAPE)

        }

        if (lessonListDoc.hasClass("btn--load-more")) {

            val loadMoreHref = lessonListDoc.selectFirst("p.btn--load-more a")?.attr("href")
            ContentScraperUtil.createQueueItem(queueDao!!, URL(indexerUrl, loadMoreHref), categoryEntry, categoryFolder,
                    ScraperConstants.VoaContentType.LESSONS.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)

        }

    }


    companion object {

        private val ROOT_URL = "https://learningenglish.voanews.com/"

        private val VOA = "VOA"
        private var url: URL? = null
        private lateinit var contentEntryDao: ContentEntryDao
        private lateinit var contentParentChildJoinDao: ContentEntryParentChildJoinDao

        private val CATEGORY = arrayOf("Test Your English", "The Day in Photos", "Most Popular ", "Read, Listen & Learn")
        private lateinit var englishLang: Language
        private lateinit var queueDao: ScrapeQueueItemDao
        private lateinit var scrapeWorkQueue: LiveDataWorkQueue<ScrapeQueueItem>

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                System.err.println("Usage: <file destination><file container><optional log{trace, debug, info, warn, error, fatal}>")
                exitProcess(1)
            }
            UMLogUtil.setLevel(if (args.size == 3) args[2] else "")
            UMLogUtil.logInfo(args[0])

            try {
                //Replace this with DI
                lateinit var runDao: ScrapeRunDao
                //val runDao = UmAppDatabase.getInstance(Any(), replaceMeWithDi()).scrapeRunDao

                scrapeFromRoot(File(args[0]), File(args[1]), 0)
            } catch (e: Exception) {
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Main method exception catch khan")
            }

        }

        @Throws(IOException::class)
        private fun scrapeFromRoot(dest: File, containerDir: File, runId: Int) {
            startScrape(ROOT_URL, dest, containerDir, runId)
        }

        @Throws(IOException::class)
        private fun startScrape(scrapeUrl: String, destinationDir: File, containerDir: File, runId: Int) {
            try {
                url = URL(scrapeUrl)
            } catch (e: MalformedURLException) {
                UMLogUtil.logError("Index Malformed url$scrapeUrl")
                throw IllegalArgumentException("Malformed url$scrapeUrl", e)
            }

            destinationDir.mkdirs()
            containerDir.mkdirs()

            //Replace this with DI
            lateinit var db: UmAppDatabase
            //val db = UmAppDatabase.getInstance(Any(), replaceMeWithDi())
            val repository = db// db.getRepository("https://localhost", "")
            contentEntryDao = repository.contentEntryDao
            contentParentChildJoinDao = repository.contentEntryParentChildJoinDao
            val languageDao = repository.languageDao
            queueDao = db.scrapeQueueItemDao

            englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English")

            val masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                    ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang!!.langUid, null,
                    "", false, "", "",
                    "", "", 0, contentEntryDao!!)

            val parentVoa = ContentScraperUtil.createOrUpdateContentEntry("https://learningenglish.voanews.com/", "Voice of America - Learning English",
                    "https://learningenglish.voanews.com/", VOA, LICENSE_TYPE_PUBLIC_DOMAIN, englishLang!!.langUid, null,
                    "Learn American English with English language lessons from Voice of America. " +
                            "VOA Learning English helps you learn English with vocabulary, listening and " +
                            "comprehension lessons through daily news and interactive English learning activities.",
                    false, "", "https://learningenglish.voanews.com/Content/responsive/VOA/img/top_logo_news.png",
                    "", "", 0, contentEntryDao!!)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, masterRootParent, parentVoa, 18)

            val beginningUrl = "https://learningenglish.voanews.com/p/5609.html"
            val intermediateUrl = "https://learningenglish.voanews.com/p/5610.html"
            val advancedUrl = "https://learningenglish.voanews.com/p/5611.html"
            val historyUrl = "https://learningenglish.voanews.com/p/6353.html"

            val beginningLevel = ContentScraperUtil.createOrUpdateContentEntry("5609", "Beginning Level",
                    beginningUrl, VOA, LICENSE_TYPE_PUBLIC_DOMAIN, englishLang!!.langUid, null, "", false, "", "",
                    "", "", 0, contentEntryDao!!)

            val intermediateLevel = ContentScraperUtil.createOrUpdateContentEntry("5610", "Intermediate Level",
                    intermediateUrl, VOA, LICENSE_TYPE_PUBLIC_DOMAIN, englishLang!!.langUid, null, "", false, "", "",
                    "", "", 0, contentEntryDao!!)

            val advancedLevel = ContentScraperUtil.createOrUpdateContentEntry("5611", "Advanced Level",
                    advancedUrl, VOA, LICENSE_TYPE_PUBLIC_DOMAIN, englishLang!!.langUid, null, "", false, "", "",
                    "", "", 0, contentEntryDao!!)

            val usHistory = ContentScraperUtil.createOrUpdateContentEntry("6353", "US History",
                    historyUrl, VOA, LICENSE_TYPE_PUBLIC_DOMAIN, englishLang!!.langUid, null, "", false, "", "",
                    "", "", 0, contentEntryDao!!)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parentVoa, beginningLevel, 0)
            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parentVoa, intermediateLevel, 1)
            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parentVoa, advancedLevel, 2)
            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parentVoa, usHistory, 3)

            ContentScraperUtil.createQueueItem(queueDao!!, URL(beginningUrl), beginningLevel,
                    destinationDir, ScraperConstants.VoaContentType.LEVELS.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)
            ContentScraperUtil.createQueueItem(queueDao!!, URL(intermediateUrl), intermediateLevel,
                    destinationDir, ScraperConstants.VoaContentType.LEVELS.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)
            ContentScraperUtil.createQueueItem(queueDao!!, URL(advancedUrl), advancedLevel,
                    destinationDir, ScraperConstants.VoaContentType.LEVELS.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)
            ContentScraperUtil.createQueueItem(queueDao!!, URL(historyUrl), usHistory,
                    destinationDir, ScraperConstants.VoaContentType.LEVELS.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)


            //start the indexing work queue
            val indexProcessors = 2
            val indexWorkQueue = LiveDataWorkQueue<ScrapeQueueItem>(queueDao!!.findNextQueueItems(ScrapeQueueItem.ITEM_TYPE_INDEX),
                    { item1, item2 -> item1.sqiUid == item2.sqiUid },
                    indexProcessors) {

                queueDao.updateSetStatusById(it.sqiUid, STATUS_RUNNING, 0)
                val parent = contentEntryDao.findByUidAsync(it.sqiContentEntryParentUid)

                val scrapeContentUrl: URL
                try {
                    scrapeContentUrl = URL(it.scrapeUrl!!)
                    VoaScraper(scrapeContentUrl,
                            File(it.destDir!!),
                            containerDir,
                            parent!!, it.sqiUid).run()
                } catch (ignored: IOException) {
                    throw RuntimeException("SEVERE: invalid URL to scrape: should not be in queue:" + it!!.scrapeUrl!!)
                }

            }
            GlobalScope.launch {
                indexWorkQueue.start()
            }


            val scrapePrecessor = 6
            scrapeWorkQueue = LiveDataWorkQueue(queueDao.findNextQueueItems(ScrapeQueueItem.ITEM_TYPE_SCRAPE),
                    { item1, item2 -> item1.sqiUid == item2.sqiUid }, scrapePrecessor) {

                queueDao.updateSetStatusById(it.sqiUid, STATUS_RUNNING, 0)
                val parent = contentEntryDao.findByUidAsync(it!!.sqiContentEntryParentUid)

                val scrapeContentUrl: URL
                try {
                    scrapeContentUrl = URL(it.scrapeUrl!!)
                    VoaScraper(scrapeContentUrl,
                            File(it.destDir!!),
                            containerDir,
                            parent!!, it.sqiUid).run()
                } catch (ignored: IOException) {
                    throw RuntimeException("SEVERE: invalid URL to scrape: should not be in queue:" + it.scrapeUrl!!)
                }
            }
            GlobalScope.launch {
                scrapeWorkQueue.start()
            }


        }
    }


}
