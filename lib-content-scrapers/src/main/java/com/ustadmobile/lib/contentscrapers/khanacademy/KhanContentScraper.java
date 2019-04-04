package com.ustadmobile.lib.contentscrapers.khanacademy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LogIndex;
import com.ustadmobile.lib.contentscrapers.LogResponse;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.ShrinkerUtil;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ATTEMPT_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ATTEMPT_JSON_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ATTEMPT_JSON_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ATTEMPT_KHAN_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.COMPLETE_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.COMPLETE_KHAN_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.CORRECT_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.CORRECT_KHAN_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.HINT_JSON_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.HINT_JSON_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.INTERNAL_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.INTERNAL_JSON_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN_CSS_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN_CSS_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_CSS;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_JSON;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_KHAN;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_SVG;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_WEB_CHUNK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TRY_AGAIN_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TRY_AGAIN_KHAN_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static com.ustadmobile.lib.contentscrapers.ck12.CK12ContentScraper.RESPONSE_RECEIVED;


/**
 * Every khan academy content is categorized into several types - video, exercise, article
 * <p>
 * For Video Content, the index has the url of video in their json.
 * The Scraper checks if this was downloaded before by checking the etag from the header of the url
 * Downloads the content and saves it into the zip
 * <p>
 * For Exercise Content, first it is required to be logged in for the exercise.
 * Then, get the json of the exercise from the source code of the website
 * This is needed to get the all the list of exercises in the exercise, the exericse id and the last modified date
 * If already downloaded this content, it checks if the date is the same
 * Load the website using Selenium and wait for the page to load fully to get all its content.
 * Setup Selenium and Chrome
 * Run selenium and wait for everything to load on the screen by waiting for the element div[data-test-id=tutorial-page]
 * Once that is done, get the logs for the network and store in a list
 * Filter the responses based on the message RESPONSE RECEIVED
 * Store the mimeType and url of each response.
 * Copy and Save the content of each url and use request headers if required.
 * <p>
 * Once all the logs have been saved, download all the exercises belonging to that exercise using the list from earlier
 * to get all the item id to call the url
 * https://www.khanacademy.org/api/internal/user/exercises/{exericse-id}/items/{item-id}/assessment_item
 * Extract all the images from the url so those images can be used offline
 * <p>
 * Create a content directory for all the url and their location into a json so it can be played back.
 * Zip all files with the course as the name
 * <p>
 * For Article Content, extract the json from the page, load it into ArticleResponse
 * This is used to check when the article was last updated
 * If updated, Setup Selenium and Chrome
 * Run selenium and wait for everything to load on the screen by waiting for the element ul[class*=listWrapper]
 * Once that is done, get the logs for the network and store in a list
 * Filter the responses based on the message RESPONSE RECEIVED
 * Store the mimeType and url of each response.
 * Copy and Save the content of each url and use request headers if required.
 * <p>
 * Create a content directory for all the url and their location into a json so it can be played back.
 * Zip all files with the course as the name
 */
public class KhanContentScraper implements Runnable {

    private static final String KHAN_CSS = "<link rel='stylesheet' href='/khanscraper.css' type='text/css'/>";
    private static final String KHAN_COOKIE = "<script> document.cookie = \"fkey=abcde;\" </script>";
    public static final String CONTENT_DETAIL_SOURCE_URL_KHAN_ID = "content-detail?sourceUrl=khan-id://";
    private File containerDir;

    private GenericObjectPool<ChromeDriver> factory;
    private int sqiUid;
    private String contentType;
    private ContentEntry parentEntry;
    private File destinationDirectory;
    private URL url;
    private ChromeDriver driver;

    private String regexUrlPrefix = "https://(www.khanacademy.org|cdn.kastatic.org)/(.*)";

    private String secondExerciseUrl = "https://www.khanacademy.org/api/internal/user/exercises/";

    private String exerciseMidleUrl = "/items/";

    private String exercisePostUrl = "/assessment_item";

    private boolean isContentUpdated = true;
    private String nodeSlug;
    private String mimeType;


    public KhanContentScraper(URL scrapeUrl, File destinationDirectory, File containerDir, ContentEntry parent, String contentType, int sqiUid, GenericObjectPool<ChromeDriver> factory) {

        this.destinationDirectory = destinationDirectory;
        this.containerDir = containerDir;
        this.url = scrapeUrl;
        this.parentEntry = parent;
        this.contentType = contentType;
        this.sqiUid = sqiUid;
        this.factory = factory;

    }

