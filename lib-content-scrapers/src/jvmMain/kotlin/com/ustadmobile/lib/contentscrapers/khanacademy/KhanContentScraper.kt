package com.ustadmobile.lib.contentscrapers.khanacademy

import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.lib.contentscrapers.*
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ATTEMPT_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ATTEMPT_JSON_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ATTEMPT_JSON_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ATTEMPT_KHAN_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.COMPLETE_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.COMPLETE_KHAN_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CORRECT_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CORRECT_KHAN_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.HINT_JSON_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.HINT_JSON_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.INTERNAL_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.INTERNAL_JSON_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN
import com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN_CSS_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN_CSS_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_CSS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_JSON
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_KHAN
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_SVG
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_WEB_CHUNK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TRY_AGAIN_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TRY_AGAIN_KHAN_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.ck12.CK12ContentScraper.Companion.RESPONSE_RECEIVED
import com.ustadmobile.lib.db.entities.ContentEntry
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.commons.pool2.impl.GenericObjectPool
import org.jsoup.Jsoup
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.regex.Pattern


/**
 * Every khan academy content is categorized into several types - video, exercise, article
 *
 *
 * For Video Content, the index has the url of video in their json.
 * The Scraper checks if this was downloaded before by checking the etag from the header of the url
 * Downloads the content and saves it into the zip
 *
 *
 * For Exercise Content, first it is required to be logged in for the exercise.
 * Then, get the json of the exercise from the source code of the website
 * This is needed to get the all the list of exercises in the exercise, the exericse id and the last modified date
 * If already downloaded this content, it checks if the date is the same
 * Load the website using Selenium and wait for the page to load fully to get all its content.
 * Setup Selenium and Chrome
 * Run selenium and wait for everything to load on the screen by waiting for the element div[data-test-id=tutorial-page]
 * Once that is done, get the logs for the network and store in a list
 * Filter the responses based on the message RESPONSE RECEIVED
 * Store the mimeType and url of each response.
 * Copy and Save the content of each url and use request headers if required.
 *
 *
 * Once all the logs have been saved, download all the exercises belonging to that exercise using the list from earlier
 * to get all the item id to call the url
 * https://www.khanacademy.org/api/internal/user/exercises/{exericse-id}/items/{item-id}/assessment_item
 * Extract all the images from the url so those images can be used offline
 *
 *
 * Create a content directory for all the url and their location into a json so it can be played back.
 * Zip all files with the course as the name
 *
 *
 * For Article Content, extract the json from the page, load it into ArticleResponse
 * This is used to check when the article was last updated
 * If updated, Setup Selenium and Chrome
 * Run selenium and wait for everything to load on the screen by waiting for the element ul[class*=listWrapper]
 * Once that is done, get the logs for the network and store in a list
 * Filter the responses based on the message RESPONSE RECEIVED
 * Store the mimeType and url of each response.
 * Copy and Save the content of each url and use request headers if required.
 *
 *
 * Create a content directory for all the url and their location into a json so it can be played back.
 * Zip all files with the course as the name
 */
@ExperimentalStdlibApi
class KhanContentScraper : Runnable {
    private lateinit var containerDir: File

    private lateinit var factory: GenericObjectPool<ChromeDriver>
    private var sqiUid: Int = 0
    private lateinit var contentType: String
    private lateinit var parentEntry: ContentEntry
    private var destinationDirectory: File
    private lateinit var url: URL
    private var driver: ChromeDriver? = null

    private val regexUrlPrefix = "https://((.*).khanacademy.org|cdn.kastatic.org)/(.*)"

    private val secondExerciseUrl = "https://www.khanacademy.org/api/internal/user/exercises/"

    private val exerciseMidleUrl = "/items/"

    private val exercisePostUrl = "/assessment_item"

    var isContentUpdated = true
        private set
    private var nodeSlug: String? = null
    var mimeType: String? = null
        private set


    constructor(scrapeUrl: URL, destinationDirectory: File, containerDir: File, parent: ContentEntry, contentType: String, sqiUid: Int, factory: GenericObjectPool<ChromeDriver>) {

        this.destinationDirectory = destinationDirectory
        this.containerDir = containerDir
        this.url = scrapeUrl
        this.parentEntry = parent
        this.contentType = contentType
        this.sqiUid = sqiUid
        this.factory = factory

    }

