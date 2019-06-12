package com.ustadmobile.lib.contentscrapers.ck12

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.LogIndex
import com.ustadmobile.lib.contentscrapers.LogResponse
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.ck12.plix.PlixResponse
import com.ustadmobile.lib.contentscrapers.ck12.practice.AnswerResponse
import com.ustadmobile.lib.contentscrapers.ck12.practice.PracticeResponse
import com.ustadmobile.lib.contentscrapers.ck12.practice.QuestionResponse
import com.ustadmobile.lib.contentscrapers.ck12.practice.ScriptEngineReader
import com.ustadmobile.lib.contentscrapers.ck12.practice.TestResponse

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.logging.LogEntries
import org.openqa.selenium.logging.LogEntry
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList

import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException

import com.ustadmobile.lib.contentscrapers.ScraperConstants.CHECK_NAME
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
import org.openqa.selenium.WebElement


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
constructor(private val urlString: String, private val destinationDirectory: File) {
    private val scrapUrl: URL
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


    init {
        scrapUrl = URL(urlString)
    }

    @Throws(IOException::class)
    fun scrapePlixContent() {

        val gson = GsonBuilder().disableHtmlEscaping().create()

        val plixId = urlString.substring(urlString.lastIndexOf("-") + 1, urlString.lastIndexOf("?"))
        val plixName = FilenameUtils.getBaseName(scrapUrl.path)

        val plixDirectory = File(destinationDirectory, plixName)
        plixDirectory.mkdirs()

        assetDirectory = File(plixDirectory, "asset")
        assetDirectory!!.mkdirs()

        val plixUrl = generatePlixLink(plixId)

        val response = gson.fromJson(
                IOUtils.toString(URL(plixUrl), UTF_ENCODING), PlixResponse::class.java)

        val fileLastModified = File(destinationDirectory, plixName + LAST_MODIFIED_TXT)
        isContentUpdated = ContentScraperUtil.isFileContentsUpdated(fileLastModified, ContentScraperUtil.parseServerDate(response.response!!.question!!.updated!!).toString())

        if (!isContentUpdated) {
            return
        }

        ContentScraperUtil.setChromeDriverLocation()

        val driver = ContentScraperUtil.setupLogIndexChromeDriver()

        driver.get(urlString)
        val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM.toLong())
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
        try {
            waitDriver.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#questionController"))).click()
        } catch (e: Exception) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
        }

        val les = driver.manage().logs().get(LogType.PERFORMANCE)
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
                    val urlDirectory = ContentScraperUtil.createDirectoryFromUrl(plixDirectory, url)
                    val file = ContentScraperUtil.downloadFileFromLogIndex(url, urlDirectory, log)

                    if (file.name.contains("plix.js")) {
                        var plixJs = FileUtils.readFileToString(file, UTF_ENCODING)
                        val startIndex = plixJs.indexOf("\"trialscount.plix.\"")
                        val lastIndex = plixJs.lastIndexOf("():")
                        plixJs = StringBuilder(plixJs).insert(lastIndex + 3, "*/").insert(startIndex, "/*").toString()
                        FileUtils.writeStringToFile(file, plixJs, UTF_ENCODING)
                    }

                    if (file.name.contains("plix.css")) {
                        var plixJs = FileUtils.readFileToString(file, UTF_ENCODING)
                        val startIndex = plixJs.indexOf("@media only screen and (max-device-width:")
                        val endIndex = plixJs.indexOf(".plix{")
                        plixJs = StringBuilder(plixJs).insert(endIndex, "*/").insert(startIndex, "/*").toString()
                        FileUtils.writeStringToFile(file, plixJs, UTF_ENCODING)
                    }

                    if (file.name.contains("plix.html")) {
                        val plixJs = FileUtils.readFileToString(file, UTF_ENCODING)
                        val doc = Jsoup.parse(plixJs)

                        doc.selectFirst("div.read-more-container").remove()
                        doc.selectFirst("div#portraitView").remove()
                        doc.selectFirst("div#ToolBarView").remove()
                        doc.selectFirst("div#deviceCompatibilityAlertPlix").remove()
                        doc.selectFirst("div#leftBackWrapper").remove()

                        val head = doc.head()
                        head.append(css)

                        val iframe = doc.selectFirst("div.plixIFrameContainer")
                        iframe.removeClass("plixIFrameContainer")

                        val leftWrapper = doc.selectFirst("div#plixLeftWrapper")
                        leftWrapper.removeClass("plixLeftWrapper")
                        leftWrapper.addClass("small-12")
                        leftWrapper.addClass("medium-6")
                        leftWrapper.addClass("large-6")
                        val leftAttr = leftWrapper.attr("style")
                        leftWrapper.attr("style", leftAttr + "display: block;")

                        val rightWrapper = doc.selectFirst("div#plixRightWrapper")
                        rightWrapper.removeClass("small-6")
                        rightWrapper.addClass("small-12")
                        rightWrapper.addClass("medium-6")
                        rightWrapper.addClass("large-6")

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

        FileUtils.writeStringToFile(File(plixDirectory, "index.json"), gson.toJson(indexList), UTF_ENCODING)

    }

    @Throws(IOException::class)
    fun scrapeVideoContent() {

        val fullSite = Jsoup.connect(urlString).get()

        val videoContentName = FilenameUtils.getBaseName(scrapUrl.path)
        val videoHtmlLocation = File(destinationDirectory, videoContentName)
        videoHtmlLocation.mkdirs()

        assetDirectory = File(videoHtmlLocation, "asset")
        assetDirectory!!.mkdirs()

        val modifiedFile = File(destinationDirectory, videoContentName + LAST_MODIFIED_TXT)
        isContentUpdated = ContentScraperUtil.isFileContentsUpdated(modifiedFile, isPageUpdated(fullSite).toString())

        if (!isContentUpdated) {
            return
        }


        var videoContent = getMainContent(fullSite, "div.modality_content[data-loadurl]", "data-loadurl")
        // sometimes video stored in iframe
        if (videoContent == null) {
            videoContent = getMainContent(fullSite, "iframe[src]", "src")
            if (videoContent == null) {
                UMLogUtil.logError("Unsupported video content$urlString")
                throw IOException("Did not find video content$urlString")
            }
        }

        val videoElement = getIframefromHtml(videoContent)

        val imageThumbnail = fullSite.select("meta[property=og:image]").attr("content")

        if (imageThumbnail != null && !imageThumbnail.isEmpty()) {
            try {
                val thumbnail = File(assetDirectory, "$videoContentName-video-thumbnail.jpg")
                if (!ContentScraperUtil.fileHasContent(thumbnail)) {
                    FileUtils.copyURLToFile(URL(scrapUrl, imageThumbnail), thumbnail)
                }

            } catch (ignored: IOException) {
            }

        }

        val videoSource = ContentScraperUtil.downloadAllResources(videoElement.outerHtml(), assetDirectory, scrapUrl)

        val videoTitleHtml = getTitleHtml(fullSite)

        val detailHtml = removeAllHref(getDetailSectionHtml(fullSite))

        val indexHtml = videoTitleHtml + videoSource + detailHtml

        FileUtils.writeStringToFile(File(videoHtmlLocation, "index.html"), indexHtml, UTF_ENCODING)

        try {
            ContentScraperUtil.generateTinCanXMLFile(videoHtmlLocation, videoContentName, "en", "index.html",
                    ScraperConstants.VIDEO_TIN_CAN_FILE, scrapUrl.path, "", "")
        } catch (e: TransformerException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Video Tin can file unable to create for url$urlString")
        } catch (e: ParserConfigurationException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Video Tin can file unable to create for url$urlString")
        }

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
                val contentUrl = URL(scrapUrl, path)
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
            val contentUrl = URL(scrapUrl, path)
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
    fun scrapeReadContent() {

        val html = Jsoup.connect(urlString).get()

        val readContentName = FilenameUtils.getBaseName(scrapUrl.path)
        val readHtmlLocation = File(destinationDirectory, readContentName)
        readHtmlLocation.mkdirs()

        assetDirectory = File(readHtmlLocation, "asset")
        assetDirectory!!.mkdirs()

        val modifiedFile = File(destinationDirectory, readContentName + LAST_MODIFIED_TXT)
        isContentUpdated = ContentScraperUtil.isFileContentsUpdated(modifiedFile, isPageUpdated(html).toString())

        if (!isContentUpdated) {
            return
        }

        val readTitle = getTitleHtml(html)

        val content = getMainContent(html, "div.modality_content[data-loadurl]", "data-loadurl")

        if (content == null) {
            UMLogUtil.logError("Unsupported read content$urlString")
            throw IllegalArgumentException("Did not find read content$urlString")
        }
        var readHtml = content.html()

        readHtml = removeAllHref(ContentScraperUtil.downloadAllResources(readHtml, assetDirectory, scrapUrl))

        val vocabHtml = removeAllHref(getVocabHtml(html))

        var detailHtml = removeAllHref(getDetailSectionHtml(html))

        if (readHtml.contains("x-ck12-mathEditor")) {
            readHtml = appendMathJax() + readHtml
            detailHtml = detailHtml + appendMathJaxScript()

            val mathJaxDir = File(readHtmlLocation, "mathjax")
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

        FileUtils.writeStringToFile(File(readHtmlLocation, "index.html"), readHtml, UTF_ENCODING)

        try {
            ContentScraperUtil.generateTinCanXMLFile(readHtmlLocation, readContentName, "en", "index.html",
                    ScraperConstants.ARTICLE_TIN_CAN_FILE, scrapUrl.path, "", "")
        } catch (e: TransformerException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Read Tin can file unable to create for url$urlString")
        } catch (e: ParserConfigurationException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Read Tin can file unable to create for url$urlString")
        }

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
    fun scrapePracticeContent() {

        val gson = GsonBuilder().disableHtmlEscaping().create()

        val practiceUrl = FilenameUtils.getBaseName(scrapUrl.path)

        val testIdLink = generatePracticeLink(practiceUrl)

        val practiceDirectory = File(destinationDirectory, practiceUrl)
        practiceDirectory.mkdirs()

        val practiceAssetDirectory = File(practiceDirectory, "asset")
        practiceAssetDirectory.mkdirs()

        val response = gson.fromJson(
                IOUtils.toString(URL(testIdLink), UTF_ENCODING), PracticeResponse::class.java)

        val testId = response.response!!.test!!.id
        val goal = response.response!!.test!!.goal

        val questionsCount = response.response!!.test!!.questionsCount
        val practiceName = response.response!!.test!!.title
        val updated = response.response!!.test!!.updated

        val modifiedFile = File(destinationDirectory, practiceUrl + LAST_MODIFIED_TXT)
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

            val questionAsset = File(practiceDirectory, questionId!!)
            questionAsset.mkdirs()

            questionResponse.response!!.stem!!.displayText = ContentScraperUtil.downloadAllResources(
                    questionResponse.response!!.stem!!.displayText, questionAsset, scrapUrl)

            val hintsList = questionResponse.response!!.hints
            for (j in hintsList!!.indices) {
                hintsList[j] = ContentScraperUtil.downloadAllResources(hintsList[j], practiceAssetDirectory, scrapUrl)
            }
            questionResponse.response!!.hints = hintsList

            val answerResponse = extractAnswerFromEncryption(questionResponse.response!!.data)

            val answer = gson.fromJson(answerResponse, AnswerResponse::class.java)
            answer.instance!!.solution = ContentScraperUtil.downloadAllResources(answer.instance!!.solution, questionAsset, scrapUrl)

            answer.instance!!.answer = downloadAllResourcesFromAnswer(answer.instance!!.answer!!, questionAsset, scrapUrl)

            if (ScraperConstants.QUESTION_TYPE.MULTI_CHOICE.type.equals(questionResponse.response!!.questionType!!, ignoreCase = true)) {

                val questionOrderList = questionResponse.response!!.responseObjects
                val answerObjectsList = answer.instance!!.responseObjects
                for (order in questionOrderList!!.indices) {

                    val question = questionOrderList[order]

                    question.displayText = ContentScraperUtil.downloadAllResources(question.displayText, questionAsset, scrapUrl)
                    question.optionKey = ContentScraperUtil.downloadAllResources(question.optionKey, questionAsset, scrapUrl)

                    val answerObject = answerObjectsList!![order]
                    answerObject.displayText = ContentScraperUtil.downloadAllResources(answerObject.displayText, questionAsset, scrapUrl)
                    answerObject.optionKey = ContentScraperUtil.downloadAllResources(answerObject.optionKey, questionAsset, scrapUrl)

                }
            }


            questionResponse.response!!.answer = answer

            questionList.add(questionResponse)

        }

        try {
            ContentScraperUtil.generateTinCanXMLFile(practiceDirectory, practiceUrl, "en", "index.html",
                    ScraperConstants.ASSESMENT_TIN_CAN_FILE, scrapUrl.path, "", "")
        } catch (e: TransformerException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Practice Tin can file unable to create for url$urlString")
        } catch (e: ParserConfigurationException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("Practice Tin can file unable to create for url$urlString")
        }

        ContentScraperUtil.saveListAsJson(practiceDirectory, questionList, ScraperConstants.QUESTIONS_JSON)
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.JS_TAG), File(practiceDirectory, JQUERY_JS))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.MATERIAL_CSS_LINK), File(practiceDirectory, MATERIAL_CSS))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.MATERIAL_JS_LINK), File(practiceDirectory, ScraperConstants.MATERIAL_JS))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.CK12_INDEX_HTML_TAG), File(practiceDirectory, INDEX_HTML))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.TIMER_PATH), File(practiceDirectory, TIMER_NAME))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.TROPHY_PATH), File(practiceDirectory, TROPHY_NAME))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.CHECK_PATH), File(practiceDirectory, CHECK_NAME))

    }

    /**
     * Given encrypted data from json response
     *
     * @param data return the result as json string
     * @return
     */
    fun extractAnswerFromEncryption(data: String?): String {
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
    private fun downloadAllResourcesFromAnswer(answer: MutableList<Any>, questionAsset: File, scrapUrl: URL): List<T> {

        for (i in answer.indices) {

            val `object` = answer[i]
            if (`object` is String) {
                answer[i] = ContentScraperUtil.downloadAllResources(`object` as String, questionAsset, scrapUrl)
            } else if (`object` is List<Any>) {
                answer[i] = downloadAllResourcesFromAnswer(`object`, questionAsset, scrapUrl)
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

        val RESPONSE_RECEIVED = "Network.responseReceived"

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 3) {
                System.err.println("Usage: <ck12 json url> <file destination><type READ or PRACTICE or VIDEO or plix><optional log{trace, debug, info, warn, error, fatal}>")
                System.exit(1)
            }
            UMLogUtil.setLevel(if (args.size == 4) args[3] else "")


            UMLogUtil.logInfo(args[0])
            UMLogUtil.logInfo(args[1])
            UMLogUtil.logInfo(args[2])


            try {
                val scraper = CK12ContentScraper(args[0], File(args[1]))
                val type = args[2]
                when (type.toLowerCase()) {

                    "video" -> scraper.scrapeVideoContent()
                    "plix" -> scraper.scrapePlixContent()
                    "practice" -> scraper.scrapePracticeContent()
                    "read", "activities", "study aids", "lesson plans", "real world" -> scraper.scrapeReadContent()
                    else -> UMLogUtil.logError("found a group type not supported $type")
                }

            } catch (e: IOException) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                UMLogUtil.logError("Exception running scrapeContent ck12")
            }

        }
    }


}
