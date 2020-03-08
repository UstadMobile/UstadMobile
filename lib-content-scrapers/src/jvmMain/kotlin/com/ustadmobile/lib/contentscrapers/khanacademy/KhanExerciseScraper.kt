package com.ustadmobile.lib.contentscrapers.khanacademy

import com.google.gson.GsonBuilder
import com.ustadmobile.core.contentformats.har.HarRegexPair
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.HarScraper
import com.ustadmobile.lib.contentscrapers.abztract.ScraperException
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.exerciseMidleUrl
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.exercisePostUrl
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.regexUrlPrefix
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.secondExerciseUrl
import com.ustadmobile.lib.contentscrapers.util.StringEntrySource
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.runBlocking
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.client.ClientUtil
import net.lightbody.bmp.proxy.CaptureType
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

class KhanExerciseScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long, sqiUid: Int) : HarScraper(containerDir, db, contentEntryUid, sqiUid) {

    override fun scrapeUrl(sourceUrl: String) {

        var entry: ContentEntry? = null
        runBlocking {
            entry = contentEntryDao.findByUidAsync(contentEntryUid)
        }

        if (entry == null) {
            hideContentEntry()
            setScrapeDone(false, ERROR_TYPE_NO_SOURCE_URL_FOUND)
            throw ScraperException(ERROR_TYPE_NO_SOURCE_URL_FOUND, "Content Entry was not found for url $sourceUrl")
        }

        var lang = sourceUrl.substringBefore(".khan").substringAfter("://")
        if(lang == "wwww"){
            lang = "en"
        }

        val url = URL(sourceUrl)


        val jsonContent = getJsonContent(url)

        val gson = GsonBuilder().disableHtmlEscaping().create()

        var data: SubjectListResponse? = gson.fromJson(jsonContent, SubjectListResponse::class.java)
        if (data!!.componentProps == null) {
            data = gson.fromJson(jsonContent, PropsSubjectResponse::class.java).props
        }

        val compProps = data!!.componentProps
        val navData = compProps!!.tutorialNavData ?: compProps.tutorialPageData

        var contentList: MutableList<SubjectListResponse.ComponentData.NavData.ContentModel>? = navData!!.contentModels
        if (contentList == null || contentList.isEmpty()) {
            contentList = mutableListOf()
            contentList.add(navData.contentModel!!)
        }


        var nodeSlug: String? = null
        var exerciseId: String? = "0"
        var exerciseList: List<SubjectListResponse.ComponentData.Card.UserExercise.Model.AssessmentItem>? = null
        for (content in contentList) {

            if (sourceUrl.contains(content.nodeSlug!!)) {

                exerciseList = content.allAssessmentItems
                nodeSlug = content.nodeSlug
                exerciseId = content.id
                val dateModified = ContentScraperUtil.parseServerDate(content.dateModified
                        ?: content.creationDate!!)

                val recentContainer = containerDao.getMostRecentContainerForContentEntry(contentEntryUid)

                val isContentUpdated = if (recentContainer == null) true else {
                    dateModified > recentContainer.cntLastModified
                }

                if (!isContentUpdated) {
                    showContentEntry()
                    setScrapeDone(true, 0)
                    return
                }

            }

        }

        var realPractice = loginKhanAcademy(sourceUrl, lang)
        val scraperResult = startHarScrape(sourceUrl, {

            it.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div.perseus-renderer div")))

        }, filters = listOf { entry ->

            if (entry.request.url == sourceUrl) {

                val doc = Jsoup.parse(entry.response.content.text)
                doc.head().append(KhanConstants.KHAN_CSS)
                doc.head().append(KhanConstants.KHAN_COOKIE)

                entry.response.content.text = doc.html()

            }
            entry
        }, regexes = listOf(HarRegexPair("&_=([^&]*)", ""),
                HarRegexPair("last_seen_problem_sha=(.*)&", ""),
                HarRegexPair("^https:\\/\\/([a-z\\-]+?)(.khanacademy.org\\/.*\\/attempt\\?)(.*)",
                        "https://www.khanacademy.org/attempt"),
                HarRegexPair("^https:\\/\\/([a-z\\-]+?)(.khanacademy.org\\/.*\\/hint\\?)(.*)",
                        "https://www.khanacademy.org/hint"))) {


            val entries = it.har.log.entries

            val sourceEntry = it.har.log.entries.find { url -> url.request.url == sourceUrl }
            if (sourceEntry != null) {
                it.har.log.entries.remove(sourceEntry)
                it.har.log.entries.add(0, sourceEntry)
            }


            val practiceEntry = it.har.log.entries.find { it.request.url.contains("/task/practice/") }
            if(practiceEntry != null){

                val practiceJson = gson.fromJson(realPractice, PracticeJson::class.java)
                val reservedList = practiceJson.taskJson?.reservedItems
                reservedList?.forEachIndexed {  index, item ->

                    val split = item.split("|")
                    val exercise = split[0]
                    val assessmentItem = split[1]

                    val practiceUrl = URL(url, secondExerciseUrl + exercise + exerciseMidleUrl + assessmentItem + exercisePostUrl + lang)

                    val problemUrl = URL(url, "/api/internal/user/exercises/comparing_whole_numbers/problems/${index+1}/assessment_item?lang=$lang")

                    val itemData = IOUtils.toString(practiceUrl, ScraperConstants.UTF_ENCODING)

                    val itemResponse = gson.fromJson(itemData, ItemResponse::class.java)

                    entries.add(addHarEntry(
                            itemData, mimeType = ScraperConstants.MIMETYPE_JSON,
                            requestUrl = problemUrl.toString()))

                    entries.add(addHarEntry(
                            itemData, mimeType = ScraperConstants.MIMETYPE_JSON,
                            requestUrl = practiceUrl.toString()))

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

                    for (imageValue in images.keys) {
                        var conn: HttpURLConnection? = null
                        try {
                            val image = imageValue.replace(ScraperConstants.EMPTY_SPACE.toRegex(), ScraperConstants.EMPTY_STRING)
                            var imageUrlString = image
                            if (image.contains(ScraperConstants.GRAPHIE)) {
                                imageUrlString = ScraperConstants.KHAN_GRAPHIE_PREFIX + image.substring(image.lastIndexOf("/") + 1) + ScraperConstants.SVG_EXT
                            }

                            val imageUrl = URL(imageUrlString)
                            conn = imageUrl.openConnection() as HttpURLConnection
                            conn.requestMethod = ScraperConstants.REQUEST_HEAD
                            val mimeType = conn.contentType
                            val length = conn.contentLength

                            val base64 = Base64.getEncoder().encodeToString(IOUtils.toByteArray(imageUrl))

                            entries.add(addHarEntry(
                                    base64, encoding = "base64",
                                    size = length,
                                    mimeType = mimeType,
                                    requestUrl = imageUrl.toString()))


                        } catch (e: MalformedURLException) {
                            UMLogUtil.logDebug(ExceptionUtils.getStackTrace(e))
                        } catch (e: Exception) {
                            UMLogUtil.logError(ExceptionUtils.getStackTrace(e))
                            UMLogUtil.logError("Error downloading an image for index log$imageValue with url $sourceUrl")
                        } finally {
                            conn?.disconnect()
                        }

                    }




                }


            }


            entries.add(addHarEntry(
                    IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.KHAN_CSS_LINK), ScraperConstants.UTF_ENCODING),
                    mimeType = ScraperConstants.MIMETYPE_CSS,
                    requestUrl = "https://www.khanacademy.org/khanscraper.css"))

