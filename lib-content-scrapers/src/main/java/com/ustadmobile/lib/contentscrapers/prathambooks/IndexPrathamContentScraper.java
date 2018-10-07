package com.ustadmobile.lib.contentscrapers.prathambooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.text.AbstractDocument;

import sun.nio.ch.IOUtil;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.chromeDriverLocation;

public class IndexPrathamContentScraper {

    String prefixUrl = "https://storyweaver.org.in/api/v1/books-search?page=1&per_page=";

    String prefixEPub = "https://storyweaver.org.in/v0/stories/download-story/";
    String ePubExt = " .epub";

    String signIn = "https://storyweaver.org.in/users/sign_in";

    private File destinationDirectory;

    private ArrayList<OpdsEntryWithRelations> entryWithRelationsList;
    private ArrayList<OpdsEntryParentToChildJoin> parentToChildJoins;
    private Gson gson;
    private String cookie;

    public void findContent(File destinationDir) throws IOException, URISyntaxException {

        URL firstUrl = new URL(prefixUrl + "1");

        destinationDir.mkdirs();
        destinationDirectory = destinationDir;

        entryWithRelationsList = new ArrayList<>();
        parentToChildJoins = new ArrayList<>();

        System.setProperty("webdriver.chrome.driver", chromeDriverLocation);
        ChromeDriver driver = new ChromeDriver();

        driver.get(signIn);
        WebDriverWait waitDriver = new WebDriverWait(driver, 10000);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);

        driver.findElement(By.id("user_email")).sendKeys("samihmustafa@gmail.com");
        driver.findElement(By.id("user_password")).sendKeys("reading123");
        driver.findElement(By.name("commit")).click();

        for (Cookie ck : driver.manage().getCookies()) {

            if(ck.getName().equalsIgnoreCase("_session_id")){
                cookie = ck.getName() + "=" + ck.getValue();
                System.out.println(cookie);
            }
        }


        gson = new GsonBuilder().disableHtmlEscaping().create();

        BooksResponse books = gson.fromJson(IOUtils.toString(firstUrl.toURI(), ScraperConstants.UTF_ENCODING), BooksResponse.class);

        URL contentUrl = new URL(prefixUrl + books.metadata.hits);

        BooksResponse contentBooksList = gson.fromJson(IOUtils.toString(contentUrl.toURI(), ScraperConstants.UTF_ENCODING), BooksResponse.class);

        for (BooksResponse.Data data : contentBooksList.data) {

            String epub = prefixEPub + data.slug + ePubExt;

            URL epubUrl = new URL(epub);

            URLConnection connection = epubUrl.openConnection();
            connection.setRequestProperty("Cookie", cookie);

            File file = new File(destinationDir, String.valueOf(data.id));
            file.mkdirs();
            File content = new File(file, data.slug + ePubExt);
            if(!ContentScraperUtil.isFileModified(connection, file, data.slug)){
                continue;
            }
            FileUtils.copyInputStreamToFile(connection.getInputStream(), content);


        }

    }


}
