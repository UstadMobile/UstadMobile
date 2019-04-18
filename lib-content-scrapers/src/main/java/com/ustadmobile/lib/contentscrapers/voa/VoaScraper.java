package com.ustadmobile.lib.contentscrapers.voa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.JQUERY_JS;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MATERIAL_CSS;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ZIP_EXT;

/**
 * The Voice of America Website is an html website with content for learning english.
 * In a single page, you have access a lesson with lots of images, videos and a quiz.
 * <p>
 * By using jsoup, you can extract all the data that is required.
 * A script inside the page has details about the lesson and its last modified date.
 * <p>
 * Start the page by checking if there is quiz in the lesson - store the link to the quiz
 * Remove all the tags that are not required for us - comments, share, links to other pages.
 * <p>
 * All the quiz information is accessed at https://learningenglish.voanews.com/Quiz/Answer with a post request
 * Need to build a json of the quiz based on the data. The questions, choices, images and videos in the quiz.
 * <p>
 * Once you have all the data, download all the src in the page.
 * <p>
 * Store the quiz data, store the page data, add some css and tags to existing page to make it more mobile friendly
 * Write a tin can file for the html content and zip everything in the directory.
 */
public class VoaScraper implements Runnable {

    private int sqiUid;
    private ContentEntry parentEntry;
    private URL scrapUrl;
    private File voaDirectory;
    private File destinationDir;
    private boolean isContentUpdated = true;

    public String answerUrl = "https://learningenglish.voanews.com/Quiz/Answer";

    public VoaScraper(String url, File destinationDir) throws IOException {
        scrapUrl = new URL(url);
        this.destinationDir = destinationDir;
        voaDirectory = new File(destinationDir, FilenameUtils.getBaseName(scrapUrl.getPath()));
        voaDirectory.mkdirs();
    }

    public VoaScraper(URL scrapeUrl, File destinationDirectory, ContentEntry parent, int sqiUid) {
        this.destinationDir = destinationDirectory;
        this.scrapUrl = scrapeUrl;
        this.parentEntry = parent;
        this.sqiUid = sqiUid;
        voaDirectory = new File(destinationDir, FilenameUtils.getBaseName(scrapUrl.getPath()));
        voaDirectory.mkdirs();
    }


