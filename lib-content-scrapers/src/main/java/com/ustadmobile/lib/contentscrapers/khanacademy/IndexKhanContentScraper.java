package com.ustadmobile.lib.contentscrapers.khanacademy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY_NC;

/**
 * The Khan Academy website has a list of topics that they teach about at https://www.khanacademy.org/
 * Each topic have multiple sections eg grade 1 or algebra
 * Each section have different courses which have tutorial content in the from of videos, exercises, articles, quizzes, challenges.
 * <p>
 * Every page in khan academy have json content in a script that loads the information of the page.
 * Extract the json and put into the pojo object - TopicListResponse
 * TopicResponse has a list of domains which have all the topics in khan academy
 * Each domain has an href for the link to the next page - subjects
 * <p>
 * For the subject, extract the json from the script and load into SubjectListResponse
 * SubjectResponse has a list of modules which can be categorized with variable kind
 * TableOfContents - has another list of sub-subjects
 * SubjectProgress - has another list of sub-subjects which is found in list of modules with kind SubjectPageTopicCard
 * SubjectChallenge - quizzes for the subjects
 * <p>
 * Once we reach to the courses Page, extract the json from the script and load into SubjectListResponse
 * SubjectResponse has a list of tutorials which each have a list of content items
 * Every content item is a course categorized by Video, Exercise or Article.
 */
public class IndexKhanContentScraper {

    public static final String TABLE_OF_CONTENTS_ROW = "TableOfContentsRow";
    public static final String SUBJECT_PAGE_TOPIC_CARD = "SubjectPageTopicCard";
    public static final String SUBJECT_CHALLENGE = "SubjectChallenge";
    public static final String SUBJECT_PROGRESS = "SubjectProgress";
    private URL url;
    private File destinationDirectory;
    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntryFileDao contentEntryFileDao;
    private ContentEntryContentEntryFileJoinDao contentEntryFileJoin;
    private ContentEntryFileStatusDao contentFileStatusDao;
    private LanguageDao languageDao;
    private Language englishLang;

