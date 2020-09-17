package com.ustadmobile.lib.contentscrapers.ck12

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.contentscrapers.abztract.HarScraper
import org.kodein.di.DI
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions

@ExperimentalStdlibApi
class CK12FlexScraper(contentEntryUid: Long, sqiUid: Int, parentContentEntryUid: Long, endpoint: Endpoint, di: DI) : HarScraper(contentEntryUid, sqiUid, parentContentEntryUid, endpoint, di) {

    override fun scrapeUrl(sourceUrl: String) {

        startHarScrape(sourceUrl, {
            it.until<WebElement>(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.contentarea"))).click()
        }){
            true
        }

    }

}