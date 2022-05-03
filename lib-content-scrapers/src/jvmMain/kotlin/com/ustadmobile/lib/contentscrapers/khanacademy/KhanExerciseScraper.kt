package com.ustadmobile.lib.contentscrapers.khanacademy

import com.google.gson.GsonBuilder
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentformats.har.HarExtra
import com.ustadmobile.core.contentformats.har.HarInterceptor.Companion.KHAN_PROBLEM
import com.ustadmobile.core.contentformats.har.HarRegexPair
import com.ustadmobile.core.contentformats.har.Interceptors
import com.ustadmobile.core.io.ext.addFileToContainer
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_HAR
import com.ustadmobile.lib.contentscrapers.ScraperConstants.POST_METHOD
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.abztract.HarScraper
import com.ustadmobile.lib.contentscrapers.abztract.ScraperException
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.exerciseMidleUrl
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.exercisePostUrl
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.regexUrlPrefix
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanConstants.secondExerciseUrl
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin
import kotlinx.coroutines.runBlocking
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.client.ClientUtil
import net.lightbody.bmp.core.har.HarNameValuePair
import net.lightbody.bmp.proxy.CaptureType
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.kodein.di.DI
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.time.Duration
import java.util.*
import java.util.regex.Pattern


class KhanExerciseScraper(contentEntryUid: Long, sqiUid: Int, parentContentEntryUid: Long, endpoint: Endpoint, override val di: DI) : HarScraper(contentEntryUid, sqiUid, parentContentEntryUid, endpoint, di) {

