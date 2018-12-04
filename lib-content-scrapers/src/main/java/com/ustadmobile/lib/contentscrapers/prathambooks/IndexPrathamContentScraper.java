package com.ustadmobile.lib.contentscrapers.prathambooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neovisionaries.i18n.LanguageAlpha3Code;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LanguageList;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.Language;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;


/**
 * Storyweaver has an api for all their epub books.
 * To download each book, i need to have a cookie session id
 * I get session by logging in the website and entering the credentials and retrieving the cookie
 * To get the total number of books,
 * hit the api with just 1 book request and in the json, the total number of books is stored in metadata.hits
 * Call the api again with the request for all books
 * create the url to get the epub, open the url connection and add the cookie session
 * <p>
 * If IOException is thrown, might be because the session expired so login again.
 * otherwise file is downloaded in its folder
 */
public class IndexPrathamContentScraper {

    public static final String PRATHAM = "Pratham";
    String prefixUrl = "https://storyweaver.org.in/api/v1/books-search?page=";

    String prefixEPub = "https://storyweaver.org.in/v0/stories/download-story/";
    String ePubExt = ".epub";

    String signIn = "https://storyweaver.org.in/users/sign_in";

    private Gson gson;
    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntryFileDao contentEntryFileDao;
    private ContentEntryContentEntryFileJoinDao contentEntryFileJoinDao;
    private ContentEntryFileStatusDao contentFileStatusDao;
    private ContentEntry prathamParentEntry;
    private Language englishLang;
    private LanguageDao languageDao;

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

        destinationDir.mkdirs();
        ContentScraperUtil.setChromeDriverLocation();
        String cookie = loginPratham();

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        db.setMaster(true);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        contentEntryFileDao = repository.getContentEntryFileDao();
        contentEntryFileJoinDao = repository.getContentEntryContentEntryFileJoinDao();
        contentFileStatusDao = repository.getContentEntryFileStatusDao();
        languageDao = repository.getLanguageDao();

        new LanguageList().addAllLanguages();

        englishLang = ContentScraperUtil.insertOrUpdateLanguage(languageDao, "English");



        ContentEntry masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);



        prathamParentEntry = ContentScraperUtil.createOrUpdateContentEntry("https://storyweaver.org.in/", "Pratham Books",
                "https://storyweaver.org.in/", PRATHAM, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                "Every Child in School & Learning Well", false, EMPTY_STRING,
                "https://prathambooks.org/wp-content/uploads/2018/04/Logo-black.png", EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, prathamParentEntry, 3);

        gson = new GsonBuilder().disableHtmlEscaping().create();

        downloadPrathamContentList(generatePrathamUrl(String.valueOf(1)), cookie, destinationDir);

    }


    private void downloadPrathamContentList(URL contentUrl, String cookie, File destinationDir) throws URISyntaxException, IOException {

        BooksResponse contentBooksList = gson.fromJson(IOUtils.toString(contentUrl.toURI(), ScraperConstants.UTF_ENCODING), BooksResponse.class);

        if(contentBooksList.data.size() == 0){
            return;
        }

        int retry = 0;
        for (int contentCount = 0; contentCount < contentBooksList.data.size(); contentCount++) {

            try {

                BooksResponse.Data data = contentBooksList.data.get(contentCount);

                URL epubUrl = generatePrathamEPubFileUrl(data.slug);

                URLConnection connection = epubUrl.openConnection();
                connection.setRequestProperty("Cookie", cookie);

                String lang = getLangCode(data.language);
                Language langEntity = ContentScraperUtil.insertOrUpdateLanguage(languageDao, lang);
                File resourceFolder = new File(destinationDir, String.valueOf(data.id));
                resourceFolder.mkdirs();
                String resourceFileName = data.slug + ePubExt;

                ContentEntry contentEntry = ContentScraperUtil.createOrUpdateContentEntry(data.slug, data.title,
                        epubUrl.getPath(), PRATHAM, LICENSE_TYPE_CC_BY, langEntity.getLangUid(), null,
                        data.description, true, EMPTY_STRING, data.coverImage.sizes.get(0).url,
                        EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao,
                        prathamParentEntry, contentEntry, contentCount);

                File content = new File(resourceFolder, resourceFileName);
                if (!ContentScraperUtil.isFileModified(connection, resourceFolder, String.valueOf(data.id))) {

                    ContentScraperUtil.checkAndUpdateDatabaseIfFileDownloadedButNoDataFound(content, contentEntry, contentEntryFileDao,
                            contentEntryFileJoinDao, contentFileStatusDao, ScraperConstants.MIMETYPE_EPUB, true);


                    continue;
                }
                try {
                    FileUtils.copyInputStreamToFile(connection.getInputStream(), content);
                } catch (IOException io) {
                    cookie = loginPratham();
                    retry++;
                    io.printStackTrace();
                    System.err.println("Error for book " + data.title + " with id " + data.slug);
                    if (retry == 2) {
                        retry = 0;
                        continue;
                    }
                    contentCount--;
                    continue;
                }
                retry = 0;

                ContentScraperUtil.insertContentEntryFile(content, contentEntryFileDao, contentFileStatusDao, contentEntry,
                        ContentScraperUtil.getMd5(content), contentEntryFileJoinDao, true, ScraperConstants.MIMETYPE_EPUB);

            } catch (Exception e) {
                System.err.println("Error saving book " + contentBooksList.data.get(contentCount).slug);
                e.printStackTrace();
            }

        }

        downloadPrathamContentList(generatePrathamUrl(String.valueOf(++contentBooksList.metadata.page)), cookie, destinationDir);

    }

    private String getLangCode(String language) {
        String[] list = language.split("-");
        return list[0];
    }

    public URL generatePrathamEPubFileUrl(String resourceId) throws MalformedURLException {
        return new URL(prefixEPub + resourceId + ePubExt);
    }

    public URL generatePrathamUrl(String number) throws MalformedURLException {
        return new URL(prefixUrl + number);
    }

    public String loginPratham() {
        ChromeDriver driver = ContentScraperUtil.setupChrome(false);

        String cookie = "";
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

        return cookie;
    }


}
