package com.ustadmobile.lib.contentscrapers.voa

import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.JQUERY_JS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MATERIAL_CSS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.staging.contentscrapers.replaceMeWithDi
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException

/**
 * The Voice of America Website is an html website with content for learning english.
 * In a single page, you have access a lesson with lots of images, videos and a quiz.
 *
 *
 * By using jsoup, you can extract all the data that is required.
 * A script inside the page has details about the lesson and its last modified date.
 *
 *
 * Start the page by checking if there is quiz in the lesson - store the link to the quiz
 * Remove all the tags that are not required for us - comments, share, links to other pages.
 *
 *
 * All the quiz information is accessed at https://learningenglish.voanews.com/Quiz/Answer with a post request
 * Need to build a json of the quiz based on the data. The questions, choices, images and videos in the quiz.
 *
 *
 * Once you have all the data, download all the src in the page.
 *
 *
 * Store the quiz data, store the page data, add some css and tags to existing page to make it more mobile friendly
 * Write a tin can file for the html content and zip everything in the directory.
 */

class VoaScraper : Runnable {

    private lateinit var containerDir: File
    private var sqiUid: Int = 0
    private lateinit var parentEntry: ContentEntry
    private var scrapUrl: URL
    private var voaDirectory: File
    private var destinationDir: File
    internal var isContentUpdated = true
        private set

    var answerUrl = "https://learningenglish.voanews.com/Quiz/Answer"

    @Throws(IOException::class)
    constructor(url: String, destinationDir: File) {
        scrapUrl = URL(url)
        this.destinationDir = destinationDir
        voaDirectory = File(destinationDir, FilenameUtils.getBaseName(scrapUrl!!.path))
        voaDirectory!!.mkdirs()
    }

    constructor(scrapeUrl: URL, destinationDirectory: File, containerDir: File, parent: ContentEntry, sqiUid: Int) {
        this.destinationDir = destinationDirectory
        this.containerDir = containerDir
        this.scrapUrl = scrapeUrl
        this.parentEntry = parent
        this.sqiUid = sqiUid
        voaDirectory = File(destinationDir, FilenameUtils.getBaseName(scrapUrl!!.path))
        voaDirectory!!.mkdirs()
    }


    override fun run() {
        System.gc()
        //replace this with DI
        lateinit var db: UmAppDatabase
        //val db = UmAppDatabase.getInstance(Any(), replaceMeWithDi())
        val repository = db //db.getRepository("https://localhost", "")
        val containerDao = repository.containerDao
        val queueDao = db.scrapeQueueItemDao


        val startTime = System.currentTimeMillis()
        UMLogUtil.logInfo("Started scraper url $scrapUrl at start time: $startTime")
        queueDao.setTimeStarted(sqiUid, startTime)

        var successful = false
        try {
            scrapeContent()

            val content = File(destinationDir, FilenameUtils.getBaseName(scrapUrl!!.path))
            successful = true
            if (isContentUpdated) {
                ContentScraperUtil.insertContainer(containerDao, parentEntry,
                        true, ScraperConstants.MIMETYPE_TINCAN,
                        content.lastModified(), content,
                        db, repository, containerDir)
            }

        } catch (e: Exception) {
            UMLogUtil.logTrace(ExceptionUtils.getStackTrace(e))
            ContentScraperUtil.deleteFile(
                    File(destinationDir,
                            FilenameUtils.getBaseName(scrapUrl!!.path) + ScraperConstants.LAST_MODIFIED_TXT))
        }

        queueDao.updateSetStatusById(sqiUid, if (successful) ScrapeQueueItemDao.STATUS_DONE else ScrapeQueueItemDao.STATUS_FAILED, 0)
        queueDao.setTimeFinished(sqiUid, System.currentTimeMillis())
        val duration = System.currentTimeMillis() - startTime
        UMLogUtil.logInfo("Ended scrape for url $scrapUrl in duration: $duration")


    }

