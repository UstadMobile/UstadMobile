package com.ustadmobile.lib.contentscrapers.khanacademy

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao.Companion.STATUS_RUNNING
import com.ustadmobile.core.db.dao.ScrapeRunDao
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.LanguageList
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING
import com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY_NC
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import com.ustadmobile.lib.db.entities.ScrapeRun
import com.ustadmobile.sharedse.util.LiveDataWorkQueue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.commons.pool2.impl.GenericObjectPool
import org.jsoup.Jsoup
import org.openqa.selenium.chrome.ChromeDriver
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import kotlin.system.exitProcess

/**
 * The Khan Academy website has a list of topics that they teach about at https://www.khanacademy.org/
 * Each topic have multiple sections eg grade 1 or algebra
 * Each section have different courses which have tutorial content in the from of videos, exercises, articles, quizzes, challenges.
 *
 *
 * Every page in khan academy have json content in a script that loads the information of the page.
 * Extract the json and put into the pojo object - TopicListResponse
 * TopicResponse has a list of domains which have all the topics in khan academy
 * Each domain has an href for the link to the next page - subjects
 *
 *
 * For the subject, extract the json from the script and load into SubjectListResponse
 * SubjectResponse has a list of modules which can be categorized with variable kind
 * TableOfContents - has another list of sub-subjects
 * SubjectProgress - has another list of sub-subjects which is found in list of modules with kind SubjectPageTopicCard
 * SubjectChallenge - quizzes for the subjects
 *
 *
 * Once we reach to the courses Page, extract the json from the script and load into SubjectListResponse
 * SubjectResponse has a list of tutorials which each have a list of content items
 * Every content item is a course categorized by Video, Exercise or Article.
 */
