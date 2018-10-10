package com.ustadmobile.lib.contentscrapers.prathambooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;


/**
 * Storyweaver has an api for all their epub books.
 * To download each book, i need to have a cookie session id
 * I get session by logging in the website and entering the credentials and retrieving the cookie
 * To get the total number of books,
 * hit the api with just 1 book request and in the json, the total number of books is stored in metadata.hits
 * Call the api again with the request for all books
 * create the url to get the epub, open the url connection and add the cookie session
 *
 * If IOException is thrown, might be because the session expired so login again.
 * otherwise file is downloaded in its folder
 */
public class IndexPrathamContentScraper {

    String prefixUrl = "https://storyweaver.org.in/api/v1/books-search?page=1&per_page=";

    String prefixEPub = "https://storyweaver.org.in/v0/stories/download-story/";
    String ePubExt = ".epub";

    String signIn = "https://storyweaver.org.in/users/sign_in";

    private ArrayList<OpdsEntryWithRelations> entryWithRelationsList;
    private ArrayList<OpdsEntryParentToChildJoin> parentToChildJoins;
    private Gson gson;
    private String cookie;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: <file destination>");
            System.exit(1);
        }

        System.out.println(args[0]);
        try {
            new IndexPrathamContentScraper().findContent(new File(args[0]));
        } catch (IOException | URISyntaxException e) {
            System.err.println("Exception running findContent");
            e.printStackTrace();
        }
    }

    public void findContent(File destinationDir) throws IOException, URISyntaxException {

        URL firstUrl = new URL(prefixUrl + "1");

        destinationDir.mkdirs();

        entryWithRelationsList = new ArrayList<>();
        parentToChildJoins = new ArrayList<>();

        loginPratham();

        OpdsEntryWithRelations parentPratham = new OpdsEntryWithRelations(
                UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), "https://storyweaver.org.in/", "Pratham Books");

        entryWithRelationsList.add(parentPratham);

        gson = new GsonBuilder().disableHtmlEscaping().create();

        BooksResponse books = gson.fromJson(IOUtils.toString(firstUrl.toURI(), ScraperConstants.UTF_ENCODING), BooksResponse.class);

        URL contentUrl = new URL(prefixUrl + books.metadata.hits);

        BooksResponse contentBooksList = gson.fromJson(IOUtils.toString(contentUrl.toURI(), ScraperConstants.UTF_ENCODING), BooksResponse.class);

        int retry = 0;
        for (int i = 0; i < contentBooksList.data.size(); i++) {

            try {

                BooksResponse.Data data = contentBooksList.data.get(i);

                String epub = prefixEPub + data.slug + ePubExt;

                URL epubUrl = new URL(epub);

                URLConnection connection = epubUrl.openConnection();
                connection.setRequestProperty("Cookie", cookie);

                File file = new File(destinationDir, String.valueOf(data.id));
                file.mkdirs();
                String fileName = data.slug + ePubExt;
                File content = new File(file, fileName);
                if (!ContentScraperUtil.isFileModified(connection, file, String.valueOf(data.id))) {
                    continue;
                }
                try {
                    FileUtils.copyInputStreamToFile(connection.getInputStream(), content);
                } catch (IOException io) {
                    loginPratham();
                    retry++;
                    System.err.println("Login and retry the link again attempt" + retry);
                    if(retry == 2){
                        retry = 0;
                        continue;
                    }
                    i--;
                    continue;
                }
                retry = 0;

                OpdsEntryWithRelations childEntry = new OpdsEntryWithRelations(UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()),
                        data.slug, data.title);

                OpdsLink newEntryLink = new OpdsLink(childEntry.getUuid(), "application/epub+zip",
                        file.getName() + "/" + data.slug, OpdsEntry.LINK_REL_ACQUIRE);
                newEntryLink.setLength(new File(file, fileName).length());
                childEntry.setLinks(Collections.singletonList(newEntryLink));

                OpdsEntryParentToChildJoin join = new OpdsEntryParentToChildJoin(childEntry.getUuid(),
                        parentPratham.getUuid(), i);

                entryWithRelationsList.add(childEntry);
                parentToChildJoins.add(join);


            } catch (Exception e) {
                System.err.println("Error saving book " + contentBooksList.data.get(i).slug);
                e.printStackTrace();
            }

        }

    }

    private void loginPratham() {
        ChromeDriver driver = ContentScraperUtil.setupChrome(false);

        driver.get(signIn);
        WebDriverWait waitDriver = new WebDriverWait(driver, 10000);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);

        driver.findElement(By.id("user_email")).sendKeys("samihmustafa@gmail.com");
        driver.findElement(By.id("user_password")).sendKeys("reading123");
        driver.findElement(By.name("commit")).click();

        for (Cookie ck : driver.manage().getCookies()) {

            if (ck.getName().equalsIgnoreCase("_session_id")) {
                cookie = ck.getName() + "=" + ck.getValue();
                System.out.println(cookie);
            }
        }

        driver.close();
    }


}
