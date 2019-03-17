package com.ustadmobile.lib.contentscrapers.prathambooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LanguageList;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.ShrinkerUtil;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.ustadmobile.lib.contentscrapers.ContentScraperUtil.deleteETagOrModified;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EPUB_EXT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
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

    private static final String PRATHAM = "Pratham";
    private static final String GMAIL = "samihmustafa@gmail.com";
    private static final String PASS = "reading123";
    String prefixUrl = "https://storyweaver.org.in/api/v1/books-search?page=";

    String prefixEPub = "https://storyweaver.org.in/v0/stories/download-story/";

    String signIn = "https://storyweaver.org.in/users/sign_in";

    private Gson gson;
    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntry prathamParentEntry;
    private LanguageDao languageDao;
    private ContainerDao containerDao;
    private UmAppDatabase db;
    private UmAppDatabase repository;
    private File containerDir;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <file destination><file container><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }
        UMLogUtil.setLevel(args.length == 3 ? args[2] : "");
        try {
            new IndexPrathamContentScraper().findContent(new File(args[0]), new File(args[1]));
        } catch (IOException | URISyntaxException e) {
            UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logFatal("Exception running findContent pratham");
        }
    }

    public void findContent(File destinationDir, File containerDir) throws IOException, URISyntaxException {

        destinationDir.mkdirs();
        containerDir.mkdirs();
        this.containerDir = containerDir;
        ContentScraperUtil.setChromeDriverLocation();
        String cookie = loginPratham();

        db = UmAppDatabase.getInstance(null);
        repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        containerDao = repository.getContainerDao();
        languageDao = repository.getLanguageDao();

        new LanguageList().addAllLanguages();

        Language englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English");


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

        BooksResponse contentBooksList = gson.fromJson(IOUtils.toString(contentUrl.toURI(), UTF_ENCODING), BooksResponse.class);

        if (contentBooksList.data.size() == 0) {
            return;
        }

        int retry = 0;
        for (int contentCount = 0; contentCount < contentBooksList.data.size(); contentCount++) {
            HttpURLConnection connection = null;
            File resourceFolder = null;
            try {

                BooksResponse.Data data = contentBooksList.data.get(contentCount);

                URL epubUrl = generatePrathamEPubFileUrl(data.slug);

                connection = (HttpURLConnection) epubUrl.openConnection();
                connection.setRequestProperty("Cookie", cookie);

                String lang = getLangCode(data.language);
                Language langEntity = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, lang);
                resourceFolder = new File(destinationDir, String.valueOf(data.id));
                resourceFolder.mkdirs();
                ContentEntry contentEntry = ContentScraperUtil.createOrUpdateContentEntry(data.slug, data.title,
                        epubUrl.toString(), PRATHAM, LICENSE_TYPE_CC_BY, langEntity.getLangUid(), null,
                        data.description, true, EMPTY_STRING, data.coverImage.sizes.get(0).url,
                        EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao,
                        prathamParentEntry, contentEntry, contentCount);

                File content = new File(resourceFolder, data.slug + EPUB_EXT);
                boolean isUpdated = ContentScraperUtil.isFileModified(connection, resourceFolder, String.valueOf(data.id));
                File tmpDir = new File(UMFileUtil.stripExtensionIfPresent(content.getPath()));

                if (ContentScraperUtil.fileHasContent(tmpDir)) {
                    FileUtils.deleteDirectory(tmpDir);
                }

                isUpdated = true;

                if (!isUpdated) {
                    continue;
                }
                try {
                    FileUtils.copyInputStreamToFile(connection.getInputStream(), content);
                    ShrinkerUtil.EpubShrinkerOptions options = new ShrinkerUtil.EpubShrinkerOptions();
                    options.styleElementHelper = styleElement -> {
                        String text = styleElement.text();
                        if (text.startsWith("@font-face") || text.startsWith(".english")) {
                            return ShrinkerUtil.STYLE_OUTSOURCE_TO_LINKED_CSS;
                        } else {
                            return ShrinkerUtil.STYLE_DROP;
                        }
                    };
                    options.editor = document -> {
                        Elements elements = document.select("p");
                        List<Element> elementsToRemove = new ArrayList<>();
                        for (Element element : elements) {
                            if (element.text().isEmpty()) {
                                elementsToRemove.add(element);
                            }
                        }
                        elementsToRemove.forEach(Node::remove);
                        document.head().append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable-no\" />");
                        return document;
                    };
                    options.linkHelper = () -> {
                        try {
                            return IOUtils.toString(getClass().getResourceAsStream(ScraperConstants.PRATHAM_CSS_HELPER), UTF_ENCODING);
                        } catch (IOException e) {
                            return null;
                        }
                    };
                    File tmpFolder = ShrinkerUtil.shrinkEpub(content, options);
                    ContentScraperUtil.insertContainer(containerDao, contentEntry,
                            true, ScraperConstants.MIMETYPE_EPUB,
                            tmpFolder.lastModified(), tmpFolder,
                            db, repository, containerDir);
                    ContentScraperUtil.deleteFile(content);

                } catch (IOException io) {
                    cookie = loginPratham();
                    retry++;
                    deleteETagOrModified(resourceFolder, String.valueOf(data.id));
                    if (retry == 2) {
                        UMLogUtil.logInfo(ExceptionUtils.getStackTrace(io));
                        UMLogUtil.logError("Error for book " + data.title + " with id " + data.slug);
                        retry = 0;
                        continue;
                    }
                    contentCount--;
                    continue;
                } finally {
                    connection.disconnect();
                }
                retry = 0;


            } catch (Exception e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Error saving book " + contentBooksList.data.get(contentCount).slug);
                if (resourceFolder != null) {
                    deleteETagOrModified(resourceFolder, resourceFolder.getName());
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        }

        downloadPrathamContentList(generatePrathamUrl(String.valueOf(++contentBooksList.metadata.page)), cookie, destinationDir);

    }


    private String getLangCode(String language) {
        String[] list = language.split("-");
        return list[0];
    }

    public URL generatePrathamEPubFileUrl(String resourceId) throws MalformedURLException {
        return new URL(prefixEPub + resourceId + EPUB_EXT);
    }

    public URL generatePrathamUrl(String number) throws MalformedURLException {
        return new URL(prefixUrl + number + "&per_page=24");
    }

    public String loginPratham() {
        ChromeDriver driver = ContentScraperUtil.setupChrome(false);

        String cookie = "";
        driver.get(signIn);
        WebDriverWait waitDriver = new WebDriverWait(driver, TIME_OUT_SELENIUM);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);

        driver.findElement(By.id("user_email")).sendKeys(GMAIL);
        driver.findElement(By.id("user_password")).sendKeys(PASS);
        driver.findElement(By.name("commit")).click();

        for (Cookie ck : driver.manage().getCookies()) {

            if (ck.getName().equalsIgnoreCase("_session_id")) {
                cookie = ck.getName() + "=" + ck.getValue();
                UMLogUtil.logDebug(cookie);
            }
        }

        driver.close();
        driver.quit();

        return cookie;
    }


}