class KhanContentIndexer internal constructor(private val indexerUrl: URL, private val parentEntry: ContentEntry, private val indexLocation: File,
                                              private val contentType: String, private val scrapeQueueItemUid: Int, private val runId: Int) : Runnable {


    override fun run() {
        System.gc()
        queueDao.setTimeStarted(scrapeQueueItemUid, System.currentTimeMillis())
        var successful = false
        if (ScraperConstants.KhanContentType.TOPICS.type == contentType) {
            try {
                browseTopics(parentEntry, indexerUrl, indexLocation)
                successful = true
            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Error creating topics for url $indexerUrl")
            }

        } else if (ScraperConstants.KhanContentType.SUBJECT.type == contentType) {
            try {
                browseSubjects(parentEntry, indexerUrl, indexLocation)
                successful = true
            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Error creating subjects for url $indexerUrl")
            }

        }

        queueDao.updateSetStatusById(scrapeQueueItemUid, if (successful) ScrapeQueueItemDao.STATUS_DONE else ScrapeQueueItemDao.STATUS_FAILED)
        queueDao.setTimeFinished(scrapeQueueItemUid, System.currentTimeMillis())
    }


    @Throws(IOException::class)
    fun browseTopics(parent: ContentEntry, url: URL, fileLocation: File) {

        val jsonString = getJsonStringFromScript(url.toString())

        var response: TopicListResponse? = gson.fromJson(jsonString, TopicListResponse::class.java)
        if (response!!.componentProps ==
                null) {
            response = gson.fromJson(jsonString, PropsTopiclistResponse::class.java).props
        }

        val modulesList = response!!.componentProps!!.modules

        for (module in modulesList!!) {

            if (module.domains == null || module.domains!!.isEmpty()) {
                continue
            }

            val domainList = module.domains

            var topicCount = 0
            for (domain in domainList!!) {

                val topicUrl = URL(url, domain.href!!)

                val topicEntry = ContentScraperUtil.createOrUpdateContentEntry(domain.identifier!!,
                        domain.translatedTitle, topicUrl.toString(), KHAN,
                        LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, EMPTY_STRING, false,
                        EMPTY_STRING, domain.icon, EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao!!)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, parent, topicEntry,
                        topicCount++)

                ContentScraperUtil.createQueueItem(queueDao, topicUrl, topicEntry, fileLocation,
                        ScraperConstants.KhanContentType.SUBJECT.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)

            }


        }
    }

    @Throws(IOException::class)
    private fun browseSubjects(topicEntry: ContentEntry, topicUrl: URL, topicFolder: File) {

        val subjectJson = getJsonStringFromScript(topicUrl.toString())

        var response: SubjectListResponse? = gson.fromJson(subjectJson, SubjectListResponse::class.java)
        if (response!!.componentProps == null) {
            response = gson.fromJson(subjectJson, PropsSubjectResponse::class.java).props
        }

        // one page on the website doesn't follow standard code
        if (response == null) {
            browseHourOfCode(topicEntry, topicUrl, topicFolder)
            return
        }

        val tabList = response.componentProps!!.curation!!.tabs

        for (tab in tabList!!) {

            if (tab.modules == null || tab.modules!!.isEmpty()) {
                continue
            }

            val moduleList = tab.modules

            var subjectCount = 0
            for (module in moduleList!!) {

                if (SUBJECT_PROGRESS == module.kind) {

                    val moduleItems = module.modules

                    if (module.modules == null || module.modules!!.isEmpty()) {
                        continue
                    }

                    for (moduleItem in moduleItems!!) {

                        if (SUBJECT_PAGE_TOPIC_CARD == moduleItem.kind) {

                            val subjectUrl = URL(topicUrl, moduleItem.url!!)

                            val subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(moduleItem.slug!!, moduleItem.title,
                                    subjectUrl.toString(), KHAN, LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null,
                                    moduleItem.description, false, EMPTY_STRING, moduleItem.icon, EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao!!)

                            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, topicEntry, subjectEntry, subjectCount++)

                            ContentScraperUtil.createQueueItem(queueDao, subjectUrl, subjectEntry, topicFolder,
                                    ScraperConstants.KhanContentType.SUBJECT.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)

                        }


                    }


                } else if (TABLE_OF_CONTENTS_ROW == module.kind) {

                    val subjectUrl = URL(topicUrl, module.url!!)

                    val subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(module.slug!!, module.title, subjectUrl.toString(),
                            KHAN, LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null,
                            module.description, false, EMPTY_STRING, module.icon, EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao!!)

                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, topicEntry, subjectEntry, subjectCount++)

                    ContentScraperUtil.createQueueItem(queueDao!!, subjectUrl, subjectEntry, topicFolder,
                            ScraperConstants.KhanContentType.SUBJECT.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)

                } else if (SUBJECT_CHALLENGE == module.kind) {

                    // TODO

                } else if (module.tutorials != null && !module.tutorials!!.isEmpty()) {

                    val tutorialList = module.tutorials

                    var tutorialCount = 0
                    for (tutorial in tutorialList!!) {

                        if (tutorial == null) {
                            continue
                        }

                        val tutorialUrl = URL(topicUrl, tutorial.url!!)

                        val tutorialEntry = ContentScraperUtil.createOrUpdateContentEntry(tutorial.slug!!, tutorial.title,
                                tutorialUrl.toString(), KHAN, LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, tutorial.description, false, EMPTY_STRING, EMPTY_STRING,
                                EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao!!)

                        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao!!, topicEntry,
                                tutorialEntry, tutorialCount++)

                        val contentList = tutorial.contentItems

                        browseContent(contentList, tutorialEntry, tutorialUrl, topicFolder)


                    }


                }


            }


        }


    }

    @Throws(IOException::class)
    private fun browseHourOfCode(topicEntry: ContentEntry, topicUrl: URL, topicFolder: File) {

        val document = Jsoup.connect(topicUrl.toString()).get()

        val subjectList = document.select("div.hoc-box-white")

        var hourOfCode = 0
        for (subject in subjectList) {

            val imageSrc = subject.selectFirst("img").attr("src")
            val title = subject.selectFirst("h3").text()
            val description = subject.selectFirst("p").text()
            var hrefLink = subject.selectFirst("a").attr("href")

            hrefLink = hrefLink.substring(0, hrefLink.indexOf("/v/"))

            val subjectUrl = URL(topicUrl, hrefLink)

            val subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(hrefLink, title,
                    subjectUrl.toString(), KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.langUid, null, description, false, EMPTY_STRING, URL(topicUrl, imageSrc).toString(),
                    EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, topicEntry,
                    subjectEntry, hourOfCode++)

            ContentScraperUtil.createQueueItem(queueDao, subjectUrl, subjectEntry, topicFolder,
                    ScraperConstants.KhanContentType.SUBJECT.type, runId,
                    ScrapeQueueItem.ITEM_TYPE_INDEX)

        }

    }

    @Throws(IOException::class)
    private fun browseContent(contentList: List<ModuleResponse.Tutorial.ContentItem>?, tutorialEntry: ContentEntry, tutorialUrl: URL, subjectFolder: File) {

        if (contentList == null) {
            UMLogUtil.logError("no content list inside url $tutorialUrl")
            return
        }

        if (contentList.isEmpty()) {
            UMLogUtil.logError("empty content list inside url $tutorialUrl")
            return
        }

        var contentCount = 0
        for (contentItem in contentList) {

            if (contentItem == null) {
                continue
            }

            val url = URL(tutorialUrl, contentItem.nodeUrl!!)
            val newContentFolder = File(subjectFolder, contentItem.contentId!!)
            newContentFolder.mkdirs()

            val entry = ContentScraperUtil.createOrUpdateContentEntry(contentItem.slug!!, contentItem.title,
                    KHAN_PREFIX + contentItem.contentId!!, KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.langUid, null, contentItem.description, true, EMPTY_STRING, contentItem.thumbnailUrl,
                    EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao)

            ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentParentChildJoinDao, tutorialEntry, entry, contentCount++)

            ContentScraperUtil.createQueueItem(queueDao, url, entry, newContentFolder,
                    contentItem.kind!!, runId, ScrapeQueueItem.ITEM_TYPE_SCRAPE)

        }


    }

    companion object {

        val ROOT_URL = "https://www.khanacademy.org/"

        val TABLE_OF_CONTENTS_ROW = "TableOfContentsRow"
        val SUBJECT_PAGE_TOPIC_CARD = "SubjectPageTopicCard"
        val SUBJECT_CHALLENGE = "SubjectChallenge"
        val SUBJECT_PROGRESS = "SubjectProgress"
        private val KHAN_PREFIX = "khan-id://"
        private lateinit var contentEntryDao: ContentEntryDao
        private lateinit var contentParentChildJoinDao: ContentEntryParentChildJoinDao
        private lateinit var englishLang: Language


        private lateinit var gson: Gson
        private lateinit var queueDao: ScrapeQueueItemDao
        private lateinit var scrapeWorkQueue: LiveDataWorkQueue<ScrapeQueueItem>
        private lateinit var factory: GenericObjectPool<ChromeDriver>


        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                System.err.println("Usage:<file destination><file container><optional log{trace, debug, info, warn, error, fatal}>")
                exitProcess(1)
            }

            UMLogUtil.logDebug(args[0])
            UMLogUtil.setLevel(if (args.size == 3) args[2] else "")

            try {
                val runDao = UmAppDatabase.getInstance(Any()).scrapeRunDao

                var runId = runDao.findPendingRunIdByScraperType(ScrapeRunDao.SCRAPE_TYPE_KHAN)
                if (runId == 0) {
                    runId = runDao.insert(ScrapeRun(ScrapeRunDao.SCRAPE_TYPE_KHAN,
                            ScrapeQueueItemDao.STATUS_PENDING)).toInt()
                }

                scrapeFromRoot(File(args[0]), File(args[1]), runId)
            } catch (e: Exception) {
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Main method exception catch khan")
            }

        }

        @Throws(IOException::class)
        fun startScrape(startUrl: String, destDir: File, containerDir: File, runId: Int) {
            //setup the database
            val url: URL
            try {
                url = URL(startUrl)
            } catch (e: MalformedURLException) {
                UMLogUtil.logFatal("Index Malformed url$startUrl")
                throw IllegalArgumentException("Malformed url$startUrl", e)
            }

            val db = UmAppDatabase.getInstance(Any())
            val repository = db // db.getRepository("https://localhost", "")

            destDir.mkdirs()
            containerDir.mkdirs()
            contentEntryDao = repository.contentEntryDao
            contentParentChildJoinDao = repository.contentEntryParentChildJoinDao
            val languageDao = repository.languageDao
            queueDao = db.scrapeQueueItemDao

            gson = GsonBuilder().disableHtmlEscaping().create()

            LanguageList().addAllLanguages()

            englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English")

            val masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                    ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang.langUid, null,
                    EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                    EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao)

            val khanAcademyEntry = ContentScraperUtil.createOrUpdateContentEntry("https://www.khanacademy.org/", "Khan Academy",
                    "https://www.khanacademy.org/", KHAN, LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null,
                    "You can learn anything.\n" + "For free. For everyone. Forever.", false, EMPTY_STRING,
                    "https://cdn.kastatic.org/images/khan-logo-dark-background.new.png",
                    EMPTY_STRING, EMPTY_STRING, 0, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, khanAcademyEntry, 6)

            val englishFolder = File(destDir, "en")
            englishFolder.mkdirs()

            ContentScraperUtil.createQueueItem(queueDao, url, khanAcademyEntry, englishFolder, ScraperConstants.KhanContentType.TOPICS.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)


            factory = GenericObjectPool(KhanDriverFactory())
            //start the indexing work queue

            val indexProcessor = 4
            val indexWorkQueue = LiveDataWorkQueue(queueDao.findNextQueueItems(runId, ScrapeQueueItem.ITEM_TYPE_INDEX),
                    { item1, item2 -> item1.sqiUid == item2.sqiUid },
                    indexProcessor) {
                queueDao.updateSetStatusById(it.sqiUid, STATUS_RUNNING)
                val parent = contentEntryDao.findByUidAsync(it.sqiContentEntryParentUid)
                val queueUrl: URL

                try {
                    queueUrl = URL(it.scrapeUrl!!)
                    KhanContentIndexer(queueUrl, parent!!, File(it.destDir!!),
                            it.contentType!!, it.sqiUid, runId).run()
                } catch (ignored: IOException) {
                    //Must never happen
                    throw RuntimeException("SEVERE: invalid URL to index: should not be in queue:" + it.scrapeUrl!!)
                }
            }

            GlobalScope.launch {
                indexWorkQueue.start()
            }


            val scrapePrecessor = 6
            scrapeWorkQueue = LiveDataWorkQueue(queueDao.findNextQueueItems(runId, ScrapeQueueItem.ITEM_TYPE_SCRAPE),
                    { item1, item2 -> item1.sqiUid == item2.sqiUid }, scrapePrecessor) {

                queueDao.updateSetStatusById(it.sqiUid, STATUS_RUNNING)
                val parent = contentEntryDao.findByUidAsync(it.sqiContentEntryParentUid)

                val scrapeUrl: URL
                try {
                    scrapeUrl = URL(it.scrapeUrl!!)
                    KhanContentScraper(scrapeUrl, File(it.destDir!!),
                            containerDir, parent!!,
                            it.contentType!!, it.sqiUid, factory).run()
                } catch (ignored: IOException) {
                    throw RuntimeException("SEVERE: invalid URL to scrape: should not be in queue:" + it.scrapeUrl!!)
                }
            }
            GlobalScope.launch {
                scrapeWorkQueue.start()
            }

            ContentScraperUtil.waitForQueueToFinish(queueDao, runId)

            factory.close()

        }

        @Throws(IOException::class)
        fun scrapeFromRoot(destDir: File, containerDir: File, runId: Int) {
            startScrape(ROOT_URL, destDir, containerDir, runId)
        }

        @Throws(IOException::class)
        fun getJsonStringFromScript(url: String): String {

            val document = Jsoup.connect(url).maxBodySize(9437184).get()

            val scriptList = document.getElementsByTag("script")
            for (script in scriptList) {

                for (node in script.dataNodes()) {

                    if (node.wholeData.contains("ReactComponent(")) {

                        val data = node.wholeData
                        try {
                            val index = data.indexOf("ReactComponent(") + 15
                            val end = data.indexOf("loggedIn\": false})") + 17
                            return data.substring(index, end)
                        } catch (e: IndexOutOfBoundsException) {
                            UMLogUtil.logError("Could not get json from the script for url $url")
                            return EMPTY_STRING
                        }

                    } else if (node.wholeData.contains("{\"initialState\"")) {

                        val data = node.wholeData
                        try {
                            val index = data.indexOf("{\"initialState\"")
                            val end = data.lastIndexOf("})")
                            return data.substring(index, end)
                        } catch (e: IndexOutOfBoundsException) {
                            UMLogUtil.logError("Could not get json from the script for url $url")
                            return EMPTY_STRING
                        }

                    }
                }
            }
            return EMPTY_STRING

        }
    }

}
