package com.ustadmobile.lib.contentscrapers.ck12;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.ck12.practice.AnswerResponse;
import com.ustadmobile.lib.contentscrapers.ck12.practice.PracticeResponse;
import com.ustadmobile.lib.contentscrapers.ck12.practice.QuestionResponse;
import com.ustadmobile.lib.contentscrapers.ck12.practice.ScriptEngineReader;
import com.ustadmobile.lib.contentscrapers.ck12.practice.TestResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.CHECK_NAME;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.INDEX_HTML;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.JQUERY_JS;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MATERIAL_CSS;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TIMER_NAME;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TROPHY_NAME;


/**
 * The ck12 content is in found in multiple types
 * Currently supported content includes: read, video, practice
 * <p>
 * Most content is made up of 3 sections:- Title, Main Content, Detail Content
 * each section has a method to get their html section
 * Title from div.title
 * Main Content is from div.modality_content which has an attribute data-loadurl which has a url to load and get the main content from
 * Detail comes from div.metadataview
 * <p>
 * Read Content:
 * All 3 sections are available in read content in its usual format
 * An html page is generated with these sections to create an index.html page
 * <p>
 * Video Content:
 * title and detail are from the 2 methods defined
 * main content can come from an iframe or the usual modality_content
 * An html page is generated with these sections to create an index.html page
 * <p>
 * Practice Content:
 * Does not have the 3 usual sections
 * The content is generated based on the url to the practice course
 * 1st url to get the practice link
 * 2nd url to get the test link and its id
 * 3rd url format to generate each question
 * A question contains an encrypted answer which can be extracted using script engine class
 * and crypto js to decrypt it and store the answer back into the question json
 *
 */
public class CK12ContentScraper {

    private final String urlString;
    private final File destinationDirectory;
    private final URL scrapUrl;
    private final File assetDirectory;


    public final String postfix = "?hints=true&evalData=true";
    public final String POLICIES = "?policies=[{\"name\":\"shuffle\",\"value\":false},{\"name\":\"shuffle_question_options\",\"value\":false},{\"name\":\"max_questions\",\"value\":15},{\"name\":\"adaptive\",\"value\":false}]";
    public final String practicePost = "?nextPractice=true&adaptive=true&checkUserLogin=false";

    String practiceIdLink = "https://www.ck12.org/assessment/api/get/info/test/practice/";
    String startTestLink = "https://www.ck12.org/assessment/api/start/test/";
    String questionLinkId = "https://www.ck12.org/assessment/api/render/questionInstance/test/";
    // sample questionLink 5985b3d15aa4136da1e858b8/2/5b7a41ba5aa413662008f44f


    public ScriptEngineReader scriptEngineReader = new ScriptEngineReader();


    public CK12ContentScraper(String url, File destDir) throws MalformedURLException {
        this.urlString = url;
        scrapUrl = new URL(url);
        this.destinationDirectory = destDir;
        assetDirectory = new File(destDir, "asset");
        assetDirectory.mkdirs();
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: <ck12 json url> <file destination><type READ or PRACTICE or VIDEO");
            System.exit(1);
        }

