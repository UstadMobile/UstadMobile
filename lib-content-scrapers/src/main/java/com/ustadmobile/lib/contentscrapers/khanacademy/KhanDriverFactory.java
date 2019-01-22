package com.ustadmobile.lib.contentscrapers.khanacademy;

import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.openqa.selenium.chrome.ChromeDriver;

public class KhanDriverFactory extends BasePooledObjectFactory<ChromeDriver> {


    @Override
    public ChromeDriver create() {
        ContentScraperUtil.setChromeDriverLocation();
        return ContentScraperUtil.loginKhanAcademy();
    }

    @Override
    public PooledObject<ChromeDriver> wrap(ChromeDriver chromeDriver) {
        return new DefaultPooledObject<>(chromeDriver);
    }

    @Override
    public void passivateObject(PooledObject<ChromeDriver> p) {
        ContentScraperUtil.clearChromeConsoleLog(p.getObject());
    }

    @Override
    public void destroyObject(PooledObject<ChromeDriver> p) {
        ChromeDriver driver = p.getObject();
        driver.close();
        driver.quit();
    }
}
