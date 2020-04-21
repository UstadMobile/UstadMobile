package com.ustadmobile.lib.contentscrapers.abztract

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.contentformats.har.HarRegexPair
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_JS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.util.HarEntrySource
import com.ustadmobile.lib.contentscrapers.util.StringEntrySource
import com.ustadmobile.lib.db.entities.Container
import io.github.bonigarcia.wdm.WebDriverManager
import io.ktor.client.request.request
import kotlinx.coroutines.runBlocking
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.client.ClientUtil
import net.lightbody.bmp.core.har.HarEntry
import net.lightbody.bmp.core.har.HarRequest
import net.lightbody.bmp.core.har.HarResponse
import net.lightbody.bmp.proxy.CaptureType
import org.apache.http.client.utils.DateUtils
import org.openqa.selenium.InvalidArgumentException
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.io.StringWriter
import java.net.URL
import java.util.concurrent.TimeUnit


typealias ScrapeFilterFn = (harEntry: HarEntry) -> HarEntry

typealias WaitConditionFn = (waitCondition: WebDriverWait) -> Unit


abstract class HarScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long, sqiUid: Int) : Scraper(containerDir, db, contentEntryUid, sqiUid) {

    protected var chromeDriver: ChromeDriver
    var proxy: BrowserMobProxyServer = BrowserMobProxyServer()
    private val cleanUpRegex = "[^a-zA-Z0-9\\.\\-]".toRegex()
    val gson: Gson

    private val offlineRegex = Regex("window.addEventListener\\(\"(online|offline)\"")

    private val offlineReplacement = "window.addEventListener(\\\"offlineDisabled\\\""

    data class HarScraperResult(val updated: Boolean, val containerManager: ContainerManager?)


    init {
        WebDriverManager.chromedriver().setup()
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
        chromeDriver = ChromeDriver(options)

        gson = GsonBuilder().disableHtmlEscaping().create()
    }


    /**
     * url - Starting url for chrome
     * waitCondition - conditions to wait for so the page is fully loaded
     * filters - filters every request and removes if not needed
     * regexes - regexes to apply to the request (eq removing timestamp from query)
     * addHarContent - option to save the har content after saving all the files in the container - default true
     * block - check here if the content to download is updated or additional url links are required to load in the har before creating the container manager
     */
    fun startHarScrape(url: String, waitCondition: WaitConditionFn? = null,
                       filters: List<ScrapeFilterFn> = listOf(),
                       regexes: List<HarRegexPair> = listOf(),
                       addHarContent: Boolean = true,
                       block: (proxy: BrowserMobProxyServer) -> Boolean): HarScraperResult {

        proxy.newHar("Scraper")

        try {
            chromeDriver.navigate().to(url)
        } catch (e: InvalidArgumentException) {
            throw IllegalArgumentException(e)
        }

        val waitDriver = WebDriverWait(chromeDriver, ScraperConstants.TIME_OUT_SELENIUM.toLong())
        waitForJSandJQueryToLoad(waitDriver)
        waitCondition?.invoke(waitDriver)
        proxy.waitForQuiescence(30, 3, TimeUnit.SECONDS)


        checkStartingUrlNot404(proxy.har.log.entries, url)

        val isContentUpdated = block.invoke(proxy)

        val containerManager = if (isContentUpdated) {
            makeHarContainer(proxy, proxy.har.log.entries, filters, regexes, addHarContent)
        } else {
            null
        }

        return HarScraperResult(isContentUpdated, containerManager)
    }

    private fun checkStartingUrlNot404(entries: List<HarEntry>, url: String) {
        entries.find { it.request.url == url }.also {
            require(it?.response?.status != 404) {
                "404 Starting Url"
            }
        }
    }


    private fun makeHarContainer(proxy: BrowserMobProxyServer, entries: MutableList<HarEntry>, filters: List<ScrapeFilterFn>, regexes: List<HarRegexPair>, addHarContent: Boolean): ContainerManager {

        val containerManager = ContainerManager(createBaseContainer(ScraperConstants.MIMETYPE_HAR), db, db, containerDir.absolutePath)

        entries.forEachIndexed { counter, it ->

            try {

                val request = it.request

                if (request.url.contains("accounts.google.com")) {
                    entries.remove(it)
                    return@forEachIndexed
                }

                val decodedUrl = URL(request.url)
                var containerPath = request.url

                // to remove timestamps from queries
                var regexedString = decodedUrl.toString()
                regexes.forEach { itRegex ->
                    regexedString = regexedString.replace(Regex(itRegex.regex), itRegex.replacement)
                }

                filters.forEach { filterFn ->
                    filterFn.invoke(it)
                }
                if(it.response.content.mimeType == MIMETYPE_JS){
                    it.response.content.text = it.response.content.text.replace(offlineRegex, offlineReplacement)
                }

                if (it.response == null) {
                    return@forEachIndexed
                }

                runBlocking {

                    if(containerManager.getEntry(containerPath) == null) {
                        containerPath += counter
                    }
                    containerManager.addEntries(HarEntrySource(it, listOf(containerPath)))
                }

                it.response.content.text = containerPath
                it.request.url = regexedString

            } catch (e: Exception) {
                UMLogUtil.logError("Index url failed at ${it.request.url}")
                UMLogUtil.logDebug(e.message!!)

            }
        }

        val writer = StringWriter()
        proxy.har.writeTo(writer)

        if (addHarContent) {
            runBlocking {
                containerManager.addEntries(StringEntrySource(writer.toString(), listOf("harcontent")))
            }
        }

        return containerManager
    }

    fun addHarEntry(content: String, size: Int = content.length, encoding: String = UTF_ENCODING, mimeType: String, requestUrl: String, requestMethod: String = "GET"): HarEntry {

        val entry = HarEntry()
        entry.response = HarResponse()
        entry.response.content.text = content
        entry.response.content.size = size.toLong()
        entry.response.content.encoding = encoding
        entry.response.content.mimeType = mimeType

        entry.request = HarRequest()
        entry.request.method = requestMethod
        entry.request.url = requestUrl

        return entry

    }

    fun isContentUpdated(harEntry: HarEntry, container: Container): Boolean {
        val entryModified = harEntry.response.headers.find { valuePair -> valuePair.name == LAST_MODIFIED }
        val entryETag = harEntry.response.headers.find { valuePair -> valuePair.name == ETAG }

        if (entryModified != null) {
            val time = DateUtils.parseDate(entryModified.value).time
            return time > container.cntLastModified
        }

        if (entryETag != null) {
            val eTagValue = entryETag.value
            val eTag = db.containerETagDao.getEtagOfContainer(container.containerUid)
            return eTagValue != eTag
        }
        return true
    }

    /**
     * Once Selenium is setup and you load a page, use this method to wait for the page to load completely
     *
     * @param waitDriver driver used to wait for conditions on webpage
     * @return true once wait is complete
     */
    fun waitForJSandJQueryToLoad(waitDriver: WebDriverWait): Boolean {

        // wait for jQuery to load
        val jQueryLoad = ExpectedCondition { driver ->
            try {
                (driver as JavascriptExecutor).executeScript("return jQuery.active") as Long == 0L
            } catch (e: Exception) {
                // no jQuery present
                true
            }
        }

        // wait for Javascript to load
        val jsLoad = ExpectedCondition { driver ->
            (driver as JavascriptExecutor).executeScript("return document.readyState")
                    .toString() == "complete"
        }

        return waitDriver.until(jQueryLoad) && waitDriver.until(jsLoad)
    }

    override fun close() {
        chromeDriver.quit()
        if(!proxy.isStopped){
            proxy.stop()
        }
    }


}