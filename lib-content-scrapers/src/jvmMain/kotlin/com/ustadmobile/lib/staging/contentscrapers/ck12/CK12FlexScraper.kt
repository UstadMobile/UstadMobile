package com.ustadmobile.lib.contentscrapers.ck12

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.abztract.HarScraper
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import java.io.File
@ExperimentalStdlibApi
class CK12FlexScraper(containerDir: File, db: UmAppDatabase, contentEntryUid: Long, sqiUid: Int) : HarScraper(containerDir, db, contentEntryUid, sqiUid) {

    override fun scrapeUrl(sourceUrl: String) {

        startHarScrape(sourceUrl, {
            it.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.contentarea"))).click()
        }){
            true
        }

    }

}