    public KhanContentScraper(File destinationDirectory, ChromeDriver driver) {
        this.destinationDirectory = destinationDirectory;
        this.driver = driver;
    }

    @Override
    public void run() {
        System.gc();
        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        ContainerDao containerDao = repository.getContainerDao();
        ScrapeQueueItemDao queueDao = db.getScrapeQueueItemDao();


        long startTime = System.currentTimeMillis();
        UMLogUtil.logInfo("Started scraper url " + url + " at start time: " + startTime);
        queueDao.setTimeStarted(sqiUid, startTime);

        boolean successful = false;
        try {
            driver = factory.borrowObject();
            File content = new File(destinationDirectory, destinationDirectory.getName());
            String mimetype = MIMETYPE_WEB_CHUNK;
            if (ScraperConstants.KhanContentType.VIDEO.getType().equals(contentType)) {
                scrapeVideoContent(url.toString());
                successful = true;
                mimetype = MIMETYPE_KHAN;
            } else if (ScraperConstants.KhanContentType.EXERCISE.getType().equals(contentType)) {
                scrapeExerciseContent(url.toString());
                successful = true;
            } else if (ScraperConstants.KhanContentType.ARTICLE.getType().equals(contentType)) {
                scrapeArticleContent(url.toString());
                successful = true;
            } else {
                UMLogUtil.logError("unsupported kind = " + contentType + " at url = " + url);
                throw new IllegalArgumentException("unsupported kind = " + contentType + " at url = " + url);
            }

            if (isContentUpdated()) {
                ContentScraperUtil.insertContainer(containerDao, parentEntry, true,
                        mimetype, content.lastModified(), content, db, repository,
                        containerDir);
                FileUtils.deleteDirectory(content);
            }

        } catch (Exception e) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logError("Unable to scrape content from url " + url);
            ContentScraperUtil.deleteETagOrModified(destinationDirectory, destinationDirectory.getName());
        }

        if (factory != null) {
            factory.returnObject(driver);
        }

        queueDao.updateSetStatusById(sqiUid, successful ? ScrapeQueueItemDao.STATUS_DONE : ScrapeQueueItemDao.STATUS_FAILED);
        queueDao.setTimeFinished(sqiUid, System.currentTimeMillis());
        long duration = System.currentTimeMillis() - startTime;
        UMLogUtil.logInfo("Ended scrape for url " + url + " in duration: " + duration);

    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isContentUpdated() {
        return isContentUpdated;
    }

