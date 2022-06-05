package com.ustadmobile.lib.contentscrapers.ck12

import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.lib.contentscrapers.*
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.getDefaultSeleniumProxy
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CHECK_NAME
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CK12_ACTIVITIES
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CK12_LESSONS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CK12_PRACTICE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CK12_READ
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CK12_READ_WORLD
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CK12_STUDY_AIDS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CONFIG_INPUT_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CONFIG_INPUT_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CONFIG_OUTPUT_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CONFIG_OUTPUT_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EXTENSION_TEX_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.EXTENSION_TEX_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.FONT_DATA_1_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.FONT_DATA_1_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.FONT_DATA_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.FONT_DATA_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.INDEX_HTML
import com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_CONFIG_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_CONFIG_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_ELEMENT_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_ELEMENT_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_INPUT_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_INPUT_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_OUTPUT_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_OUTPUT_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.JQUERY_JS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.LAST_MODIFIED_TXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MATERIAL_CSS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MATH_EVENTS_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MATH_EVENTS_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MATH_JAX_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MATH_JAX_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_MP4
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_TINCAN
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_WEB_CHUNK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MTABLE_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MTABLE_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_AMS_MATH_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_AMS_MATH_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_AMS_SYMBOL_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_AMS_SYMBOL_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_AUTOLOAD_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_AUTOLOAD_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_CANCEL_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_CANCEL_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_COLOR_FILE
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_COLOR_LINK
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TIMER_NAME
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TROPHY_NAME
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.ck12.plix.PlixResponse
import com.ustadmobile.lib.contentscrapers.ck12.practice.*
import com.ustadmobile.lib.contentscrapers.harscraper.setupProxyWithSelenium
import com.ustadmobile.lib.contentscrapers.harscraper.scrapeUrlwithHar
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.staging.contentscrapers.replaceMeWithDi
import net.lightbody.bmp.BrowserMobProxyServer
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.time.Duration
import java.util.*
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException


/**
 * The ck12 content is in found in multiple types
 * Currently supported content includes: read, video, practice, plix
 *
 *
 * Most content is made up of 3 sections:- Title, Main Content, Detail Content
 * each section has a method to get their html section
 * Title from div.title
 * Main Content is from div.modality_content which has an attribute data-loadurl which has a url to load and get the main content from
 * Detail comes from div.metadataview
 *
 *
 * Read Content:
 * All 3 sections are available in read content in its usual format
 * An html page is generated with these sections to create an index.html page
 *
 *
 * Video Content:
 * title and detail are from the 2 methods defined
 * main content can come from an iframe or the usual modality_content
 * An html page is generated with these sections to create an index.html page
 *
 *
 * Practice Content:
 * Does not have the 3 usual sections
 * The content is generated based on the url to the practice course
 * 1st url to get the practice link
 * 2nd url to get the test link and its id
 * 3rd url format to generate each question
 * A question contains an encrypted answer which can be extracted using script engine class
 * and crypto js to decrypt it and store the answer back into the question json
 *
 *
 * Plix:
 * Use selenium and chrome tools to find the all the files plix opens
 * Get the id of the plix in the url. Setup Selenium and Chrome
 * Run selenium and wait for everything to load on the screen by waiting for the element div#questionController
 * Once that is done, get the logs for the network and store in a list
 * Filter the responses based on the message RESPONSE RECEIVED
 * Store the mimeType and url of each response.
 * Copy and Save the content of each url and use request headers if required.
 *
 *
 * To avoid forced sign-in, find
 * else a = "trialscount.plix." + location.hostname, localStorage.getItem(a) && !y || x.preview ? Oe() : (c = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx".replace(/x/g, function(e)
 * Comment out the condition - from = until : to avoid the call to the Oe method (which forced signin)
 *
 *
 * To avoid clickable links in plix content, find the div and use the style display: none to hide it.
 *
 *
 *
 * To fit the plix in all resolutions:
 * First remove @media call in plix.css
 * Add columns to div tags plixLeftWrapper and plixRightWrapper
 * Remove unnecessary parts of the plix page with Jsoup
 * Append some custom css
 *
 * Create a content directory for all the url and their location into a json so it can be played back.
 *
 */