    @Override
    public void run() {
        System.gc();
        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        ContentEntryFileDao contentEntryFileDao = repository.getContentEntryFileDao();
        ContentEntryContentEntryFileJoinDao contentEntryFileJoin = repository.getContentEntryContentEntryFileJoinDao();
        ContentEntryFileStatusDao contentFileStatusDao = db.getContentEntryFileStatusDao();
        ScrapeQueueItemDao queueDao = db.getScrapeQueueItemDao();


        long startTime = System.currentTimeMillis();
        UMLogUtil.logInfo("Started scraper url " + scrapUrl + " at start time: " + startTime);
        queueDao.setTimeStarted(sqiUid, startTime);

        boolean successful = false;
        try {
            scrapeContent();

            File content = new File(destinationDir, FilenameUtils.getBaseName(scrapUrl.getPath()) + ScraperConstants.ZIP_EXT);
            successful = true;
            if (isContentUpdated()) {
                ContentScraperUtil.insertContentEntryFile(content, contentEntryFileDao, contentFileStatusDao,
                        parentEntry, ContentScraperUtil.getMd5(content), contentEntryFileJoin, true,
                        ScraperConstants.MIMETYPE_ZIP);
            } else {
                ContentScraperUtil.checkAndUpdateDatabaseIfFileDownloadedButNoDataFound(content, parentEntry, contentEntryFileDao,
                        contentEntryFileJoin, contentFileStatusDao, ScraperConstants.MIMETYPE_ZIP, true);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        queueDao.updateSetStatusById(sqiUid, successful ? ScrapeQueueItemDao.STATUS_DONE : ScrapeQueueItemDao.STATUS_FAILED);
        queueDao.setTimeFinished(sqiUid, System.currentTimeMillis());
        long duration = System.currentTimeMillis() - startTime;
        UMLogUtil.logInfo("Ended scrape for url " + scrapUrl + " in duration: " + duration);


    }

    public void scrapeContent() throws IOException {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        ContentScraperUtil.setChromeDriverLocation();

        ChromeDriver driver = ContentScraperUtil.setupChrome(true);

        driver.get(scrapUrl.toString());
        WebDriverWait waitDriver = new WebDriverWait(driver, TIME_OUT_SELENIUM);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);

        String lessonId = FilenameUtils.getBaseName(scrapUrl.getPath());

        File voaDirectory = new File(destinationDir, lessonId);
        voaDirectory.mkdirs();

        File zipFile = new File(destinationDir, FilenameUtils.getBaseName(scrapUrl.getPath()) + ZIP_EXT);

        try {
            WebElement element = driver.findElementByCssSelector("script[type*=json]");
            JavascriptExecutor js = driver;
            String scriptText = (String) js.executeScript("return arguments[0].innerText;", element);

            VoaResponse response = gson.fromJson(scriptText, VoaResponse.class);

            long dateModified = ContentScraperUtil.parseServerDate(response.dateModified.replace("Z", "").replace(" ", "T"));

            File modifiedFile = new File(voaDirectory, voaDirectory.getName() + ScraperConstants.LAST_MODIFIED_TXT);
            String text;


            boolean isUpdated = true;
            if (ContentScraperUtil.fileHasContent(modifiedFile)) {
                text = FileUtils.readFileToString(modifiedFile, UTF_ENCODING);
                isUpdated = !String.valueOf(dateModified).equalsIgnoreCase(text);
            } else {
                FileUtils.writeStringToFile(modifiedFile, String.valueOf(dateModified), ScraperConstants.UTF_ENCODING);
            }

            if (!isUpdated && ContentScraperUtil.fileHasContent(zipFile)) {
                isContentUpdated = false;
                driver.close();
                driver.quit();
                return;
            }

        } catch (NoSuchElementException ignored) {

            File file = new File(destinationDir, FilenameUtils.getBaseName(scrapUrl.getPath()) + ZIP_EXT);
            if (file.exists()) {
                long modified = ContentScraperUtil.parseServerDate(driver.findElementByCssSelector("time").getAttribute("datetime"));
                isContentUpdated = modified > file.lastModified();
                driver.close();
                driver.quit();
                return;
            }

        }
        String quizHref = null;
        String quizAjaxUrl = null;

        try {
            WebElement quizElement = driver.findElement(By.cssSelector("a[data-ajax-url*=Quiz]"));
            quizHref = quizElement.getAttribute("href");
            quizAjaxUrl = quizElement.getAttribute("data-ajax-url");
        } catch (NoSuchElementException ignored) {

        }

        Document document = Jsoup.connect(scrapUrl.toString()).get();
        removeAllAttributesFromVideoAudio(document);

        File assetDirectory = new File(voaDirectory, "asset");
        assetDirectory.mkdirs();

        driver.close();
        driver.quit();

        if (quizHref != null && !quizHref.isEmpty()) {

            VoaQuiz quizResponse = new VoaQuiz();
            String quizId = quizAjaxUrl.substring(quizAjaxUrl.indexOf("id=") + 3, quizAjaxUrl.indexOf("&"));
            int quizCount = 12;
            quizResponse.quizId = quizId;
            File quizFile = new File(voaDirectory, "questions.json");
            List<VoaQuiz.Questions> questionList = new ArrayList<>();

            for (int i = 1; i <= quizCount; i = i + 2) {

                HttpURLConnection conn = null;
                HttpURLConnection selectedConn = null;
                try {
                    URL answersUrl = new URL(answerUrl);
                    File urlDirectory = ContentScraperUtil.createDirectoryFromUrl(voaDirectory, answersUrl);
                    urlDirectory.mkdirs();

                    File questionPage = new File(urlDirectory, i + "question");

                    Map<String, String> params = createParams(quizId, i, null, "True");
                    StringBuffer requestParams = ContentScraperUtil.convertMapToStringBuffer(params);

                    conn = createConnectionForPost(answersUrl, requestParams);
                    conn.connect();

                    String questionData = IOUtils.toString(conn.getInputStream(), UTF_ENCODING);

                    Document questionDoc = Jsoup.parse(questionData);
                    removeAllAttributesFromVideoAudio(questionDoc);

                    String quizSize = questionDoc.select("span.caption").text();
                    quizSize = quizSize.substring(quizSize.length() - 1);
                    quizCount = Integer.valueOf(quizSize) * 2;

                    questionData = ContentScraperUtil.downloadAllResources(questionDoc.html(), assetDirectory, scrapUrl);
                    Element answerLabel = questionDoc.selectFirst("input[name=SelectedAnswerId]");
                    FileUtils.writeStringToFile(questionPage, questionData, UTF_ENCODING);

                    Document videoDoc = Jsoup.parse(questionData);

                    VoaQuiz.Questions question = new VoaQuiz.Questions();
                    question.questionText = questionDoc.selectFirst("h2.ta-l").text();
                    try {
                        Element mediaSource = videoDoc.selectFirst("div.quiz__answers-img video,div.quiz__answers-img img");
                        question.videoHref = mediaSource.attr("src");
                    } catch (NoSuchElementException | NullPointerException ignored) {

                    }

                    List<VoaQuiz.Questions.Choices> choiceList = new ArrayList<>();
                    Elements answerTextList = questionDoc.select("label.quiz__answers-label");
                    for (Element answer : answerTextList) {
                        VoaQuiz.Questions.Choices choices = new VoaQuiz.Questions.Choices();
                        choices.id = answer.selectFirst("input").attr("value");
                        choices.answerText = answer.selectFirst("span.quiz__answers-item-text").text();
                        choiceList.add(choices);
                    }
                    question.choices = choiceList;

                    String answerId = answerLabel.attr("value");

                    Map<String, String> selectedParams = createParams(quizId, i + 1, answerId, "False");

                    StringBuffer selectedRequestParams = ContentScraperUtil.convertMapToStringBuffer(selectedParams);

                    selectedConn = createConnectionForPost(answersUrl, selectedRequestParams);

                    File answerPage = new File(urlDirectory, answerId + "answersIndex");
                    selectedConn.connect();
                    FileUtils.copyInputStreamToFile(selectedConn.getInputStream(), answerPage);

                    Document selectedAnswerDoc = Jsoup.parse(answerPage, UTF_ENCODING);
                    question.answerId = selectedAnswerDoc.selectFirst("li.quiz__answers-item--correct input")
                            .attr("value");
                    question.answer = selectedAnswerDoc.selectFirst("p.p-t-md").text();
                    questionList.add(question);
                } catch (IOException e) {
                    UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                    if (selectedConn != null) {
                        selectedConn.disconnect();
                    }
                }


            }

            quizResponse.questions = questionList;
            FileUtils.writeStringToFile(quizFile, gson.toJson(quizResponse), UTF_ENCODING);

        }
        String voaData = ContentScraperUtil.downloadAllResources(document.selectFirst("div#content").html(), assetDirectory, scrapUrl);
        Document finalDoc = Jsoup.parse(voaData);
        finalDoc.head().append("<link rel=\"stylesheet\" href=\"asset/materialize.min.css\">");
        finalDoc.head().append("<meta charset=\"utf-8\" name=\"viewport\"\n" +
                "          content=\"width=device-width, initial-scale=1, shrink-to-fit=no,user-scalable=no\">");
        finalDoc.head().append("<link rel=\"stylesheet\" href=\"asset/voa.min.css\">");
        finalDoc.body().append("<script type=\"text/javascript\" src=\"asset/iframeResizer.min.js\"></script>");
        finalDoc.body().append("<script type=\"text/javascript\" src=\"asset/voa.min.js\"></script>");

        finalDoc.body().attr("style", "padding:2%");
        if (quizHref != null) {
            finalDoc.selectFirst("div.quiz__body").after("<div class=\"iframe-container\"><iframe src=\"quiz.html\" frameborder=\"0\" scrolling=\"no\" width=\"100%\"></frame></div>");
        }
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.QUIZ_HTML_LINK),
                new File(voaDirectory, ScraperConstants.QUIZ_HTML_FILE));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.IFRAME_RESIZE_LINK),
                new File(assetDirectory, ScraperConstants.IFRAME_RESIZE_FILE));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.IFRAME_RESIZE_WINDOW_LINK),
                new File(assetDirectory, ScraperConstants.IFRAME_RESIZE_WINDOW_FILE));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.JS_TAG),
                new File(assetDirectory, JQUERY_JS));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.MATERIAL_CSS_LINK),
                new File(assetDirectory, MATERIAL_CSS));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.MATERIAL_JS_LINK),
                new File(assetDirectory, ScraperConstants.MATERIAL_JS));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.VOA_CSS_LINK),
                new File(assetDirectory, ScraperConstants.VOA_CSS_FILE_NAME));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.VOA_JS_LINK),
                new File(assetDirectory, ScraperConstants.VOA_JS_FILE_NAME));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.VOA_QUIZ_JS_LINK),
                new File(assetDirectory, ScraperConstants.VOA_QUIZ_JS_FILE_NAME));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.VOA_QUIZ_CSS_LINK),
                new File(assetDirectory, ScraperConstants.VOA_QUIZ_CSS_FILE_NAME));


        FileUtils.writeStringToFile(new File(voaDirectory, "index.html"), finalDoc.toString(), ScraperConstants.UTF_ENCODING);

        try {
            ContentScraperUtil.generateTinCanXMLFile(voaDirectory, FilenameUtils.getBaseName(scrapUrl.toString()), "en", "index.html",
                    ScraperConstants.VIDEO_TIN_CAN_FILE, scrapUrl.getPath(), "", "");
        } catch (TransformerException | ParserConfigurationException e) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logError("VOA failed to create tin can file for url " + scrapUrl.toString());
        }

        ContentScraperUtil.zipDirectory(voaDirectory, zipFile.getName(), destinationDir);
    }

    private Map<String, String> createParams(String quizId, int count, String selectedAnswer, String voted) {
        Map<String, String> selectedParams = new HashMap<>();
        if (selectedAnswer != null) {
            selectedParams.put("SelectedAnswerId", selectedAnswer);
        }
        selectedParams.put("QuestionVoted", voted);
        selectedParams.put("quizId", quizId);
        selectedParams.put("PageIndex", String.valueOf(count));
        selectedParams.put("isEmbedded", "True");
        return selectedParams;
    }

    private HttpURLConnection createConnectionForPost(URL answersUrl, StringBuffer requestParams) throws IOException {
        HttpURLConnection conn;
        DataOutputStream out = null;
        try {
            conn = (HttpURLConnection) answersUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-length", String.valueOf(requestParams.length()));
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Referer", scrapUrl.toString());
            out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(requestParams.toString());
            out.flush();
            out.close();
        } finally {
            UMIOUtils.closeQuietly(out);
        }
        return conn;
    }

    private void removeAllAttributesFromVideoAudio(Document document) {
        document.select("div.c-spinner").remove();
        document.select("div.js-poster").remove();
        document.select("a.c-mmp__fallback-link").remove();
        document.select("div#comments").remove();
        document.select("div.article-share").remove();
        document.select("div.link-function").remove();
        document.select("div.media-download").remove();
        document.select("div.c-mmp__overlay").remove();
        document.select("button.btn-popout-player").remove();
        document.select("div.js-cpanel-container").remove();
        document.select("div.design-top-offset").remove();
        document.select("div.quiz__main-img").remove();
        document.select("div.quiz__intro").remove();
        document.select("div.media-block-wrap").remove();
        document.select("aside.js-share--horizontal").remove();
        document.select("div.nav-tabs__inner").remove();
        document.select("[href]").removeAttr("href");
        Elements linkElements = document.select("video,audio");
        for (Element link : linkElements) {
            List<String> keys = new ArrayList<>();
            Attributes attrList = link.attributes();
            for (Attribute attr : attrList) {
                if (!attr.getKey().equals("src")) {
                    keys.add(attr.getKey());
                }
            }
            for (String key : keys) {
                link.removeAttr(key);
            }
            link.attr("controls", "controls");
        }
    }

    boolean isContentUpdated() {
        return isContentUpdated;
    }

}
