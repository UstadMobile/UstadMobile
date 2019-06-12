package com.ustadmobile.lib.contentscrapers.voa

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
import com.ustadmobile.core.db.dao.LanguageDao
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.core.db.dao.ScrapeRunDao
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.LanguageList
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import com.ustadmobile.lib.db.entities.ScrapeRun
import com.ustadmobile.port.sharedse.util.WorkQueue

import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.Arrays
import java.util.concurrent.CountDownLatch

import com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.PUBLIC_DOMAIN
import java.util.function.Predicate

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

        queueDao!!.updateSetStatusById(scrapeQueueItemUid, if (successful) ScrapeQueueItemDao.STATUS_DONE else ScrapeQueueItemDao.STATUS_FAILED)
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
                            title, lessonListUrl.toString(), VOA, PUBLIC_DOMAIN, englishLang!!.langUid,
                            null, "", false, EMPTY_STRING, EMPTY_STRING,
                            EMPTY_STRING, EMPTY_STRING, contentEntryDao!!)

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
                    title, lesson.toString(), VOA, PUBLIC_DOMAIN, englishLang!!.langUid, null, "", true, EMPTY_STRING, EMPTY_STRING,
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao!!)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, categoryEntry, lessonEntry, lessonCount++)

            ContentScraperUtil.createQueueItem(queueDao!!, lesson, lessonEntry, categoryFolder,
                    "", runId, ScrapeQueueItem.ITEM_TYPE_SCRAPE)
            scrapeWorkQueue!!.checkQueue()

        }

        if (lessonListDoc.hasClass("btn--load-more")) {

            val loadMoreHref = lessonListDoc.selectFirst("p.btn--load-more a").attr("href")
            ContentScraperUtil.createQueueItem(queueDao!!, URL(indexerUrl, loadMoreHref), categoryEntry, categoryFolder,
                    ScraperConstants.VoaContentType.LESSONS.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)

        }

    }

    companion object {

        private val ROOT_URL = "https://learningenglish.voanews.com/"

        private val VOA = "VOA"
        private var url: URL? = null
        private var contentEntryDao: ContentEntryDao? = null
        private var contentParentChildJoinDao: ContentEntryParentChildJoinDao? = null

        private val CATEGORY = arrayOf("Test Your English", "The Day in Photos", "Most Popular ", "Read, Listen & Learn")
        private var englishLang: Language? = null
        private var queueDao: ScrapeQueueItemDao? = null
        private var scrapeWorkQueue: WorkQueue? = null

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                System.err.println("Usage: <file destination><file container><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }
            UMLogUtil.setLevel(if (args.size == 3) args[2] else "")
            UMLogUtil.logInfo(args[0])

            try {
                val runDao = UmAppDatabase.getInstance(Any()).scrapeRunDao

                var runId = runDao.findPendingRunIdByScraperType(ScrapeRunDao.SCRAPE_TYPE_VOA)
                if (runId == 0) {
                    runId = runDao.insert(ScrapeRun(ScrapeRunDao.SCRAPE_TYPE_VOA,
                            ScrapeQueueItemDao.STATUS_PENDING)).toInt()
                }

                scrapeFromRoot(File(args[0]), File(args[1]), runId)
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

            val db = UmAppDatabase.getInstance(Any())
            val repository = db// db.getRepository("https://localhost", "")
            contentEntryDao = repository.contentEntryDao
            contentParentChildJoinDao = repository.contentEntryParentChildJoinDao
            val languageDao = repository.languageDao
            queueDao = db.scrapeQueueItemDao

            LanguageList().addAllLanguages()

            englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English")

            val masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                    ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang!!.langUid, null,
                    EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao!!)

            val parentVoa = ContentScraperUtil.createOrUpdateContentEntry("https://learningenglish.voanews.com/", "Voice of America - Learning English",
                    "https://learningenglish.voanews.com/", VOA, PUBLIC_DOMAIN, englishLang!!.langUid, null,
                    "Learn American English with English language lessons from Voice of America. " +
                            "VOA Learning English helps you learn English with vocabulary, listening and " +
                            "comprehension lessons through daily news and interactive English learning activities.",
                    false, EMPTY_STRING, "https://learningenglish.voanews.com/Content/responsive/VOA/img/top_logo_news.png",
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao!!)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, masterRootParent, parentVoa, 7)

            val beginningUrl = "https://learningenglish.voanews.com/p/5609.html"
            val intermediateUrl = "https://learningenglish.voanews.com/p/5610.html"
            val advancedUrl = "https://learningenglish.voanews.com/p/5611.html"
            val historyUrl = "https://learningenglish.voanews.com/p/6353.html"

            val beginningLevel = ContentScraperUtil.createOrUpdateContentEntry("5609", "Beginning Level",
                    beginningUrl, VOA, PUBLIC_DOMAIN, englishLang!!.langUid, null, "", false, EMPTY_STRING, EMPTY_STRING,
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao!!)

            val intermediateLevel = ContentScraperUtil.createOrUpdateContentEntry("5610", "Intermediate Level",
                    intermediateUrl, VOA, PUBLIC_DOMAIN, englishLang!!.langUid, null, "", false, EMPTY_STRING, EMPTY_STRING,
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao!!)

            val advancedLevel = ContentScraperUtil.createOrUpdateContentEntry("5611", "Advanced Level",
                    advancedUrl, VOA, PUBLIC_DOMAIN, englishLang!!.langUid, null, "", false, EMPTY_STRING, EMPTY_STRING,
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao!!)

            val usHistory = ContentScraperUtil.createOrUpdateContentEntry("6353", "US History",
                    historyUrl, VOA, PUBLIC_DOMAIN, englishLang!!.langUid, null, "", false, EMPTY_STRING, EMPTY_STRING,
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao!!)

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

            val indexerSource = {
                val item = queueDao!!.getNextItemAndSetStatus(runId, ScrapeQueueItem.ITEM_TYPE_INDEX)
                if (item == null)
                    return

                val parent = contentEntryDao!!.findByEntryId(item!!.sqiContentEntryParentUid)
                val queueUrl: URL
                try {
                    queueUrl = URL(item!!.scrapeUrl!!)
                    return IndexVoaScraper(queueUrl, parent, File(item.destDir!!),
                            item.contentType, item.sqiUid, runId)
                } catch (ignored: IOException) {
                    //Must never happen
                    throw RuntimeException("SEVERE: invalid URL to index: should not be in queue:" + item!!.scrapeUrl!!)
                }
            }

            val scraperSource = {

                val item = queueDao!!.getNextItemAndSetStatus(runId,
                        ScrapeQueueItem.ITEM_TYPE_SCRAPE)
                if (item == null) {
                    return null
                }

                val parent = contentEntryDao!!.findByEntryId(item!!.sqiContentEntryParentUid)

                val scrapeContentUrl: URL
                try {
                    scrapeContentUrl = URL(item!!.scrapeUrl!!)
                    return VoaScraper(scrapeContentUrl,
                            File(item.destDir!!),
                            containerDir,
                            parent, item.sqiUid)
                } catch (ignored: IOException) {
                    throw RuntimeException("SEVERE: invalid URL to scrape: should not be in queue:" + item!!.scrapeUrl!!)
                }
            }
            //start the indexing work queue
            val indexerLatch = CountDownLatch(1)
            val indexWorkQueue = WorkQueue(indexerSource, 2)
            indexWorkQueue.addEmptyWorkQueueListener({ srcQueu -> indexerLatch.countDown() })
            indexWorkQueue.start()
            val scraperLatch = CountDownLatch(1)
            scrapeWorkQueue = WorkQueue(scraperSource, 1)
            scrapeWorkQueue!!.start()

            try {
                indexerLatch.await()
            } catch (ignored: InterruptedException) {
            }

            scrapeWorkQueue!!.addEmptyWorkQueueListener({ scrapeQueu -> scraperLatch.countDown() })
            try {
                scraperLatch.await()
            } catch (ignored: InterruptedException) {

            }

        }
    }


}