    public void scrapeVideoContent(String scrapUrl) throws IOException {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        File folder = new File(destinationDirectory, destinationDirectory.getName());
        folder.mkdirs();

        String initialJson = KhanContentIndexer.getJsonStringFromScript(scrapUrl);
        SubjectListResponse data = gson.fromJson(initialJson, SubjectListResponse.class);
        if (data.componentProps == null) {
            data = gson.fromJson(initialJson, PropsSubjectResponse.class).props;
        }

        SubjectListResponse.ComponentData compProps = data.componentProps;
        SubjectListResponse.ComponentData.NavData navData = compProps.tutorialNavData;
        if (navData == null) {
            navData = compProps.tutorialPageData;
        }
        List<SubjectListResponse.ComponentData.NavData.ContentModel> contentList = navData.contentModels;
        if (contentList == null || contentList.isEmpty()) {
            contentList = new ArrayList<>();
            contentList.add(navData.contentModel);
        }

        for (SubjectListResponse.ComponentData.NavData.ContentModel content : contentList) {

            if (destinationDirectory.getName().contains(content.id) || scrapUrl.contains(content.relativeUrl)) {

                String videoUrl = content.downloadUrls.mp4;
                if (videoUrl == null || videoUrl.isEmpty()) {
                    videoUrl = content.downloadUrls.mp4Low;
                    if (videoUrl == null) {
                        UMLogUtil.logError("Video was not available in any format for url: " + url);
                        break;
                    }
                    UMLogUtil.logTrace("Video was not available in mp4, found in mp4-low at " + url);
                }
                URL url = new URL(new URL(scrapUrl), videoUrl);
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("HEAD");
                    mimeType = conn.getContentType();

                    isContentUpdated = ContentScraperUtil.isFileModified(conn, destinationDirectory, destinationDirectory.getName());

                    if (ContentScraperUtil.fileHasContent(folder)) {
                        isContentUpdated = false;
                        FileUtils.deleteDirectory(folder);
                    }

                    if (!isContentUpdated) {
                        return;
                    }

                    File contentFile = new File(folder, FilenameUtils.getName(url.getPath()));
                    FileUtils.copyURLToFile(url, contentFile);
                    File webMFile = new File(folder, FilenameUtils.getName(url.getPath()));
                    ShrinkerUtil.convertKhanVideoToWebMAndCodec2(contentFile, webMFile);

                } catch (IOException e) {
                    throw e;
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }

            }
        }

    }


    public void scrapeExerciseContent(String scrapUrl) throws IOException {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        File khanDirectory = new File(destinationDirectory, destinationDirectory.getName());
        khanDirectory.mkdirs();

        String initialJson = KhanContentIndexer.getJsonStringFromScript(scrapUrl);
        SubjectListResponse response = gson.fromJson(initialJson, SubjectListResponse.class);
        if (response.componentProps == null) {
            response = gson.fromJson(initialJson, PropsSubjectResponse.class).props;
        }

        String exerciseId = "0";
        List<SubjectListResponse.ComponentData.Card.UserExercise.Model.AssessmentItem> exerciseList = null;
        long dateModified = 0;

        Map<String, String> linksMap = new HashMap<>();

        List<SubjectListResponse.ComponentData.Card.UserExercise> contentModel = response.componentProps.initialCards.userExercises;
        for (SubjectListResponse.ComponentData.Card.UserExercise content : contentModel) {

            if (content.exerciseModel == null) {
                continue;
            }

            if (content.exerciseModel.allAssessmentItems == null) {
                continue;
            }

            exerciseList = content.exerciseModel.allAssessmentItems;
            exerciseId = content.exerciseModel.id;
            nodeSlug = content.exerciseModel.nodeSlug;
            dateModified = ContentScraperUtil.parseServerDate(content.exerciseModel.dateModified);

            List<SubjectListResponse.ComponentData.Card.UserExercise.Model> relatedList = content.exerciseModel.relatedContent;

            if (relatedList != null) {

                for (SubjectListResponse.ComponentData.Card.UserExercise.Model relatedLink : relatedList) {
                    if (relatedLink == null) {
                        continue;
                    }
                    linksMap.put(relatedLink.kaUrl, "content-detail?sourceUrl=khan-id://" + relatedLink.id);
                }
            }
            List<SubjectListResponse.ComponentData.Card.UserExercise.Model> relatedVideos = content.exerciseModel.relatedVideos;

            if (relatedVideos != null) {

                for (SubjectListResponse.ComponentData.Card.UserExercise.Model relatedLink : relatedVideos) {
                    if (relatedLink == null) {
                        continue;
                    }
                    linksMap.put(relatedLink.kaUrl, "content-detail?sourceUrl=khan-id://" + relatedLink.id);
                }
            }

            break;

        }

        boolean isUpdated;
        File modifiedFile = new File(destinationDirectory, destinationDirectory.getName() + ScraperConstants.LAST_MODIFIED_TXT);
        isUpdated = ContentScraperUtil.isFileContentsUpdated(modifiedFile, String.valueOf(dateModified));

        File indexJsonFile = new File(khanDirectory, "index.json");

        if (ContentScraperUtil.fileHasContent(khanDirectory)) {
            isUpdated = false;
            FileUtils.deleteDirectory(khanDirectory);
        }

        if (!isUpdated) {
            isContentUpdated = false;
            return;
        }

        if (driver == null) {
            ContentScraperUtil.setChromeDriverLocation();
            driver = ContentScraperUtil.loginKhanAcademy();
        } else {
            ContentScraperUtil.clearChromeConsoleLog(driver);
        }

        driver.get(scrapUrl);
        WebDriverWait waitDriver = new WebDriverWait(driver, TIME_OUT_SELENIUM);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);
        try {
            waitDriver.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div[data-test-id=tutorial-page]")));
            driver.findElement(By.cssSelector("div[class*=calculatorButton")).click();
        } catch (Exception e) {
            UMLogUtil.logDebug(ExceptionUtils.getStackTrace(e));
        }

        List<LogEntry> les = ContentScraperUtil.waitForNewFiles(driver);

        List<LogIndex.IndexEntry> indexList = new ArrayList<>();

        for (LogEntry le : les) {

            LogResponse log = gson.fromJson(le.getMessage(), LogResponse.class);
            if (RESPONSE_RECEIVED.equalsIgnoreCase(log.message.method)) {
                String mimeType = log.message.params.response.mimeType;
                String urlString = log.message.params.response.url;

                try {

                    URL url = new URL(urlString);
                    File urlDirectory = ContentScraperUtil.createDirectoryFromUrl(khanDirectory, url);
                    File file = ContentScraperUtil.downloadFileFromLogIndex(url, urlDirectory, log);

                    if (urlString.equals(scrapUrl)) {

                        String khanContent = FileUtils.readFileToString(file, UTF_ENCODING);
                        Document doc = Jsoup.parse(khanContent);
                        doc.head().append(KHAN_CSS);
                        doc.head().append(KHAN_COOKIE);

                        FileUtils.writeStringToFile(file, doc.html(), UTF_ENCODING);

                    }

                    LogIndex.IndexEntry logIndex = ContentScraperUtil.createIndexFromLog(urlString, mimeType, urlDirectory, file, log);
                    indexList.add(logIndex);

                } catch (Exception e) {
                    UMLogUtil.logError(urlString);
                    UMLogUtil.logDebug(le.getMessage());
                }

            }

        }

        if (exerciseList == null) {
            UMLogUtil.logInfo("Did not get exercise list for url " + scrapUrl);
            return;
        }


        int exerciseCount = 1;
        for (SubjectListResponse.ComponentData.Card.UserExercise.Model.AssessmentItem exercise : exerciseList) {
            URL practiceUrl = new URL(secondExerciseUrl + exerciseId + exerciseMidleUrl + exercise.id + exercisePostUrl);

            File urlFile = ContentScraperUtil.createDirectoryFromUrl(khanDirectory, practiceUrl);
            File file = new File(urlFile, exerciseCount + " question");

            String itemData = IOUtils.toString(practiceUrl, UTF_ENCODING);
            FileUtils.writeStringToFile(file, itemData, UTF_ENCODING);
            ItemResponse itemResponse = gson.fromJson(itemData, ItemResponse.class);

            LogIndex.IndexEntry exerciseIndex = ContentScraperUtil.createIndexFromLog(practiceUrl.toString(), MIMETYPE_JSON,
                    urlFile, file, null);
            indexList.add(exerciseIndex);

            ItemData itemContent = gson.fromJson(itemResponse.itemData, ItemData.class);

            Map<String, ItemData.Content.Image> images = itemContent.question.images;
            if (images == null) {
                images = new HashMap<>();
            }
            for (ItemData.Content content : itemContent.hints) {
                if (content.images == null) {
                    continue;
                }
                images.putAll(content.images);
            }

            Pattern p = Pattern.compile("\\(([^)]+)\\)");
            Matcher m = p.matcher(itemContent.question.content);

            while (m.find()) {
                images.put(m.group(1), null);
            }

            if (itemContent.question.widgets != null) {

                for (ItemData.Content.Widget widget : itemContent.question.widgets.values()) {

                    if (widget.options != null) {

                        if (widget.options.options != null) {

                            for (ItemData.Content.Widget.Options.Option option : widget.options.options) {

                                Matcher matcher = p.matcher(option.content);
                                while (matcher.find()) {
                                    images.put(matcher.group(1), null);
                                }

                            }


                        }


                    }

                }

            }

            ContentScraperUtil.downloadImagesFromJsonContent(images, khanDirectory, scrapUrl, indexList);

            exerciseCount++;

        }

        SubjectListResponse.ComponentData.NavData navData = response.componentProps.tutorialPageData;
        if (navData == null) {
            navData = response.componentProps.tutorialNavData;
        }

        List<SubjectListResponse.ComponentData.NavData.ContentModel> navList = navData.navItems;
        if (navList != null) {

            for (SubjectListResponse.ComponentData.NavData.ContentModel navItem : navList) {
                if (navItem.nodeSlug.equals(nodeSlug)) {
                    continue;
                }
                linksMap.put(regexUrlPrefix + navItem.nodeSlug, CONTENT_DETAIL_SOURCE_URL_KHAN_ID + navItem.id);
            }

        }

        LogIndex.IndexEntry hintIndex = ContentScraperUtil.createIndexWithResourceFiles("https://www.khanacademy.org/hint",
                khanDirectory, MIMETYPE_JSON, getClass().getResourceAsStream(HINT_JSON_LINK), HINT_JSON_FILE);
        indexList.add(hintIndex);

        LogIndex.IndexEntry attemptIndex = ContentScraperUtil.createIndexWithResourceFiles("https://www.khanacademy.org/attempt",
                khanDirectory, MIMETYPE_JSON, getClass().getResourceAsStream(ATTEMPT_JSON_LINK), ATTEMPT_JSON_FILE);
        indexList.add(attemptIndex);

        LogIndex.IndexEntry correctIndex = ContentScraperUtil.createIndexWithResourceFiles("https://cdn.kastatic.org/images/exercise-correct.svg",
                khanDirectory, MIMETYPE_SVG, getClass().getResourceAsStream(CORRECT_KHAN_LINK), CORRECT_FILE);
        indexList.add(correctIndex);

        LogIndex.IndexEntry tryAgainIndex = ContentScraperUtil.createIndexWithResourceFiles("https://cdn.kastatic.org/images/exercise-try-again.svg",
                khanDirectory, MIMETYPE_SVG, getClass().getResourceAsStream(TRY_AGAIN_KHAN_LINK), TRY_AGAIN_FILE);
        indexList.add(tryAgainIndex);

        LogIndex.IndexEntry attmeptIndex = ContentScraperUtil.createIndexWithResourceFiles("https://cdn.kastatic.org/images/end-of-task-card/star-attempted.svg",
                khanDirectory, MIMETYPE_SVG, getClass().getResourceAsStream(ATTEMPT_KHAN_LINK), ATTEMPT_FILE);
        indexList.add(attmeptIndex);

        LogIndex.IndexEntry completeIndex = ContentScraperUtil.createIndexWithResourceFiles("https://cdn.kastatic.org/images/end-of-task-card/star-complete.svg",
                khanDirectory, MIMETYPE_SVG, getClass().getResourceAsStream(COMPLETE_KHAN_LINK), COMPLETE_FILE);
        indexList.add(completeIndex);

        LogIndex.IndexEntry khanCssFile = ContentScraperUtil.createIndexWithResourceFiles("https://www.khanacademy.org/khanscraper.css",
                khanDirectory, MIMETYPE_CSS, getClass().getResourceAsStream(KHAN_CSS_LINK), KHAN_CSS_FILE);
        indexList.add(khanCssFile);

        LogIndex.IndexEntry internalPractice = ContentScraperUtil.createIndexWithResourceFiles("https://www.khanacademy.org/api/internal/user/task/practice/",
                khanDirectory, MIMETYPE_JSON, getClass().getResourceAsStream(INTERNAL_JSON_LINK), INTERNAL_FILE);
        indexList.add(internalPractice);

        LogIndex index = new LogIndex();
        index.title = KHAN;
        index.entries = indexList;
        index.links = linksMap;

        FileUtils.writeStringToFile(indexJsonFile, gson.toJson(index), UTF_ENCODING);
    }


    public void scrapeArticleContent(String scrapUrl) throws IOException {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        File khanDirectory = new File(destinationDirectory, destinationDirectory.getName());
        khanDirectory.mkdirs();

        File indexJsonFile = new File(khanDirectory, "index.json");

        String initialJson = KhanContentIndexer.getJsonStringFromScript(scrapUrl);
        SubjectListResponse data = gson.fromJson(initialJson, SubjectListResponse.class);
        if (data.componentProps == null) {
            data = gson.fromJson(initialJson, PropsSubjectResponse.class).props;
        }

        SubjectListResponse.ComponentData compProps = data.componentProps;
        SubjectListResponse.ComponentData.NavData navData = compProps.tutorialNavData;
        if (navData == null) {
            navData = compProps.tutorialPageData;
        }
        List<SubjectListResponse.ComponentData.NavData.ContentModel> contentList = navData.contentModels;
        if (contentList == null || contentList.isEmpty()) {
            contentList = new ArrayList<>();
            contentList.add(navData.contentModel);
        }

        if (contentList.isEmpty()) {
            throw new IllegalArgumentException("Does not have the article data id which we need to scrape the page for url " + scrapUrl);
        }


        boolean foundRelative = false;
        for (SubjectListResponse.ComponentData.NavData.ContentModel content : contentList) {

            if (destinationDirectory.getName().contains(content.id) || scrapUrl.contains(content.relativeUrl)) {

                foundRelative = true;
                String articleId = content.id;
                nodeSlug = content.nodeSlug;
                String articleUrl = generateArtcleUrl(articleId);
                ArticleResponse response = gson.fromJson(IOUtils.toString(new URL(articleUrl), UTF_ENCODING), ArticleResponse.class);
                long dateModified = ContentScraperUtil.parseServerDate(response.date_modified);

                boolean isUpdated;
                File modifiedFile = new File(destinationDirectory, destinationDirectory.getName() + ScraperConstants.LAST_MODIFIED_TXT);
                isUpdated = ContentScraperUtil.isFileContentsUpdated(modifiedFile, String.valueOf(dateModified));

                if (ContentScraperUtil.fileHasContent(khanDirectory)) {
                    isUpdated = false;
                    FileUtils.deleteDirectory(khanDirectory);
                }

                if (!isUpdated) {
                    isContentUpdated = false;
                    return;
                }

                break;

            }
        }
        if (foundRelative) {
            UMLogUtil.logDebug("found the id at url " + scrapUrl);
        } else {
            throw new IllegalArgumentException("did not find id at url " + scrapUrl);
        }


        if (driver == null) {
            ContentScraperUtil.setChromeDriverLocation();
            driver = ContentScraperUtil.setupLogIndexChromeDriver();
        } else {
            ContentScraperUtil.clearChromeConsoleLog(driver);
        }

        driver.get(scrapUrl);
        WebDriverWait waitDriver = new WebDriverWait(driver, TIME_OUT_SELENIUM);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);
        try {
            waitDriver.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("ul[class*=listWrapper], div[class*=listWrapper")));
            driver.findElement(By.cssSelector("div[class*=calculatorButton")).click();
        } catch (Exception e) {
            UMLogUtil.logDebug(ExceptionUtils.getStackTrace(e));
        }

        List<LogEntry> les = ContentScraperUtil.waitForNewFiles(driver);

        List<LogIndex.IndexEntry> index = new ArrayList<>();

        for (LogEntry le : les) {

            LogResponse log = gson.fromJson(le.getMessage(), LogResponse.class);
            if (RESPONSE_RECEIVED.equalsIgnoreCase(log.message.method)) {
                String mimeType = log.message.params.response.mimeType;
                String urlString = log.message.params.response.url;

                try {
                    URL url = new URL(urlString);
                    File urlDirectory = ContentScraperUtil.createDirectoryFromUrl(khanDirectory, url);
                    File file = ContentScraperUtil.downloadFileFromLogIndex(url, urlDirectory, log);


                    if (urlString.equals(scrapUrl)) {

                        String khanContent = FileUtils.readFileToString(file, UTF_ENCODING);
                        Document doc = Jsoup.parse(khanContent);
                        doc.head().append(KHAN_CSS);
                        doc.head().append(KHAN_COOKIE);

                        FileUtils.writeStringToFile(file, doc.html(), UTF_ENCODING);

                    }

                    LogIndex.IndexEntry logIndex = ContentScraperUtil.createIndexFromLog(urlString, mimeType, urlDirectory, file, log);
                    index.add(logIndex);


                } catch (Exception e) {
                    UMLogUtil.logDebug("Index url failed at " + urlString);
                    UMLogUtil.logInfo(le.getMessage());
                }

            }

        }

        Map<String, String> linkMap = new HashMap<>();
        List<SubjectListResponse.ComponentData.NavData.ContentModel> navList = navData.navItems;
        if (navList != null) {
            for (SubjectListResponse.ComponentData.NavData.ContentModel navItem : navList) {
                if (navItem.nodeSlug.equals(nodeSlug)) {
                    continue;
                }
                linkMap.put(regexUrlPrefix + navItem.nodeSlug, CONTENT_DETAIL_SOURCE_URL_KHAN_ID + navItem.id);
            }
        } else {
            UMLogUtil.logError("Your related items are in another json for url " + scrapUrl);
        }


        LogIndex.IndexEntry khanCssFile = ContentScraperUtil.createIndexWithResourceFiles("https://www.khanacademy.org/khanscraper.css",
                khanDirectory, MIMETYPE_CSS, getClass().getResourceAsStream(KHAN_CSS_LINK), KHAN_CSS_FILE);
        index.add(khanCssFile);

        LogIndex logIndex = new LogIndex();
        logIndex.title = KHAN;
        logIndex.entries = index;
        logIndex.links = linkMap;


        FileUtils.writeStringToFile(indexJsonFile, gson.toJson(logIndex), UTF_ENCODING);
    }

    private String generateArtcleUrl(String articleId) {
        return "http://www.khanacademy.org/api/v1/articles/" + articleId;
    }


}