class CK12ContentScraper @Throws(MalformedURLException::class)
constructor(var scrapeUrl: URL, var destLocation: File, var containerDir: File, var parentEntry: ContentEntry, var contentType: String, var sqiUid: Int) : Runnable {

    private lateinit var assetDirectory: File

    val css = "<style> .read-more-container { display: none; } #plixIFrameContainer { float: left !important; margin-top: 15px; } #plixLeftWrapper { float: left !important; width: 49%; min-width: 200px; padding-left: 15px !important; padding-right: 15px !important; margin-right: 15px; } @media (max-width: 1070px) { #plixLeftWrapper { width: 98% !important; } } .plixQestionPlayer, .plixLeftMiddlequestionContainer { margin-bottom: 5px !important; } .leftTopFixedBar { padding-top: 20px !important; } #next-container { margin-top: 0 !important; } .overflow-container { background: transparent !important; width: 0px !important; } .overflow-indicator { left: 50% !important; padding: 12px !important; } .plixWrapper { width: 95% !important; max-width: inherit !important; } body.plix-modal { overflow: auto !important; padding: 0; width: 95% !important; height: inherit !important; } .show-description, .show-challenge { position: static !important; padding-top: 0 !important; } #hintModal { width: 90% !important; margin-left: -45% !important; } @media only screen and (max-device-width: 605px), only screen and (max-device-height: 605px) { #landscapeView { display: block !important; } } </style>"


    private val postfix = "?hints=true&evalData=true"
    private val POLICIES = "?policies=[{\"name\":\"shuffle\",\"value\":false},{\"name\":\"shuffle_question_options\",\"value\":false},{\"name\":\"max_questions\",\"value\":15},{\"name\":\"adaptive\",\"value\":false}]"
    private val practicePost = "?nextPractice=true&adaptive=true&checkUserLogin=false"

    internal var practiceIdLink = "https://www.ck12.org/assessment/api/get/info/test/practice/"
    internal var startTestLink = "https://www.ck12.org/assessment/api/start/test/"
    internal var questionLinkId = "https://www.ck12.org/assessment/api/render/questionInstance/test/"
    // sample questionLink 5985b3d15aa4136da1e858b8/2/5b7a41ba5aa413662008f44f

    internal var plixLink = "https://www.ck12.org/assessment/api/get/info/question/"


    var scriptEngineReader = ScriptEngineReader()
    var isContentUpdated = true
        private set


    override fun run() {
        //This needs to use DI instead
        lateinit var db: UmAppDatabase
        //val db = UmAppDatabase.getInstance(Any(), replaceMeWithDi())
        val repository = db// db.getRepository("https://localhost", "")
        val containerDao = repository.containerDao
        val queueDao = db.scrapeQueueItemDao


        val startTime = System.currentTimeMillis()
        UMLogUtil.logInfo("Started scraper url $scrapeUrl at start time: $startTime with squUid $sqiUid")
        queueDao.setTimeStarted(sqiUid, startTime)
        var successful = false
        try {
            var content = File(destLocation, destLocation.name)
            var mimeType = MIMETYPE_TINCAN
            when (contentType) {
                ScraperConstants.CK12_VIDEO -> {
                    content = File(destLocation, destLocation.name + ".mp4")
                    scrapeVideoContent(content)
                    mimeType = MIMETYPE_MP4
                    successful = true
                }
                ScraperConstants.CK12_PLIX -> {
                    scrapePlixContent(content)
                    mimeType = MIMETYPE_WEB_CHUNK
                    successful = true
                }
                CK12_READ, CK12_ACTIVITIES, CK12_LESSONS, CK12_READ_WORLD, CK12_STUDY_AIDS -> {
                    scrapeReadContent(content)
                    successful = true
                }
                CK12_PRACTICE -> {
                    scrapePracticeContent(content, scrapeUrl)
                    successful = true
                }
                else -> {
                    UMLogUtil.logError("unsupported kind = $contentType at url = $scrapeUrl")
                    queueDao.updateSetStatusById(sqiUid, if (successful) ScrapeQueueItemDao.STATUS_DONE else ScrapeQueueItemDao.STATUS_FAILED, 0)
                    queueDao.setTimeFinished(sqiUid, System.currentTimeMillis())
                    val modifiedFile = File(destLocation, destLocation.name + LAST_MODIFIED_TXT)
                    FileUtils.deleteQuietly(modifiedFile)
                    throw IllegalArgumentException("unsupported kind = $contentType at url = $scrapeUrl")
                }
            }

            if (isContentUpdated) {
                ContentScraperUtil.insertContainer(containerDao, parentEntry, true,
                        mimeType, content.lastModified(), content, db, repository,
                        containerDir)

            }

        } catch (e: Exception) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Unable to scrape content from url $scrapeUrl")
            ContentScraperUtil.deleteETagOrModified(destLocation, destLocation.name)
        }

        queueDao.updateSetStatusById(sqiUid, if (successful) ScrapeQueueItemDao.STATUS_DONE else ScrapeQueueItemDao.STATUS_FAILED, 0)
        queueDao.setTimeFinished(sqiUid, System.currentTimeMillis())
        val duration = System.currentTimeMillis() - startTime
        UMLogUtil.logInfo("Ended scrape for url $scrapeUrl in duration: $duration squUid  $sqiUid")
    }


    @Throws(IOException::class)
    fun scrapePlixContent(content: File) {

        var urlString = scrapeUrl.toString()

        val gson = GsonBuilder().disableHtmlEscaping().create()

        val plixId = urlString.substring(urlString.lastIndexOf("-") + 1, urlString.lastIndexOf("?"))

        content.mkdirs()

        val plixUrl = generatePlixLink(plixId)

        val response = gson.fromJson(
                IOUtils.toString(URL(plixUrl), UTF_ENCODING), PlixResponse::class.java)

        val fileLastModified = File(destLocation, content.name + LAST_MODIFIED_TXT)
        isContentUpdated = ContentScraperUtil.isFileContentsUpdated(fileLastModified, ContentScraperUtil.parseServerDate(response.response!!.question!!.updated!!).toString())

        if (!isContentUpdated) {
            return
        }

        ContentScraperUtil.setChromeDriverLocation()

        val driver = ContentScraperUtil.setupLogIndexChromeDriver()

        driver.get(scrapeUrl.toString())
        val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM)
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
        try {
            waitDriver.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#questionController"))).click()
        } catch (e: Exception) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
        }

        val les = ContentScraperUtil.waitForNewFiles(driver)
        val cookieList = driver.manage().cookies
        driver.close()
        driver.quit()

        val indexList = ArrayList<LogIndex.IndexEntry>()

        for (le in les) {

            val log = gson.fromJson(le.message, LogResponse::class.java)
            if (RESPONSE_RECEIVED.equals(log.message!!.method!!, ignoreCase = true)) {
                val mimeType = log.message!!.params!!.response!!.mimeType
                val urlString = log.message!!.params!!.response!!.url

                try {

                    val url = URL(urlString!!)
                    val urlDirectory = ContentScraperUtil.createDirectoryFromUrl(content, url)
                    var cookies = ContentScraperUtil.returnListOfCookies(urlString, cookieList)
                    val file = ContentScraperUtil.downloadFileFromLogIndex(url, urlDirectory, log, cookies)

                    if (file.name.contains("plix.js")) {
                        var plixJs = FileUtils.readFileToString(file, UTF_ENCODING)

                        if (plixJs.contains("\"trialscount.plix.\"")) {
                            val startIndex = plixJs.indexOf("\"trialscount.plix.\"")
                            val lastIndex = plixJs.lastIndexOf("():")
                            plixJs = StringBuilder(plixJs).insert(lastIndex + 3, "*/").insert(startIndex, "/*").toString()
                            FileUtils.writeStringToFile(file, plixJs, UTF_ENCODING)
                        }
                    }

                    if (file.name.contains("plix.css")) {
                        var plixJs = FileUtils.readFileToString(file, UTF_ENCODING)
                        if (plixJs.contains("@media only screen and (max-device-width:")) {
                            val startIndex = plixJs.indexOf("@media only screen and (max-device-width:")
                            val endIndex = plixJs.indexOf(".plix{")
                            plixJs = StringBuilder(plixJs).insert(endIndex, "*/").insert(startIndex, "/*").toString()
                            FileUtils.writeStringToFile(file, plixJs, UTF_ENCODING)
                        }
                    }

                    if (file.name.contains("plix.html")) {
                        val plixJs = FileUtils.readFileToString(file, UTF_ENCODING)
                        val doc = Jsoup.parse(plixJs)

                        doc.selectFirst("div.read-more-container")?.remove()
                        doc.selectFirst("div#portraitView")?.remove()
                        doc.selectFirst("div#ToolBarView")?.remove()
                        doc.selectFirst("div#deviceCompatibilityAlertPlix")?.remove()
                        doc.selectFirst("div#leftBackWrapper")?.remove()

                        val head = doc.head()
                        head.append(css)

                        val iframe = doc.selectFirst("div.plixIFrameContainer")
                        iframe?.removeClass("plixIFrameContainer")

                        val leftWrapper = doc.selectFirst("div#plixLeftWrapper")
                        leftWrapper?.removeClass("plixLeftWrapper")
                        leftWrapper?.addClass("small-12")
                        leftWrapper?.addClass("medium-6")
                        leftWrapper?.addClass("large-6")
                        val leftAttr = leftWrapper?.attr("style")
                        leftWrapper?.attr("style", leftAttr + "display: block;")

                        val rightWrapper = doc.selectFirst("div#plixRightWrapper")
                        rightWrapper?.removeClass("small-6")
                        rightWrapper?.addClass("small-12")
                        rightWrapper?.addClass("medium-6")
                        rightWrapper?.addClass("large-6")

                        FileUtils.writeStringToFile(file, doc.html(), UTF_ENCODING)

                    }


                    val logIndex = ContentScraperUtil.createIndexFromLog(urlString, mimeType, urlDirectory, file, log)
                    indexList.add(logIndex)

                } catch (e: Exception) {
                    UMLogUtil.logError("Index url failed at " + urlString!!)
                    UMLogUtil.logDebug(le.message)
                }

            }
        }


        val logIndex = LogIndex()
        logIndex.title = ScraperConstants.CK12
        logIndex.entries = indexList

        FileUtils.writeStringToFile(File(content, "index.json"), gson.toJson(logIndex), UTF_ENCODING)

    }

    @Throws(IOException::class)
    fun scrapeVideoContent(destination: File) {

        val fullSite = Jsoup.connect(scrapeUrl.toString()).get()

        val modifiedFile = File(destLocation, destination.name + LAST_MODIFIED_TXT)
        isContentUpdated = ContentScraperUtil.isFileContentsUpdated(modifiedFile, isPageUpdated(fullSite).toString())

        if (!isContentUpdated) {
            return
        }

        var videoContent = getMainContent(fullSite, "div.modality_content[data-loadurl]", "data-loadurl")
        // sometimes video stored in iframe
        if (videoContent == null) {
            videoContent = getMainContent(fullSite, "iframe[src]", "src")
            if (videoContent == null) {
                UMLogUtil.logError("Unsupported video content$scrapeUrl")
                throw IOException("Did not find video content$scrapeUrl")
            }
        }

        val videoElement = getIframefromHtml(videoContent)

        var urlString = Jsoup.parse(videoElement.outerHtml()).select("[src]").attr("src")

        if (urlString.startsWith("//")) {
            urlString = Jsoup.connect("https:$urlString").get().selectFirst("video source")?.attr("src")
        } else if (urlString.startsWith("/flx")) {
            var html = Jsoup.connect("https://www.ck12.org$urlString").followRedirects(true).get()
            var urlSrc = Jsoup.parse(getIframefromHtml(html).outerHtml()).selectFirst("[src]")?.attr("src")
            if (urlSrc?.contains(".mp4") == true) {
                urlString = urlSrc
            } else if (urlSrc?.startsWith("//") == true) {
                urlString = Jsoup.connect("https:$urlSrc").get().selectFirst("video source")?.attr("src")
            } else {
                UMLogUtil.logError("found flx video - might be youtube at $urlSrc")
                isContentUpdated = false
                return
            }
        }

        UMLogUtil.logError("final urlString =  $urlString")

        FileUtils.copyURLToFile(URL(urlString), destination)


    }

    private fun getIframefromHtml(videoContent: Document): Elements {

        val elements = videoContent.select("iframe")
        if (elements.size > 0) {
            return elements
        } else {
            val videoElementsList = videoContent.select("textarea").text()
            return Jsoup.parse(videoElementsList).select("iframe")
        }
    }

    /**
     * Given a document, search for content that has src or data-url to load more content and return a new document
     *
     * @param document website page source
     * @param htmlTag  tag we are looking for - div.modality_content in most cases
     * @param search   src or data-url
     * @return the rendered document found in src/data-url
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun getMainContent(document: Document, htmlTag: String, search: String): Document? {
        val elements = document.select(htmlTag)
        for (element in elements) {
            if (!element.attr(search).contains("googletag")) {
                val path = element.attr(search)
                val contentUrl = URL(scrapeUrl, path)
                return Jsoup.connect(contentUrl.toString())
                        .followRedirects(true).get()
            }
        }
        return null
    }

    @Throws(IOException::class)
    private fun getVocabHtml(site: Document): String? {

        val elements = site.select("section.vocabulary_content[data-loadurl]")
        for (element in elements) {
            val path = element.attr("data-loadurl")
            val contentUrl = URL(scrapeUrl, path)
            return Jsoup.connect(contentUrl.toString())
                    .followRedirects(true).get().html()
        }
        return null
    }


    private fun isPageUpdated(doc: Document): Long {
        val date = doc.select("h2:contains(Last Modified) ~ span").attr("data-date")
        return ContentScraperUtil.parseServerDate(date)
    }


    @Throws(IOException::class)
    fun scrapeReadContent(destination: File) {

        val html = Jsoup.connect(scrapeUrl.toString()).get()

        val readContentName = FilenameUtils.getBaseName(scrapeUrl.path)
        destination.mkdirs()

        assetDirectory = File(destination, "asset")
        assetDirectory.mkdirs()

        val modifiedFile = File(destLocation, destination.name + LAST_MODIFIED_TXT)
        isContentUpdated = ContentScraperUtil.isFileContentsUpdated(modifiedFile, isPageUpdated(html).toString())

        if (!isContentUpdated) {
            return
        }

        val readTitle = getTitleHtml(html)

        val content = getMainContent(html, "div.modality_content[data-loadurl]", "data-loadurl")

        if (content == null) {
            UMLogUtil.logError("Unsupported read destination$scrapeUrl")
            throw IllegalArgumentException("Did not find read destination$scrapeUrl")
        }
        var readHtml = content.html()

        readHtml = removeAllHref(ContentScraperUtil.downloadAllResources(readHtml, assetDirectory, scrapeUrl))

        val vocabHtml = removeAllHref(getVocabHtml(html))

        var detailHtml = removeAllHref(getDetailSectionHtml(html))

        if (readHtml.contains("x-ck12-mathEditor")) {
            readHtml = appendMathJax() + readHtml
            detailHtml += appendMathJaxScript()

            val mathJaxDir = File(destination, "mathjax")
            mathJaxDir.mkdirs()

            FileUtils.copyToFile(javaClass.getResourceAsStream(MATH_JAX_LINK), File(mathJaxDir, MATH_JAX_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(JAX_CONFIG_LINK), File(mathJaxDir, JAX_CONFIG_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(EXTENSION_TEX_LINK), File(mathJaxDir, EXTENSION_TEX_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(MATH_EVENTS_LINK), File(mathJaxDir, MATH_EVENTS_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(TEX_AMS_MATH_LINK), File(mathJaxDir, TEX_AMS_MATH_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(TEX_AMS_SYMBOL_LINK), File(mathJaxDir, TEX_AMS_SYMBOL_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(TEX_AUTOLOAD_LINK), File(mathJaxDir, TEX_AUTOLOAD_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(TEX_CANCEL_LINK), File(mathJaxDir, TEX_CANCEL_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(TEX_COLOR_LINK), File(mathJaxDir, TEX_COLOR_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(JAX_ELEMENT_LINK), File(mathJaxDir, JAX_ELEMENT_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(JAX_INPUT_LINK), File(mathJaxDir, JAX_INPUT_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(CONFIG_INPUT_LINK), File(mathJaxDir, CONFIG_INPUT_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(MTABLE_LINK), File(mathJaxDir, MTABLE_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(FONT_DATA_LINK), File(mathJaxDir, FONT_DATA_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(FONT_DATA_1_LINK), File(mathJaxDir, FONT_DATA_1_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(JAX_OUTPUT_LINK), File(mathJaxDir, JAX_OUTPUT_FILE))
            FileUtils.copyToFile(javaClass.getResourceAsStream(CONFIG_OUTPUT_LINK), File(mathJaxDir, CONFIG_OUTPUT_FILE))
        }

        readHtml = readTitle + readHtml + vocabHtml + detailHtml

        FileUtils.writeStringToFile(File(destination, "index.html"), readHtml, UTF_ENCODING)

        try {
            ContentScraperUtil.generateTinCanXMLFile(destination, readContentName, "en", "index.html",
                    ScraperConstants.ARTICLE_TIN_CAN_FILE, scrapeUrl.path, "", "")
        } catch (e: TransformerException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Read Tin can file unable to create for url$scrapeUrl")
        } catch (e: ParserConfigurationException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Read Tin can file unable to create for url$scrapeUrl")
        }

    }

    fun scrapeFlexBookContent(content: File) {

        content.mkdirs()

        val proxy = BrowserMobProxyServer()
        proxy.start()
        var chromeDriver = setupProxyWithSelenium(proxy, getDefaultSeleniumProxy(proxy), ScraperConstants.CK12)
        scrapeUrlwithHar(proxy, chromeDriver, scrapeUrl.toString(), content,
                waitCondition = {
            it.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.contentarea"))).click()
        }) {

            if (scrapeUrl.toString() == it.request.url) {

                val doc = Jsoup.parse(it.response.content.text)

                doc.selectFirst("div.breadcrumblist")?.remove()
                doc.selectFirst("header")?.remove()
                doc.selectFirst("footer")?.remove()
                doc.selectFirst("div.feedback")?.remove()
                doc.selectFirst("div#flexbook2_banner")?.remove()
                doc.selectFirst("div.ck12-annotation-toolbar-container")?.remove()
                doc.selectFirst("section.myAnnotations-container")?.remove()

                it.response.content.text = doc.html()
            }
            it
        }
        var harFile = File(content, "harcontent")
        harFile.createNewFile()
        proxy.har.writeTo(harFile)
        proxy.stop()


      /*  val gson = GsonBuilder().disableHtmlEscaping().create()

        ContentScraperUtil.setChromeDriverLocation()

        val seleniumProxy = ClientUtil.createSeleniumProxy(proxy)

        val driver = ContentScraperUtil.setupLogIndexChromeDriver(seleniumProxy)
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT, CaptureType.RESPONSE_HEADERS, CaptureType.REQUEST_HEADERS, CaptureType.RESPONSE_BINARY_CONTENT, CaptureType.REQUEST_BINARY_CONTENT)
        proxy.newHar("ck12.org")

        driver.get(scrapeUrlwithHar.toString())
        val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM.toLong())
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
        try {
            waitDriver.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.contentarea"))).click()
            //waitDriver.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.breadcrumblist"))).click()
        } catch (e: Exception) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
        }
        val les = ContentScraperUtil.waitForNewFiles(driver)
        val cookieList = driver.manage().cookies

        val document = Jsoup.parse(driver.pageSource)
        val listOfLinks = document.select("a.flexbooklink")
        val linksMap = HashMap<String, String>()
        listOfLinks.forEach {

            var href = it.attr("href")
            var url = URL(scrapeUrlwithHar, href)
            val decodedPath = URLDecoder.decode(url.toString(), UTF_ENCODING)
            var urlString = decodedPath.replaceAfter("?", "")
            linksMap[url.toString()] = "content-detail?sourceUrl=$urlString"

        }

        driver.close()
        driver.quit()

        var entries = proxy.har.log.entries
        val indexList = ArrayList<LogIndex.IndexEntry>()
        var urlFileName = ContentScraperUtil.getFileNameFromUrl(scrapeUrlwithHar)
        var harFile = File(destLocation, "har")
        harFile.createNewFile()
        proxy.har.writeTo(harFile)

        entries.forEach {

            try {

                val request = it.request
                val response = it.response

                if (request.url.contains("accounts.google.com")) return@forEach

                val decodedPath = URLDecoder.decode(request.url, UTF_ENCODING)
                val url = URL(decodedPath)
                val urlDirectory = ContentScraperUtil.createDirectoryFromUrl(content, url)
                var file = File(urlDirectory, ContentScraperUtil.getFileNameFromUrl(url))

                when {
                    file.name == urlFileName -> {
                        val doc = Jsoup.parse(response.content.text)

                        doc.selectFirst("div.breadcrumblist")?.remove()
                        doc.selectFirst("header")?.remove()
                        doc.selectFirst("footer")?.remove()
                        doc.selectFirst("div.feedback")?.remove()
                        doc.selectFirst("div#flexbook2_banner")?.remove()
                        doc.selectFirst("div.ck12-annotation-toolbar-container")?.remove()
                        doc.selectFirst("section.myAnnotations-container")?.remove()

                        FileUtils.writeStringToFile(file, doc.html(), UTF_ENCODING)

                    }
                    response.content.encoding == "base64" -> {
                        var base = Base64.getDecoder().decode(response.content.text)
                        FileUtils.writeByteArrayToFile(file, base)
                    }
                    else -> FileUtils.writeStringToFile(file, response.content.text, UTF_ENCODING)
                }

                val logIndex = ContentScraperUtil.createIndexFromHar(request.url, response.content.mimeType.replaceAfter(";", "").removeSuffix(";"), urlDirectory, file, response.headers)
                indexList.add(logIndex)
            } catch (e: Exception) {
                UMLogUtil.logError("Index url failed at${it.request.url}")
                UMLogUtil.logDebug(e.message!!)

            }

        }*/

        /* les.forEachIndexed { index, le ->

             val log = gson.fromJson(le.message, LogResponse::class.java)
             if (RESPONSE_RECEIVED.equals(log.message!!.method!!, ignoreCase = true)) {
                 val mimeType = log.message!!.params!!.response!!.mimeType
                 val urlString = log.message!!.params!!.response!!.url

                 try {

                     val url = URL(urlString!!)
                     val urlDirectory = ContentScraperUtil.createDirectoryFromUrl(content, url)
                     var cookies = ContentScraperUtil.returnListOfCookies(urlString, cookieList)
                     val file = ContentScraperUtil.downloadFileFromLogIndex(url, urlDirectory, log, cookies)

                     if (file.name == urlFileName) {
                         val startingUrl = FileUtils.readFileToString(file, UTF_ENCODING)
                         val doc = Jsoup.parse(startingUrl)

                         doc.selectFirst("div.breadcrumblist")?.remove()
                         doc.selectFirst("header")?.remove()
                         doc.selectFirst("footer")?.remove()
                         doc.selectFirst("div.feedback")?.remove()
                         doc.selectFirst("div#flexbook2_banner")?.remove()
                         doc.selectFirst("div.ck12-annotation-toolbar-container")?.remove()
                         doc.selectFirst("section.myAnnotations-container")?.remove()

                         FileUtils.writeStringToFile(file, doc.html(), UTF_ENCODING)

                     }


                     val logIndex = ContentScraperUtil.createIndexFromLog(urlString, mimeType, urlDirectory, file, log)
                     indexList.add(logIndex)

                 } catch (e: Exception) {
                     UMLogUtil.logError("Index url failed at " + urlString!!)
                     UMLogUtil.logDebug(le.message)
                 }

             } else if (REQUEST_SENT == log.message!!.method) {

                 if (log.message!!.params!!.redirectResponse != null) {

                     val mimeType = log.message!!.params!!.redirectResponse!!.mimeType
                     val urlString = log.message!!.params!!.redirectResponse!!.url!!

                     val logIndex = ContentScraperUtil.createIndexFromLog(urlString, mimeType, null, null, log)
                     indexList.add(logIndex)

                 }

             }
         }*/


   /*     val logIndex = LogIndex()
        logIndex.title = ScraperConstants.CK12
        logIndex.entries = indexList
        logIndex.links = linksMap

        FileUtils.writeStringToFile(File(content, "index.json"), gson.toJson(logIndex), UTF_ENCODING)*/

    }


    private fun appendMathJaxScript(): String {
        return "<script language=\"JavaScript\" src=\"./mathjax/MathJax.js\" type=\"text/javascript\">\n" +
                "  </script>\n" +
                "  <script>\n" +
                "   var els = document.getElementsByClassName(\"x-ck12-mathEditor\");\n" +
                "    for(var i = 0; i < els.length; i++) {\n" +
                "        var el = els.item(i);\n" +
                "        var tex = decodeURIComponent(el.getAttribute(\"data-tex\"))\n" +
                "        if (tex.indexOf(\"\\\\begin{align\") === -1) {\n" +
                "            tex = \"\\\\begin{align*}\" + tex + \"\\\\end{align*}\";\n" +
                "        }\n" +
                "        tex = (\"@$\" + tex + \"@$\").replace(/</g, \"&lt;\");\n" +
                "        el.innerHTML = tex;\n" +
                "        el.removeAttribute(\"data-tex-mathjax\");\n" +
                "    }\n" +
                "\n" +
                "    MathJax.Hub.Typeset(MathJax.Hub);\n" +
                "  </script>\n" +
                " </body>\n" +
                "</html>"
    }

    private fun appendMathJax(): String {

        return "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                " <head>\n" +
                "  <title>\n" +
                "  </title><script language=\"JavaScript\" type=\"text/x-mathjax-config\">\n" +
                "   MathJax.Hub.Config({\n" +
                "\t\textensions: [\"tex2jax.js\",\"TeX/AMSmath.js\",\"TeX/AMSsymbols.js\"],\n" +
                "\t\ttex2jax: {\n" +
                "\t\t\tinlineMath: [['@$','@$']],\n" +
                "\t\t\tdisplayMath: [['@$$','@$$']],\n" +
                "\t\t\tskipTags: [\"script\",\"noscript\",\"style\",\"textarea\",\"code\"]\n" +
                "\t\t},\n" +
                "\t\tshowMathMenu : false,\n" +
                "\t\tjax: [\"input/TeX\",\"output/HTML-CSS\"],\n" +
                "\t\tmessageStyle: \"none\",\n" +
                "\t\tTeX: {\n" +
                "\t\t\textensions: [\"cancel.js\", \"color.js\", \"autoload-all.js\"]\n" +
                "\t\t}\n" +
                "\t});\n" +
                "  </script>\n" +
                " </head>\n" +
                " <body>\n"
    }


    /**
     * Given the id, generate the plix link to find out if it was updated
     *
     * @param id
     * @return full url
     */
    fun generatePlixLink(id: String): String {
        return "$plixLink$id?includeBasicPlixDataOnly=true"
    }


    /**
     * Given a practice url - generate the url needed to create the json response
     *
     * @param url practice url
     * @return the generated url
     */
    fun generatePracticeLink(url: String): String {
        return practiceIdLink + url + practicePost
    }

    /**
     * Given the test id from practice link, generate the test url
     *
     * @param testId test id from practice links' response
     * @return the generated url for the test
     */
    fun generateTestUrl(testId: String?): String {
        return startTestLink + testId + POLICIES
    }


    /**
     * Generates the url needed to get the question for the practice
     *
     * @param testId      test id from practice link
     * @param testScoreId test score from test link
     * @param count       question number
     * @return generated url to get the question
     */
    fun generateQuestionUrl(testId: String?, testScoreId: String?, count: Int): String {
        return "$questionLinkId$testId/$count/$testScoreId$postfix"
    }


    @Throws(IOException::class)
    fun scrapePracticeContent(destination: File, startingUrl: URL) {

        val gson = GsonBuilder().disableHtmlEscaping().create()

        val practiceUrl = FilenameUtils.getBaseName(startingUrl.path)

        val testIdLink = generatePracticeLink(practiceUrl)

        destination.mkdirs()

        val practiceAssetDirectory = File(destination, "asset")
        practiceAssetDirectory.mkdirs()

        val response = gson.fromJson(
                IOUtils.toString(URL(testIdLink), UTF_ENCODING), PracticeResponse::class.java)

        val testId = response.response!!.test!!.id
        val goal = response.response!!.test!!.goal

        val questionsCount = response.response!!.test!!.questionsCount
        val practiceName = response.response!!.test!!.title
        val updated = response.response!!.test!!.updated

        val modifiedFile = File(destLocation, destination.name + LAST_MODIFIED_TXT)
        isContentUpdated = ContentScraperUtil.isFileContentsUpdated(modifiedFile, ContentScraperUtil.parseServerDate(updated!!).toString())

        if (!isContentUpdated) {
            return
        }

        var nextPracticeName: String? = ""
        var nextPracticeUrl = ""
        // not all practice urls have next practice
        if (response.response!!.test!!.nextPractice != null) {
            nextPracticeName = response.response!!.test!!.nextPractice!!.nameOfNextPractice
            nextPracticeUrl = practiceIdLink + nextPracticeName + practicePost

        }

        val testLink = generateTestUrl(testId)
        val testResponse = gson.fromJson(
                IOUtils.toString(URL(testLink), UTF_ENCODING), TestResponse::class.java)

        val testScoreId = testResponse.response!!.testScore!!.id

        val questionList = ArrayList<QuestionResponse>()
        for (i in 1..questionsCount) {

            val questionLink = generateQuestionUrl(testId, testScoreId, i)

            val questionResponse = gson.fromJson(
                    IOUtils.toString(URL(questionLink), UTF_ENCODING), QuestionResponse::class.java)

            questionResponse.response!!.goal = goal
            questionResponse.response!!.practiceName = practiceName
            questionResponse.response!!.nextPracticeName = nextPracticeName
            questionResponse.response!!.nextPracticeUrl = nextPracticeUrl

            val questionId = questionResponse.response!!.questionID

            val questionAsset = File(destination, questionId!!)
            questionAsset.mkdirs()

            questionResponse.response!!.stem!!.displayText = ContentScraperUtil.downloadAllResources(
                    questionResponse.response!!.stem!!.displayText ?: "", questionAsset, scrapeUrl)

            val hintsList = questionResponse.response!!.hints
            for (j in hintsList!!.indices) {
                hintsList[j] = ContentScraperUtil.downloadAllResources(hintsList[j], practiceAssetDirectory, startingUrl)
            }
            questionResponse.response!!.hints = hintsList

            val answerResponse = extractAnswerFromEncryption(questionResponse.response!!.data ?: "")

            val answer = gson.fromJson(answerResponse, AnswerResponse::class.java)
            answer.instance?.solution = ContentScraperUtil.downloadAllResources(answer.instance?.solution
                    ?: "", questionAsset, scrapeUrl)

            answer.instance?.answer = downloadAllResourcesFromAnswer(answer.instance!!.answer!!, questionAsset, startingUrl)

            if (ScraperConstants.QUESTION_TYPE.MULTI_CHOICE.type.equals(questionResponse.response!!.questionType!!, ignoreCase = true)) {

                val questionOrderList = questionResponse.response!!.responseObjects
                val answerObjectsList = answer.instance!!.responseObjects
                for (order in questionOrderList!!.indices) {

                    val question = questionOrderList[order]

                    question.displayText = ContentScraperUtil.downloadAllResources(question.displayText
                            ?: "", questionAsset, startingUrl)
                    question.optionKey = ContentScraperUtil.downloadAllResources(question.optionKey
                            ?: "", questionAsset, startingUrl)

                    val answerObject = answerObjectsList!![order]
                    answerObject.displayText = ContentScraperUtil.downloadAllResources(answerObject.displayText
                            ?: "", questionAsset, startingUrl)
                    answerObject.optionKey = ContentScraperUtil.downloadAllResources(answerObject.optionKey
                            ?: "", questionAsset, startingUrl)

                }
            }


            questionResponse.response!!.answer = answer

            questionList.add(questionResponse)

        }

        try {
            ContentScraperUtil.generateTinCanXMLFile(destination, practiceUrl, "en", "index.html",
                    ScraperConstants.ASSESMENT_TIN_CAN_FILE, startingUrl.path, "", "")
        } catch (e: TransformerException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Practice Tin can file unable to create for url$startingUrl")
        } catch (e: ParserConfigurationException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Practice Tin can file unable to create for url$startingUrl")
        }

        ContentScraperUtil.saveListAsJson(destination, questionList, ScraperConstants.QUESTIONS_JSON)
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.JS_TAG), File(destination, JQUERY_JS))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.MATERIAL_CSS_LINK), File(destination, MATERIAL_CSS))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.MATERIAL_JS_LINK), File(destination, ScraperConstants.MATERIAL_JS))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.CK12_INDEX_HTML_TAG), File(destination, INDEX_HTML))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.TIMER_PATH), File(destination, TIMER_NAME))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.TROPHY_PATH), File(destination, TROPHY_NAME))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.CHECK_PATH), File(destination, CHECK_NAME))

    }

    /**
     * Given encrypted data from json response
     *
     * @param data return the result as json string
     * @return
     */
    fun extractAnswerFromEncryption(data: String): String {
        return scriptEngineReader.getResult(data)
    }


    /**
     * Given a list of answers, save the resources in its directory if any found
     *
     * @param answer        return a list of objects because an answer might have its own list of objects
     * @param questionAsset folder where images might be saved
     * @param scrapUrl      base url to get images
     * @return the list of objects with the modified resources
     */
    private fun downloadAllResourcesFromAnswer(answer: MutableList<Any>, questionAsset: File, scrapUrl: URL): MutableList<Any> {

        for (i in answer.indices) {

            val `object` = answer[i]
            if (`object` is String) {
                answer[i] = ContentScraperUtil.downloadAllResources(`object`, questionAsset, scrapUrl)
            } else if (`object` is MutableList<*>) {
                answer[i] = downloadAllResourcesFromAnswer(`object` as MutableList<Any>, questionAsset, scrapUrl)
            }
        }

        return answer
    }


    private fun getTitleHtml(section: Document): String {
        return section.select("div.title").outerHtml()
    }

    private fun getDetailSectionHtml(section: Document): String {

        return section.select("div.metadataview").html()

    }

    private fun removeAllHref(html: String?): String {

        val doc = Jsoup.parse(html!!)

        doc.select("[href]").removeAttr("href")

        return doc.body().html()
    }

    companion object {

        const val RESPONSE_RECEIVED = "Network.responseReceived"

        const val REQUEST_SENT = "Network.requestWillBeSent"

    }


}