    String KHAN = "Khan Academy";
    private Gson gson;
    private ChromeDriver driver;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <khan url> <file destination><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }

        UMLogUtil.setLevel(args.length == 3 ? args[2] : "");

        UMLogUtil.logDebug(args[0]);
        UMLogUtil.logError(args[1]);

        try {
            new IndexKhanContentScraper().findContent(args[0], new File(args[1]));
        } catch (Exception e) {
            UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logError("Main method exception catch khan");
        }
    }


    public void findContent(String urlString, File destinationDir) throws IOException {

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            UMLogUtil.logFatal("Index Malformed url" + urlString);
            throw new IllegalArgumentException("Malformed url" + urlString, e);
        }

        destinationDir.mkdirs();
        destinationDirectory = destinationDir;

        ContentScraperUtil.setChromeDriverLocation();
        driver = ContentScraperUtil.loginKhanAcademy("https://www.khanacademy.org/login");

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        contentEntryFileDao = repository.getContentEntryFileDao();
        contentEntryFileJoin = repository.getContentEntryContentEntryFileJoinDao();
        contentFileStatusDao = db.getContentEntryFileStatusDao();
        languageDao = repository.getLanguageDao();

        gson = new GsonBuilder().disableHtmlEscaping().create();

        new LanguageList().addAllLanguages();

        englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English");


        ContentEntry masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentEntry khanAcademyEntry = ContentScraperUtil.createOrUpdateContentEntry("https://www.khanacademy.org/", "Khan Academy",
                "https://www.khanacademy.org/", KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(), null,
                "You can learn anything.\n" +
                        "For free. For everyone. Forever.", false, EMPTY_STRING,
                "https://cdn.kastatic.org/images/khan-logo-dark-background.new.png",
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, khanAcademyEntry, 6);

        File englishFolder = new File(destinationDirectory, "en");
        englishFolder.mkdirs();

        browseTopics(khanAcademyEntry, url, englishFolder);

        driver.close();
        driver.quit();

    }

    public static String getJsonStringFromScript(String url) throws IOException {

        Document document = Jsoup.connect(url).maxBodySize(9437184).get();

        Elements scriptList = document.getElementsByTag("script");
        for (Element script : scriptList) {

            for (DataNode node : script.dataNodes()) {

                if (node.getWholeData().contains("ReactComponent(")) {

                    String data = node.getWholeData();
                    try {
                        int index = data.indexOf("ReactComponent(") + 15;
                        int end = data.indexOf("loggedIn\": false})") + 17;
                        return data.substring(index, end);
                    } catch (IndexOutOfBoundsException e) {
                        throw new IOException("Could not get json from the script for url " + url);
                    }
                }
            }
        }
        return EMPTY_STRING;

    }


    public void browseTopics(ContentEntry parent, URL url, File fileLocation) throws IOException {

        String jsonString = getJsonStringFromScript(url.toString());

        TopicListResponse response = gson.fromJson(jsonString, TopicListResponse.class);

        List<TopicListResponse.ComponentData.Modules> modulesList = response.componentProps.modules;

        for (TopicListResponse.ComponentData.Modules module : modulesList) {

            if (module.domains == null || module.domains.isEmpty()) {
                continue;
            }

            List<TopicListResponse.ComponentData.Modules.Domains> domainList = module.domains;

            int topicCount = 0;
            for (TopicListResponse.ComponentData.Modules.Domains domain : domainList) {

                URL topicUrl = new URL(url, domain.href);

                ContentEntry topicEntry = ContentScraperUtil.createOrUpdateContentEntry(domain.identifier,
                        domain.translatedTitle, topicUrl.toString(), KHAN,
                        LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(), null, EMPTY_STRING, false,
                        EMPTY_STRING, domain.icon, EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, topicEntry,
                        topicCount++);

                browseSubjects(topicEntry, topicUrl, fileLocation);

            }


        }
    }

    private void browseSubjects(ContentEntry topicEntry, URL topicUrl, File topicFolder) throws IOException {

        String subjectJson = getJsonStringFromScript(topicUrl.toString());

        SubjectListResponse response = gson.fromJson(subjectJson, SubjectListResponse.class);

        // one page on the website doesn't follow standard code
        if (response.componentProps == null) {
            browseHourOfCode(topicEntry, topicUrl, topicFolder);
            return;
        }

        List<SubjectListResponse.ComponentData.Curation.Tab> tabList = response.componentProps.curation.tabs;

        for (SubjectListResponse.ComponentData.Curation.Tab tab : tabList) {

            if (tab.modules == null || tab.modules.isEmpty()) {
                continue;
            }

            List<ModuleResponse> moduleList = tab.modules;

            int subjectCount = 0;
            for (ModuleResponse module : moduleList) {

                if (SUBJECT_PROGRESS.equals(module.kind)) {

                    List<ModuleResponse> moduleItems = module.modules;

                    if (module.modules == null || module.modules.isEmpty()) {
                        continue;
                    }

                    for (ModuleResponse moduleItem : moduleItems) {

                        if (SUBJECT_PAGE_TOPIC_CARD.equals(moduleItem.kind)) {

                            URL subjectUrl = new URL(topicUrl, moduleItem.url);

                            ContentEntry subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(moduleItem.slug, moduleItem.title,
                                    subjectUrl.toString(), KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(), null,
                                    moduleItem.description, false, EMPTY_STRING, moduleItem.icon, EMPTY_STRING
                                    , EMPTY_STRING, contentEntryDao);

                            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, topicEntry, subjectEntry, subjectCount++);

                            browseSubjects(subjectEntry, subjectUrl, topicFolder);

                        }


                    }


                } else if (TABLE_OF_CONTENTS_ROW.equals(module.kind)) {

                    URL subjectUrl = new URL(topicUrl, module.url);

                    ContentEntry subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(module.slug, module.title, subjectUrl.toString(),
                            KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(), null,
                            module.description, false, EMPTY_STRING, module.icon, EMPTY_STRING
                            , EMPTY_STRING, contentEntryDao);

                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, topicEntry, subjectEntry, subjectCount++);

                    browseSubjects(subjectEntry, subjectUrl, topicFolder);

                } else if (SUBJECT_CHALLENGE.equals(module.kind)) {

                    // TODO

                } else if (module.tutorials != null && !module.tutorials.isEmpty()) {

                    List<ModuleResponse.Tutorial> tutorialList = module.tutorials;

                    int tutorialCount = 0;
                    for (ModuleResponse.Tutorial tutorial : tutorialList) {

                        if (tutorial == null) {
                            continue;
                        }

                        URL tutorialUrl = new URL(topicUrl, tutorial.url);

                        ContentEntry tutorialEntry = ContentScraperUtil.createOrUpdateContentEntry(tutorial.slug, tutorial.title,
                                tutorialUrl.toString(), KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(),
                                null, tutorial.description, false, EMPTY_STRING, EMPTY_STRING,
                                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, topicEntry,
                                tutorialEntry, tutorialCount++);

                        List<ModuleResponse.Tutorial.ContentItem> contentList = tutorial.contentItems;

                        browseContent(contentList, tutorialEntry, tutorialUrl, topicFolder);


                    }


                }


            }


        }


    }

    private void browseHourOfCode(ContentEntry topicEntry, URL topicUrl, File topicFolder) throws IOException {

        Document document = Jsoup.connect(topicUrl.toString()).get();

        Elements subjectList = document.select("div.hoc-box-white");

        int hourOfCode = 0;
        for (Element subject : subjectList) {

            String imageSrc = subject.selectFirst("img").attr("src");
            String title = subject.selectFirst("h3").text();
            String description = subject.selectFirst("p").text();
            String hrefLink = subject.selectFirst("a").attr("href");

            hrefLink = hrefLink.substring(0, hrefLink.indexOf("/v/"));

            URL subjectUrl = new URL(topicUrl, hrefLink);

            ContentEntry subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(hrefLink, title,
                    subjectUrl.toString(), KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(),
                    null, description, false, EMPTY_STRING, new URL(topicUrl, imageSrc).toString(),
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao);

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, topicEntry,
                    subjectEntry, hourOfCode++);

            browseSubjects(subjectEntry, subjectUrl, topicFolder);

        }

    }

    private void browseContent(List<ModuleResponse.Tutorial.ContentItem> contentList, ContentEntry tutorialEntry, URL tutorialUrl, File subjectFolder) throws IOException {

        if (contentList == null) {
            UMLogUtil.logError("no content list inside url " + tutorialUrl.toString());
            return;
        }

        if (contentList.isEmpty()) {
            UMLogUtil.logError("empty content list inside url " + tutorialUrl.toString());
            return;
        }

        int contentCount = 0;
        for (ModuleResponse.Tutorial.ContentItem contentItem : contentList) {

            if (contentItem == null) {
                continue;
            }

            URL url = new URL(tutorialUrl, contentItem.nodeUrl);
            File newContentFolder = new File(subjectFolder, contentItem.contentId);
            newContentFolder.mkdirs();


            ContentEntry entry = ContentScraperUtil.createOrUpdateContentEntry(contentItem.slug, contentItem.title,
                    contentItem.contentId, KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(),
                    null, contentItem.description, true, EMPTY_STRING, contentItem.thumbnailUrl,
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao);

            ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentParentChildJoinDao, tutorialEntry, entry
                    , contentCount++);

            KhanContentScraper scraper = new KhanContentScraper(newContentFolder, driver);
            try {
                switch (contentItem.kind) {

                    case "Video":
                        String videoUrl = contentItem.downloadUrls.mp4Low;
                        if(videoUrl == null || videoUrl.isEmpty()){
                            UMLogUtil.logInfo("Video was not available in mp4-low, found in mp4 at " + url);
                            videoUrl = contentItem.downloadUrls.mp4;
                        }
                        scraper.scrapeVideoContent(new URL(url, videoUrl).toString());
                        break;
                    case "Exercise":
                        scraper.scrapeExerciseContent(url.toString());
                        break;
                    case "Article":
                        scraper.scrapeArticleContent(url.toString());
                        break;
                    default:
                        UMLogUtil.logError("unsupported kind = " + contentItem.kind + " at url = " + url);
                        break;

                }

                File content = new File(newContentFolder, newContentFolder.getName() + ScraperConstants.ZIP_EXT);

                if (scraper.isContentUpdated()) {
                    ContentScraperUtil.insertContentEntryFile(content, contentEntryFileDao, contentFileStatusDao,
                            entry, ContentScraperUtil.getMd5(content), contentEntryFileJoin, true,
                            ScraperConstants.MIMETYPE_ZIP);

                } else {

                    ContentScraperUtil.checkAndUpdateDatabaseIfFileDownloadedButNoDataFound(content, entry, contentEntryFileDao,
                            contentEntryFileJoin, contentFileStatusDao, ScraperConstants.MIMETYPE_ZIP, true);

                }

            } catch (Exception e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Unable to scrape content from " + contentItem.title + " at url " + url);
            }


        }


    }
}
