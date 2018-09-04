package com.ustadmobile.lib.contentscrapers.ck12;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CK12ContentScraper {

    private final String urlString;
    private final File destinationDirectory;
    private final URL scrapUrl;
    private final File assetDirectory;
    String ckK12 = "https://www.ck12.org";

    String chromeDriverLocation = "C:\\Users\\suhai\\Documents\\chromedriver_win32\\chromedriver.exe";

    public final String postfix = "?hints=true&evalData=true";
    public final String POLICIES = "?policies=[{\"name\":\"shuffle\",\"value\":false},{\"name\":\"shuffle_question_options\",\"value\":false},{\"name\":\"max_questions\",\"value\":15},{\"name\":\"adaptive\",\"value\":false}]";
    public final String practicePost = "?nextPractice=true&adaptive=true&checkUserLogin=false";

    String practiceIdLink = "https://www.ck12.org/assessment/api/get/info/test/practice/";
    String startTestLink = "https://www.ck12.org/assessment/api/start/test/";
    String questionLinkId = "https://www.ck12.org/assessment/api/render/questionInstance/test/";
    // sample questionLink 5985b3d15aa4136da1e858b8/2/5b7a41ba5aa413662008f44f


    private ChromeDriver driver;
    private WebDriverWait waitDriver;

    public static final String READ_TYPE = "READ";
    public static final String VIDEO_TYPE = "VIDEO";
    public static final String PRACTICE_TYPE = "PRACTICE";

    public Rhino rhino = new Rhino();


    public CK12ContentScraper(String url, File destDir) throws MalformedURLException {
        this.urlString = url;
        scrapUrl = new URL(url);
        this.destinationDirectory = destDir;
        assetDirectory = new File(destDir, "asset");
        assetDirectory.mkdirs();
    }

    public void scrapVideoContent() throws IOException {


        Document fullSite = setUpChromeDriver(urlString);

        Document videoContent = getContentFromSite(VIDEO_TYPE, "div.flex-video iframe");

        Elements videoElement = videoContent.select("iframe");
        String link = videoElement.attr("src");

        String imageThumnail = fullSite.select("meta[property=og:image]").attr("content");

        if (imageThumnail == null || imageThumnail.isEmpty()) {
            throw new IllegalArgumentException("Did not receive image content from meta tag");
        }

        try {
            FileUtils.copyURLToFile(new URL(imageThumnail), new File(assetDirectory, "video-thumbnail.jpg"));
        } catch (MalformedURLException e) {
            imageThumnail = "";
        }


        String videoSource = ContentScraperUtil.downloadAllResources(videoElement.outerHtml(), assetDirectory, scrapUrl);

        if (link == null || link.isEmpty()) {
            throw new IllegalArgumentException("Have not finished support of video type link " + link + " for url " + urlString);
        }

        String videoTitleHtml = getTitleHtml(fullSite);

        String detailHtml = removeAllHref(getDetailSectionHtml(fullSite));

        String indexHtml = videoTitleHtml + videoSource + detailHtml;

        FileUtils.writeStringToFile(new File(destinationDirectory, "index.html"), indexHtml, ScraperConstants.UTF_ENCODING);
    }

    public void scrapReadContent() throws IOException {

        Document html = setUpChromeDriver(urlString);
        //Document html = getContentFromSite(READ_TYPE, "");

        String readTitle = getTitleHtml(html);

        String readHtml = removeAllHref(ContentScraperUtil.downloadAllResources(getContentHtml(html), assetDirectory, scrapUrl));

        String detailHtml = removeAllHref(getDetailSectionHtml(html));

        // append the title
        readHtml = readTitle + readHtml + detailHtml;

        FileUtils.writeStringToFile(new File(destinationDirectory, "index.html"), readHtml, ScraperConstants.UTF_ENCODING);

    }

    public String generatePracticeLink(String url){
        return practiceIdLink + url + practicePost;
    }

    public String generateTestUrl(String testId){
        return startTestLink + testId + POLICIES;
    }

    public String generateQuestionUrl(String testId, String testScoreId, int count){
        return questionLinkId + testId + "/" + count + "/" + testScoreId + postfix;
    }

    public void scrapPracticeContent() throws IOException {

        String practiceUrl = urlString.substring(urlString.lastIndexOf("/") + 1, urlString.indexOf("?"));

        String testIdLink = generatePracticeLink(practiceUrl);

        PracticeResponse response = new GsonBuilder().disableHtmlEscaping().create().fromJson(
                IOUtils.toString(new URL(testIdLink), ScraperConstants.UTF_ENCODING), PracticeResponse.class);

        String testId = response.response.test.id;
        int goal = response.response.test.goal;

        int questionsCount = response.response.test.questionsCount;
        String practiceName = response.response.test.title;
        String updated = response.response.test.updated;

        File modifiedFile = new File(destinationDirectory, ScraperConstants.LAST_MODIFIED_TXT);
        if (!ContentScraperUtil.isContentUpdated(ContentScraperUtil.parseServerDate(updated), modifiedFile)) {
            return;
        }

        String nextPracticeName = response.response.test.nextPractice.nameOfNextPractice;
        String nextPracticeUrl = practiceIdLink + nextPracticeName + practicePost;

        String testLink = generateTestUrl(testId);
        TestResponse testResponse = new GsonBuilder().disableHtmlEscaping().create().fromJson(
                IOUtils.toString(new URL(testLink), ScraperConstants.UTF_ENCODING), TestResponse.class);

        String testScoreId = testResponse.response.testScore.id;

        Gson gson = new GsonBuilder().create();

        ArrayList<QuestionResponse> questionList = new ArrayList<>();
        for (int i = 1; i <= questionsCount; i++) {

            String questionLink = generateQuestionUrl(testId, testScoreId, i);

            QuestionResponse questionResponse = new GsonBuilder().disableHtmlEscaping().create().fromJson(
                    IOUtils.toString(new URL(questionLink), ScraperConstants.UTF_ENCODING), QuestionResponse.class);

            questionResponse.response.goal = goal;
            questionResponse.response.practiceName = practiceName;
            questionResponse.response.nextPracticeName = nextPracticeName;
            questionResponse.response.nextPracticeUrl = nextPracticeUrl;

            String questionId = questionResponse.response.questionID;

            File questionAsset = new File(destinationDirectory, questionId);
            questionAsset.mkdirs();

            questionResponse.response.stem.displayText = ContentScraperUtil.downloadAllResources(
                    questionResponse.response.stem.displayText, questionAsset, scrapUrl);

            System.out.println(questionResponse.response.stem.displayText);

            List<String> hintsList = questionResponse.response.hints;
            for (int j = 0; j < hintsList.size(); j++) {
                hintsList.set(j, ContentScraperUtil.downloadAllResources(hintsList.get(j), questionAsset, scrapUrl));
            }
            questionResponse.response.hints = hintsList;

            String answerResponse = extractAnswerFromEncryption(questionResponse.response.data);

            System.out.println(answerResponse);

            AnswerResponse answer = gson.fromJson(answerResponse, AnswerResponse.class);
            answer.instance.solution = ContentScraperUtil.downloadAllResources(answer.instance.solution, questionAsset, scrapUrl);

            answer.instance.answer = downloadAllResourcesFromAnswer(answer.instance.answer, questionAsset, scrapUrl);

            if (ScraperConstants.QUESTION_TYPE.MULTI_CHOICE.getType().equalsIgnoreCase(questionResponse.response.questionType)) {

                List<QuestionResponse.Response.QuestionObjects> questionOrderList = questionResponse.response.responseObjects;
                List<AnswerResponse.Instance.AnswerObjects> answerObjectsList = answer.instance.responseObjects;
                for (int order = 0; order < questionOrderList.size(); order++) {

                    QuestionResponse.Response.QuestionObjects question = questionOrderList.get(order);

                    question.displayText = ContentScraperUtil.downloadAllResources(question.displayText, questionAsset, scrapUrl);
                    question.optionKey = ContentScraperUtil.downloadAllResources(question.optionKey, questionAsset, scrapUrl);

                    AnswerResponse.Instance.AnswerObjects answerObject = answerObjectsList.get(order);
                    answerObject.displayText = ContentScraperUtil.downloadAllResources(answerObject.displayText, questionAsset, scrapUrl);
                    answerObject.optionKey = ContentScraperUtil.downloadAllResources(answerObject.optionKey, questionAsset, scrapUrl);

                }
            }


            questionResponse.response.answer = answer;

            questionList.add(questionResponse);

        }

        ContentScraperUtil.saveListAsJson(destinationDirectory, questionList, ScraperConstants.QUESTIONS_JSON);

    }

    public String extractAnswerFromEncryption(String data) {
        return rhino.getResult(data);
    }

    private List<Object> downloadAllResourcesFromAnswer(List<Object> answer, File questionAsset, URL scrapUrl) {

        for (int i = 0; i < answer.size(); i++) {

            Object object = answer.get(i);
            if (object instanceof String) {
                answer.set(i, ContentScraperUtil.downloadAllResources((String) object, questionAsset, scrapUrl));
            } else if (object instanceof List<?>) {
                answer.set(i, downloadAllResourcesFromAnswer((List<Object>) object, questionAsset, scrapUrl));
            }
        }

        return answer;
    }

    private String getTitleHtml(Document section) {

        return section.select("div.title").outerHtml();
    }

    private String getDetailSectionHtml(Document section) {

        return section.select("div.metadataview").html();

    }

    private String getContentHtml(Document section) {
        return section.select("div.modalitycontent").html();
    }

    private String removeAllHref(String html) {

        Document doc = Jsoup.parse(html);

        Elements elements = doc.select("[href]");

        for (Element element : elements) {
            element.removeAttr("href");
        }

        return doc.body().html();
    }

 /*   private String getResourceSectionHtml(Document section) throws IOException {

        String path = section.select("section.resources_container").attr("data-loadurl");

        Document document = Jsoup.connect(new URL(scrapUrl, path).toString()).get();

        Elements resources = document.select("div.resource_row a.break-word");

        StringBuilder htmlString = new StringBuilder();
        for (Element resourceLink : resources) {

            htmlString.append(resourceLink.outerHtml());

        }
        return htmlString.toString();
    } */

    private Document getContentFromSite(String type, String waitForText) {

        WebElement element = waitDriver.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(waitForText)));

        if (type.equalsIgnoreCase(VIDEO_TYPE)) {
            return Jsoup.parse(driver.switchTo().frame(element).getPageSource());
        }

        if (type.equalsIgnoreCase(READ_TYPE)) {
            return Jsoup.parse(driver.getPageSource());
        }

        return null;
    }


    /**
     * Given a url, Setup chrome web driver and wait for page to be rendered
     *
     * @param url
     */
    public Document setUpChromeDriver(String url) {
        System.setProperty("webdriver.chrome.driver", chromeDriverLocation);
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setHeadless(true);
        driver = new ChromeDriver(chromeOptions);

        driver.get(url);

        waitDriver = new WebDriverWait(driver, 30);
        waitForJSandJQueryToLoad();

        return Jsoup.parse(driver.getPageSource());
    }

    private boolean waitForJSandJQueryToLoad() {

        // wait for jQuery to load
        ExpectedCondition<Boolean> jQueryLoad = driver -> {
            try {
                return ((Long) ((JavascriptExecutor) driver).executeScript("return jQuery.active") == 0);
            } catch (Exception e) {
                // no jQuery present
                return true;
            }
        };

        // wait for Javascript to load
        ExpectedCondition<Boolean> jsLoad = driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState")
                .toString().equals("complete");

        return waitDriver.until(jQueryLoad) && waitDriver.until(jsLoad);
    }


}
