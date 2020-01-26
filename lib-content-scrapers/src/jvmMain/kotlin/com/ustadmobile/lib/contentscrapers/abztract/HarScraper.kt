package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.util.HarEntrySource
import com.ustadmobile.lib.contentscrapers.util.StringEntrySource
import com.ustadmobile.lib.db.entities.Container
import io.github.bonigarcia.wdm.WebDriverManager
import kotlinx.coroutines.runBlocking
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.client.ClientUtil
import net.lightbody.bmp.core.har.HarEntry
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
import java.net.URLDecoder


typealias ScrapeFilterFn = (harEntry: HarEntry) -> HarEntry

typealias WaitConditionFn = (waitCondition: WebDriverWait) -> Unit


abstract class HarScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long) : Scraper(containerDir, db, contentEntryUid) {

    protected var chromeDriver: ChromeDriver
    protected var proxy: BrowserMobProxyServer = BrowserMobProxyServer()
    val regex = "[^a-zA-Z0-9\\.\\-]".toRegex()

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
                       regexes: List<Regex> = listOf(),
                       addHarContent: Boolean = true,
                       block: (proxy: BrowserMobProxyServer) -> Boolean): HarScraperResult {

        ContentScraperUtil.clearChromeConsoleLog(chromeDriver)
        proxy.newHar("Scraper")

        try {
            chromeDriver.navigate().to(url)
        } catch (e: InvalidArgumentException) {
            throw IllegalArgumentException(e)
        }

        val waitDriver = WebDriverWait(chromeDriver, ScraperConstants.TIME_OUT_SELENIUM.toLong())
        waitForJSandJQueryToLoad(waitDriver)
        waitCondition?.invoke(waitDriver)

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


    private fun makeHarContainer(proxy: BrowserMobProxyServer, entries: MutableList<HarEntry>, filters: List<ScrapeFilterFn>, regexes: List<Regex>, addHarContent: Boolean): ContainerManager {

        var containerManager = ContainerManager(createBaseContainer(ScraperConstants.MIMETYPE_HAR), db, db, containerDir.absolutePath)

        entries.forEach {

            try {

                val request = it.request

                if (request.url.contains("accounts.google.com")) {
                    entries.remove(it)
                    return@forEach
                }

                val decodedPath = URLDecoder.decode(request.url, ScraperConstants.UTF_ENCODING)
                val decodedUrl = URL(decodedPath)
                var containerPath = decodedUrl.toString().replace(regex, "_")

                // to remove timestamps from queries
                var regexedString = decodedUrl.toString()
                regexes.forEach { itRegex ->
                    regexedString = regexedString.replace(itRegex, "")
                }

                filters.forEach { filterFn ->
                    filterFn.invoke(it)
                }

                if (it.response == null) {
                    return@forEach
                }

                runBlocking {
                    containerManager.addEntries(HarEntrySource(it, listOf(containerPath)))
                }

                it.response.content.text = containerPath
                it.request.url = regexedString

            } catch (e: Exception) {
                UMLogUtil.logError("Index url failed at ${it.request.url}")
                UMLogUtil.logDebug(e.message!!)

            }
        }

        var writer = StringWriter()
        proxy.har.writeTo(writer)

        if (addHarContent) {
            runBlocking {
                containerManager.addEntries(StringEntrySource(writer.toString(), listOf("harcontent")))
                containerManager.addEntries(StringEntrySource(regexes.joinToString(), listOf("regexList")))
            }
        }

        return containerManager
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
        proxy.stop()
    }


}