        System.out.println(args[0]);
        System.out.println(args[1]);
        System.out.println(args[2]);
        try {
            CK12ContentScraper scraper = new CK12ContentScraper(args[0], new File(args[1]));
            String type = args[2];
            if ("READ".equalsIgnoreCase(type)) {
                scraper.scrapeReadContent();
            } else if ("PRACTICE".equalsIgnoreCase(type)) {
                scraper.scrapePracticeContent();
            } else if ("VIDEO".equalsIgnoreCase(type)) {
                scraper.scrapeVideoContent();
            }

        } catch (IOException e) {
            System.err.println("Exception running scrapeContent");
            e.printStackTrace();
        }

    }


    public void scrapeVideoContent() throws IOException {

        Document fullSite = Jsoup.connect(urlString).get();

        Document videoContent = getMainContent(fullSite, "div.modality_content[data-loadurl]", "data-loadurl");
        // sometimes video stored in iframe
        if (videoContent == null) {
            videoContent = getMainContent(fullSite, "iframe[src]", "src");
            if (videoContent == null) {
                System.err.println("Unsupported video content" + urlString);
                throw new IOException("Did not find video content" + urlString);
            }
        }

        Elements videoElement = getIframefromHtml(videoContent);
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
            throw new IllegalArgumentException("Have not finished support of video type link for url " + urlString);
        }

        String videoTitleHtml = getTitleHtml(fullSite);

        String detailHtml = removeAllHref(getDetailSectionHtml(fullSite));

        String indexHtml = videoTitleHtml + videoSource + detailHtml;

        FileUtils.writeStringToFile(new File(destinationDirectory, "index.html"), indexHtml, ScraperConstants.UTF_ENCODING);
    }

    private Elements getIframefromHtml(Document videoContent) {

        Elements elements = videoContent.select("iframe");
        if (elements.size() > 0) {
            return elements;
        } else {
            String videoElementsList = videoContent.select("textarea").text();
            return Jsoup.parse(videoElementsList).select("iframe");
        }
    }

    /**
     * Given a document, search for content that has src or data-url to load more content and return a new document
     *
     * @param document website page source
     * @param htmlTag  tag we are looking for - div.modality_content in most cases
     * @param search   src or data-url
     * @return the rendered document found in src/data-url
     * @throws IOException
     */
    private Document getMainContent(Document document, String htmlTag, String search) throws IOException {
        Elements elements = document.select(htmlTag);
        for (Element element : elements) {
            if (!element.attr(search).contains("googletag")) {
                String path = element.attr(search);
                URL contentUrl = new URL(scrapUrl, path);
                return Jsoup.connect(contentUrl.toString())
                        .followRedirects(true).get();
            }
        }
        return null;
    }

    private String getVocabHtml(Document site) throws IOException {

        Elements elements = site.select("section.vocabulary_content[data-loadurl]");
        for (Element element : elements) {
            String path = element.attr("data-loadurl");
            URL contentUrl = new URL(scrapUrl, path);
            return Jsoup.connect(contentUrl.toString())
                    .followRedirects(true).get().html();
        }
        return null;
    }


    public void scrapeReadContent() throws IOException {

        Document html = Jsoup.connect(urlString).get();

        String readTitle = getTitleHtml(html);

        Document content = getMainContent(html, "div.modality_content[data-loadurl]", "data-loadurl");

        if (content == null) {
            System.err.println("Unsupported read content" + urlString);
            throw new IllegalArgumentException("Did not find read content" + urlString);
        }
        String readHtml = content.html();

        readHtml = removeAllHref(ContentScraperUtil.downloadAllResources(readHtml, assetDirectory, scrapUrl));

        String vocabHtml = removeAllHref(getVocabHtml(html));

        String detailHtml = removeAllHref(getDetailSectionHtml(html));

        readHtml = readTitle + readHtml + vocabHtml + detailHtml;

        FileUtils.writeStringToFile(new File(destinationDirectory, "index.html"), readHtml, ScraperConstants.UTF_ENCODING);
    }

    /**
     * Given a practice url - generate the url needed to create the json response
     *
     * @param url practice url
     * @return the generated url
     */
    public String generatePracticeLink(String url) {
        return practiceIdLink + url + practicePost;
    }

    /**
     * Given the test id from practice link, generate the test url
     *
     * @param testId test id from practice links' response
     * @return the generated url for the test
     */
    public String generateTestUrl(String testId) {
        return startTestLink + testId + POLICIES;
    }


    /**
     * Generates the url needed to get the question for the practice
     *
     * @param testId      test id from practice link
     * @param testScoreId test score from test link
     * @param count       question number
     * @return generated url to get the question
     */
    public String generateQuestionUrl(String testId, String testScoreId, int count) {
        return questionLinkId + testId + "/" + count + "/" + testScoreId + postfix;
    }


    public void scrapePracticeContent() throws IOException {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        String practiceUrl = urlString.substring(urlString.lastIndexOf("/") + 1, urlString.indexOf("?"));

        String testIdLink = generatePracticeLink(practiceUrl);

        File practiceDirectory = new File(destinationDirectory, practiceUrl);
        practiceDirectory.mkdirs();

        File practiceAssetDirectory = new File(practiceDirectory, "practice-asset");
        practiceAssetDirectory.mkdirs();

        PracticeResponse response = gson.fromJson(
                IOUtils.toString(new URL(testIdLink), ScraperConstants.UTF_ENCODING), PracticeResponse.class);

        String testId = response.response.test.id;
        int goal = response.response.test.goal;

        int questionsCount = response.response.test.questionsCount;
        String practiceName = response.response.test.title;
        String updated = response.response.test.updated;

        File modifiedFile = new File(practiceDirectory, ScraperConstants.LAST_MODIFIED_TXT);
        if (!ContentScraperUtil.isContentUpdated(ContentScraperUtil.parseServerDate(updated), modifiedFile)) {
            return;
        }

        String nextPracticeName = response.response.test.nextPractice.nameOfNextPractice;
        String nextPracticeUrl = practiceIdLink + nextPracticeName + practicePost;

        String testLink = generateTestUrl(testId);
        TestResponse testResponse = gson.fromJson(
                IOUtils.toString(new URL(testLink), ScraperConstants.UTF_ENCODING), TestResponse.class);

        String testScoreId = testResponse.response.testScore.id;

        ArrayList<QuestionResponse> questionList = new ArrayList<>();
        for (int i = 1; i <= questionsCount; i++) {

            String questionLink = generateQuestionUrl(testId, testScoreId, i);

            QuestionResponse questionResponse = gson.fromJson(
                    IOUtils.toString(new URL(questionLink), ScraperConstants.UTF_ENCODING), QuestionResponse.class);

            questionResponse.response.goal = goal;
            questionResponse.response.practiceName = practiceName;
            questionResponse.response.nextPracticeName = nextPracticeName;
            questionResponse.response.nextPracticeUrl = nextPracticeUrl;

            String questionId = questionResponse.response.questionID;

            File questionAsset = new File(practiceDirectory, questionId);
            questionAsset.mkdirs();

            questionResponse.response.stem.displayText = ContentScraperUtil.downloadAllResources(
                    questionResponse.response.stem.displayText, questionAsset, scrapUrl);

            System.out.println(questionResponse.response.stem.displayText);

            List<String> hintsList = questionResponse.response.hints;
            for (int j = 0; j < hintsList.size(); j++) {
                hintsList.set(j, ContentScraperUtil.downloadAllResources(hintsList.get(j), practiceAssetDirectory, scrapUrl));
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

        ContentScraperUtil.saveListAsJson(practiceDirectory, questionList, ScraperConstants.QUESTIONS_JSON);
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.JS_TAG), new File(practiceDirectory, JQUERY_JS));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.MATERIAL_CSS_LINK), new File(practiceDirectory, MATERIAL_CSS));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.MATERIAL_JS_LINK), new File(practiceDirectory, ScraperConstants.MATERIAL_JS));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.CK12_INDEX_HTML_TAG), new File(practiceDirectory, INDEX_HTML));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.TIMER_PATH), new File(practiceDirectory, TIMER_NAME));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.TROPHY_PATH), new File(practiceDirectory, TROPHY_NAME));
        FileUtils.copyToFile(getClass().getResourceAsStream(ScraperConstants.CHECK_PATH), new File(practiceDirectory, CHECK_NAME));

        ContentScraperUtil.zipDirectory(practiceDirectory, practiceUrl, destinationDirectory);

    }

    /**
     * Given encrypted data from json response
     *
     * @param data return the result as json string
     * @return
     */
    public String extractAnswerFromEncryption(String data) {
        return scriptEngineReader.getResult(data);
    }


    /**
     * Given a list of answers, save the resources in its directory if any found
     *
     * @param answer        return a list of objects because an answer might have its own list of objects
     * @param questionAsset folder where images might be saved
     * @param scrapUrl      base url to get images
     * @return the list of objects with the modified resources
     */
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

    private String removeAllHref(String html) {

        Document doc = Jsoup.parse(html);

        Elements elements = doc.select("[href]");

        for (Element element : elements) {
            element.removeAttr("href");
        }

        return doc.body().html();
    }


}