    @Throws(IOException::class)
    fun scrapeContent() {

        val gson = GsonBuilder().disableHtmlEscaping().create()

        ContentScraperUtil.setChromeDriverLocation()

        val driver = ContentScraperUtil.setupChrome(true)

        driver.get(scrapUrl!!.toString())
        val waitDriver = WebDriverWait(driver, TIME_OUT_SELENIUM)
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)

        val lessonId = FilenameUtils.getBaseName(scrapUrl!!.path)

        val voaDirectory = File(destinationDir, lessonId)
        val modifiedFile = File(destinationDir, lessonId + ScraperConstants.LAST_MODIFIED_TXT)
        voaDirectory.mkdirs()
        var isUpdated: Boolean
        isUpdated = try {
            val element = driver.findElement(By.cssSelector("script[type*=json]"))
            val scriptText = driver.executeScript("return arguments[0].innerText;", element) as String

            val response = gson.fromJson(scriptText, VoaResponse::class.java)

            val dateModified = ContentScraperUtil.parseServerDate(response.dateModified!!.replace("Z", "").replace(" ", "T"))
            ContentScraperUtil.isFileContentsUpdated(modifiedFile, dateModified.toString())

        } catch (ignored: NoSuchElementException) {

            val modified = ContentScraperUtil.parseServerDate(driver.findElement(By.cssSelector("time")).getAttribute("datetime"))
            ContentScraperUtil.isFileContentsUpdated(modifiedFile, modified.toString())

        }

        if (!isUpdated) {
            isContentUpdated = false
            driver.close()
            driver.quit()
            return
        }

        if (ContentScraperUtil.fileHasContent(voaDirectory)) {
            FileUtils.deleteDirectory(voaDirectory)
            voaDirectory.mkdirs()
        }

        var quizHref: String? = null
        var quizAjaxUrl: String? = null

        try {
            val quizElement = driver.findElement(By.cssSelector("a[data-ajax-url*=Quiz]"))
            quizHref = quizElement.getAttribute("href")
            quizAjaxUrl = quizElement.getAttribute("data-ajax-url")
        } catch (ignored: NoSuchElementException) {

        }

        val document = Jsoup.connect(scrapUrl!!.toString()).get()
        removeAllAttributesFromVideoAudio(document)

        val assetDirectory = File(voaDirectory, "asset")
        assetDirectory.mkdirs()

        driver.close()
        driver.quit()