    constructor(destinationDirectory: File, driver: ChromeDriver) {
        this.destinationDirectory = destinationDirectory
        this.driver = driver
    }

    override fun run() {
        System.gc()
        val db = UmAppDatabase.getInstance(Any())
        val repository = db// db.getRepository("https://localhost", "")
        val containerDao = repository.containerDao
        val queueDao = db.scrapeQueueItemDao


        val startTime = System.currentTimeMillis()
        UMLogUtil.logInfo("Started scraper url $url at start time: $startTime")
        queueDao.setTimeStarted(sqiUid, startTime)

        var successful = false
        try {
            driver = factory.borrowObject()
            val content = File(destinationDirectory, destinationDirectory.name)
            var mimetype = MIMETYPE_WEB_CHUNK
            when (contentType) {
                ScraperConstants.KhanContentType.VIDEO.type -> {
                    scrapeVideoContent(url.toString())
                    successful = true
                    mimetype = MIMETYPE_KHAN
                }
                ScraperConstants.KhanContentType.EXERCISE.type -> {
                    scrapeExerciseContent(url.toString())
                    successful = true
                }
                ScraperConstants.KhanContentType.ARTICLE.type -> {
                    scrapeArticleContent(url.toString())
                    successful = true
                }
                else -> {
                    UMLogUtil.logError("unsupported kind = $contentType at url = $url")
                    throw IllegalArgumentException("unsupported kind = $contentType at url = $url")
                }
            }

            if (isContentUpdated) {
                ContentScraperUtil.insertContainer(containerDao, parentEntry, true,
                        mimetype, content.lastModified(), content, db, repository,
                        containerDir)
                FileUtils.deleteDirectory(content)
            }

        } catch (e: Exception) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Unable to scrape content from url $url")
            ContentScraperUtil.deleteETagOrModified(destinationDirectory, destinationDirectory.name)
        }

        factory.returnObject(driver)

