/*
package com.ustadmobile.lib.contentscrapers.ck12

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.*
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.LanguageList
import com.ustadmobile.lib.contentscrapers.ScraperConstants

import com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY_NC
import com.ustadmobile.core.util.LiveDataWorkQueue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*


*/
/**
 * The CK 12 Website has a list of available subjects to download content from "https://www.ck12.org/browse/"
 * Each Subject has a list of topics that appear in different layouts.
 * Each Topic leads to a variety of content for example - Video, Text, Interactive(PLIX) or Practice Questions
 *
 *
 * Each subject is found by using the css selector - a.subject-link
 * A Folder is created for each content
 * There are 3 kinds of layout structure that could be found in each Subject.
 *
 *
 * For Elementary Subjects:
 * Selenium is needed here to get the final page source
 * Find the grade level by using css selector - li.js-grade a
 * Find the list of topics in each grade by css selector - div.topic-details-container
 * Each topic have different concepts to teach found by css selector - div.concept-track-wrapper
 * Each Concept has a list of subtopics that leads to all the variety content found by using selector - div.concept-list-container a
 *
 *
 * For Other Subjects
 * Content is found in Concepts or FlexBook Textbooks (not supported)
 * For Concepts:
 * Concepts have a list of content found using selector - div.concept-container
 * Content is categorised in list of topics and subtopics first by using selector - div.level1-inner-container to get list of topics
 * Each Topic might have their own list of subtopics identified by using checking the class
 * concept-container contains the content information to go to the variety of content - plix, video, questions
 * however if the class contains the word parent, this means there is more concept containers within the parent
 *
 *
 * Once the content url is found -
 * Selenium is needed here to wait for the page to load and click on the expand all button(which opens all the content)
 * Each content is found by the class name js-components-newspaper-Cards-Cards__cardsRow
 * Identify the type of content it is by searching the class name for js-components-newspaper-Card-Card__groupType
 * Link to the content can be found using the class js-components-newspaper-Card-Card__title
 * Once all information is found, use the groupType to identify the scraper to use.
 *//*

class IndexCategoryCK12Content @Throws(IOException::class)
constructor(val queueUrl: URL, val parentEntry: ContentEntry, val destLocation: File,
            val contentType: String, val scrapeQueueItemUid: Int, val runId: Int) : Runnable {


    override fun run() {
        System.gc()
        queueDao.setTimeStarted(scrapeQueueItemUid, System.currentTimeMillis())
        var successful = false
        try {
            when (contentType) {
                ScraperConstants.CK12ContentType.ROOT.type -> {
                    browseRootContent(queueUrl, destLocation, parentEntry)
                    successful = true
                }
                ScraperConstants.CK12ContentType.SUBJECTS.type -> {
                    browseSubjects(queueUrl, destLocation, parentEntry)
                    successful = true
                }
                ScraperConstants.CK12ContentType.GRADES.type -> {
                    browseGradeTopics(queueUrl, destLocation, parentEntry)
                    successful = true
                }
                ScraperConstants.CK12ContentType.CONTENT.type -> {
                    browseContent(queueUrl, destLocation, parentEntry)
                    successful = true
                }
            }

        } catch (e: Exception) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Error creating topics for url $queueUrl")
        }

        queueDao.updateSetStatusById(scrapeQueueItemUid, if (successful) ScrapeQueueItemDao.STATUS_DONE else ScrapeQueueItemDao.STATUS_FAILED, 0)
        queueDao.setTimeFinished(scrapeQueueItemUid, System.currentTimeMillis())
    }

    fun browseRootContent(queueUrl: URL, destLocation: File, parentEntry: ContentEntry) {

        val driver = ContentScraperUtil.setupLogIndexChromeDriver()
        try {
            driver.get(queueUrl.toString())
            val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM.toLong())
            ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
            if (driver.currentUrl.contains("signin")) {
                ContentScraperUtil.loginCK12(driver)
                driver.get(url.toString())
                ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
            }
            waitDriver.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div#browse-grid-wrapper a.dxtrack-user-action")))
        } catch (e: TimeoutException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
        }

        var document = Jsoup.parse(driver.pageSource)
        driver.close()

        var categoryList = document.select("div[role]")
        UMLogUtil.logInfo("size of category List = ${categoryList.size}")

        var categoryCounter = 0
        for (category in categoryList) {

            val categoryTitle = category.text()

            UMLogUtil.logInfo("category title = $categoryTitle")

            val categoryFolder = File(destLocation, categoryTitle)
            categoryFolder.mkdirs()

            val subjectList = document.select("div[data-header=\"$categoryTitle\"] li a")

            UMLogUtil.logInfo("subject size from category $categoryTitle is = ${subjectList.size}")

            val categoryEntry = ContentScraperUtil.createOrUpdateContentEntry(categoryTitle, categoryTitle, "ck12://$categoryTitle", CK_12,
                    LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, "", false,
                    "", "", "", "", 0, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentEntry, categoryEntry, categoryCounter++)

            var subjectCounter = 0
            for (subject in subjectList) {

                var hrefLink = subject.attr("href")
                var title = subject.text()

                UMLogUtil.logInfo("subject title = $title")

                val subjectFolder = File(categoryFolder, title)
                subjectFolder.mkdirs()

                val subjectUrl = URL(queueUrl, hrefLink)

                val subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(hrefLink, title, subjectUrl.toString(), CK_12,
                        LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, "", false,
                        "", "", "", "", 0, contentEntryDao)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, categoryEntry, subjectEntry, subjectCounter++)

                ContentScraperUtil.createQueueItem(queueDao, subjectUrl, subjectEntry, subjectFolder, ScraperConstants.CK12ContentType.SUBJECTS.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)

            }

        }

    }


    @Throws(IOException::class)
    private fun browseSubjects(url: URL, destinationDirectory: File, parent: ContentEntry) {

        val driver = ContentScraperUtil.setupLogIndexChromeDriver()
        try {
            driver.get(url.toString())
            val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM.toLong())
            ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
            if (driver.currentUrl.contains("signin")) {
                ContentScraperUtil.loginCK12(driver)
                driver.get(url.toString())
                ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
            }
        } catch (e: TimeoutException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
        }

        val doc = Jsoup.parse(driver.pageSource)
        driver.close()

        val subCategory = HashSet<String>()
        val gradesList = doc.select("li.js-grade a")
        UMLogUtil.logInfo("size of grades List = ${gradesList.size}")
        var count = 0
        for (grade in gradesList) {

            val hrefLink = grade.attr("href")
            val isAdded = subCategory.add(hrefLink)

            if (isAdded) {

                val title = grade.text()
                val subCategoryUrl = URL(url, hrefLink)

                UMLogUtil.logInfo("grade title = $title")

                val gradeFolder = File(destinationDirectory, title)
                gradeFolder.mkdirs()

                val gradeEntry = ContentScraperUtil.createOrUpdateContentEntry(hrefLink, title, subCategoryUrl.toString(), CK_12, LICENSE_TYPE_CC_BY_NC,
                        englishLang!!.langUid, null, "", false, "", "",
                        "", "", 0, contentEntryDao)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, gradeEntry, count++)

                ContentScraperUtil.createQueueItem(queueDao, subCategoryUrl, gradeEntry, gradeFolder, ScraperConstants.CK12ContentType.GRADES.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)
            }
        }

        val categoryList = doc.select("div.concept-container")


        var listOfChilds: MutableList<ContentEntryParentChildJoin> = mutableListOf()
        if (categoryList.isNotEmpty()) {
            listOfChilds = contentParentChildJoinDao.findListOfChildsByParentUuid(parent.contentEntryUid).toMutableList()
        }

        for (category in categoryList) {

            val level1CategoryTitle = category.select("span.concept-name").attr("title")
            val fakePath = "$url/$level1CategoryTitle"

            val topicEntry = ContentScraperUtil.createOrUpdateContentEntry(fakePath, level1CategoryTitle, fakePath, CK_12,
                    LICENSE_TYPE_CC_BY_NC, englishLang?.langUid ?: 0, null, "", false,
                    "", "", "", "", 0, contentEntryDao)

            val categoryJoin = ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, topicEntry, count++)
            var foundJoin = listOfChilds.find { categoryJoin == it }
            if (foundJoin != null) {
                listOfChilds.remove(foundJoin)
            }

            browseListOfTopics(category.selectFirst("div.level1-inner-container"), destinationDirectory, fakePath, topicEntry)
        }
        if (listOfChilds.isNotEmpty()) {
            turnOffPublicContentEntry(listOfChilds, parent.sourceUrl)
        }


        if (count == 0) {
            UMLogUtil.logInfo("No Topics were found to browse for url $url")
        }

    }

    fun turnOffPublicContentEntry(listOfChilds: List<ContentEntryParentChildJoin>, sourceUrl: String?) {
        val listToRemove = listOfChilds.map { it.cepcjChildContentEntryUid }
        val listOfEntries = contentEntryDao.getContentEntryFromUids(listToRemove)
        listOfEntries.forEach {
            it.ceInactive = true
        }
        contentEntryDao.updateList(listOfEntries)
    }

    @Throws(IOException::class)
    private fun browseListOfTopics(firstCategory: Element, destinationDirectory: File, fakePath: String, parent: ContentEntry) {

        val secondListCategory = firstCategory.select(":root > div > div")
        var listOfChilds = contentParentChildJoinDao.findListOfChildsByParentUuid(parent.contentEntryUid).toMutableList()

        var count = 0
        for (secondCategory in secondListCategory) {

            if (secondCategory.attr("class").contains("concept-container")) {

                val hrefLink = secondCategory.select("a").attr("href")
                val title = secondCategory.select("span").attr("title")

                val contentUrl = URL(url, hrefLink)

                val lastTopicEntry = ContentScraperUtil.createOrUpdateContentEntry(hrefLink, title, contentUrl.toString(), CK_12,
                        LICENSE_TYPE_CC_BY_NC, englishLang?.langUid ?: 0, null, "", false,
                        "", "", "", "", 0, contentEntryDao)

                val secondCategoryJoin = ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, lastTopicEntry, count++)
                var foundJoin = listOfChilds.find { secondCategoryJoin == it }
                if (foundJoin != null) {
                    listOfChilds.remove(foundJoin)
                }

                val topicDestination = File(destinationDirectory, title)
                topicDestination.mkdirs()

                ContentScraperUtil.createQueueItem(queueDao, contentUrl, lastTopicEntry, topicDestination, ScraperConstants.CK12ContentType.CONTENT.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)


            } else if (secondCategory.attr("class").contains("parent")) {

                val title = secondCategory.select("span").attr("title")

                val appendPath = "$fakePath/$title"

                val subTopicEntry = ContentScraperUtil.createOrUpdateContentEntry(appendPath, title, appendPath, CK_12,
                        LICENSE_TYPE_CC_BY_NC, englishLang?.langUid ?: 0, null,
                        "", false, "", "", "",
                        "", 0, contentEntryDao)


                val parentJoin = ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, subTopicEntry, count++)
                var foundJoin = listOfChilds.find { parentJoin == it }
                if (foundJoin != null) {
                    listOfChilds.remove(foundJoin)
                }

                browseListOfTopics(secondCategory.child(1), destinationDirectory, appendPath, subTopicEntry)

            }

        }

        if (listOfChilds.isNotEmpty()) {
            turnOffPublicContentEntry(listOfChilds, parent.sourceUrl)
        }

    }


    @Throws(IOException::class)
    fun browseGradeTopics(subCategoryUrl: URL, destination: File, parent: ContentEntry) {

        val driver = ContentScraperUtil.setupLogIndexChromeDriver()
        try {
            driver.get(subCategoryUrl.toString())
            val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM.toLong())
            ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
            if (driver.currentUrl.contains("signin")) {
                ContentScraperUtil.loginCK12(driver)
                driver.get(subCategoryUrl.toString())
                ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
            }
        } catch (e: TimeoutException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
        }

        val doc = Jsoup.parse(driver.pageSource)
        driver.close()

        var count = 0
        val headerList = doc.select("div.topic-details-container")
        for (header in headerList) {


            val headingTitle = header.select("div.topic-header span").attr("title")

            val fakePathTopic = "$subCategoryUrl/$headingTitle"

            val thumbnailUrl = doc.selectFirst("div.topic-wrapper[title*=$headingTitle] img").attr("src")

            val headingEntry = ContentScraperUtil.createOrUpdateContentEntry(fakePathTopic, headingTitle, fakePathTopic, CK_12,
                    LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, "", false,
                    "", thumbnailUrl, "", "", 0, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, headingEntry, count++)

            val topicList = header.select("div.concept-track-wrapper")

            var topicCount = 0
            for (topic in topicList) {

                val title = topic.selectFirst("div.concept-track-parent").attr("title")
                val fakeParentTopic = "$fakePathTopic/$title"

                val topicThumbnailUrl = topic.selectFirst("div.concept-track-parent span img").attr("src")

                val topicEntry = ContentScraperUtil.createOrUpdateContentEntry(fakeParentTopic, title, fakeParentTopic, CK_12,
                        LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, "", false,
                        "", topicThumbnailUrl, "", "", 0, contentEntryDao)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, headingEntry, topicEntry, topicCount++)

                val subTopicsList = topic.select("div.concept-list-container a")

                var subTopicCount = 0
                for (subTopic in subTopicsList) {

                    val hrefLink = subTopic.attr("href")
                    val subTitle = subTopic.text()

                    val topicDestination = File(destination, subTitle)
                    topicDestination.mkdirs()
                    val contentUrl = URL(subCategoryUrl, hrefLink)

                    val subTopicEntry = ContentScraperUtil.createOrUpdateContentEntry(hrefLink, subTitle, contentUrl.toString(), CK_12,
                            LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, "", false,
                            "", "", "", "", 0, contentEntryDao)


                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, topicEntry, subTopicEntry, subTopicCount++)

                    ContentScraperUtil.createQueueItem(queueDao, contentUrl, subTopicEntry, topicDestination, ScraperConstants.CK12ContentType.CONTENT.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)

                }


            }
        }

    }

    @Throws(IOException::class)
    fun browseContent(contentUrl: URL, topicDestination: File, parent: ContentEntry) {

        val driver = ContentScraperUtil.setupLogIndexChromeDriver()
        try {
            driver.get(contentUrl.toString())
            val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM.toLong())
            ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
            if (driver.currentUrl.contains("signin")) {
                ContentScraperUtil.loginCK12(driver)
                driver.get(contentUrl.toString())
                ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
            }
            waitDriver.until(ExpectedConditions.elementToBeClickable(By.cssSelector("i.icon-expand"))).click()
        } catch (e: TimeoutException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
        } catch (e: NoSuchElementException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
        }

        val courseList = driver.findElements(By.cssSelector("div[class*=js-components-newspaper-Cards-Cards__cardsRow]"))

        var courseCount = 0
        for (course in courseList) {

            try {

                val groupType = course.findElement(
                        By.cssSelector("div[class*=js-components-newspaper-Card-Card__groupType] span"))
                        .text

                var imageLink = course.findElement(
                        By.cssSelector("a[class*=js-components-newspaper-Card-Card] img"))
                        .getAttribute("src")

                imageLink = URL(contentUrl, imageLink).toString()

                val link = course.findElement(
                        By.cssSelector("h2[class*=js-components-newspaper-Card-Card__title] a"))

                val hrefLink = link.getAttribute("href")
                val title = link.getAttribute("title")

                val summary = course.findElement(
                        By.cssSelector("div[class*=js-components-newspaper-Card-Card__summary]"))
                        .text

                val url = URL(contentUrl, hrefLink)

                val topicEntry = ContentScraperUtil.createOrUpdateContentEntry(url.path, title, url.toString().substring(0, url.toString().indexOf("?")), CK_12,
                        LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, summary, true,
                        "", imageLink, "", "",  ScraperConstants.CONTENT_MAP_CK12[groupType?.toLowerCase()]
                        ?: 0, contentEntryDao)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, topicEntry, courseCount++)

                ContentScraperUtil.createQueueItem(queueDao, url, topicEntry, topicDestination,
                        groupType.toLowerCase(), runId, ScrapeQueueItem.ITEM_TYPE_SCRAPE)

            } catch (e: Exception) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("error getting content from page $contentUrl with hrefLink ${course.findElement(
                        By.cssSelector("h2[class*=js-components-newspaper-Card-Card__title] a")).getAttribute("href")}")
            }

        }

        driver.close()

    }

    companion object {

        private val CK_12 = "CK12"

        val ROOT_URL = "https://www.ck12.org/browse/"

        lateinit var queueDao: ScrapeQueueItemDao
        lateinit var contentEntryDao: ContentEntryDao
        lateinit var contentParentChildJoinDao: ContentEntryParentChildJoinDao
        lateinit var languageDao: LanguageDao
        lateinit var containerDao: ContainerDao
        lateinit var db: UmAppDatabase
        lateinit var repository: UmAppDatabase
        private var englishLang: Language? = null
        private lateinit var ck12ParentEntry: ContentEntry
        internal lateinit var url: URL


        fun scrapeFromRoot(destination: File, container: File, runId: Int) {
            startScrape(ROOT_URL, destination, container, runId)
        }

        fun startScrape(urlString: String, destination: File, container: File, runId: Int) {
            try {
                url = URL(urlString)
            } catch (e: MalformedURLException) {
                UMLogUtil.logError("Index Malformed url$urlString")
                throw IllegalArgumentException("Malformed url$urlString", e)
            }

            destination.mkdirs()
            container.mkdirs()


            db = UmAppDatabase.getInstance(Any())
            repository = db //db.getRepository("https://localhost", "")
            contentEntryDao = repository.contentEntryDao
            contentParentChildJoinDao = repository.contentEntryParentChildJoinDao
            containerDao = repository.containerDao
            languageDao = repository.languageDao
            queueDao = db.scrapeQueueItemDao

            LanguageList().addAllLanguages()

            ContentScraperUtil.setChromeDriverLocation()

            englishLang = languageDao.findByTwoCode(ScraperConstants.ENGLISH_LANG_CODE)
            if (englishLang == null) {
                englishLang = Language()
                englishLang!!.name = "English"
                englishLang!!.iso_639_1_standard = ScraperConstants.ENGLISH_LANG_CODE
                englishLang!!.iso_639_2_standard = "eng"
                englishLang!!.iso_639_3_standard = "eng"
                englishLang!!.langUid = languageDao.insert(englishLang!!)
            }

            val masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ScraperConstants.ROOT, ScraperConstants.USTAD_MOBILE,
                    ScraperConstants.ROOT, ScraperConstants.USTAD_MOBILE, LICENSE_TYPE_CC_BY,
                    englishLang!!.langUid, null, "", false, "", "",
                    "", "", 0, contentEntryDao)


            ck12ParentEntry = ContentScraperUtil.createOrUpdateContentEntry("https://www.ck12.org/", "CK-12 Foundation",
                    "https://www.ck12.org/", CK_12, LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null,
                    "100% Free, Personalized Learning for Every Student", false, "",
                    "https://img1.ck12.org/media/build-20181015164501/images/ck12-logo-livetile.png",
                    "", "", 0, contentEntryDao)

            //ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, ck12ParentEntry, 2)

            ContentScraperUtil.createQueueItem(queueDao, url, ck12ParentEntry, destination, ScraperConstants.GDLContentType.ROOT.type, runId, ScrapeQueueItem.ITEM_TYPE_INDEX)

            val indexProcessor = 4
            val indexWorkQueue = LiveDataWorkQueue(queueDao.findNextQueueItems(runId, ScrapeQueueItem.ITEM_TYPE_INDEX),
                    { item1, item2 -> item1.sqiUid == item2.sqiUid },
                    indexProcessor) {
                queueDao.updateSetStatusById(it.sqiUid, ScrapeQueueItemDao.STATUS_RUNNING, 0)
                val parent = contentEntryDao.findByUidAsync(it.sqiContentEntryParentUid)
                val queueUrl: URL

                try {
                    queueUrl = URL(it.scrapeUrl!!)
                    IndexCategoryCK12Content(queueUrl, parent!!, File(it.destDir!!),
                            it.contentType!!, it.sqiUid, runId).run()
                } catch (ignored: Exception) {
                    //Must never happen
                    throw RuntimeException("SEVERE: invalid URL to index: should not be in queue:" + it.scrapeUrl!!)
                }
            }

            GlobalScope.launch {
                indexWorkQueue.start()
            }

            val scrapePrecessor = 4
            var scrapeWorkQueue = LiveDataWorkQueue(queueDao.findNextQueueItems(runId, ScrapeQueueItem.ITEM_TYPE_SCRAPE),
                    { item1, item2 -> item1.sqiUid == item2.sqiUid }, scrapePrecessor) {

                queueDao.updateSetStatusById(it.sqiUid, ScrapeQueueItemDao.STATUS_RUNNING, 0)
                val parent = contentEntryDao.findByUidAsync(it.sqiContentEntryParentUid)

                val scrapeUrl: URL
                try {
                    scrapeUrl = URL(it.scrapeUrl!!)
                    CK12ContentScraper(scrapeUrl, File(it.destDir!!),
                            container, parent!!,
                            it.contentType!!, it.sqiUid).run()
                } catch (ignored: Exception) {
                    throw RuntimeException("SEVERE: invalid URL to scrape: should not be in queue:" + it.scrapeUrl!!)
                }
            }
            GlobalScope.launch {
                scrapeWorkQueue.start()
            }

            ContentScraperUtil.waitForQueueToFinish(queueDao, runId)
            UMLogUtil.logInfo("Finished Indexer")
        }


        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                System.err.println("Usage: <file destination><folder container><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }

            UMLogUtil.setLevel(if (args.size == 3) args[2] else "")
            UMLogUtil.logInfo(args[0])
            UMLogUtil.logInfo(args[1])
            ContentScraperUtil.checkIfPathsToDriversExist()
            try {
                val runDao = UmAppDatabase.getInstance(Any()).scrapeRunDao

                var runId = runDao.findPendingRunIdByScraperType(ScrapeRunDao.SCRAPE_TYPE_CK12)
                if (runId == 0) {
                    runId = runDao.insert(ScrapeRun(ScrapeRunDao.SCRAPE_TYPE_CK12,
                            ScrapeQueueItemDao.STATUS_PENDING)).toInt()
                }

                scrapeFromRoot(File(args[0]), File(args[1]), runId)
            } catch (e: Exception) {
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logFatal("Exception running findContent CK12 Index Scraper")
            }
        }
    }


}
*/
