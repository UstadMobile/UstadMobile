package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.core.har.HarEntry
import org.apache.commons.io.FileUtils
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.Proxy
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.net.URL
import java.net.URLDecoder
import java.util.*

typealias ScrapeFilterFn = (harEntry: HarEntry) -> HarEntry

typealias WaitConditionFn = (waitCondition: WebDriverWait) -> Unit


abstract class WebChunkScraper : Scraper {

    var chromeDriver: ChromeDriver

    constructor(chromeDriver: ChromeDriver, containerManager: ContainerManager) : super(containerManager) {
        this.chromeDriver = chromeDriver

    }

    fun startHarScrape(url: String, waitCondition :WaitConditionFn? = null): BrowserMobProxyServer {
        /* clearAnyLogsInChrome()
        chromeDriver.get(url)
        val waitDriver = WebDriverWait(chromeDriver, ScraperConstants.TIME_OUT_SELENIUM.toLong())
        waitForJSandJQueryToLoad(waitDriver)
        waitCondition?.invoke(waitDriver)*/
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


    fun makeHarContainer(proxy: BrowserMobProxyServer, filter: ScrapeFilterFn? = null): ContainerManager {

        /*var entries = proxy.har.log.entries

        entries.forEach {

            try {

                val request = it.request
                val response = it.response

                if (request.url.contains("accounts.google.com")) {
                    entries.remove(it)
                    return@forEach
                }

                val decodedPath = URLDecoder.decode(request.url, ScraperConstants.UTF_ENCODING)
                val decodedUrl = URL(decodedPath)
                val urlDirectory = ContentScraperUtil.createDirectoryFromUrl(destinationFolder, decodedUrl)
                var file = File(urlDirectory, request.method + "_" + ContentScraperUtil.getFileNameFromUrl(decodedUrl))

                filter?.invoke(it)

                when {
                    response.content.encoding == "base64" -> {
                        var base = Base64.getDecoder().decode(response.content.text)
                        FileUtils.writeByteArrayToFile(file, base)
                    }
                    else -> FileUtils.writeStringToFile(file, response.content.text, ScraperConstants.UTF_ENCODING)
                }
                it.response.content.text = (urlDirectory.name + ScraperConstants.FORWARD_SLASH + file.name)
                it.request = null


            } catch (e: Exception) {
                UMLogUtil.logError("Index url failed at${it.request.url}")
                UMLogUtil.logDebug(e.message!!)

            }
        }*/
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


}