            entries.add(addHarEntry(
                    IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.HINT_JSON_LINK), ScraperConstants.UTF_ENCODING),
                    mimeType = ScraperConstants.MIMETYPE_JSON,
                    requestUrl = "https://www.khanacademy.org/hint"))

            entries.add(addHarEntry(
                    IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.ATTEMPT_JSON_LINK), ScraperConstants.UTF_ENCODING),
                    mimeType = ScraperConstants.MIMETYPE_JSON,
                    requestUrl = "https://www.khanacademy.org/attempt"))

            entries.add(addHarEntry(
                    IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.GENWEB_C9E_LINK), ScraperConstants.UTF_ENCODING),
                    mimeType = ScraperConstants.MIMETYPE_JS,
                    requestUrl = "https://cdn.kastatic.org/genwebpack/prod/en/c55338d5bef2f8bf5dcdbf515448fef8.80da5ef39e9989febc9e.js"))

            entries.add(addHarEntry(
                    IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.GENWEB_184_LINK), ScraperConstants.UTF_ENCODING),
                    mimeType = ScraperConstants.MIMETYPE_JS,
                    requestUrl = "https://cdn.kastatic.org/genwebpack/prod/pl/c55338d5bef2f8bf5dcdbf515448fef8.76a6cc3bf717e4197184.js"))

            entries.add(addHarEntry(
                    IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.CORRECT_KHAN_LINK), ScraperConstants.UTF_ENCODING),
                    mimeType = ScraperConstants.MIMETYPE_SVG,
                    requestUrl = "https://cdn.kastatic.org/images/exercise-correct.svg"))

            entries.add(addHarEntry(
                    IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.TRY_AGAIN_KHAN_LINK), ScraperConstants.UTF_ENCODING),
                    mimeType = ScraperConstants.MIMETYPE_SVG,
                    requestUrl = "https://cdn.kastatic.org/images/exercise-try-again.svg"))

            entries.add(addHarEntry(
                    IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.ATTEMPT_KHAN_LINK), ScraperConstants.UTF_ENCODING),
                    mimeType = ScraperConstants.MIMETYPE_SVG,
                    requestUrl = "https://cdn.kastatic.org/images/end-of-task-card/star-attempted.svg"))

            entries.add(addHarEntry(
                    IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.COMPLETE_KHAN_LINK), ScraperConstants.UTF_ENCODING),
                    mimeType = ScraperConstants.MIMETYPE_SVG,
                    requestUrl = "https://cdn.kastatic.org/images/end-of-task-card/star-complete.svg"))

            true
        }


        val linksMap = HashMap<String, String>()
        val navList = navData.navItems
        if (navList != null) {

            for (navItem in navList) {
                if (navItem.nodeSlug == nodeSlug) {
                    continue
                }
                linksMap[regexUrlPrefix + navItem.nodeSlug!!] = KhanContentScraper.CONTENT_DETAIL_SOURCE_URL_KHAN_ID + navItem.id!!
            }

        }

        runBlocking {
            scraperResult.containerManager?.addEntries(StringEntrySource(gson.toJson(linksMap).toString(), listOf("linksMap")))
        }


        setScrapeDone(true, 0)
        close()
    }

    fun loginKhanAcademy(sourceUrl: String, lang: String): String? {

        var proxy = BrowserMobProxyServer()
        proxy.start()
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_HEADERS,
                CaptureType.RESPONSE_CONTENT,
                CaptureType.RESPONSE_HEADERS,
                CaptureType.RESPONSE_BINARY_CONTENT)

        val seleniumProxy = ClientUtil.createSeleniumProxy(proxy)
        seleniumProxy.noProxy = "<-loopback>"

        val options = ChromeOptions()
        options.setCapability(CapabilityType.PROXY, seleniumProxy)
        options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true)
        val chromeDriver = ChromeDriver(options)

        chromeDriver.get(ScraperConstants.KHAN_LOGIN_LINK)
        val waitDriver = WebDriverWait(chromeDriver, ScraperConstants.TIME_OUT_SELENIUM.toLong())
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
        waitDriver.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#login-signup-root")))

        chromeDriver.findElement(By.cssSelector("div#login-signup-root input[data-test-id=identifier]")).sendKeys(ScraperConstants.KHAN_USERNAME)
        chromeDriver.findElement(By.cssSelector("div#login-signup-root input[data-test-id=password]")).sendKeys(ScraperConstants.KHAN_PASS)

        val elements = chromeDriver.findElements(By.cssSelector("div#login-signup-root button div"))
        for (element in elements) {
            if (element.text.equals("Zaloguj się")) {
                element.click()
                break
            }
        }

        waitDriver.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.user-info-container")))

        proxy.newHar("Login")

        chromeDriver.get(sourceUrl)

        waitForJSandJQueryToLoad(waitDriver)
        waitDriver.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.perseus-renderer div")))

        var entry = proxy.har.log.entries.find { it.request.url.contains("/task/practice/") }

        proxy.stop()

        return entry?.response?.content?.text
    }


}
