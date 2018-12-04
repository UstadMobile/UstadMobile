package com.ustadmobile.lib.contentscrapers.ck12;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.ck12.plix.PlixIndex;
import com.ustadmobile.lib.contentscrapers.ck12.plix.PlixLog;
import com.ustadmobile.lib.contentscrapers.ck12.plix.PlixResponse;
import com.ustadmobile.lib.contentscrapers.ck12.practice.AnswerResponse;
import com.ustadmobile.lib.contentscrapers.ck12.practice.PracticeResponse;
import com.ustadmobile.lib.contentscrapers.ck12.practice.QuestionResponse;
import com.ustadmobile.lib.contentscrapers.ck12.practice.ScriptEngineReader;
import com.ustadmobile.lib.contentscrapers.ck12.practice.TestResponse;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.CHECK_NAME;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.CONFIG_INPUT_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.CONFIG_INPUT_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.CONFIG_OUTPUT_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.CONFIG_OUTPUT_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EXTENSION_TEX_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EXTENSION_TEX_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.FONT_DATA_1_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.FONT_DATA_1_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.FONT_DATA_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.FONT_DATA_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.INDEX_HTML;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_CONFIG_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_CONFIG_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_ELEMENT_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_ELEMENT_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_INPUT_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_INPUT_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_OUTPUT_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.JAX_OUTPUT_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.JQUERY_JS;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MATERIAL_CSS;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MATH_EVENTS_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MATH_EVENTS_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MATH_JAX_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MATH_JAX_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MTABLE_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.MTABLE_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_AMS_MATH_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_AMS_MATH_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_AMS_SYMBOL_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_AMS_SYMBOL_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_AUTOLOAD_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_AUTOLOAD_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_CANCEL_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_CANCEL_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_COLOR_FILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TEX_COLOR_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TIMER_NAME;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TROPHY_NAME;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;


/**
 * The ck12 content is in found in multiple types
 * Currently supported content includes: read, video, practice, plix
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
 * <p>
 * Plix:
 * Use selenium and chrome tools to find the all the files plix opens
 * Get the id of the plix in the url. Setup Selenium and Chrome
 * Run selenium and wait for everything to load on the screen by waiting for the element div#questionController
 * Once that is done, get the logs for the network and store in a list
 * Filter the responses based on the message RESPONSE RECEIVED
 * Store the mimeType and url of each response.
 * Copy and Save the content of each url and use request headers if required.
 * <p>
 * To avoid forced sign-in, find
 * else a = "trialscount.plix." + location.hostname, localStorage.getItem(a) && !y || x.preview ? Oe() : (c = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx".replace(/x/g, function(e)
 * Comment out the condition - from = until : to avoid the call to the Oe method (which forced signin)
 * <p>
 * To avoid clickable links in plix content, find the div and use the style display: none to hide it.
 * </p>
 * <p>
 * To fit the plix in all resolutions:
 * First remove @media call in plix.css
 * Add columns to div tags plixLeftWrapper and plixRightWrapper
 * Remove unnecessary parts of the plix page with Jsoup
 * Append some custom css
 * </p>
 * Create a content directory for all the url and their location into a json so it can be played back.
 * Zip all files with the plixId as the name
 */
public class CK12ContentScraper {

    private final String urlString;
    private final File destinationDirectory;
    private final URL scrapUrl;
    private File assetDirectory;

    public final String css = "<style> .read-more-container { display: none; } #plixIFrameContainer { float: left !important; margin-top: 15px; } #plixLeftWrapper { float: left !important; width: 49%; min-width: 200px; padding-left: 15px !important; padding-right: 15px !important; margin-right: 15px; } @media (max-width: 1070px) { #plixLeftWrapper { width: 98% !important; } } .plixQestionPlayer, .plixLeftMiddlequestionContainer { margin-bottom: 5px !important; } .leftTopFixedBar { padding-top: 20px !important; } #next-container { margin-top: 0 !important; } .overflow-container { background: transparent !important; width: 0px !important; } .overflow-indicator { left: 50% !important; padding: 12px !important; } .plixWrapper { width: 95% !important; max-width: inherit !important; } body.plix-modal { overflow: auto !important; padding: 0; width: 95% !important; height: inherit !important; } .show-description, .show-challenge { position: static !important; padding-top: 0 !important; } #hintModal { width: 90% !important; margin-left: -45% !important; } @media only screen and (max-device-width: 605px), only screen and (max-device-height: 605px) { #landscapeView { display: block !important; } } </style>";


