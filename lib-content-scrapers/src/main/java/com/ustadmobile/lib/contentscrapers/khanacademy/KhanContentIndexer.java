package com.ustadmobile.lib.contentscrapers.khanacademy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao;
import com.ustadmobile.core.db.dao.ScrapeRunDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LanguageList;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.entities.ScrapeQueueItem;
import com.ustadmobile.lib.db.entities.ScrapeRun;
import com.ustadmobile.port.sharedse.util.WorkQueue;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
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
import java.util.concurrent.CountDownLatch;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.PNG_EXT;
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
public class KhanContentIndexer implements Runnable {

    public static final String ROOT_URL = "https://www.khanacademy.org/";

    public static final String TABLE_OF_CONTENTS_ROW = "TableOfContentsRow";
    public static final String SUBJECT_PAGE_TOPIC_CARD = "SubjectPageTopicCard";
    public static final String SUBJECT_CHALLENGE = "SubjectChallenge";
    public static final String SUBJECT_PROGRESS = "SubjectProgress";
    private static final String KHAN_PREFIX = "khan-id://";
    private static ContentEntryDao contentEntryDao;
    private static ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private static Language englishLang;


    private static Gson gson;
    private static ScrapeQueueItemDao queueDao;
    private int runId;
    private static WorkQueue scrapeWorkQueue;
    private static GenericObjectPool<ChromeDriver> factory;

    private ContentEntry parentEntry;
    private URL indexerUrl;
    private String contentType;
    private File indexLocation;

