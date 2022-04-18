package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.waitForJSandJQueryToLoad
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM_SECS
import com.ustadmobile.lib.contentscrapers.abztract.Scraper.Companion.ERROR_TYPE_CONTENT_NOT_FOUND
import com.ustadmobile.lib.contentscrapers.abztract.Scraper.Companion.ERROR_TYPE_LINK_NOT_FOUND
import io.github.bonigarcia.wdm.WebDriverManager
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.client.ClientUtil
import net.lightbody.bmp.core.har.HarEntry
import net.lightbody.bmp.proxy.CaptureType
import org.kodein.di.DI
import org.openqa.selenium.InvalidArgumentException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration


abstract class HarIndexer(parentContentEntry: Long, runUid: Int, sqiUid: Int, contentEntryUid: Long, endpoint: Endpoint, di: DI) : Indexer(parentContentEntry, runUid, sqiUid, contentEntryUid, endpoint, di) {

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
            setIndexerDone(false, ERROR_TYPE_CONTENT_NOT_FOUND)
            close()
            throw IllegalArgumentException(e)
        }

        val waitDriver = WebDriverWait(chromeDriver, TIME_OUT_SELENIUM)
        waitForJSandJQueryToLoad(waitDriver)
        waitCondition?.invoke(waitDriver)

        val fileList = mutableListOf<HarEntry>()

        regexes.forEach { regex ->

            var fileEntry: HarEntry? = null

            var counterRequest = 0
            while (fileEntry == null && counterRequest < TIME_OUT_SELENIUM_SECS) {

                fileEntry = proxy.har.log.entries.find { harEntry ->
                    harEntry.request.url.contains(regex)
                }
                Thread.sleep(1000)
                counterRequest++
            }

            if (fileEntry == null) {
                setIndexerDone(false, ERROR_TYPE_LINK_NOT_FOUND)
                close()
                throw ScraperException(ERROR_TYPE_LINK_NOT_FOUND, "no request found for link")
            }


            var counterResponse = 0
            while (fileEntry.response.content.text.isNullOrEmpty() && counterResponse < TIME_OUT_SELENIUM_SECS) {
                Thread.sleep(1000)
                counterResponse++
            }

            if (fileEntry.response.content.text.isNullOrEmpty()) {
                setIndexerDone(false, ERROR_TYPE_CONTENT_NOT_FOUND)
                close()
                throw ScraperException(ERROR_TYPE_CONTENT_NOT_FOUND, "file didnt load for ${fileEntry.request.url} for source url $sourceUrl")
            }

            fileList.add(fileEntry)

        }

        block.invoke(proxy)
        close()

        return fileList

    }

    override fun close() {
        chromeDriver.quit()
        if(!proxy.isStopped){
            proxy.stop()
        }

    }
}