    public final String postfix = "?hints=true&evalData=true";
    public final String POLICIES = "?policies=[{\"name\":\"shuffle\",\"value\":false},{\"name\":\"shuffle_question_options\",\"value\":false},{\"name\":\"max_questions\",\"value\":15},{\"name\":\"adaptive\",\"value\":false}]";
    public final String practicePost = "?nextPractice=true&adaptive=true&checkUserLogin=false";

    String practiceIdLink = "https://www.ck12.org/assessment/api/get/info/test/practice/";
    String startTestLink = "https://www.ck12.org/assessment/api/start/test/";
    String questionLinkId = "https://www.ck12.org/assessment/api/render/questionInstance/test/";
    // sample questionLink 5985b3d15aa4136da1e858b8/2/5b7a41ba5aa413662008f44f

    String plixLink = "https://www.ck12.org/assessment/api/get/info/question/";


    public ScriptEngineReader scriptEngineReader = new ScriptEngineReader();

    private ChromeDriver driver;
    private WebDriverWait waitDriver;

    public static final String RESPONSE_RECEIVED = "Network.responseReceived";
    private boolean isContentUpdated = true;


    public CK12ContentScraper(String url, File destDir) throws MalformedURLException {
        this.urlString = url;
        scrapUrl = new URL(url);
        this.destinationDirectory = destDir;
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: <ck12 json url> <file destination><type READ or PRACTICE or VIDEO or plix");
            System.exit(1);
        }

