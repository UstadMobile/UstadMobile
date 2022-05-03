package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.waitForJSandJQueryToLoad
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM
import io.github.bonigarcia.wdm.WebDriverManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.kodein.di.DI
import org.openqa.selenium.InvalidArgumentException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait


abstract class SeleniumIndexer(parentContentEntry: Long, runUid: Int, sqiUid: Int, contentEntryUid: Long, endpoint: Endpoint, di: DI) : Indexer(parentContentEntry, runUid, sqiUid, contentEntryUid, endpoint, di) {

    protected var chromeDriver: ChromeDriver

    init {
        WebDriverManager.chromedriver().setup()
        val options = ChromeOptions()
        chromeDriver = ChromeDriver(options)
    }

    fun startSeleniumIndexer(url: String, waitCondition: WaitConditionFn? = null): Document {

        ContentScraperUtil.clearChromeConsoleLog(chromeDriver)

        try {
            chromeDriver.navigate().to(url)
        } catch (e: InvalidArgumentException) {
            setIndexerDone(false, Scraper.ERROR_TYPE_CONTENT_NOT_FOUND)
            close()
            throw IllegalArgumentException(e)
        }

        val waitDriver = WebDriverWait(chromeDriver, TIME_OUT_SELENIUM)
        waitForJSandJQueryToLoad(waitDriver)
        waitCondition?.invoke(waitDriver)

        val doc = Jsoup.parse(chromeDriver.pageSource)
        close()
        return doc
    }

    override fun close() {
        chromeDriver.close()
    }
}