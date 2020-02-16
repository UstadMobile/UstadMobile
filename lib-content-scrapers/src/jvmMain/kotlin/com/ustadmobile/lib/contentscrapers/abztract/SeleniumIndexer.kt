package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.waitForJSandJQueryToLoad
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import io.github.bonigarcia.wdm.WebDriverManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.InvalidArgumentException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.WebDriverWait

abstract class SeleniumIndexer(parentContentEntry: Long, runUid: Int, db: UmAppDatabase, sqiUid: Int) : Indexer(parentContentEntry, runUid, db, sqiUid) {

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
            throw IllegalArgumentException(e)
        }

        val waitDriver = WebDriverWait(chromeDriver, ScraperConstants.TIME_OUT_SELENIUM.toLong())
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