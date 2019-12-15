package com.ustadmobile.lib.contentscrapers.abztract

import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import net.lightbody.bmp.core.har.HarEntry
import org.openqa.selenium.Proxy
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File

abstract class WebChunkScraper: Scraper() {

    abstract fun filterContent(entry: HarEntry): HarEntry

    abstract fun waitCondition(waitDriver: WebDriverWait)


    fun setChromeDriverLocation{
        ContentScraperUtil.setChromeDriverLocation()
    }

    fun createProxy(harName: String): Proxy{

    }


    fun setupChromeDriverWithSeleniumProxy(seleniumProxy: Proxy): ChromeDriver {

    }

    fun checkStartingUrlNot404(entryList: List<HarEntry>, startingUrl: String){

    }

    fun saveAllHarContent(entryList: List<HarEntry>, destinationFolder: File){

    }

    fun saveHarFile(proxy: Proxy, harLocation: File){


    }



}