    private int scrapeQueueItemUid;


    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage:<file destination><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }

        UMLogUtil.logDebug(args[0]);
        UMLogUtil.setLevel(args.length == 2 ? args[1] : "");

        // UMLogUtil.logError(args[1]);

        try {
            ScrapeRunDao runDao = UmAppDatabase.getInstance(null).getScrapeRunDao();

            int runId = runDao.findPendingRunIdByScraperType(ScrapeRunDao.SCRAPE_TYPE_KHAN);
            if (runId == 0) {
                runId = (int) runDao.insert(new ScrapeRun(ScrapeRunDao.SCRAPE_TYPE_KHAN,
                        ScrapeQueueItemDao.STATUS_PENDING));
            }

            scrapeFromRoot(new File(args[0]), runId);
        } catch (Exception e) {
            UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logError("Main method exception catch khan");
        }
    }

    public static void startScrape(String startUrl, File destDir, int runId) throws IOException {
        //setup the database
        URL url;
        try {
            url = new URL(startUrl);
        } catch (MalformedURLException e) {
            UMLogUtil.logFatal("Index Malformed url" + startUrl);
            throw new IllegalArgumentException("Malformed url" + startUrl, e);
        }

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");

        destDir.mkdirs();
        File destinationDirectory = destDir;

        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        LanguageDao languageDao = repository.getLanguageDao();
        queueDao = db.getScrapeQueueItemDao();

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

        ContentScraperUtil.createQueueItem(queueDao, url, khanAcademyEntry, englishFolder, ScraperConstants.KhanContentType.TOPICS.getType(), runId, ScrapeQueueItem.ITEM_TYPE_INDEX);

        //create to work queues - one for indexing, one for content scrape
        WorkQueue.WorkQueueSource indexerSource = () -> {
            ScrapeQueueItem item = queueDao.getNextItemAndSetStatus(runId, ScrapeQueueItem.ITEM_TYPE_INDEX);
            if (item == null)
                return null;

            ContentEntry parent = contentEntryDao.findByUid(item.getSqiContentEntryParentUid());
            URL queueUrl;
            try {
                queueUrl = new URL(item.getScrapeUrl());
                return new KhanContentIndexer(queueUrl, parent, new File(item.getDestDir()),
                        item.getContentType(), item.getSqiUid(), runId);
            } catch (IOException ignored) {
                //Must never happen
                throw new RuntimeException("SEVERE: invalid URL to index: should not be in queue:" +
                        item.getScrapeUrl());
            }
        };

        WorkQueue.WorkQueueSource scraperSource = () -> {

            ScrapeQueueItem item = queueDao.getNextItemAndSetStatus(runId,
                    ScrapeQueueItem.ITEM_TYPE_SCRAPE);
            if (item == null) {
                return null;
            }

            ContentEntry parent = contentEntryDao.findByUid(item.getSqiContentEntryParentUid());

            URL scrapeUrl;
            try {
                scrapeUrl = new URL(item.getScrapeUrl());
                return new KhanContentScraper(scrapeUrl, new File(item.getDestDir()), parent,
                        item.getContentType(), item.getSqiUid(), factory);
            } catch (IOException ignored) {
                throw new RuntimeException("SEVERE: invalid URL to scrape: should not be in queue:" +
                        item.getScrapeUrl());
            }
        };

        factory = new GenericObjectPool<>(new KhanDriverFactory());
        //start the indexing work queue
        CountDownLatch indexerLatch = new CountDownLatch(1);
        WorkQueue indexWorkQueue = new WorkQueue(indexerSource, 4);
        indexWorkQueue.addEmptyWorkQueueListener((srcQueu) ->
                indexerLatch.countDown());
        indexWorkQueue.start();
        CountDownLatch scraperLatch = new CountDownLatch(1);
        scrapeWorkQueue = new WorkQueue(scraperSource, 6);
        scrapeWorkQueue.start();

        try {
            indexerLatch.await();
        } catch (InterruptedException ignored) {
        }

        scrapeWorkQueue.addEmptyWorkQueueListener((scrapeQueu) ->
                scraperLatch.countDown());
        try {
            scraperLatch.await();
        } catch (InterruptedException ignored) {

        }
        factory.close();

    }

    public static void scrapeFromRoot(File destDir, int runId) throws IOException {
        startScrape(ROOT_URL, destDir, runId);
    }


    KhanContentIndexer(URL indexerUrl, ContentEntry parentEntry, File indexLocation,
                       String contentType, int scrapeQueueItemUid, int runId) {
        this.indexerUrl = indexerUrl;
        this.parentEntry = parentEntry;
        this.contentType = contentType;
        this.indexLocation = indexLocation;
        this.scrapeQueueItemUid = scrapeQueueItemUid;
        this.runId = runId;
    }


    public void run() {
        System.gc();
        queueDao.setTimeStarted(scrapeQueueItemUid, System.currentTimeMillis());
        boolean successful = false;
        if (ScraperConstants.KhanContentType.TOPICS.getType().equals(contentType)) {
            try {
                browseTopics(parentEntry, indexerUrl, indexLocation);
                successful = true;
            } catch (Exception e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Error creating topics for url " + indexerUrl);
            }
        } else if (ScraperConstants.KhanContentType.SUBJECT.getType().equals(contentType)) {
            try {
                browseSubjects(parentEntry, indexerUrl, indexLocation);
                successful = true;
            } catch (Exception e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Error creating subjects for url " + indexerUrl);
            }
        }

        queueDao.updateSetStatusById(scrapeQueueItemUid, successful ? ScrapeQueueItemDao.STATUS_DONE : ScrapeQueueItemDao.STATUS_FAILED);
        queueDao.setTimeFinished(scrapeQueueItemUid, System.currentTimeMillis());
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
                        UMLogUtil.logError("Could not get json from the script for url " + url);
                        return EMPTY_STRING;
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

                ContentScraperUtil.createQueueItem(queueDao, topicUrl, topicEntry, fileLocation,
                        ScraperConstants.KhanContentType.SUBJECT.getType(), runId, ScrapeQueueItem.ITEM_TYPE_INDEX);

            }


        }
    }

    private void browseSubjects(ContentEntry topicEntry, URL topicUrl, File topicFolder) throws IOException {

        String subjectJson = getJsonStringFromScript(topicUrl.toString());

        SubjectListResponse response = gson.fromJson(subjectJson, SubjectListResponse.class);

        // one page on the website doesn't follow standard code
        if (response == null) {
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

                            ContentScraperUtil.createQueueItem(queueDao, subjectUrl, subjectEntry, topicFolder,
                                    ScraperConstants.KhanContentType.SUBJECT.getType(), runId, ScrapeQueueItem.ITEM_TYPE_INDEX);

                        }


                    }


                } else if (TABLE_OF_CONTENTS_ROW.equals(module.kind)) {

                    URL subjectUrl = new URL(topicUrl, module.url);

                    ContentEntry subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(module.slug, module.title, subjectUrl.toString(),
                            KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(), null,
                            module.description, false, EMPTY_STRING, module.icon, EMPTY_STRING
                            , EMPTY_STRING, contentEntryDao);

                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, topicEntry, subjectEntry, subjectCount++);

                    ContentScraperUtil.createQueueItem(queueDao, subjectUrl, subjectEntry, topicFolder,
                            ScraperConstants.KhanContentType.SUBJECT.getType(), runId, ScrapeQueueItem.ITEM_TYPE_INDEX);

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

            ContentScraperUtil.createQueueItem(queueDao, subjectUrl, subjectEntry, topicFolder,
                    ScraperConstants.KhanContentType.SUBJECT.getType(), runId,
                    ScrapeQueueItem.ITEM_TYPE_INDEX);

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
                    KHAN_PREFIX + contentItem.contentId, KHAN, LICENSE_TYPE_CC_BY_NC, englishLang.getLangUid(),
                    null, contentItem.description, true, EMPTY_STRING, contentItem.thumbnailUrl,
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao);

            ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentParentChildJoinDao, tutorialEntry, entry
                    , contentCount++);

            if (ScraperConstants.KhanContentType.VIDEO.getType().equals(contentItem.kind)) {
                String videoUrl = contentItem.downloadUrls.mp4;
                if (videoUrl == null || videoUrl.isEmpty()) {
                    videoUrl = contentItem.downloadUrls.mp4Low;
                    if (videoUrl == null) {
                        UMLogUtil.logError("Video was not available in any format for url: " + url);
                        continue;
                    }
                    UMLogUtil.logTrace("Video was not available in mp4, found in mp4-low at " + url);
                }
                url = new URL(url, videoUrl);
            }

            ContentScraperUtil.createQueueItem(queueDao, url, entry, newContentFolder,
                    contentItem.kind, runId, ScrapeQueueItem.ITEM_TYPE_SCRAPE);
            scrapeWorkQueue.checkQueue();

        }


    }

}
