package com.ustadmobile.lib.contentscrapers.khanacademy

import com.ustadmobile.lib.contentscrapers.ContentScraperUtil

import org.apache.commons.pool2.BasePooledObjectFactory
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject
import org.openqa.selenium.chrome.ChromeDriver

@ExperimentalStdlibApi
class KhanDriverFactory : BasePooledObjectFactory<ChromeDriver>() {


    override fun create(): ChromeDriver {
        ContentScraperUtil.setChromeDriverLocation()
        return ContentScraperUtil.loginKhanAcademy()
    }

    override fun wrap(chromeDriver: ChromeDriver): PooledObject<ChromeDriver> {
        return DefaultPooledObject(chromeDriver)
    }

    override fun passivateObject(p: PooledObject<ChromeDriver>) {
        ContentScraperUtil.clearChromeConsoleLog(p.getObject())
    }

    override fun destroyObject(p: PooledObject<ChromeDriver>) {
        val driver = p.getObject()
        driver.close()
        driver.quit()
    }
}