        queueDao.updateSetStatusById(sqiUid, if (successful) ScrapeQueueItemDao.STATUS_DONE else ScrapeQueueItemDao.STATUS_FAILED, 0)
        queueDao.setTimeFinished(sqiUid, System.currentTimeMillis())
        val duration = System.currentTimeMillis() - startTime
        UMLogUtil.logInfo("Ended scrape for url $url in duration: $duration")

    }

    @Throws(IOException::class)
    fun scrapeVideoContent(scrapUrl: String) {

        val gson = GsonBuilder().disableHtmlEscaping().create()

        val folder = File(destinationDirectory, destinationDirectory.name)
        folder.mkdirs()

        val initialJson = KhanContentIndexer.getJsonStringFromScript(scrapUrl)
        var data: SubjectListResponse? = gson.fromJson(initialJson, SubjectListResponse::class.java)
        if (data!!.componentProps == null) {
            data = gson.fromJson(initialJson, PropsSubjectResponse::class.java).props
        }

        val compProps = data!!.componentProps
        var navData: SubjectListResponse.ComponentData.NavData? = compProps!!.tutorialNavData
        if (navData == null) {
            navData = compProps.tutorialPageData
        }
        var contentList: MutableList<SubjectListResponse.ComponentData.NavData.ContentModel>? = navData!!.contentModels!!
        if (contentList == null || contentList.isEmpty()) {
            contentList = ArrayList()
            contentList.add(navData.contentModel!!)
        }

        for (content in contentList) {

            if (destinationDirectory.name.contains(content.id!!) || scrapUrl.contains(content.relativeUrl!!)) {

                var videoUrl = content.downloadUrls!!.mp4
                if (videoUrl == null || videoUrl.isEmpty()) {
                    videoUrl = content.downloadUrls!!.mp4Low
                    if (videoUrl == null) {
                        UMLogUtil.logError("Video was not available in any format for url: $url")
                        break
                    }
                    UMLogUtil.logTrace("Video was not available in mp4, found in mp4-low at $url")
                }
                val url = URL(URL(scrapUrl), videoUrl)
                var conn: HttpURLConnection? = null
                try {
                    conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "HEAD"
                    mimeType = conn.contentType

                    isContentUpdated = ContentScraperUtil.isFileModified(conn, destinationDirectory, destinationDirectory.name)

                    if (ContentScraperUtil.fileHasContent(folder)) {
                        isContentUpdated = false
                        FileUtils.deleteDirectory(folder)
                    }

                    if (!isContentUpdated) {
                        return
                    }

                    val contentFile = File(folder, FilenameUtils.getName(url.path))
                    FileUtils.copyURLToFile(url, contentFile)
                    val webMFile = File(folder, FilenameUtils.getName(url.path))
                    ShrinkerUtil.convertKhanVideoToWebMAndCodec2(contentFile, webMFile)

                } catch (e: IOException) {
                    throw e
                } finally {
                    conn?.disconnect()
                }

            }
        }

    }


    @Throws(IOException::class)
    fun scrapeExerciseContent(scrapUrl: String) {

        val gson = GsonBuilder().disableHtmlEscaping().create()

        val khanDirectory = File(destinationDirectory, destinationDirectory.name)
        khanDirectory.mkdirs()

        val initialJson = KhanContentIndexer.getJsonStringFromScript(scrapUrl)
        var response: SubjectListResponse? = gson.fromJson(initialJson, SubjectListResponse::class.java)
        if (response!!.componentProps == null) {
            response = gson.fromJson(initialJson, PropsSubjectResponse::class.java).props
        }

        var exerciseId: String? = "0"
        var exerciseList: List<SubjectListResponse.ComponentData.Card.UserExercise.Model.AssessmentItem>? = null
        var dateModified: Long = 0

        val linksMap = HashMap<String, String>()

        val contentModel = response!!.componentProps!!.initialCards!!.userExercises
        for (content in contentModel!!) {

            if (content.exerciseModel == null) {
                continue
            }

            if (content.exerciseModel!!.allAssessmentItems == null) {
                continue
            }

            exerciseList = content.exerciseModel!!.allAssessmentItems
            exerciseId = content.exerciseModel!!.id
            nodeSlug = content.exerciseModel!!.nodeSlug
            dateModified = ContentScraperUtil.parseServerDate(content.exerciseModel!!.dateModified!!)

            val relatedList = content.exerciseModel!!.relatedContent

            if (relatedList != null) {

                for (relatedLink in relatedList) {
                    if (relatedLink == null) {
                        continue
                    }
                    linksMap[relatedLink.kaUrl!!] = "content-detail?sourceUrl=khan-id://" + relatedLink.id!!
                }
            }
            val relatedVideos = content.exerciseModel!!.relatedVideos

            if (relatedVideos != null) {

                for (relatedLink in relatedVideos) {
                    if (relatedLink == null) {
                        continue
                    }
                    linksMap[relatedLink.kaUrl!!] = "content-detail?sourceUrl=khan-id://" + relatedLink.id!!
                }
            }

            break

        }

        var isUpdated: Boolean
        val modifiedFile = File(destinationDirectory, destinationDirectory.name + ScraperConstants.LAST_MODIFIED_TXT)
        isUpdated = ContentScraperUtil.isFileContentsUpdated(modifiedFile, dateModified.toString())

        val indexJsonFile = File(khanDirectory, "index.json")

        if (ContentScraperUtil.fileHasContent(khanDirectory)) {
            isUpdated = false
            FileUtils.deleteDirectory(khanDirectory)
        }

        if (!isUpdated) {
            isContentUpdated = false
            return
        }

        if (driver == null) {
            ContentScraperUtil.setChromeDriverLocation()
            driver = ContentScraperUtil.loginKhanAcademy()
        } else {
            ContentScraperUtil.clearChromeConsoleLog(driver!!)
        }

        driver!!.get(scrapUrl)
        val waitDriver = WebDriverWait(driver!!, TIME_OUT_SELENIUM.toLong())
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
        try {
            waitDriver.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div[data-test-id=tutorial-page]")))
            driver!!.findElement(By.cssSelector("div[class*=calculatorButton")).click()
        } catch (e: Exception) {
            UMLogUtil.logDebug(ExceptionUtils.getStackTrace(e))
        }

        val les = ContentScraperUtil.waitForNewFiles(driver!!)

        val indexList = ArrayList<LogIndex.IndexEntry>()

        for (le in les) {

            val log = gson.fromJson(le.message, LogResponse::class.java)
            if (RESPONSE_RECEIVED.equals(log.message!!.method!!, ignoreCase = true)) {
                val mimeType = log.message!!.params!!.response!!.mimeType
                val urlString = log.message!!.params!!.response!!.url

                try {

                    val url = URL(urlString!!)
                    val urlDirectory = ContentScraperUtil.createDirectoryFromUrl(khanDirectory, url)
                    val file = ContentScraperUtil.downloadFileFromLogIndex(url, urlDirectory, log, null)

                    if (urlString == scrapUrl) {

                        val khanContent = FileUtils.readFileToString(file, UTF_ENCODING)
                        val doc = Jsoup.parse(khanContent)
                        //doc.head().append(KHAN_CSS)
                        //doc.head().append(KHAN_COOKIE)

                        FileUtils.writeStringToFile(file, doc.html(), UTF_ENCODING)

                    }

                    val logIndex = ContentScraperUtil.createIndexFromLog(urlString, mimeType, urlDirectory, file, log)
                    indexList.add(logIndex)

                } catch (e: Exception) {
                    UMLogUtil.logError(urlString!!)
                    UMLogUtil.logDebug(le.message)
                }

            }

        }

        if (exerciseList == null) {
            UMLogUtil.logInfo("Did not get exercise list for url $scrapUrl")
            return
        }


        var exerciseCount = 1
        for (exercise in exerciseList) {
            val practiceUrl = URL(secondExerciseUrl + exerciseId + exerciseMidleUrl + exercise.id + exercisePostUrl)

            val urlFile = ContentScraperUtil.createDirectoryFromUrl(khanDirectory, practiceUrl)
            val file = File(urlFile, "$exerciseCount question")

            val itemData = IOUtils.toString(practiceUrl, UTF_ENCODING)
            FileUtils.writeStringToFile(file, itemData, UTF_ENCODING)
            val itemResponse = gson.fromJson(itemData, ItemResponse::class.java)

            val exerciseIndex = ContentScraperUtil.createIndexFromLog(practiceUrl.toString(), MIMETYPE_JSON,
                    urlFile, file, null)
            indexList.add(exerciseIndex)

            val itemContent = gson.fromJson(itemResponse.itemData, ItemData::class.java)

            var images: MutableMap<String, ItemData.Content.Image?>? = itemContent.question!!.images
            if (images == null) {
                images = HashMap()
            }
            for (content in itemContent.hints!!) {
                if (content.images == null) {
                    continue
                }
                images.putAll(content.images!!)
            }

            val p = Pattern.compile("\\(([^)]+)\\)")
            val m = p.matcher(itemContent.question!!.content!!)

            while (m.find()) {
                images[m.group(1)] = null
            }

            if (itemContent.question!!.widgets != null) {

                for (widget in itemContent.question!!.widgets!!.values) {

                    if (widget.options != null) {

                        if (widget.options!!.options != null) {

                            for (option in widget.options!!.options!!) {

                                val matcher = p.matcher(option.content!!)
                                while (matcher.find()) {
                                    images[matcher.group(1)] = null
                                }

                            }


                        }


                    }

                }

            }

            ContentScraperUtil.downloadImagesFromJsonContent(images, khanDirectory, scrapUrl, indexList)

            exerciseCount++

        }

        var navData: SubjectListResponse.ComponentData.NavData? = response.componentProps!!.tutorialPageData
        if (navData == null) {
            navData = response.componentProps!!.tutorialNavData
        }

        val navList = navData!!.navItems
        if (navList != null) {

            for (navItem in navList) {
                if (navItem.nodeSlug == nodeSlug) {
                    continue
                }
                linksMap[regexUrlPrefix + navItem.nodeSlug!!] = CONTENT_DETAIL_SOURCE_URL_KHAN_ID + navItem.id!!
            }

        }

        val hintIndex = ContentScraperUtil.createIndexWithResourceFiles("https://www.khanacademy.org/hint",
                khanDirectory, MIMETYPE_JSON, javaClass.getResourceAsStream(HINT_JSON_LINK), HINT_JSON_FILE)
        indexList.add(hintIndex)

        val attemptIndex = ContentScraperUtil.createIndexWithResourceFiles("https://www.khanacademy.org/attempt",
                khanDirectory, MIMETYPE_JSON, javaClass.getResourceAsStream(ATTEMPT_JSON_LINK), ATTEMPT_JSON_FILE)
        indexList.add(attemptIndex)

        val correctIndex = ContentScraperUtil.createIndexWithResourceFiles("https://cdn.kastatic.org/images/exercise-correct.svg",
                khanDirectory, MIMETYPE_SVG, javaClass.getResourceAsStream(CORRECT_KHAN_LINK), CORRECT_FILE)
        indexList.add(correctIndex)

        val tryAgainIndex = ContentScraperUtil.createIndexWithResourceFiles("https://cdn.kastatic.org/images/exercise-try-again.svg",
                khanDirectory, MIMETYPE_SVG, javaClass.getResourceAsStream(TRY_AGAIN_KHAN_LINK), TRY_AGAIN_FILE)
        indexList.add(tryAgainIndex)

        val attmeptIndex = ContentScraperUtil.createIndexWithResourceFiles("https://cdn.kastatic.org/images/end-of-task-card/star-attempted.svg",
                khanDirectory, MIMETYPE_SVG, javaClass.getResourceAsStream(ATTEMPT_KHAN_LINK), ATTEMPT_FILE)
        indexList.add(attmeptIndex)

        val completeIndex = ContentScraperUtil.createIndexWithResourceFiles("https://cdn.kastatic.org/images/end-of-task-card/star-complete.svg",
                khanDirectory, MIMETYPE_SVG, javaClass.getResourceAsStream(COMPLETE_KHAN_LINK), COMPLETE_FILE)
        indexList.add(completeIndex)

        val khanCssFile = ContentScraperUtil.createIndexWithResourceFiles("https://www.khanacademy.org/khanscraper.css",
                khanDirectory, MIMETYPE_CSS, javaClass.getResourceAsStream(KHAN_CSS_LINK), KHAN_CSS_FILE)
        indexList.add(khanCssFile)

        val internalPractice = ContentScraperUtil.createIndexWithResourceFiles("https://www.khanacademy.org/api/internal/user/task/practice/",
                khanDirectory, MIMETYPE_JSON, javaClass.getResourceAsStream(INTERNAL_JSON_LINK), INTERNAL_FILE)
        indexList.add(internalPractice)

        val index = LogIndex()
        index.title = KHAN
        index.entries = indexList
        index.links = linksMap

        FileUtils.writeStringToFile(indexJsonFile, gson.toJson(index), UTF_ENCODING)
    }


    @Throws(IOException::class)
    fun scrapeArticleContent(scrapUrl: String) {

        val gson = GsonBuilder().disableHtmlEscaping().create()

        val khanDirectory = File(destinationDirectory, destinationDirectory!!.name)
        khanDirectory.mkdirs()

        val indexJsonFile = File(khanDirectory, "index.json")

        val initialJson = KhanContentIndexer.getJsonStringFromScript(scrapUrl)
        var data: SubjectListResponse? = gson.fromJson(initialJson, SubjectListResponse::class.java)
        if (data!!.componentProps == null) {
            data = gson.fromJson(initialJson, PropsSubjectResponse::class.java).props
        }

        val compProps = data!!.componentProps
        var navData = compProps!!.tutorialNavData
        if (navData == null) {
            navData = compProps.tutorialPageData
        }
        var contentList: MutableList<SubjectListResponse.ComponentData.NavData.ContentModel>? = navData!!.contentModels
        if (contentList == null || contentList.isEmpty()) {
            contentList = ArrayList()
            contentList.add(navData.contentModel!!)
        }

        if (contentList.isEmpty()) {
            throw IllegalArgumentException("Does not have the article data id which we need to scrape the page for url $scrapUrl")
        }


        var foundRelative = false
        for (content in contentList) {

            if (destinationDirectory.name.contains(content.id!!) || scrapUrl.contains(content.relativeUrl!!)) {

                foundRelative = true
                val articleId = content.id
                nodeSlug = content.nodeSlug
                val articleUrl = generateArtcleUrl(articleId)
                val response = gson.fromJson(IOUtils.toString(URL(articleUrl), UTF_ENCODING), ArticleResponse::class.java)
                val dateModified = ContentScraperUtil.parseServerDate(response.date_modified!!)

                var isUpdated: Boolean
                val modifiedFile = File(destinationDirectory, destinationDirectory.name + ScraperConstants.LAST_MODIFIED_TXT)
                isUpdated = ContentScraperUtil.isFileContentsUpdated(modifiedFile, dateModified.toString())

                if (ContentScraperUtil.fileHasContent(khanDirectory)) {
                    isUpdated = false
                    FileUtils.deleteDirectory(khanDirectory)
                }

                if (!isUpdated) {
                    isContentUpdated = false
                    return
                }

                break

            }
        }
        if (foundRelative) {
            UMLogUtil.logDebug("found the id at url $scrapUrl")
        } else {
            throw IllegalArgumentException("did not find id at url $scrapUrl")
        }


        if (driver == null) {
            ContentScraperUtil.setChromeDriverLocation()
            driver = ContentScraperUtil.setupLogIndexChromeDriver()
        } else {
            ContentScraperUtil.clearChromeConsoleLog(driver!!)
        }

        driver!!.get(scrapUrl)
        val waitDriver = WebDriverWait(driver!!, TIME_OUT_SELENIUM.toLong())
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
        try {
            waitDriver.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("ul[class*=listWrapper], div[class*=listWrapper")))
            driver!!.findElement(By.cssSelector("div[class*=calculatorButton")).click()
        } catch (e: Exception) {
            UMLogUtil.logDebug(ExceptionUtils.getStackTrace(e))
        }

        val les = ContentScraperUtil.waitForNewFiles(driver!!)

        val index = ArrayList<LogIndex.IndexEntry>()

        for (le in les) {

            val log = gson.fromJson(le.message, LogResponse::class.java)
            if (RESPONSE_RECEIVED.equals(log.message!!.method!!, ignoreCase = true)) {
                val mimeType = log.message!!.params!!.response!!.mimeType
                val urlString = log.message!!.params!!.response!!.url

                try {
                    val url = URL(urlString!!)
                    val urlDirectory = ContentScraperUtil.createDirectoryFromUrl(khanDirectory, url)
                    val file = ContentScraperUtil.downloadFileFromLogIndex(url, urlDirectory, log, null)


                    if (urlString == scrapUrl) {

                        val khanContent = FileUtils.readFileToString(file, UTF_ENCODING)
                        val doc = Jsoup.parse(khanContent)

                        FileUtils.writeStringToFile(file, doc.html(), UTF_ENCODING)

                    }

                    val logIndex = ContentScraperUtil.createIndexFromLog(urlString, mimeType, urlDirectory, file, log)
                    index.add(logIndex)


                } catch (e: Exception) {
                    UMLogUtil.logDebug("Index url failed at " + urlString!!)
                    UMLogUtil.logInfo(le.message)
                }

            }

        }

        val linkMap = HashMap<String, String>()
        val navList = navData.navItems
        if (navList != null) {
            for (navItem in navList) {
                if (navItem.nodeSlug == nodeSlug) {
                    continue
                }
                linkMap[regexUrlPrefix + navItem.nodeSlug!!] = CONTENT_DETAIL_SOURCE_URL_KHAN_ID + navItem.id!!
            }
        } else {
            UMLogUtil.logError("Your related items are in another json for url $scrapUrl")
        }


        val khanCssFile = ContentScraperUtil.createIndexWithResourceFiles("https://www.khanacademy.org/khanscraper.css",
                khanDirectory, MIMETYPE_CSS, javaClass.getResourceAsStream(KHAN_CSS_LINK), KHAN_CSS_FILE)
        index.add(khanCssFile)

        val logIndex = LogIndex()
        logIndex.title = KHAN
        logIndex.entries = index
        logIndex.links = linkMap


        FileUtils.writeStringToFile(indexJsonFile, gson.toJson(logIndex), UTF_ENCODING)
    }

    private fun generateArtcleUrl(articleId: String?): String {
        return "http://www.khanacademy.org/api/v1/articles/" + articleId!!
    }

    companion object {

        const val CONTENT_DETAIL_SOURCE_URL_KHAN_ID = "content-detail?sourceUrl=khan-id://"
    }


}