        if (quizHref != null && !quizHref.isEmpty()) {

            val quizResponse = VoaQuiz()
            val quizId = quizAjaxUrl!!.substring(quizAjaxUrl.indexOf("id=") + 3, quizAjaxUrl.indexOf("&"))
            var quizCount = 12
            quizResponse.quizId = quizId
            val quizFile = File(voaDirectory, "questions.json")
            val questionList = ArrayList<VoaQuiz.Questions>()

            var i = 1
            while (i <= quizCount) {

                var conn: HttpURLConnection? = null
                var selectedConn: HttpURLConnection? = null
                try {
                    val answersUrl = URL(answerUrl)
                    val urlDirectory = ContentScraperUtil.createDirectoryFromUrl(voaDirectory, answersUrl)
                    urlDirectory.mkdirs()

                    val questionPage = File(urlDirectory, i.toString() + "question")

                    val params = createParams(quizId, i, null, "True")
                    val requestParams = ContentScraperUtil.convertMapToStringBuffer(params)

                    conn = createConnectionForPost(answersUrl, requestParams)
                    conn.connect()

                    var questionData: String? = IOUtils.toString(conn.inputStream, UTF_ENCODING)

                    val questionDoc = Jsoup.parse(questionData!!)
                    removeAllAttributesFromVideoAudio(questionDoc)

                    var quizSize = questionDoc.select("span.caption").text()
                    quizSize = quizSize.substring(quizSize.length - 1)
                    quizCount = Integer.valueOf(quizSize) * 2

                    questionData = ContentScraperUtil.downloadAllResources(questionDoc.html(), assetDirectory, scrapUrl)
                    val answerLabel = questionDoc.selectFirst("input[name=SelectedAnswerId]")
                    FileUtils.writeStringToFile(questionPage, questionData, UTF_ENCODING)

                    val videoDoc = Jsoup.parse(questionData!!)

                    val question = VoaQuiz.Questions()
                    question.questionText = questionDoc.selectFirst("h2.ta-l")?.text()
                    try {
                        val mediaSource = videoDoc.selectFirst("div.quiz__answers-img video,div.quiz__answers-img img")
                        question.videoHref = mediaSource?.attr("src")
                    } catch (ignored: NoSuchElementException) {

                    } catch (ignored: NullPointerException) {
                    }

                    val choiceList = ArrayList<VoaQuiz.Questions.Choices>()
                    val answerTextList = questionDoc.select("label.quiz__answers-label")
                    for (answer in answerTextList) {
                        val choices = VoaQuiz.Questions.Choices()
                        choices.id = answer.selectFirst("input")?.attr("value")
                        choices.answerText = answer.selectFirst("span.quiz__answers-item-text")?.text()
                        choiceList.add(choices)
                    }
                    question.choices = choiceList

                    val answerId = answerLabel?.attr("value")

                    val selectedParams = createParams(quizId, i + 1, answerId, "False")

                    val selectedRequestParams = ContentScraperUtil.convertMapToStringBuffer(selectedParams)

                    selectedConn = createConnectionForPost(answersUrl, selectedRequestParams)

                    val answerPage = File(urlDirectory, answerId + "answersIndex")
                    selectedConn.connect()
                    FileUtils.copyInputStreamToFile(selectedConn.inputStream, answerPage)

                    val selectedAnswerDoc = Jsoup.parse(answerPage, UTF_ENCODING)
                    question.answerId = selectedAnswerDoc.selectFirst("li.quiz__answers-item--correct input")
                            ?.attr("value")
                    question.answer = selectedAnswerDoc.selectFirst("p.p-t-md")?.text()
                    questionList.add(question)
                } catch (e: IOException) {
                    UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                } finally {
                    conn?.disconnect()
                    selectedConn?.disconnect()
                }
                i = i + 2


            }

            quizResponse.questions = questionList
            FileUtils.writeStringToFile(quizFile, gson.toJson(quizResponse), UTF_ENCODING)

        }
        val voaData = ContentScraperUtil.downloadAllResources(document.selectFirst("div#content")?.html() ?: "", assetDirectory, scrapUrl)
        val finalDoc = Jsoup.parse(voaData!!)
        finalDoc.head().append("<link rel=\"stylesheet\" href=\"asset/materialize.min.css\">")
        finalDoc.head().append("<meta charset=\"utf-8\" name=\"viewport\"\n" + "          content=\"width=device-width, initial-scale=1, shrink-to-fit=no,user-scalable=no\">")
        finalDoc.head().append("<link rel=\"stylesheet\" href=\"asset/voa.min.css\">")
        finalDoc.body().append("<script type=\"text/javascript\" src=\"asset/iframeResizer.min.js\"></script>")
        finalDoc.body().append("<script type=\"text/javascript\" src=\"asset/voa.min.js\"></script>")

