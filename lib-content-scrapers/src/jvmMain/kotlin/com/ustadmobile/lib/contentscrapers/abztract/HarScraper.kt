package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.CHROME_PATH_KEY
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import com.ustadmobile.lib.contentscrapers.util.HarEntrySource
import com.ustadmobile.lib.contentscrapers.util.StringEntrySource
import kotlinx.coroutines.runBlocking
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.client.ClientUtil
import net.lightbody.bmp.core.har.HarEntry
import net.lightbody.bmp.proxy.CaptureType
import org.openqa.selenium.InvalidArgumentException
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import org.openqa.selenium.chrome.ChromeOptions
import java.io.StringWriter
import java.lang.IllegalArgumentException
import java.net.URL
import java.net.URLDecoder


typealias ScrapeFilterFn = (harEntry: HarEntry) -> HarEntry

typealias WaitConditionFn = (waitCondition: WebDriverWait) -> Unit


abstract class HarScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long) : Scraper(containerDir, db, contentEntryUid) {

    var chromeDriver: ChromeDriver
    var proxy: BrowserMobProxyServer = BrowserMobProxyServer()
    val regex = "[^a-zA-Z0-9\\.\\-]".toRegex()

    init {
        System.setProperty("chromedriver", System.getProperty(CHROME_PATH_KEY))
        proxy.start()
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_HEADERS,
                CaptureType.RESPONSE_CONTENT,
                CaptureType.RESPONSE_HEADERS,
                CaptureType.RESPONSE_BINARY_CONTENT)

        var seleniumProxy = ClientUtil.createSeleniumProxy(proxy)
        seleniumProxy.noProxy = "<-loopback>"
        val options = ChromeOptions()
        options.setCapability(CapabilityType.PROXY, seleniumProxy)
        chromeDriver = ChromeDriver(options)
    }

    fun startHarScrape(url: String, waitCondition: WaitConditionFn? = null,
                       filters: List<ScrapeFilterFn> = listOf(),
                       regexes: List<Regex> = listOf(),
                       block: (proxy: BrowserMobProxyServer) -> Boolean): ContainerManager? {

        clearAnyLogsInChrome()
        proxy.newHar("Scraper")


        try {
            chromeDriver.get(url)
        } catch (e: InvalidArgumentException) {
            throw IllegalArgumentException(e)
        }

        val waitDriver = WebDriverWait(chromeDriver, ScraperConstants.TIME_OUT_SELENIUM.toLong())
        waitForJSandJQueryToLoad(waitDriver)
        waitCondition?.invoke(waitDriver)

        var entries = proxy.har.log.entries

        checkStartingUrlNot404(entries, url)

        block.invoke(proxy)

        return makeHarContainer(proxy, entries, filters, regexes)
    }


    private fun clearAnyLogsInChrome() {
        val js = chromeDriver as JavascriptExecutor
        js.executeScript("console.clear()")
    }

    private fun checkStartingUrlNot404(entries: List<HarEntry>, url: String) {
        entries.find { it.request.url == url }.also {
            require(it?.response?.status != 404) {
                "404 Starting Url"
            }
        }
    }


    private fun makeHarContainer(proxy: BrowserMobProxyServer, entries: MutableList<HarEntry>, filters: List<ScrapeFilterFn>, regexes: List<Regex>): ContainerManager {

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

                var regexedString = decodedUrl.toString()
                regexes.forEach { itRegex ->
                    regexedString = regexedString.replace(itRegex, "")
                }

                filters.forEach { filterFn ->
                    filterFn.invoke(it)
                }

                runBlocking {
                    containerManager.addEntries(HarEntrySource(it, containerPath))
                }

                it.response.content.text = containerPath
                it.request.url = regexedString

            } catch (e: Exception) {
                UMLogUtil.logError("Index url failed at${it.request.url}")
                UMLogUtil.logDebug(e.message!!)

            }
        }

        var writer = StringWriter()
        proxy.har.writeTo(writer)

        runBlocking {
            containerManager.addEntries(StringEntrySource(writer.toString(), "harcontent"))
        }

        return containerManager
    }

    /**
     * Once Selenium is setup and you load a page, use this method to wait for the page to load completely
     *
     * @param waitDriver driver used to wait for conditions on webpage
     * @return true once wait is complete
     */
    private fun waitForJSandJQueryToLoad(waitDriver: WebDriverWait): Boolean {

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