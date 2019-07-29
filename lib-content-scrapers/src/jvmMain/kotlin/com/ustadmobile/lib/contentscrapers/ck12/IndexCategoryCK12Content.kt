package com.ustadmobile.lib.contentscrapers.ck12

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao
import com.ustadmobile.core.db.dao.LanguageDao
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.LanguageList
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING
import com.ustadmobile.lib.contentscrapers.ScraperConstants.LAST_MODIFIED_TXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_TINCAN
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_WEB_CHUNK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY
import com.ustadmobile.lib.db.entities.ContentEntry.Companion.LICENSE_TYPE_CC_BY_NC
import com.ustadmobile.lib.db.entities.Language
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*


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
 */
class IndexCategoryCK12Content @Throws(IOException::class)
constructor(urlString: String, private val destinationDirectory: File, private val containerDir: File) {
    private val contentEntryDao: ContentEntryDao
    private val contentParentChildJoinDao: ContentEntryParentChildJoinDao
    private val languageDao: LanguageDao
    private val containerDao: ContainerDao
    private val db: UmAppDatabase
    private val repository: UmAppDatabase
    private var englishLang: Language? = null
    private val ck12ParentEntry: ContentEntry
    internal var url: URL


    init {

        try {
            url = URL(urlString)
        } catch (e: MalformedURLException) {
            UMLogUtil.logError("Index Malformed url$urlString")
            throw IllegalArgumentException("Malformed url$urlString", e)
        }

        destinationDirectory.mkdirs()
        containerDir.mkdirs()


        db = UmAppDatabase.getInstance(Any())
        repository = db //db.getRepository("https://localhost", "")
        contentEntryDao = repository.contentEntryDao
        contentParentChildJoinDao = repository.contentEntryParentChildJoinDao
        containerDao = repository.containerDao
        languageDao = repository.languageDao

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
                englishLang!!.langUid, null, EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao)


        ck12ParentEntry = ContentScraperUtil.createOrUpdateContentEntry("https://www.ck12.org/", "CK-12 Foundation",
                "https://www.ck12.org/", CK_12, LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null,
                "100% Free, Personalized Learning for Every Student", false, EMPTY_STRING,
                "https://img1.ck12.org/media/build-20181015164501/images/ck12-logo-livetile.png",
                EMPTY_STRING, EMPTY_STRING, contentEntryDao)

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, ck12ParentEntry, 2)

    }

    /**
     * Given a ck12 url, find the content and download it all
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun findContent() {

        val document = Jsoup.connect(url.toString()).get()

        val subjectList = document.select("a.subject-link")

        // each subject appears twice on ck12 for different layouts
        val uniqueSubjects = HashSet<String>()
        var count = 0
        for (subject in subjectList) {

            val hrefLink = subject.attr("href")
            val isAdded = uniqueSubjects.add(hrefLink)

            if (isAdded) {

                val subjectUrl = URL(url, hrefLink)
                val title = subject.attr("title")

                val subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(hrefLink, title, subjectUrl.toString(), CK_12,
                        LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, EMPTY_STRING, false,
                        EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, contentEntryDao)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, ck12ParentEntry, subjectEntry, count++)

                val subjectFolder = File(destinationDirectory, title)
                subjectFolder.mkdirs()

                browseSubjects(subjectUrl, subjectFolder, subjectEntry)

            }

        }

    }

    @Throws(IOException::class)
    private fun browseSubjects(url: URL, destinationDirectory: File, parent: ContentEntry) {

        val driver = ContentScraperUtil.setupChrome(true)
        try {
            driver.get(url.toString())
            val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM.toLong())
            ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
        } catch (e: TimeoutException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
        }

        val doc = Jsoup.parse(driver.pageSource)
        driver.close()

        val subCategory = HashSet<String>()
        val gradesList = doc.select("li.js-grade a")
        var count = 0
        for (grade in gradesList) {

            val hrefLink = grade.attr("href")
            val isAdded = subCategory.add(hrefLink)

            if (isAdded) {

                val title = grade.text()
                val subCategoryUrl = URL(url, hrefLink)

                val gradeFolder = File(destinationDirectory, title)
                gradeFolder.mkdirs()

                val gradeEntry = ContentScraperUtil.createOrUpdateContentEntry(hrefLink, title, subCategoryUrl.toString(), CK_12, LICENSE_TYPE_CC_BY_NC,
                        englishLang!!.langUid, null, EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                        EMPTY_STRING, EMPTY_STRING, contentEntryDao)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, gradeEntry, count++)

                browseGradeTopics(subCategoryUrl, gradeFolder, gradeEntry)
            }
        }

        val categoryList = doc.select("div.concept-container")

        for (category in categoryList) {

            val level1CategoryTitle = category.select("span.concept-name").attr("title")
            val fakePath = "$url/$level1CategoryTitle"

            val topicEntry = ContentScraperUtil.createOrUpdateContentEntry(fakePath, level1CategoryTitle, fakePath, CK_12,
                    LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, EMPTY_STRING, false,
                    EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, topicEntry, count++)

            val firstListCategory = categoryList.select("div.level1-inner-container")

            for (firstCategory in firstListCategory) {

                browseListOfTopics(firstCategory, destinationDirectory, fakePath, topicEntry)

            }
        }

        if (count == 0) {
            UMLogUtil.logInfo("No Topics were found to browse for url $url")
        }

    }

    @Throws(IOException::class)
    private fun browseListOfTopics(firstCategory: Element, destinationDirectory: File, fakePath: String, parent: ContentEntry) {

        val secondListCategory = firstCategory.select(":root > div > div")

        var count = 0
        for (secondCategory in secondListCategory) {

            if (secondCategory.attr("class").contains("concept-container")) {

                val hrefLink = secondCategory.select("a").attr("href")
                val title = secondCategory.select("span").attr("title")

                val contentUrl = URL(url, hrefLink)

                val lastTopicEntry = ContentScraperUtil.createOrUpdateContentEntry(hrefLink, title, contentUrl.toString(), CK_12,
                        LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, EMPTY_STRING, false,
                        EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, contentEntryDao)

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, lastTopicEntry, count++)

                val topicDestination = File(destinationDirectory, title)
                topicDestination.mkdirs()

                browseContent(contentUrl, topicDestination, lastTopicEntry)

            } else if (secondCategory.attr("class").contains("parent")) {

                val title = secondCategory.select("span").attr("title")

                val appendPath = "$fakePath/$title"

                val subTopicEntry = ContentScraperUtil.createOrUpdateContentEntry(appendPath, title, appendPath, CK_12,
                        LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null,
                        EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING,
                        EMPTY_STRING, contentEntryDao)


                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, subTopicEntry, count++)

                browseListOfTopics(secondCategory.child(1), destinationDirectory, appendPath, subTopicEntry)

            }

        }

    }


    @Throws(IOException::class)
    private fun browseGradeTopics(subCategoryUrl: URL, destination: File, parent: ContentEntry) {

        val driver = ContentScraperUtil.setupChrome(true)
        try {
            driver.get(subCategoryUrl.toString())
            val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM.toLong())
            ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
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
                    LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, EMPTY_STRING, false,
                    EMPTY_STRING, thumbnailUrl, EMPTY_STRING, EMPTY_STRING, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, headingEntry, count++)

            val topicList = header.select("div.concept-track-wrapper")

            var topicCount = 0
            for (topic in topicList) {

                val title = topic.selectFirst("div.concept-track-parent").attr("title")
                val fakeParentTopic = "$fakePathTopic/$title"

                val topicThumbnailUrl = topic.selectFirst("div.concept-track-parent span img").attr("src")

                val topicEntry = ContentScraperUtil.createOrUpdateContentEntry(fakeParentTopic, title, fakeParentTopic, CK_12,
                        LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, EMPTY_STRING, false,
                        EMPTY_STRING, topicThumbnailUrl, EMPTY_STRING, EMPTY_STRING, contentEntryDao)

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
                            LICENSE_TYPE_CC_BY_NC, englishLang!!.langUid, null, EMPTY_STRING, false,
                            EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, contentEntryDao)


                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, topicEntry, subTopicEntry, subTopicCount++)

                    browseContent(contentUrl, topicDestination, subTopicEntry)

                }


            }
        }

    }

    @Throws(IOException::class)
    private fun browseContent(contentUrl: URL, topicDestination: File, parent: ContentEntry) {

        val driver = ContentScraperUtil.setupChrome(true)
        try {
            driver.get(contentUrl.toString())
            val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM.toLong())
            ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
            waitDriver.until(ExpectedConditions.elementToBeClickable(By.cssSelector("i.icon-expand"))).click()
        } catch (e: TimeoutException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
        } catch (e: NoSuchElementException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
        }

        val courseList = driver.findElements(By.cssSelector("div[class*=js-components-newspaper-Cards-Cards__cardsRow]"))

        var courseCount = 0
        for (course in courseList) {

            val groupType = course.findElement(
                    By.cssSelector("div[class*=js-components-newspaper-Card-Card__groupType] span"))
                    .text

            val imageLink = course.findElement(
                    By.cssSelector("a[class*=js-components-newspaper-Card-Card__link]"))
                    .getAttribute("href")


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
                    EMPTY_STRING, imageLink, EMPTY_STRING, EMPTY_STRING, contentEntryDao)

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, topicEntry, courseCount++)

            val scraper = CK12ContentScraper(url.toString(), topicDestination)
            try {
                var mimeType = MIMETYPE_TINCAN
                when (groupType.toLowerCase()) {

                    "video" -> scraper.scrapeVideoContent()
                    "plix" -> {
                        scraper.scrapePlixContent()
                        mimeType = MIMETYPE_WEB_CHUNK
                    }
                    "practice" -> scraper.scrapePracticeContent()
                    "read", "activities", "study aids", "lesson plans", "real world" -> scraper.scrapeReadContent()
                    else -> UMLogUtil.logError("found a group type not supported $groupType for url $url")
                }


                val content = File(topicDestination, FilenameUtils.getBaseName(url.path))
                if (scraper.isContentUpdated) {
                    ContentScraperUtil.insertContainer(containerDao, topicEntry, true,
                            mimeType, content.lastModified(), content, db, repository,
                            containerDir)
                }

            } catch (e: Exception) {
                UMLogUtil.logError("Unable to scrape content from $groupType at url $url")
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                val modifiedFile = File(topicDestination, FilenameUtils.getBaseName(url.path) + LAST_MODIFIED_TXT)
                ContentScraperUtil.deleteFile(modifiedFile)
            }

        }

        driver.close()

    }

    companion object {

        private val CK_12 = "CK12"

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 3) {
                System.err.println("Usage: <ck12 url> <file destination><folder container><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }

            UMLogUtil.setLevel(if (args.size == 4) args[3] else "")

            UMLogUtil.logInfo(args[0])
            UMLogUtil.logInfo(args[1])
            try {
                IndexCategoryCK12Content(args[0], File(args[1]), File(args[2])).findContent()
            } catch (e: Exception) {
                UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logFatal("Exception running findContent CK12 Index Scraper")
            }

        }
    }


}
