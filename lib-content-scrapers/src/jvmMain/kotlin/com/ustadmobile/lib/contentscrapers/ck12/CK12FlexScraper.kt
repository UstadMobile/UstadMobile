package com.ustadmobile.lib.contentscrapers.ck12

import com.ustadmobile.lib.contentscrapers.abztract.WebChunkScraper
import java.io.File

class CK12FlexScraper(containerDir: File) : WebChunkScraper(containerDir) {

    override fun scrapeUrl(url: String, tmpLocation: File) {

    }

    override fun isContentUpdated(): Boolean {
        return true
    }

    override fun close() {
        chromeDriver.quit()
    }


}