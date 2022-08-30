package com.ustadmobile.lib.staging.contentscrapers.gdl

import com.ustadmobile.core.contentformats.opds.OpdsFeed
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.core.db.dao.ScrapeQueueItemDaoCommon.STATUS_RUNNING
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants

import com.ustadmobile.lib.contentscrapers.ScraperConstants.GDL
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_EPUB
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_PDF
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY_NC
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY_ND
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY_SA
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY_SA_NC
import com.ustadmobile.core.util.LiveDataWorkQueue
import com.ustadmobile.lib.staging.contentscrapers.replaceMeWithDi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.lang.exception.ExceptionUtils
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import kotlin.system.exitProcess


class GdlContentIndexer(val queueUrl: URL, val parentEntry: ContentEntry, val destLocation: File,
                        val contentType: String, val scrapeQueueItemUid: Int, val runId: Int) : Runnable {

    override fun run() {
        System.gc()
        queueDao.setTimeStarted(scrapeQueueItemUid, System.currentTimeMillis())
        var successful = false
        var feed: OpdsFeed? = null
        try {
            feed = getFeed(queueUrl)
        } catch (e: Exception) {
            queueDao.updateSetStatusById(scrapeQueueItemUid, if (successful) ScrapeQueueItemDaoCommon.STATUS_DONE else ScrapeQueueItemDaoCommon.STATUS_FAILED, 0)
            queueDao.setTimeFinished(scrapeQueueItemUid, System.currentTimeMillis())
        }
        when (contentType) {
            ScraperConstants.GDLContentType.ROOT.type -> try {
                browseLanguages(feed!!, parentEntry, queueUrl, destLocation)
                browsePages(feed, parentEntry, queueUrl, destLocation)
                browseContent(feed, parentEntry, queueUrl, destLocation)
                successful = true
            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Error creating topics for url $queueUrl")
            }

            ScraperConstants.GDLContentType.LANGPAGE.type -> try {
                browsePages(feed!!, parentEntry, queueUrl, destLocation)
                browseContent(feed, parentEntry, queueUrl, destLocation)
                successful = true
            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Error creating subjects for url $queueUrl")
            }

            ScraperConstants.GDLContentType.CONTENT.type -> try {
                browseContent(feed!!, parentEntry, queueUrl, destLocation)
                successful = true
            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Error creating subjects for url $queueUrl")
            }
        }

        queueDao.updateSetStatusById(scrapeQueueItemUid, if (successful) ScrapeQueueItemDaoCommon.STATUS_DONE else ScrapeQueueItemDaoCommon.STATUS_FAILED, 0)
        queueDao.setTimeFinished(scrapeQueueItemUid, System.currentTimeMillis())
    }

    private fun browsePages(feed: OpdsFeed, parentEntry: ContentEntry, queueUrl: URL, destLocation: File) {

        var lastLink = feed.linkList.find { it.rel == "last" }
        var hrefLink = lastLink!!.href
        var sizeOfPages = hrefLink.substringAfter("page=").toInt()

        if (sizeOfPages == 1) {
            return
        }

        for (x in 2..sizeOfPages) {

            var pageLink = hrefLink.replaceAfter("page=", x.toString())
            ContentScraperUtil.createQueueItem(queueDao, URL(pageLink), gdlEntry, destLocation, ScraperConstants.GDLContentType.CONTENT.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)

        }

    }

    private fun browseLanguages(feed: OpdsFeed, parentEntry: ContentEntry, queueUrl: URL, destLocation: File) {

        feed.linkList
                .filter { it.facetGroup == "Languages" && it.activeFacet == false }
                .forEach { lang ->

                    var parentFolder = destLocation.parentFile
                    var langFolder = File(parentFolder, lang.title!!)
                    langFolder.mkdirs()

                    ContentScraperUtil.createQueueItem(queueDao, URL(lang.href), gdlEntry, langFolder, ScraperConstants.GDLContentType.LANGPAGE.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)

                }
    }

    private fun getFeed(queueUrl: URL): OpdsFeed {
        var opfIn: InputStream? = null
        val xppFactory = XmlPullParserFactory.newInstance()
        try {
            opfIn = queueUrl.openStream()
            val parser = xppFactory.newPullParser()
            parser.setInput(opfIn, "UTF-8")
            var feed = OpdsFeed()
            feed.loadFromParser(parser)
            return feed
        } catch (e: Exception) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Error creating subjects for url $queueUrl")
            throw java.lang.IllegalArgumentException("Error creating subjects for url $queueUrl")
        } finally {
            opfIn?.close()
        }
    }

    private fun browseContent(feed: OpdsFeed, parentEntry: ContentEntry, url: URL, destLocation: File) {

        feed.entryList.forEach {

            var sourceUrl = it.linkList.find { type -> type.type == MIMETYPE_EPUB }
                    ?: it.linkList.find { otherType -> otherType.type == MIMETYPE_PDF }
                    ?: return@forEach

            var thumbnail = it.linkList.find { rel -> rel.rel == "http://opds-spec.org/image/thumbnail" }

            var licenseType = ccMap[it.license] ?: -1

            if (licenseType == -1) {
                UMLogUtil.logError("${it.license} for book title ${it.title}")
            }

            var language = url.toString().substringAfter(".io/v1/").substringBefore("/root.xml")

            var primary = language.split("-")

            var lang: Language
            if (primary[0].length == 2) {
                lang = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(langDao, primary[0])
            } else {
                lang = ContentScraperUtil.insertOrUpdateLanguageByThreeCode(langDao, primary[0])
            }

            var variant: LanguageVariant? = null
            if (primary.size > 1) {
                variant = ContentScraperUtil.insertOrUpdateLanguageVariant(langVariantDao, primary[1], lang)
            }

            var contentEntry = ContentScraperUtil.createOrUpdateContentEntry(it.id, it.title, sourceUrl.href, it.publisher!!, licenseType, lang.langUid, variant?.langVariantUid,
                    it.summary, true, it.author, thumbnail?.href, "", "", 0, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentEntry, contentEntry, index++)

            var contentFolder = File(destLocation, it.id.substringAfter("uuid:"))
            contentFolder.mkdirs()

            var scrapeType = if (sourceUrl.type == MIMETYPE_EPUB) MIMETYPE_EPUB else MIMETYPE_PDF

            val schema = ContentScraperUtil.insertOrUpdateSchema(categorySchemeDao,
                    "African Storybooks Reading Level", "africanstorybooks/reading/")

            val category = ContentScraperUtil.insertOrUpdateCategoryContent(categoryDao, schema, it.targetName!!)
            ContentScraperUtil.insertOrUpdateChildWithMultipleCategoriesJoin(contentCategoryJoinDao, category, contentEntry)

            ContentScraperUtil.createQueueItem(queueDao, URL(sourceUrl.href), contentEntry, contentFolder,
                    scrapeType, runId, ScrapeQueueItem.ITEM_TYPE_SCRAPE)
        }

    }


    companion object {

        val ROOT_URL = "https://opds.staging.digitallibrary.io/v1/en/root.xml"

        private lateinit var contentEntryDao: ContentEntryDao
        private lateinit var contentParentChildJoinDao: ContentEntryParentChildJoinDao
        private lateinit var englishLang: Language
        private lateinit var queueDao: ScrapeQueueItemDao
        private lateinit var gdlEntry: ContentEntry
        private lateinit var langDao: LanguageDao
        private lateinit var langVariantDao: LanguageVariantDao
        private lateinit var categorySchemeDao: ContentCategorySchemaDao
        private lateinit var categoryDao: ContentCategoryDao
        private lateinit var contentCategoryJoinDao: ContentEntryContentCategoryJoinDao

        private var index = 0
        private lateinit var scrapeWorkQueue: LiveDataWorkQueue<ScrapeQueueItem>

        private val ccMap = mapOf(
                "Creative Commons Attribution 4.0 International" to LICENSE_TYPE_CC_BY,
                "Creative Commons Attribution Non Commercial 4.0 International" to LICENSE_TYPE_CC_BY_NC,
                "Creative Commons Attribution Non Commercial 3.0 Unported" to LICENSE_TYPE_CC_BY_NC,
                "Creative Commons Attribution Non Commercial Share Alike 4.0 International" to LICENSE_TYPE_CC_BY_SA_NC,
                "Creative Commons Attribution No Derivatives 4.0 International" to LICENSE_TYPE_CC_BY_ND,
                "Creative Commons Attribution Share Alike 4.0 International" to LICENSE_TYPE_CC_BY_SA,
                "Creative Commons Attribution 3.0 Unported" to LICENSE_TYPE_CC_BY)

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                System.err.println("Usage: <file destination><container destination><optional log{trace, debug, info, warn, error, fatal}>")
                exitProcess(1)
            }
            UMLogUtil.setLevel(if (args.size == 3) args[2] else "")
            UMLogUtil.logInfo(args[0])
            UMLogUtil.logInfo(args[1])
            ContentScraperUtil.checkIfPathsToDriversExist()
            try {
                //This needs replaced with DI
                //lateinit var runDao: ScrapeRunDao
                //val runDao = UmAppDatabase.getInstance(Any(), replaceMeWithDi()).scrapeRunDao


                scrapeFromRoot(File(args[0]), File(args[1]), 0)
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

            //This needs replaced with DI
            lateinit var db: UmAppDatabase
            //val db = UmAppDatabase.getInstance(Any(), replaceMeWithDi())
            val repository = db // db.getRepository("https://localhost", "")

            destinationDir.mkdirs()
            containerDir.mkdirs()
            contentEntryDao = repository.contentEntryDao
            contentParentChildJoinDao = repository.contentEntryParentChildJoinDao
            langDao = repository.languageDao
            langVariantDao = repository.languageVariantDao
            categorySchemeDao = repository.contentCategorySchemaDao
            contentCategoryJoinDao = repository.contentEntryContentCategoryJoinDao
            categoryDao = repository.contentCategoryDao
            val languageDao = repository.languageDao
            queueDao = db.scrapeQueueItemDao

            englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English")

            ContentScraperUtil.insertOrUpdateLanguageManual(langDao, "Bukusu", "bxk")
            ContentScraperUtil.insertOrUpdateLanguageManual(langDao, "Lu", "khb")
            ContentScraperUtil.insertOrUpdateLanguageManual(langDao, "Kalanguya", "kak")
            ContentScraperUtil.insertOrUpdateLanguageManual(langDao, "Hadiyya", "hdy")
            ContentScraperUtil.insertOrUpdateLanguageManual(langDao, "Gusii", "guz")
            ContentScraperUtil.insertOrUpdateLanguageManual(langDao, "Wanga", "lwg")

            val masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, ScraperConstants.USTAD_MOBILE,
                    ROOT, ScraperConstants.USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang.langUid, null,
                    "", false, "", "",
                    "", "", 0, contentEntryDao)

            gdlEntry = ContentScraperUtil.createOrUpdateContentEntry("https://digitallibrary.io/", GDL,
                    "https://opds.staging.digitallibrary.io/v1/en/root.xml/", GDL, LICENSE_TYPE_CC_BY_NC, englishLang.langUid, null,
                    "bringing books to every child in the world by 2030", false, "",
                    "https://www.ustadmobile.com/files/gdl-logo.webp",
                    "", "", 0, contentEntryDao)

            val englishFolder = File(destinationDir, "English")
            englishFolder.mkdirs()

            ContentScraperUtil.createQueueItem(queueDao, url, gdlEntry, englishFolder, ScraperConstants.GDLContentType.ROOT.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)

            val indexProcessor = 4
            val indexWorkQueue = LiveDataWorkQueue(queueDao.findNextQueueItems(ScrapeQueueItem.ITEM_TYPE_INDEX),
                    { item1, item2 -> item1.sqiUid == item2.sqiUid },
                    indexProcessor) {
                queueDao.updateSetStatusById(it.sqiUid, STATUS_RUNNING, 0)
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

            val scrapePrecessor = 6
            scrapeWorkQueue = LiveDataWorkQueue(queueDao.findNextQueueItems(ScrapeQueueItem.ITEM_TYPE_SCRAPE),
                    { item1, item2 -> item1.sqiUid == item2.sqiUid }, scrapePrecessor) {

                queueDao.updateSetStatusById(it.sqiUid, STATUS_RUNNING, 0)
                val parent = contentEntryDao.findByUidAsync(it.sqiContentEntryParentUid)

                val scrapeUrl: URL
                try {
                    scrapeUrl = URL(it.scrapeUrl!!)
                    GdlContentScraper(scrapeUrl, File(it.destDir!!),
                            containerDir, parent!!,
                            it.contentType!!, it.sqiUid).run()
                } catch (ignored: Exception) {
                    throw RuntimeException("SEVERE: invalid URL to scrape: should not be in queue:" + it.scrapeUrl!!)
                }
            }
            GlobalScope.launch {
                scrapeWorkQueue.start()
            }

            UMLogUtil.logInfo("Finished Indexer")

        }


    }


}