package com.ustadmobile.lib.contentscrapers.khanacademy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.LogIndex;
import com.ustadmobile.lib.contentscrapers.ck12.plix.PlixLog;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TRY_AGAIN_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TRY_AGAIN_KHAN_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static com.ustadmobile.lib.contentscrapers.ck12.CK12ContentScraper.RESPONSE_RECEIVED;

public class KhanContentScraper {

    private final File destinationDirectory;

    String exerciseUrl = "https://www.khanacademy.org/api/internal/user/exercises/";

    String exerciseNameUrl = "/problems/";

    String postFixUrl = "/assessment_item?last_seen_problem_sha=";

    String secondExerciseUrl = "https://www.khanacademy.org/api/internal/user/exercises/";

    String exerciseMidleUrl = "/items/";

    String exercisePostUrl = "/assessment_item";

    private boolean isContentUpdated = true;

    public KhanContentScraper(File destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
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
        if (!ContentScraperUtil.isFileModified(conn, folder, FilenameUtils.getBaseName(url))) {
            isContentUpdated = false;
            return;
        }

        FileUtils.copyURLToFile(scrapUrl, content);
        ContentScraperUtil.zipDirectory(folder, destinationDirectory.getName(), destinationDirectory);

    }


    public void scrapeExerciseContent(String scrapUrl) throws IOException {

        ContentScraperUtil.setChromeDriverLocation();

        ChromeDriver driver = ContentScraperUtil.getCookieForKhan("https://www.khanacademy.org/login");

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

        if(!isUpdated){
            isContentUpdated = false;
            return;
        }

        driver.get(scrapUrl);
        WebDriverWait waitDriver = new WebDriverWait(driver, 10000);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);
        try {
            waitDriver.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div[data-test-id=tutorial-page]")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        LogEntries les = driver.manage().logs().get(LogType.PERFORMANCE);
        driver.close();

        List<LogIndex> index = new ArrayList<>();

        for (LogEntry le : les) {

            PlixLog log = gson.fromJson(le.getMessage(), PlixLog.class);
            if (RESPONSE_RECEIVED.equalsIgnoreCase(log.message.method)) {
                String mimeType = log.message.params.response.mimeType;
                String urlString = log.message.params.response.url;

                try {
                    URL url = new URL(urlString);
                    File urlFile = new File(khanDirectory, url.getAuthority().replaceAll("[^a-zA-Z0-9\\.\\-]", "_"));
                    urlFile.mkdirs();
                    String fileName = ContentScraperUtil.getFileNameFromUrl(url);
                    File file = new File(urlFile, fileName);
                    if (log.message.params.response.requestHeaders != null) {
                        URLConnection conn = url.openConnection();
                        for (Map.Entry<String, String> e : log.message.params.response.requestHeaders.entrySet()) {
                            if (e.getKey().equalsIgnoreCase("Accept-Encoding")) {
                                continue;
                            }
                            conn.addRequestProperty(e.getKey().replaceAll(":", ""), e.getValue());
                        }
                        FileUtils.copyInputStreamToFile(conn.getInputStream(), file);
                    } else {
                        FileUtils.copyURLToFile(url, file);
                    }

                    LogIndex logIndex = new LogIndex();
                    logIndex.url = urlString;
                    logIndex.mimeType = mimeType;
                    logIndex.path = urlFile.getName() + "/" + file.getName();
                    logIndex.headers = log.message.params.response.headers;

                    index.add(logIndex);

                } catch (Exception e) {
                    System.err.println(urlString);
                    System.err.println(le.getMessage());
                    e.printStackTrace();

                }


            }

        }

        if (exerciseList == null) {
            System.err.println("Did not get exercise list for url " + scrapUrl);
            return;
        }


        int exerciseCount = 1;
        for (SubjectListResponse.ComponentData.Card.UserExercise.Model.AssessmentItem exercise : exerciseList) {
            URL practiceUrl = new URL(secondExerciseUrl + exerciseId + exerciseMidleUrl + exercise.id + exercisePostUrl);

            File urlFile = new File(khanDirectory, practiceUrl.getAuthority().replaceAll("[^a-zA-Z0-9\\.\\-]", "_"));
            urlFile.mkdirs();

            File file = new File(urlFile, exerciseCount + " question");

            String itemData = IOUtils.toString(practiceUrl, UTF_ENCODING);
            FileUtils.writeStringToFile(file, itemData, UTF_ENCODING);
            ItemResponse itemResponse = gson.fromJson(itemData, ItemResponse.class);

            LogIndex exerciseIndex = new LogIndex();
            exerciseIndex.url = practiceUrl.toString();
            exerciseIndex.mimeType = MIMETYPE_JSON;
            exerciseIndex.path = urlFile.getName() + "/" + file.getName();

            index.add(exerciseIndex);

            ItemData itemContent = gson.fromJson(itemResponse.itemData, ItemData.class);

            Map<String, ItemData.Content.Image> images = itemContent.question.images;
            for (ItemData.Content content : itemContent.hints) {
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
                    URL imageUrl = new URL(image);
                    File imageFile = new File(khanDirectory, imageUrl.getAuthority().replaceAll("[^a-zA-Z0-9\\.\\-]", "_"));
                    imageFile.mkdirs();

                    File imageContent = new File(imageFile, FilenameUtils.getName(imageUrl.getPath()));
                    FileUtils.copyURLToFile(imageUrl, imageContent);

                    LogIndex khanImages = new LogIndex();
                    khanImages.url = imageUrl.toString();
                    khanImages.mimeType = MIMETYPE_JPG;
                    khanImages.path = imageFile.getName() + "/" + imageContent.getName();

                    index.add(khanImages);
                } catch (Exception e) {
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


        FileUtils.writeStringToFile(new File(khanDirectory, "index.json"), gson.toJson(index), UTF_ENCODING);
        ContentScraperUtil.zipDirectory(khanDirectory, khanDirectory.getName(), destinationDirectory);

    }

    public void scrapeArticleContent(String scrapUrl) throws IOException {

        ContentScraperUtil.setChromeDriverLocation();

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        File khanDirectory = new File(destinationDirectory, FilenameUtils.getBaseName(scrapUrl));
        khanDirectory.mkdirs();

        String initialJson = IndexKhanContentScraper.getJsonStringFromScript(scrapUrl);
        SubjectListResponse data = gson.fromJson(initialJson, SubjectListResponse.class);
        List<SubjectListResponse.ComponentData.NavData.ContentModel> contentList = data.componentProps.tutorialNavData.contentModels;

        for(SubjectListResponse.ComponentData.NavData.ContentModel content: contentList){

            if(content.relativeUrl.contains(scrapUrl)){

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

                if(!isUpdated){
                    isContentUpdated = false;
                    return;
                }

                break;

            }
        }


        DesiredCapabilities d = DesiredCapabilities.chrome();
        d.setCapability("opera.arguments", "-screenwidth 1024 -screenheight 768");
        // d.merge(capabilities);
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        d.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        ChromeDriver driver = new ChromeDriver(d);


        driver.get(scrapUrl);
        WebDriverWait waitDriver = new WebDriverWait(driver, 10000);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);
        try {
            waitDriver.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("ul[class*=listWrapper]")));
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LogEntries les = driver.manage().logs().get(LogType.PERFORMANCE);
        driver.close();

        List<LogIndex> index = new ArrayList<>();

        for (LogEntry le : les) {

            PlixLog log = gson.fromJson(le.getMessage(), PlixLog.class);
            if (RESPONSE_RECEIVED.equalsIgnoreCase(log.message.method)) {
                String mimeType = log.message.params.response.mimeType;
                String urlString = log.message.params.response.url;

                try {
                    URL url = new URL(urlString);
                    File urlFile = new File(khanDirectory, url.getAuthority().replaceAll("[^a-zA-Z0-9\\.\\-]", "_"));
                    urlFile.mkdirs();
                    String fileName = ContentScraperUtil.getFileNameFromUrl(url);
                    File file = new File(urlFile, fileName);
                    if (log.message.params.response.requestHeaders != null) {
                        URLConnection conn = url.openConnection();
                        for (Map.Entry<String, String> e : log.message.params.response.requestHeaders.entrySet()) {
                            if (e.getKey().equalsIgnoreCase("Accept-Encoding")) {
                                continue;
                            }
                            conn.addRequestProperty(e.getKey().replaceAll(":", ""), e.getValue());
                        }
                        FileUtils.copyInputStreamToFile(conn.getInputStream(), file);
                    } else {
                        FileUtils.copyURLToFile(url, file);
                    }

                    LogIndex logIndex = new LogIndex();
                    logIndex.url = urlString;
                    logIndex.mimeType = mimeType;
                    logIndex.path = urlFile.getName() + "/" + file.getName();
                    logIndex.headers = log.message.params.response.headers;

                    index.add(logIndex);

                } catch (Exception e) {
                    System.err.println(urlString);
                    System.err.println(le.getMessage());
                    e.printStackTrace();

                }


            }

        }
        FileUtils.writeStringToFile(new File(khanDirectory, "index.json"), gson.toJson(index), UTF_ENCODING);
        ContentScraperUtil.zipDirectory(khanDirectory, khanDirectory.getName(), destinationDirectory);


    }

    private String generateArtcleUrl(String articleId) {
        return "http://www.khanacademy.org/api/v1/articles/" + articleId;
    }
}
