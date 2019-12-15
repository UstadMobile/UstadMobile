package com.ustadmobile.lib.contentscrapers.ck12

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.lib.contentscrapers.abztract.WebChunkScraper
import org.openqa.selenium.Proxy
import org.openqa.selenium.chrome.ChromeDriver
import java.io.File

class CK12FlexScraper(chromeDriver: ChromeDriver, containerManager: ContainerManager) : WebChunkScraper(chromeDriver, containerManager) {

    override fun scrapeUrl(url: String, tmpLocation: File) {

    }

    override fun isContentUpdated(): Boolean {
        return true
    }

    override fun close() {
        chromeDriver.quit()
    }


}