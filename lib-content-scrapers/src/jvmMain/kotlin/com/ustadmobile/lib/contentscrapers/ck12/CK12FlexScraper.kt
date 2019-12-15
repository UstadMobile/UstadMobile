package com.ustadmobile.lib.contentscrapers.ck12

import com.ustadmobile.lib.contentscrapers.abztract.WebChunkScraper
import net.lightbody.bmp.core.har.HarEntry
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File

class CK12FlexScraper: WebChunkScraper() {

    override fun scrapeUrl(startingUrl: String, tmpLocation: File) {



    }

    override fun filterContent(entry: HarEntry): HarEntry {


        return entry
    }

    override fun waitCondition(waitDriver: WebDriverWait) {

    }

    override fun isContentUpdated(): Boolean {
        return true
    }

}