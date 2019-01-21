package com.ustadmobile.lib.contentscrapers.khanacademy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.LogIndex;
import com.ustadmobile.lib.contentscrapers.LogResponse;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
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
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_JPG;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_JSON;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MIMETYPE_SVG;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.SVG_EXT;
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
public class KhanContentScraper {

    private final File destinationDirectory;
    private ChromeDriver driver;

    private String secondExerciseUrl = "https://www.khanacademy.org/api/internal/user/exercises/";

    private String exerciseMidleUrl = "/items/";

    private String exercisePostUrl = "/assessment_item";

    private boolean isContentUpdated = true;

    public KhanContentScraper(File destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
        this.driver = null;
    }

    public KhanContentScraper(File destinationDirectory, ChromeDriver driver){
        this.destinationDirectory = destinationDirectory;
        this.driver = driver;
    }

    public boolean isContentUpdated() {
        return isContentUpdated;
    }

    public void scrapeVideoContent(String url) throws IOException {

        URL scrapUrl = new URL(url);

        File folder = new File(destinationDirectory, destinationDirectory.getName());
        folder.mkdirs();


        File content = new File(folder, FilenameUtils.getName(scrapUrl.getPath()));
        URLConnection conn = scrapUrl.openConnection();
        if (!ContentScraperUtil.isFileModified(conn, folder, FilenameUtils.getBaseName(url)) && ContentScraperUtil.fileHasContent(content)) {
            isContentUpdated = false;
            return;
        }

        FileUtils.copyURLToFile(scrapUrl, content);
        ContentScraperUtil.zipDirectory(folder, destinationDirectory.getName(), destinationDirectory);

    }


    public void scrapeExerciseContent(String scrapUrl) throws IOException {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        File khanDirectory = new File(destinationDirectory, FilenameUtils.getBaseName(scrapUrl));
        khanDirectory.mkdirs();

        String initialJson = IndexKhanContentScraper.getJsonStringFromScript(scrapUrl);
        SubjectListResponse response = gson.fromJson(initialJson, SubjectListResponse.class);
        String exerciseId = "0";
        List<SubjectListResponse.ComponentData.Card.UserExercise.Model.AssessmentItem> exerciseList = null;
        long dateModified = 0;

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
            dateModified = ContentScraperUtil.parseServerDate(content.exerciseModel.dateModified);

            break;

        }

        boolean isUpdated = true;
        File modifiedFile = new File(khanDirectory, FilenameUtils.getBaseName(exerciseId) + ScraperConstants.LAST_MODIFIED_TXT);
        String text;

        if (ContentScraperUtil.fileHasContent(modifiedFile)) {
            text = FileUtils.readFileToString(modifiedFile, UTF_ENCODING);
            isUpdated = !String.valueOf(dateModified).equalsIgnoreCase(text);
        } else {
            FileUtils.writeStringToFile(modifiedFile, String.valueOf(dateModified), ScraperConstants.UTF_ENCODING);
        }

        File indexJsonFile = new File(khanDirectory, "index.json");

        if (!isUpdated && ContentScraperUtil.fileHasContent(indexJsonFile)) {
            isContentUpdated = false;
            return;
        }

        if(driver == null){
            ContentScraperUtil.setChromeDriverLocation();
            driver = ContentScraperUtil.loginKhanAcademy("https://www.khanacademy.org/login");
        }else{
            ContentScraperUtil.clearChromeConsoleLog(driver);
        }