        finalDoc.body().attr("style", "padding:2%")
        if (quizHref != null) {
            finalDoc.selectFirst("div.quiz__body")?.after("<div class=\"iframe-container\"><iframe id=\"myFrame\" src=\"quiz.html\" frameborder=\"0\" scrolling=\"no\" width=\"100%\"></frame></div>")
        }
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.QUIZ_HTML_LINK),
                File(voaDirectory, ScraperConstants.QUIZ_HTML_FILE))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.IFRAME_RESIZE_LINK),
                File(assetDirectory, ScraperConstants.IFRAME_RESIZE_FILE))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.IFRAME_RESIZE_WINDOW_LINK),
                File(assetDirectory, ScraperConstants.IFRAME_RESIZE_WINDOW_FILE))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.JS_TAG),
                File(assetDirectory, JQUERY_JS))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.MATERIAL_CSS_LINK),
                File(assetDirectory, MATERIAL_CSS))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.MATERIAL_JS_LINK),
                File(assetDirectory, ScraperConstants.MATERIAL_JS))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.VOA_CSS_LINK),
                File(assetDirectory, ScraperConstants.VOA_CSS_FILE_NAME))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.VOA_JS_LINK),
                File(assetDirectory, ScraperConstants.VOA_JS_FILE_NAME))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.VOA_QUIZ_JS_LINK),
                File(assetDirectory, ScraperConstants.VOA_QUIZ_JS_FILE_NAME))
        FileUtils.copyToFile(javaClass.getResourceAsStream(ScraperConstants.VOA_QUIZ_CSS_LINK),
                File(assetDirectory, ScraperConstants.VOA_QUIZ_CSS_FILE_NAME))


        FileUtils.writeStringToFile(File(voaDirectory, "index.html"), finalDoc.toString(), ScraperConstants.UTF_ENCODING)

        try {
            ContentScraperUtil.generateTinCanXMLFile(voaDirectory, FilenameUtils.getBaseName(scrapUrl!!.toString()), "en", "index.html",
                    ScraperConstants.VIDEO_TIN_CAN_FILE, scrapUrl!!.path, "", "")
        } catch (e: TransformerException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("VOA failed to create tin can file for url " + scrapUrl!!.toString())
        } catch (e: ParserConfigurationException) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
            UMLogUtil.logError("VOA failed to create tin can file for url " + scrapUrl!!.toString())
        }

    }

    private fun createParams(quizId: String, count: Int, selectedAnswer: String?, voted: String): Map<String, String> {
        val selectedParams = HashMap<String, String>()
        if (selectedAnswer != null) {
            selectedParams["SelectedAnswerId"] = selectedAnswer
        }
        selectedParams["QuestionVoted"] = voted
        selectedParams["quizId"] = quizId
        selectedParams["PageIndex"] = count.toString()
        selectedParams["isEmbedded"] = "True"
        return selectedParams
    }

    @Throws(IOException::class)
    private fun createConnectionForPost(answersUrl: URL, requestParams: StringBuffer): HttpURLConnection {
        val conn: HttpURLConnection
        var out: DataOutputStream? = null
        try {
            conn = answersUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.doInput = true
            conn.setRequestProperty("Content-length", requestParams.length.toString())
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.setRequestProperty("Referer", scrapUrl!!.toString())
            out = DataOutputStream(conn.outputStream)
            out.writeBytes(requestParams.toString())
        } finally {
            out?.flush()
            out?.close()
        }
        return conn
    }

    private fun removeAllAttributesFromVideoAudio(document: Document) {
        document.select("div.c-spinner").remove()
        document.select("div.js-poster").remove()
        document.select("a.c-mmp__fallback-link").remove()
        document.select("div#comments").remove()
        document.select("div.article-share").remove()
        document.select("div.link-function").remove()
        document.select("div.media-download").remove()
        document.select("div.c-mmp__overlay").remove()
        document.select("button.btn-popout-player").remove()
        document.select("div.js-cpanel-container").remove()
        document.select("div.design-top-offset").remove()
        document.select("div.quiz__main-img").remove()
        document.select("div.quiz__intro").remove()
        document.select("div.media-block-wrap").remove()
        document.select("aside.js-share--horizontal").remove()
        document.select("div.nav-tabs__inner").remove()
        document.select("[href]").removeAttr("href")
        val linkElements = document.select("video,audio")
        for (link in linkElements) {
            val keys = ArrayList<String>()
            val attrList = link.attributes()
            for ((key) in attrList) {
                if (key != "src") {
                    keys.add(key)
                }
            }
            for (key in keys) {
                link.removeAttr(key)
            }
            link.attr("controls", "controls")
        }
    }

}