        System.out.println(args[0]);
        System.out.println(args[1]);
        System.out.println(args[2]);
        try {
            CK12ContentScraper scraper = new CK12ContentScraper(args[0], new File(args[1]));
            String type = args[2];
            switch (type.toLowerCase()) {

                case "video":
                    scraper.scrapeVideoContent();
                    break;
                case "plix":
                    scraper.scrapePlixContent();
                    break;
                case "practice":
                    scraper.scrapePracticeContent();
                    break;
                case "read":
                case "activities":
                case "study aids":
                case "lesson plans":
                case "real world":
                    scraper.scrapeReadContent();
                    break;
                default:
                    System.out.println("found a group type not supported " + type);
            }

        } catch (IOException e) {
            System.err.println("Exception running scrapeContent");
            e.printStackTrace();
        }

    }

    public void scrapePlixContent() throws IOException {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        String plixId = urlString.substring(urlString.lastIndexOf("-") + 1, urlString.lastIndexOf("?"));

        File plixDirectory = new File(destinationDirectory, plixId);
        plixDirectory.mkdirs();

        assetDirectory = new File(plixDirectory, "asset");
        assetDirectory.mkdirs();

        String plixUrl = generatePlixLink(plixId);

        PlixResponse response = gson.fromJson(
                IOUtils.toString(new URL(plixUrl), ScraperConstants.UTF_ENCODING), PlixResponse.class);

        File fileLastModified = new File(plixDirectory, "plix-last-modified.txt");
        isContentUpdated = ContentScraperUtil.isContentUpdated(
                ContentScraperUtil.parseServerDate(response.response.question.updated), fileLastModified);

        if (!isContentUpdated) {
            return;
        }

        ContentScraperUtil.setChromeDriverLocation();

        DesiredCapabilities d = DesiredCapabilities.chrome();
        d.setCapability("opera.arguments", "-screenwidth 1024 -screenheight 768");
        // d.merge(capabilities);
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        d.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        driver = new ChromeDriver(d);

        driver.get(urlString);
        waitDriver = new WebDriverWait(driver, 10000);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);
        try {
            waitDriver.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#questionController"))).click();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogEntries les = driver.manage().logs().get(LogType.PERFORMANCE);
        driver.close();

        List<PlixIndex> index = new ArrayList<>();

        for (LogEntry le : les) {

            PlixLog log = gson.fromJson(le.getMessage(), PlixLog.class);
            if (RESPONSE_RECEIVED.equalsIgnoreCase(log.message.method)) {
                String mimeType = log.message.params.response.mimeType;
                String urlString = log.message.params.response.url;

                try {
                    URL url = new URL(urlString);
                    File urlFile = new File(plixDirectory, url.getAuthority().replaceAll("[^a-zA-Z0-9\\.\\-]", "_"));
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

                    if (file.getName().contains("plix.js")) {
                        String plixJs = FileUtils.readFileToString(file, UTF_ENCODING);
                        int startIndex = plixJs.indexOf("\"trialscount.plix.\"");
                        int lastIndex = plixJs.lastIndexOf("():");
                        plixJs = new StringBuilder(plixJs).insert(lastIndex + 3, "*/").insert(startIndex, "/*").toString();
                        FileUtils.writeStringToFile(file, plixJs, UTF_ENCODING);
                    }

                    if (file.getName().contains("plix.css")) {
                        String plixJs = FileUtils.readFileToString(file, UTF_ENCODING);
                        int startIndex = plixJs.indexOf("@media only screen and (max-device-width:605px)");
                        int endIndex = plixJs.indexOf(".plix{");
                        plixJs = new StringBuilder(plixJs).insert(endIndex, "*/").insert(startIndex, "/*").toString();
                        FileUtils.writeStringToFile(file, plixJs, UTF_ENCODING);
                    }

                    if (file.getName().contains("plix.html")) {
                        String plixJs = FileUtils.readFileToString(file, UTF_ENCODING);
                        Document doc = Jsoup.parse(plixJs);

                        doc.selectFirst("div.read-more-container").remove();
                        doc.selectFirst("div#portraitView").remove();
                        doc.selectFirst("div#ToolBarView").remove();
                        doc.selectFirst("div#deviceCompatibilityAlertPlix").remove();
                        doc.selectFirst("div#leftBackWrapper").remove();

                        Element head = doc.head();
                        head.append(css);

                        Element iframe = doc.selectFirst("div.plixIFrameContainer");
                        iframe.removeClass("plixIFrameContainer");

                        Element leftWrapper = doc.selectFirst("div#plixLeftWrapper");
                        leftWrapper.removeClass("plixLeftWrapper");
                        leftWrapper.addClass("small-12");
                        leftWrapper.addClass("medium-6");
                        leftWrapper.addClass("large-6");
                        String leftAttr = leftWrapper.attr("style");
                        leftWrapper.attr("style", leftAttr + "display: block;");

                        Element rightWrapper = doc.selectFirst("div#plixRightWrapper");
                        rightWrapper.removeClass("small-6");
                        rightWrapper.addClass("small-12");
                        rightWrapper.addClass("medium-6");
                        rightWrapper.addClass("large-6");

                        FileUtils.writeStringToFile(file, doc.html(), UTF_ENCODING);

                    }


                    PlixIndex plixIndex = new PlixIndex();
                    plixIndex.url = urlString;
                    plixIndex.mimeType = mimeType;
                    plixIndex.path = urlFile.getName() + "/" + file.getName();
                    plixIndex.headers = log.message.params.response.headers;

                    index.add(plixIndex);

                } catch (IOException e) {
                    System.err.println(urlString);
                    System.err.println(le.getMessage());
                    e.printStackTrace();
                }
            }
        }

        FileUtils.writeStringToFile(new File(plixDirectory, "index.json"), gson.toJson(index), UTF_ENCODING);
        ContentScraperUtil.zipDirectory(plixDirectory, FilenameUtils.getBaseName(scrapUrl.getPath()), destinationDirectory);
    }

    public void scrapeVideoContent() throws IOException {

        Document fullSite = Jsoup.connect(urlString).get();

        String videoContentName = FilenameUtils.getBaseName(scrapUrl.getPath());
        File videoHtmlLocation = new File(destinationDirectory, videoContentName);
        videoHtmlLocation.mkdirs();

        assetDirectory = new File(videoHtmlLocation, "asset");
        assetDirectory.mkdirs();

        File lastUpdated = new File(videoHtmlLocation, "video-last-modified.txt");
        isContentUpdated = isPageUpdated(fullSite, lastUpdated);

        if (!isContentUpdated) {
            return;
        }


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
            File thumbnail = new File(assetDirectory, videoContentName + "-" + "video-thumbnail.jpg");
            if (!ContentScraperUtil.fileHasContent(thumbnail)) {
                FileUtils.copyURLToFile(new URL(imageThumnail), thumbnail);
            }

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

        FileUtils.writeStringToFile(new File(videoHtmlLocation, "index.html"), indexHtml, ScraperConstants.UTF_ENCODING);

        try {
            ContentScraperUtil.generateTinCanXMLFile(videoHtmlLocation, videoContentName, "en", "index.html",
                    ScraperConstants.VIDEO_TIN_CAN_FILE, scrapUrl.getPath(), "", "");
        } catch (TransformerException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        ContentScraperUtil.zipDirectory(videoHtmlLocation, videoContentName, destinationDirectory);
    }

    public boolean isContentUpdated() {
        return isContentUpdated;
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

    private boolean isPageUpdated(Document doc, File file) {
        String date = doc.select("h2:contains(Last Modified) ~ span").attr("data-date");
        long parsedDate = ContentScraperUtil.parseServerDate(date);
        return ContentScraperUtil.isContentUpdated(parsedDate, file);
    }


    public void scrapeReadContent() throws IOException {

        Document html = Jsoup.connect(urlString).get();

        String readContentName = FilenameUtils.getBaseName(scrapUrl.getPath());
        File readHtmlLocation = new File(destinationDirectory, readContentName);
        readHtmlLocation.mkdirs();

        assetDirectory = new File(readHtmlLocation, "asset");
        assetDirectory.mkdirs();

        File lastUpdated = new File(readHtmlLocation, "read-last-modified.txt");
        isContentUpdated = isPageUpdated(html, lastUpdated);

        if (!isContentUpdated) {
            return;
        }

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

        if (readHtml.contains("x-ck12-mathEditor")) {
            readHtml = appendMathJax() + readHtml;
            detailHtml = detailHtml + appendMathJaxScript();

            File mathJaxDir = new File(readHtmlLocation, "mathjax");
            mathJaxDir.mkdirs();

            FileUtils.copyToFile(getClass().getResourceAsStream(MATH_JAX_LINK), new File(mathJaxDir, MATH_JAX_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(JAX_CONFIG_LINK), new File(mathJaxDir, JAX_CONFIG_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(EXTENSION_TEX_LINK), new File(mathJaxDir, EXTENSION_TEX_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(MATH_EVENTS_LINK), new File(mathJaxDir, MATH_EVENTS_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(TEX_AMS_MATH_LINK), new File(mathJaxDir, TEX_AMS_MATH_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(TEX_AMS_SYMBOL_LINK), new File(mathJaxDir, TEX_AMS_SYMBOL_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(TEX_AUTOLOAD_LINK), new File(mathJaxDir, TEX_AUTOLOAD_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(TEX_CANCEL_LINK), new File(mathJaxDir, TEX_CANCEL_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(TEX_COLOR_LINK), new File(mathJaxDir, TEX_COLOR_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(JAX_ELEMENT_LINK), new File(mathJaxDir, JAX_ELEMENT_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(JAX_INPUT_LINK), new File(mathJaxDir, JAX_INPUT_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(CONFIG_INPUT_LINK), new File(mathJaxDir, CONFIG_INPUT_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(MTABLE_LINK), new File(mathJaxDir, MTABLE_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(FONT_DATA_LINK), new File(mathJaxDir, FONT_DATA_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(FONT_DATA_1_LINK), new File(mathJaxDir, FONT_DATA_1_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(JAX_OUTPUT_LINK), new File(mathJaxDir, JAX_OUTPUT_FILE));
            FileUtils.copyToFile(getClass().getResourceAsStream(CONFIG_OUTPUT_LINK), new File(mathJaxDir, CONFIG_OUTPUT_FILE));
        }

        readHtml = readTitle + readHtml + vocabHtml + detailHtml;

        FileUtils.writeStringToFile(new File(readHtmlLocation, "index.html"), readHtml, ScraperConstants.UTF_ENCODING);

        try {
            ContentScraperUtil.generateTinCanXMLFile(readHtmlLocation, readContentName, "en", "index.html",
                    ScraperConstants.ARTICLE_TIN_CAN_FILE, scrapUrl.getPath(), "", "");
        } catch (TransformerException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        ContentScraperUtil.zipDirectory(readHtmlLocation, readContentName, destinationDirectory);
    }

    private String appendMathJaxScript() {
        return "<script language=\"JavaScript\" src=\"./mathjax/MathJax.js\" type=\"text/javascript\">\n" +
                "  </script>\n" +
                "  <script>\n" +
                "   var els = document.getElementsByClassName(\"x-ck12-mathEditor\");\n" +
                "    for(var i = 0; i < els.length; i++) {\n" +
                "        var el = els.item(i);\n" +
                "        var tex = decodeURIComponent(el.getAttribute(\"data-tex\"))\n" +
                "        if (tex.indexOf(\"\\\\begin{align\") === -1) {\n" +
                "            tex = \"\\\\begin{align*}\" + tex + \"\\\\end{align*}\";\n" +
                "        }\n" +
                "        tex = (\"@$\" + tex + \"@$\").replace(/</g, \"&lt;\");\n" +
                "        el.innerHTML = tex;\n" +
                "        el.removeAttribute(\"data-tex-mathjax\");\n" +
                "    }\n" +
                "\n" +
                "    MathJax.Hub.Typeset(MathJax.Hub);\n" +
                "  </script>\n" +
                " </body>\n" +
                "</html>";
    }

    private String appendMathJax() {

        return "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                " <head>\n" +
                "  <title>\n" +
                "  </title><script language=\"JavaScript\" type=\"text/x-mathjax-config\">\n" +
                "   MathJax.Hub.Config({\n" +
                "\t\textensions: [\"tex2jax.js\",\"TeX/AMSmath.js\",\"TeX/AMSsymbols.js\"],\n" +
                "\t\ttex2jax: {\n" +
                "\t\t\tinlineMath: [['@$','@$']],\n" +
                "\t\t\tdisplayMath: [['@$$','@$$']],\n" +
                "\t\t\tskipTags: [\"script\",\"noscript\",\"style\",\"textarea\",\"code\"]\n" +
                "\t\t},\n" +
                "\t\tshowMathMenu : false,\n" +
                "\t\tjax: [\"input/TeX\",\"output/HTML-CSS\"],\n" +
                "\t\tmessageStyle: \"none\",\n" +
                "\t\tTeX: {\n" +
                "\t\t\textensions: [\"cancel.js\", \"color.js\", \"autoload-all.js\"]\n" +
                "\t\t}\n" +
                "\t});\n" +
                "  </script>\n" +
                " </head>\n" +
                " <body>\n";
    }


    /**
     * Given the id, generate the plix link to find out if it was updated
     *
     * @param id
     * @return full url
     */
    public String generatePlixLink(String id) {
        return plixLink + id + "?includeBasicPlixDataOnly=true";
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

        String practiceUrl = FilenameUtils.getBaseName(scrapUrl.getPath());

        String testIdLink = generatePracticeLink(practiceUrl);

        File practiceDirectory = new File(destinationDirectory, practiceUrl);
        practiceDirectory.mkdirs();

        File practiceAssetDirectory = new File(practiceDirectory, "asset");
        practiceAssetDirectory.mkdirs();

        PracticeResponse response = gson.fromJson(
                IOUtils.toString(new URL(testIdLink), ScraperConstants.UTF_ENCODING), PracticeResponse.class);

        String testId = response.response.test.id;
        int goal = response.response.test.goal;

        int questionsCount = response.response.test.questionsCount;
        String practiceName = response.response.test.title;
        String updated = response.response.test.updated;

        File modifiedFile = new File(practiceDirectory, ScraperConstants.LAST_MODIFIED_TXT);
        isContentUpdated = ContentScraperUtil.isContentUpdated(ContentScraperUtil.parseServerDate(updated), modifiedFile);
        if (!isContentUpdated) {
            return;
        }

        String nextPracticeName = "";
        String nextPracticeUrl = "";
        // not all practice urls have next practice
        if (response.response.test.nextPractice != null) {
            nextPracticeName = response.response.test.nextPractice.nameOfNextPractice;
            nextPracticeUrl = practiceIdLink + nextPracticeName + practicePost;

        }

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

            List<String> hintsList = questionResponse.response.hints;
            for (int j = 0; j < hintsList.size(); j++) {
                hintsList.set(j, ContentScraperUtil.downloadAllResources(hintsList.get(j), practiceAssetDirectory, scrapUrl));
            }
            questionResponse.response.hints = hintsList;

            String answerResponse = extractAnswerFromEncryption(questionResponse.response.data);

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

        try {
            ContentScraperUtil.generateTinCanXMLFile(practiceDirectory, practiceUrl, "en", "index.html",
                    ScraperConstants.ASSESMENT_TIN_CAN_FILE, scrapUrl.getPath(), "", "");
        } catch (TransformerException | ParserConfigurationException e) {
            e.printStackTrace();
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