        driver.get(scrapUrl);
        WebDriverWait waitDriver = new WebDriverWait(driver, 10000);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);
        try {
            waitDriver.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div[data-test-id=tutorial-page]")));
        } catch (Exception e) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
        }

        LogEntries les = driver.manage().logs().get(LogType.PERFORMANCE);

        List<LogIndex> index = new ArrayList<>();

        for (LogEntry le : les) {

            LogResponse log = gson.fromJson(le.getMessage(), LogResponse.class);
            if (RESPONSE_RECEIVED.equalsIgnoreCase(log.message.method)) {
                String mimeType = log.message.params.response.mimeType;
                String urlString = log.message.params.response.url;

                try {

                    URL url = new URL(urlString);
                    File urlDirectory = ContentScraperUtil.createDirectoryFromUrl(khanDirectory, url);
                    File file = ContentScraperUtil.downloadFileFromLogIndex(url, urlDirectory, log);

                    if(log.message.params.response.headers != null) {
                        if(log.message.params.response.headers.containsKey("set-cookie")){
                            String cookie = log.message.params.response.headers.get("set-cookie");
                            cookie += " fkey=abcdef;";
                            log.message.params.response.headers.replace("set-cookie", cookie);
                        }
                    }

                    LogIndex logIndex = ContentScraperUtil.createIndexFromLog(urlString, mimeType, urlDirectory, file, log);
                    index.add(logIndex);

                } catch (Exception e) {
                    UMLogUtil.logError(urlString);
                    UMLogUtil.logInfo(le.getMessage());
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

            LogIndex exerciseIndex = ContentScraperUtil.createIndexFromLog(practiceUrl.toString(), MIMETYPE_JSON,
                    urlFile, file, null);
            index.add(exerciseIndex);

            ItemData itemContent = gson.fromJson(itemResponse.itemData, ItemData.class);

            Map<String, ItemData.Content.Image> images = itemContent.question.images;
            if(images == null){
                images = new HashMap<>();
            }
            for (ItemData.Content content : itemContent.hints) {
                if(content.images == null){
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


            for (String image : images.keySet()) {

                try {
                    image = image.replaceAll(" ", "");
                    String imageUrlString = image;
                    if(image.contains("+graphie")){
                        imageUrlString = "https://cdn.kastatic.org/ka-perseus-graphie/" + image.substring(image.lastIndexOf("/") + 1) + SVG_EXT;
                    }
                    URL imageUrl = new URL(imageUrlString);
                    File imageFile = ContentScraperUtil.createDirectoryFromUrl(khanDirectory, imageUrl);

                    File imageContent = new File(imageFile, FilenameUtils.getName(imageUrl.getPath()));
                    FileUtils.copyURLToFile(imageUrl, imageContent);

                    LogIndex logIndex = ContentScraperUtil.createIndexFromLog(image, MIMETYPE_JPG,
                            imageFile, imageContent, null);
                    index.add(logIndex);
                } catch (Exception e) {
                    UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                    UMLogUtil.logError("Error downloading an image for index log" + image + " with url " + scrapUrl);
                }

            }

            exerciseCount++;

        }

        LogIndex hintIndex = ContentScraperUtil.createIndexWithResourceFiles("https://www.khanacademy.org/hint",
                khanDirectory, MIMETYPE_JSON, getClass().getResourceAsStream(HINT_JSON_LINK), HINT_JSON_FILE);
        index.add(hintIndex);

        LogIndex attemptIndex = ContentScraperUtil.createIndexWithResourceFiles("https://www.khanacademy.org/attempt",
                khanDirectory, MIMETYPE_JSON, getClass().getResourceAsStream(ATTEMPT_JSON_LINK), ATTEMPT_JSON_FILE);
        index.add(attemptIndex);

        LogIndex correctIndex = ContentScraperUtil.createIndexWithResourceFiles("https://cdn.kastatic.org/images/exercise-correct.svg",
                khanDirectory, MIMETYPE_SVG, getClass().getResourceAsStream(CORRECT_KHAN_LINK), CORRECT_FILE);
        index.add(correctIndex);

        LogIndex tryAgainIndex = ContentScraperUtil.createIndexWithResourceFiles("https://cdn.kastatic.org/images/exercise-try-again.svg",
                khanDirectory, MIMETYPE_SVG, getClass().getResourceAsStream(TRY_AGAIN_KHAN_LINK), TRY_AGAIN_FILE);
        index.add(tryAgainIndex);

        LogIndex attmeptIndex = ContentScraperUtil.createIndexWithResourceFiles("https://cdn.kastatic.org/images/end-of-task-card/star-attempted.svg",
                khanDirectory, MIMETYPE_SVG, getClass().getResourceAsStream(ATTEMPT_KHAN_LINK), ATTEMPT_FILE);
        index.add(attmeptIndex);

        LogIndex completeIndex = ContentScraperUtil.createIndexWithResourceFiles("https://cdn.kastatic.org/images/end-of-task-card/star-complete.svg",
                khanDirectory, MIMETYPE_SVG, getClass().getResourceAsStream(COMPLETE_KHAN_LINK), COMPLETE_FILE);
        index.add(completeIndex);


        FileUtils.writeStringToFile(indexJsonFile, gson.toJson(index), UTF_ENCODING);
        ContentScraperUtil.zipDirectory(khanDirectory, khanDirectory.getName(), destinationDirectory);

    }

    public void scrapeArticleContent(String scrapUrl) throws IOException {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        File khanDirectory = new File(destinationDirectory, FilenameUtils.getBaseName(scrapUrl));
        khanDirectory.mkdirs();

        File indexJsonFile = new File(khanDirectory, "index.json");

        String initialJson = IndexKhanContentScraper.getJsonStringFromScript(scrapUrl);
        SubjectListResponse data = gson.fromJson(initialJson, SubjectListResponse.class);
        SubjectListResponse.ComponentData compProps = data.componentProps;
        SubjectListResponse.ComponentData.NavData navData = compProps.tutorialNavData;
        if(navData == null){
            navData = compProps.tutorialPageData;
        }
        List<SubjectListResponse.ComponentData.NavData.ContentModel> contentList = navData.contentModels;
        if(contentList == null || contentList.isEmpty()){
            contentList = new ArrayList<>();
            contentList.add(navData.contentModel);
        }

        if(contentList.isEmpty()){
            throw new IllegalArgumentException("Does not have the article data id which we need to scrape the page for url " + scrapUrl);
        }

        for (SubjectListResponse.ComponentData.NavData.ContentModel content : contentList) {

            if (content.relativeUrl.contains(scrapUrl)) {

                String articleId = content.id;
                String articleUrl = generateArtcleUrl(articleId);
                ArticleResponse response = gson.fromJson(IOUtils.toString(new URL(articleUrl), UTF_ENCODING), ArticleResponse.class);
                long dateModified = ContentScraperUtil.parseServerDate(response.date_modified);

                File modifiedFile = new File(khanDirectory, articleId + ScraperConstants.LAST_MODIFIED_TXT);
                String text;

                boolean isUpdated = true;
                if (ContentScraperUtil.fileHasContent(modifiedFile)) {
                    text = FileUtils.readFileToString(modifiedFile, UTF_ENCODING);
                    isUpdated = !String.valueOf(dateModified).equalsIgnoreCase(text);
                } else {
                    FileUtils.writeStringToFile(modifiedFile, String.valueOf(dateModified), ScraperConstants.UTF_ENCODING);
                }

                if (!isUpdated && ContentScraperUtil.fileHasContent(indexJsonFile)) {
                    isContentUpdated = false;
                    return;
                }

                break;

            }
        }

        if(driver == null){
            ContentScraperUtil.setChromeDriverLocation();
            driver = ContentScraperUtil.setupLogIndexChromeDriver();
        }else{
            ContentScraperUtil.clearChromeConsoleLog(driver);
        }

        driver.get(scrapUrl);
        WebDriverWait waitDriver = new WebDriverWait(driver, 10000);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);
        try {
            waitDriver.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("ul[class*=listWrapper]")));
            Thread.sleep(5000);
        } catch (Exception e) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
        }

        LogEntries les = driver.manage().logs().get(LogType.PERFORMANCE);

        List<LogIndex> index = new ArrayList<>();

        for (LogEntry le : les) {

            LogResponse log = gson.fromJson(le.getMessage(), LogResponse.class);
            if (RESPONSE_RECEIVED.equalsIgnoreCase(log.message.method)) {
                String mimeType = log.message.params.response.mimeType;
                String urlString = log.message.params.response.url;

                try {
                    URL url = new URL(urlString);
                    File urlDirectory = ContentScraperUtil.createDirectoryFromUrl(khanDirectory, url);
                    File file = ContentScraperUtil.downloadFileFromLogIndex(url, urlDirectory, log);

                    LogIndex logIndex = ContentScraperUtil.createIndexFromLog(urlString, mimeType, urlDirectory, file, log);
                    index.add(logIndex);

                } catch (Exception e) {
                    UMLogUtil.logError("Index url failed at " + urlString);
                    UMLogUtil.logInfo(le.getMessage());
                }


            }

        }
        FileUtils.writeStringToFile(indexJsonFile, gson.toJson(index), UTF_ENCODING);
        ContentScraperUtil.zipDirectory(khanDirectory, khanDirectory.getName(), destinationDirectory);

    }

    private String generateArtcleUrl(String articleId) {
        return "http://www.khanacademy.org/api/v1/articles/" + articleId;
    }
}
