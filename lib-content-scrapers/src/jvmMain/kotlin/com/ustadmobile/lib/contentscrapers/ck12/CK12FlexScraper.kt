package com.ustadmobile.lib.contentscrapers.ck12

import com.ustadmobile.lib.contentscrapers.abztract.WebChunkScraper
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import java.io.File

class CK12FlexScraper(containerDir: File) : WebChunkScraper(containerDir) {

    override fun scrapeUrl(url: String, tmpLocation: File) {

        startHarScrape(url, {
            it.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.contentarea"))).click()
        }){

        }

    }

    override fun isContentUpdated(): Boolean {
        return true
    }


}