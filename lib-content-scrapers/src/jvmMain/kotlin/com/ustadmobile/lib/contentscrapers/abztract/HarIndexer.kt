package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.waitForJSandJQueryToLoad
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import io.github.bonigarcia.wdm.WebDriverManager
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.client.ClientUtil
import net.lightbody.bmp.core.har.HarEntry
import net.lightbody.bmp.proxy.CaptureType
import org.openqa.selenium.InvalidArgumentException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.support.ui.WebDriverWait

abstract class HarIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase, sqiUid: Int) : Indexer(parentContentEntry, runUid, db, sqiUid) {

    protected var chromeDriver: ChromeDriver
    var proxy: BrowserMobProxyServer = BrowserMobProxyServer()

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

    fun startHarIndexer(sourceUrl: String, regexes: List<Regex> = listOf(), waitCondition: WaitConditionFn? = null, block: (proxy: BrowserMobProxyServer) -> Boolean): List<HarEntry> {
        ContentScraperUtil.clearChromeConsoleLog(chromeDriver)
        proxy.newHar("Indexer")

        try {
            chromeDriver.navigate().to(sourceUrl)
        } catch (e: InvalidArgumentException) {
            throw IllegalArgumentException(e)
        }

        val waitDriver = WebDriverWait(chromeDriver, ScraperConstants.TIME_OUT_SELENIUM.toLong())
        waitForJSandJQueryToLoad(waitDriver)
        waitCondition?.invoke(waitDriver)

        val fileList = mutableListOf<HarEntry>()

        regexes.forEach { regex ->

            var fileEntry: HarEntry? = null

            var counterRequest = 0
            while (fileEntry == null && counterRequest < ScraperConstants.TIME_OUT_SELENIUM) {

                fileEntry = proxy.har.log.entries.find { harEntry ->
                    harEntry.request.url.contains(regex)

                }
                Thread.sleep(1000)
                counterRequest++
            }

            if (fileEntry == null) {
                throw ScraperException(Scraper.ERROR_TYPE_LINK_NOT_FOUND, "no request found for link")
            }


            var counterResponse = 0
            while (fileEntry.response.content.text.isNullOrEmpty() && counterResponse < ScraperConstants.TIME_OUT_SELENIUM) {
                Thread.sleep(1000)
                counterResponse++
            }

            if (fileEntry.response.content.text.isNullOrEmpty()) {
                throw ScraperException(Scraper.ERROR_TYPE_CONTENT_NOT_FOUND, "file didnt load for ${fileEntry.request.url} for source url $sourceUrl")
            }

            fileList.add(fileEntry)

        }

        block.invoke(proxy)

        return fileList

    }

    override fun close() {
        chromeDriver.quit()
        proxy.stop()
    }
}