    override fun scrapeUrl(sourceUrl: String) {

        var entry: ContentEntry? = null
        runBlocking {
            entry = db.contentEntryDao.findByUidAsync(contentEntryUid)
        }

        if (entry == null) {
            close()
            hideContentEntry()
            setScrapeDone(false, ERROR_TYPE_ENTRY_NOT_CREATED)
            throw ScraperException(ERROR_TYPE_ENTRY_NOT_CREATED, "Content Entry was not found for url $sourceUrl")
        }

        var lang = sourceUrl.substringBefore(".khan").substringAfter("://")
        if (lang == "www") {
            lang = "en"
        }

        val url = URL(sourceUrl)

        val jsonContent = getJsonContent(url)

        val gson = GsonBuilder().disableHtmlEscaping().serializeNulls().create()

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

        val content = contentList.find { sourceUrl.contains(it.nodeSlug!!) }

        if (content == null) {
            close()
            hideContentEntry()
            setScrapeDone(false, ERROR_TYPE_CONTENT_NOT_FOUND)
            throw ScraperException(ERROR_TYPE_CONTENT_NOT_FOUND, "no content was found in url : $sourceUrl")
        }

        val nodeSlug = content.nodeSlug
        val slug = content.slug

        val dateModified = ContentScraperUtil.parseServerDate(content.dateModified
                ?: content.creationDate!!)

        val recentContainer = db.containerDao.getMostRecentContainerForContentEntry(contentEntryUid)

        val isContentUpdated = if (recentContainer == null) true else {
            recentContainer.mimeType != MIMETYPE_HAR && dateModified > recentContainer.cntLastModified
        }

        val sourceId = entry!!.sourceUrl!!
        val commonSourceUrl = "%${sourceId.substringBefore(".")}%"
        val commonEntryList = db.contentEntryDao.findSimilarIdEntryForKhan(commonSourceUrl)
        commonEntryList.forEach {

            if (it.sourceUrl == sourceId) {
                return@forEach
            }
            ContentScraperUtil.insertOrUpdateRelatedContentJoin(db.contentEntryRelatedEntryJoinDao, it, entry!!,
                    ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION)
        }

        if (!isContentUpdated) {
            close()
            showContentEntry()
            setScrapeDone(true, 0)
            return
        }

        val harExtra = HarExtra()
        harExtra.regexes = listOf(
                HarRegexPair("&_=([^&]*)", ""),
                HarRegexPair("last_seen_problem_sha=(.*)&", ""),
                HarRegexPair("^https:\\/\\/([a-z\\-]+?)(.khanacademy.org\\/.*\\/attempt\\?)(.*)",
                        "https://www.khanacademy.org/attempt"),
                HarRegexPair("^https:\\/\\/([a-z\\-]+?)(.khanacademy.org\\/.*\\/getAssessmentItem\\?)(.*)",
                        "https://www.khanacademy.org/getAssessmentItem"),
                HarRegexPair("^https:\\/\\/([a-z\\-]+?)(.khanacademy.org\\/.*\\/attemptProblem\\?)(.*)",
                        "https://www.khanacademy.org/attemptProblem"),
                HarRegexPair("^https:\\/\\/([a-z\\-]+?)(.khanacademy.org\\/.*\\/Take-a-hint\\?)(.*)",
                        "https://www.khanacademy.org/take-a-hint"),
                HarRegexPair("^https:\\/\\/([a-z\\-]+?)(.khanacademy.org\\/.*\\/hint\\?)(.*)",
                        "https://www.khanacademy.org/hint"))


        val scraperResult: HarScraperResult
        try {
            val realPractice = loginKhanAcademy(sourceUrl, lang)
            scraperResult = startHarScrape(sourceUrl, {

                it.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("div.perseus-renderer div")))

                try {
                    chromeDriver.findElement(By.cssSelector("div[class*=checkbox-and-option]")).click()
                } catch (e: Exception) {
                    // ignore
                }

                try {
                    chromeDriver.findElements(By.cssSelector("input"))?.forEach { web ->
                        web.sendKeys("1")
                    }
                } catch (e: Exception) {
                    // ignore
                }


                try {
                    chromeDriver.findElements(By.cssSelector("div.widget-inline-block span.mq-root-block"))?.forEach { web ->
                        web.click()
                        web.sendKeys("1")
                    }
                } catch (e: Exception) {
                    // ignore
                }

                try {
                    chromeDriver.findElements(By.cssSelector("div.keypad-input"))?.forEach { web ->
                        web.click()
                        chromeDriver.findElement(By.cssSelector("div[role=button] span")).click()
                    }
                } catch (e: Exception) {
                    // ignore
                }

                try {
                    val rect = chromeDriver.findElements(By.cssSelector("div.perseus-widget-plotter span"))
                    rect.forEach { web ->
                        try {
                            web.click()
                        } catch (e: Exception) {

                        }

                    }
                } catch (e: Exception) {
                    // ignore
                    print(e)
                }

                try {
                    chromeDriver.findElement(By.cssSelector("div.graphie svg")).click()
                } catch (e: Exception) {
                    // ignore
                    print(e)
                }

                try {
                    val dropdownList = chromeDriver.findElements(By.cssSelector("button[aria-haspopup=listbox]"))
                    dropdownList.forEach { web ->
                        web.click()
                        chromeDriver.findElement(By.cssSelector("div[role=option][aria-selected=false]")).click()
                    }
                } catch (e: Exception) {
                    // ignore
                }



                try {
                    val graphie = chromeDriver.findElement(By.cssSelector("div.graphie div div ellipse"))
                    val actions = Actions(chromeDriver)
                    actions.dragAndDropBy(graphie, 10, 10).build().perform()
                } catch (e: Exception) {
                    // ignore
                }


                try {
                    val dragElements = chromeDriver.findElements(By.cssSelector("li[class*=perseus-sortable-draggable]"))
                    val actions = Actions(chromeDriver)
                    if (dragElements.size > 1) {
                        actions.dragAndDrop(dragElements[0], dragElements[1]).build().perform()
                    } else {
                        if (dragElements.isNotEmpty()) UMLogUtil.logError("did not find more than 1 element to drag for $sourceUrl")
                    }
                } catch (e: Exception) {
                    // ignore
                }

                try {
                    val dragElement = chromeDriver.findElement(By.cssSelector("div[class*=perseus-interactive] div[class*=perseus-interactive]"))
                    val dragBox = chromeDriver.findElement(By.cssSelector("div[class*=drag-hint]"))
                    val actions = Actions(chromeDriver)
                    actions.dragAndDrop(dragElement, dragBox).build().perform()
                } catch (e: Exception) {
                    // ignore
                }


                val checkAnswer = chromeDriver.findElement(By.cssSelector("button[data-test-id=exercise-check-answer]"))
                if (checkAnswer.isEnabled) {
                    checkAnswer.click()
                } else {
                    throw ScraperException(ERROR_TYPE_KHAN_QUESTION_SOLVER, "Cannot attempt this question")
                }

                try {
                    WebDriverWait(chromeDriver, Duration.ofSeconds(5)).until<WebElement>(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-test-id=exercise-feedback-popover-skip-link]"))).click()
                } catch (e: Exception) {
                    // ignore
                }

                try {
                    WebDriverWait(chromeDriver, Duration.ofSeconds(5)).until<WebElement>(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-test-id=exercise-next-question]"))).click()
                } catch (e: Exception) {
                    // ignore
                }


            }, filters = listOf { entry ->

                if (entry.request.url == sourceUrl) {

                    val doc = Jsoup.parse(entry.response.content.text)
                    doc.head().append(KhanConstants.KHAN_CSS)
                    doc.head().append(KhanConstants.KHAN_COOKIE)

                    entry.response.content.text = doc.html()

                } else if (entry.request.url.contains("gvt1.com/edgedl/")) {
                    entry.response = null
                }


                entry
            }, regexes = harExtra.regexes!!) {

                val entries = it.har.log.entries

                val sourceEntry = it.har.log.entries.find { url -> url.request.url == sourceUrl }
                if (sourceEntry != null) {
                    it.har.log.entries.remove(sourceEntry)
                    it.har.log.entries.add(0, sourceEntry)
                }

                val practiceJson = gson.fromJson(realPractice, PracticeJson::class.java)
                val practiceTask = gson.fromJson(realPractice, PracticeTask::class.java)
                val reservedList = practiceJson.taskJson?.reservedItems
                        ?: practiceTask.data?.getOrCreatePracticeTask?.result?.userTask?.task?.reservedItems

                if (reservedList.isNullOrEmpty()) {
                    close()
                    hideContentEntry()
                    setScrapeDone(false, ERROR_TYPE_MISSING_QUESTIONS)
                    throw ScraperException(ERROR_TYPE_MISSING_QUESTIONS, "no questions found for exercise")
                }

                // since we have the 7 questions, we should remove the default one as it conflicts with the others
                val problemList = it.har.log.entries.filter { problemEntry -> problemEntry.request.url.contains("getAssessmentItem") }
                problemList.forEach { pEntry ->
                    pEntry.response.content.text = null
                }

                reservedList.forEachIndexed { index, item ->

                    val split = item.split("|")
                    val exercise = split[0]
                    val assessmentItem = split[1]

                    val practiceUrl = URL(url, "$secondExerciseUrl$exercise$exerciseMidleUrl$assessmentItem$exercisePostUrl$lang")

                    val problemUrl = URL(url, "$secondExerciseUrl$slug/problems/${index + 1}$exercisePostUrl$lang")

                    val assessmentItemUrl = URL(url, "/getAssessmentItem")

                    val itemData = IOUtils.toString(practiceUrl, ScraperConstants.UTF_ENCODING)

                    val itemResponse = gson.fromJson(itemData, ItemResponse::class.java)

                    val assItem = Item()
                    val data = Item.Data()
                    val assignment = Item.Data.AssessmentItem()
                    val dataItem = Item.Data.AssessmentItem.ItemData()
                    dataItem.id = itemResponse.id
                    dataItem.itemData = itemResponse.itemData
                    dataItem.sha = itemResponse.sha
                    dataItem.problemType = itemResponse.problemType
                    dataItem.__typename = itemResponse.contentKind ?: itemResponse.kind
                    assignment.item = dataItem
                    assignment.__typename = "${itemResponse.contentKind
                            ?: itemResponse.kind}OrError"
                    data.assessmentItem = assignment
                    assItem.data = data

                    val assItemData = gson.toJson(assItem)

                    entries.add(addHarEntry(
                            itemData, mimeType = ScraperConstants.MIMETYPE_JSON,
                            requestUrl = problemUrl.toString()))

                    entries.add(addHarEntry(
                            itemData, mimeType = ScraperConstants.MIMETYPE_JSON,
                            requestUrl = practiceUrl.toString()))

                    val problemEntry = addHarEntry(
                            assItemData, mimeType = ScraperConstants.MIMETYPE_JSON,
                            requestMethod = "POST",
                            requestUrl = assessmentItemUrl.toString())
                    problemEntry.request.headers.add(0, HarNameValuePair("itemId", assessmentItem))
                    entries.add(problemEntry)

                    val linkPattern = Pattern.compile("(https://|web\\+graphie://)([^\")]*)")
                    val matcher = linkPattern.matcher(itemResponse.itemData)

                    val imageList = mutableSetOf<String>()
                    while (matcher.find()) {
                        imageList.add(matcher.group())
                    }

                    for (imageValue in imageList) {
                        var conn: HttpURLConnection? = null
                        try {
                            val image = imageValue.replace(ScraperConstants.EMPTY_SPACE.toRegex(), "")
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


                entries.add(addHarEntry(
                        IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.KHAN_CSS_LINK), ScraperConstants.UTF_ENCODING),
                        mimeType = ScraperConstants.MIMETYPE_CSS,
                        requestUrl = "https://www.khanacademy.org/khanscraper.css"))

                entries.add(addHarEntry(
                        IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.KHAN_TAKE_HINT_LINK), ScraperConstants.UTF_ENCODING),
                        mimeType = ScraperConstants.MIMETYPE_TEXT,
                        requestUrl = "https://www.khanacademy.org/take-a-hint",
                        requestMethod = POST_METHOD))

                entries.add(addHarEntry(
                        IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.ATTEMPT_JSON_LINK), ScraperConstants.UTF_ENCODING),
                        mimeType = ScraperConstants.MIMETYPE_JSON,
                        requestUrl = "https://www.khanacademy.org/attempt",
                        requestMethod = POST_METHOD))


                val regex = Regex("num_problems_\\d")
                var problemText = IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.ATTEMPT_PROBLEM_JSON_LINK), ScraperConstants.UTF_ENCODING)
                problemText = problemText.replace(regex, "num_problems_${reservedList.size}")

                entries.add(addHarEntry(
                        problemText,
                        mimeType = ScraperConstants.MIMETYPE_JSON,
                        requestUrl = "https://www.khanacademy.org/attemptProblem",
                        requestMethod = POST_METHOD))

                entries.add(addHarEntry(
                        IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.HINT_JSON_LINK), ScraperConstants.UTF_ENCODING),
                        mimeType = ScraperConstants.MIMETYPE_JSON,
                        requestUrl = "https://www.khanacademy.org/hint",
                        requestMethod = POST_METHOD))

                val fileList = KhanConstants.fileMap[lang]

                fileList?.forEach { file ->

                    entries.add(addHarEntry(
                            IOUtils.toString(javaClass.getResourceAsStream(file.fileLocation), ScraperConstants.UTF_ENCODING),
                            mimeType = file.mimeType,
                            requestUrl = file.url))
                }

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

                entries.add(addHarEntry(
                        IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.END_OF_TASK_AUDIO), ScraperConstants.UTF_ENCODING),
                        mimeType = ScraperConstants.MIMETYPE_OGG,
                        requestUrl = "https://cdn.kastatic.org/sounds/end-of-task.ogg"))

                entries.add(addHarEntry(
                        IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.LATO_LATIN_REGULAR_WOFF), ScraperConstants.UTF_ENCODING),
                        mimeType = ScraperConstants.MIMETYPE_WOFF2,
                        requestUrl = "https://cdn.kastatic.org/fonts/LatoLatin-Regular.woff2"))

                entries.add(addHarEntry(
                        IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.LATO_LATIN_BOLD_WOFF), ScraperConstants.UTF_ENCODING),
                        mimeType = ScraperConstants.MIMETYPE_WOFF2,
                        requestUrl = "https://cdn.kastatic.org/fonts/LatoLatin-Bold.woff2"))

                entries.add(addHarEntry(
                        IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.LATO_LATIN_ITALITC_WOFF), ScraperConstants.UTF_ENCODING),
                        mimeType = ScraperConstants.MIMETYPE_WOFF2,
                        requestUrl = "https://cdn.kastatic.org/fonts/LatoLatin-Italic.woff2"))

                entries.add(addHarEntry(
                        IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.NOTO_REGULAR_WOFF), ScraperConstants.UTF_ENCODING),
                        mimeType = ScraperConstants.MIMETYPE_WOFF2,
                        requestUrl = "https://cdn.kastatic.org/fonts/armenian/NotoSansArmenian-Regular.woff2"))

                entries.add(addHarEntry(
                        IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.NOTO_BOLD_WOFF), ScraperConstants.UTF_ENCODING),
                        mimeType = ScraperConstants.MIMETYPE_WOFF2,
                        requestUrl = "https://cdn.kastatic.org/fonts/armenian/NotoSansArmenian-Bold.woff2"))

                entries.add(addHarEntry(
                        IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.MATH_JAX_4_REG_WOFF), ScraperConstants.UTF_ENCODING),
                        mimeType = ScraperConstants.MIMETYPE_WOFF2,
                        requestUrl = "https://cdn.kastatic.org/third_party/javascript-khansrc/khan-mathjax/2.1/fonts/HTML-CSS/TeX/woff/MathJax_Size4-Regular.woff"))

                entries.add(addHarEntry(
                        IOUtils.toString(javaClass.getResourceAsStream(ScraperConstants.MATH_JAX_4_REG_OTF), ScraperConstants.UTF_ENCODING),
                        mimeType = ScraperConstants.MIMETYPE_OTF,
                        requestUrl = "https://cdn.kastatic.org/third_party/javascript-khansrc/khan-mathjax/2.1/fonts/HTML-CSS/TeX/otf/MathJax_Size4-Regular.otf"))


                true
            }
        } catch (e: ScraperException) {
            throw e
        } catch (e: Exception) {
            close()
            hideContentEntry()
            setScrapeDone(false, 0)
            throw ScraperException(0, e.message)
        }
        val linksList = mutableListOf<HarRegexPair>()
        val navList = navData.navItems
        if (navList != null) {

            for (navItem in navList) {
                if (navItem.nodeSlug == nodeSlug) {
                    continue
                }
                linksList.add(HarRegexPair(regexUrlPrefix + navItem.nodeSlug!!, ScraperConstants.CONTENT_DETAIL_SOURCE_URL_KHAN_ID + navItem.id!!))
            }
        }
        harExtra.links = linksList
        harExtra.interceptors = listOf(Interceptors(KHAN_PROBLEM, ""))

        runBlocking {

            val harExtraFile = File.createTempFile("harextras", "json")
            val contentInputStream = gson.toJson(harExtra).byteInputStream()
            contentInputStream.writeToFile(harExtraFile)
            val containerAddOptions = ContainerAddOptions(storageDirUri = containerFolder.toDoorUri())
            repo.addFileToContainer(scraperResult.containerUid, harExtraFile.toDoorUri(),
                    harExtraFile.name, Any(), di, containerAddOptions)
            harExtraFile.delete()
        }

        showContentEntry()
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

        chromeDriver.get("https://$lang.khanacademy.org/login")
        val waitDriver = WebDriverWait(chromeDriver, TIME_OUT_SELENIUM)
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)
        waitDriver.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#login-signup-root")))

        chromeDriver.findElement(By.cssSelector("div#login-signup-root input[data-test-id=identifier]")).sendKeys(ScraperConstants.KHAN_USERNAME)
        chromeDriver.findElement(By.cssSelector("div#login-signup-root input[data-test-id=password]")).sendKeys(ScraperConstants.KHAN_PASS)

        val elements = chromeDriver.findElements(By.cssSelector("div#login-signup-root button div"))
        for (element in elements) {
            if (element.text == KhanConstants.loginLangMap[lang]) {
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

        val entry = proxy.har.log.entries.find { it.request.url.contains("/task/practice/") || it.request.url.contains("/getOrCreatePracticeTask") }

        chromeDriver.quit()
        proxy.stop()

        if (entry?.response?.content?.text.isNullOrEmpty()) {
            close()
            hideContentEntry()
            setScrapeDone(false, ERROR_TYPE_PRACTICE_CONTENT_NOT_FOUND)
            throw ScraperException(ERROR_TYPE_CONTENT_NOT_FOUND, "no practice found for $sourceUrl")
        }

        return entry?.response?.content?.text
    }


}
