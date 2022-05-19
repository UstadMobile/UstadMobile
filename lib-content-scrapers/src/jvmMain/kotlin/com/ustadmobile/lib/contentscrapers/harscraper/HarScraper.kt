package com.ustadmobile.lib.contentscrapers.harscraper

import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.UMLogUtil
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.core.har.HarEntry
import net.lightbody.bmp.proxy.CaptureType
import org.apache.commons.io.FileUtils
import org.openqa.selenium.InvalidArgumentException
import org.openqa.selenium.Proxy
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.lang.IllegalArgumentException
import java.net.URL
import java.net.URLDecoder
import java.time.Duration
import java.util.*

typealias ScrapeFilterFn = (harEntry: HarEntry) -> HarEntry

typealias WaitConditionFn = (waitCondition: WebDriverWait) -> Unit

fun setupProxyWithSelenium(proxy: BrowserMobProxyServer, seleniumProxy: Proxy, name: String): ChromeDriver {

    ContentScraperUtil.setChromeDriverLocation()
    val driver = ContentScraperUtil.setupChromeDriverWithSeleniumProxy(seleniumProxy)
    proxy.enableHarCaptureTypes(CaptureType.REQUEST_HEADERS, CaptureType.RESPONSE_CONTENT, CaptureType.RESPONSE_HEADERS, CaptureType.RESPONSE_BINARY_CONTENT)
    proxy.newHar(name)

    return driver

}

fun scrapeUrlwithHar(proxy: BrowserMobProxyServer, driver: ChromeDriver, url: String, destination: File, waitCondition: WaitConditionFn?, filter: ScrapeFilterFn?) {

    try {
        driver.get(url)
    }catch (e: InvalidArgumentException){
        driver.quit()
        throw IllegalArgumentException(e)
    }
    val waitDriver = WebDriverWait(driver, ScraperConstants.TIME_OUT_SELENIUM)
    ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver)

    waitCondition?.invoke(waitDriver)

    var entries = proxy.har.log.entries

    // looks in the list for starting url, checks if its not 404 otherwise return error
    entries.find { it.request.url == url }.also {
        require(it?.response?.status != 404) {
            driver.quit()
            "404 Starting Url" }
    }

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
            val urlDirectory = ContentScraperUtil.createDirectoryFromUrl(destination, decodedUrl)
            var file = File(urlDirectory, request.method + "_" + ContentScraperUtil.getFileNameFromUrl(decodedUrl))

            filter?.invoke(it)

            when (response.content.encoding) {
                "base64" -> {
                    var base = Base64.getDecoder().decode(response.content.text)
                    FileUtils.writeByteArrayToFile(file, base)
                }
                else -> FileUtils.writeStringToFile(file, response.content.text, ScraperConstants.UTF_ENCODING)
            }
            it.response.content.text = (urlDirectory.name + ScraperConstants.FORWARD_SLASH + file.name)
            it.request = null


        } catch (e: Exception) {
            UMLogUtil.logError("har scrapped url failed for request url ${it.request.url}")
            UMLogUtil.logError(e.message!!)
        }

        driver.quit()

    }






}