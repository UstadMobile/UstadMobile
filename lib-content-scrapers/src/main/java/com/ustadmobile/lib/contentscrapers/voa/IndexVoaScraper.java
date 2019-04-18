package com.ustadmobile.lib.contentscrapers.voa;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao;
import com.ustadmobile.core.db.dao.ScrapeRunDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LanguageList;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanDriverFactory;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.entities.ScrapeQueueItem;
import com.ustadmobile.lib.db.entities.ScrapeRun;
import com.ustadmobile.port.sharedse.util.WorkQueue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;
import static com.ustadmobile.lib.db.entities.ContentEntry.PUBLIC_DOMAIN;

/**
 * The VOA website can be scraped at https://learningenglish.voanews.com/
 * They are 4 categories in the website we are interested in:
 * beginning level, intermediate level, advanced level and us history
 * <p>
 * Each of these categories have sub categories to scrape
 * The subcategory can be found using css selector h2.section-head a to find the href link
 * Each subcategory has a list of lessons that can have multiple video and audio.
 * These lessons can be found using css selector: div.container div.media-block-wrap div.media-block a.img-wrap
 */
public class IndexVoaScraper implements Runnable {

    private static final String ROOT_URL = "https://learningenglish.voanews.com/";

    private static final String VOA = "VOA";
    private static URL url;
    private static ContentEntryDao contentEntryDao;
    private static ContentEntryParentChildJoinDao contentParentChildJoinDao;

    private static final String[] CATEGORY = {
            "Test Your English", "The Day in Photos",
            "Most Popular ", "Read, Listen & Learn"};
    private static Language englishLang;
    private static ScrapeQueueItemDao queueDao;
    private static WorkQueue scrapeWorkQueue;


    private final URL indexerUrl;
    private final ContentEntry parentEntry;
    private final String contentType;
    private final File indexLocation;
    private final int scrapeQueueItemUid;
    private final int runId;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: <file destination><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }
        UMLogUtil.setLevel(args.length == 2 ? args[1] : "");
        UMLogUtil.logInfo(args[0]);

        try {
            ScrapeRunDao runDao = UmAppDatabase.getInstance(null).getScrapeRunDao();

            int runId = runDao.findPendingRunIdByScraperType(ScrapeRunDao.SCRAPE_TYPE_VOA);
            if (runId == 0) {
                runId = (int) runDao.insert(new ScrapeRun(ScrapeRunDao.SCRAPE_TYPE_VOA,
                        ScrapeQueueItemDao.STATUS_PENDING));
            }

            scrapeFromRoot(new File(args[0]), runId);
        } catch (Exception e) {
            UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logError("Main method exception catch khan");
        }
    }

    private static void scrapeFromRoot(File dest, int runId) throws IOException {
        startScrape(ROOT_URL, dest, runId);
    }

    private static void startScrape(String scrapeUrl, File destinationDir, int runId) throws IOException {
        try {
            url = new URL(scrapeUrl);
        } catch (MalformedURLException e) {
            UMLogUtil.logError("Index Malformed url" + scrapeUrl);
            throw new IllegalArgumentException("Malformed url" + scrapeUrl, e);
        }

        destinationDir.mkdirs();

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        LanguageDao languageDao = repository.getLanguageDao();
        queueDao = db.getScrapeQueueItemDao();

        new LanguageList().addAllLanguages();

        englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English");

        ContentEntry masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentEntry parentVoa = ContentScraperUtil.createOrUpdateContentEntry("https://learningenglish.voanews.com/", "Voice of America - Learning English",
                "https://learningenglish.voanews.com/", VOA, PUBLIC_DOMAIN, englishLang.getLangUid(), null,
                "Learn American English with English language lessons from Voice of America. " +
                        "VOA Learning English helps you learn English with vocabulary, listening and " +
                        "comprehension lessons through daily news and interactive English learning activities.",
                false, EMPTY_STRING, "https://learningenglish.voanews.com/Content/responsive/VOA/img/top_logo_news.png",
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, parentVoa, 7);

        String beginningUrl = "https://learningenglish.voanews.com/p/5609.html";
        String intermediateUrl = "https://learningenglish.voanews.com/p/5610.html";
        String advancedUrl = "https://learningenglish.voanews.com/p/5611.html";
        String historyUrl = "https://learningenglish.voanews.com/p/6353.html";

        ContentEntry beginningLevel = ContentScraperUtil.createOrUpdateContentEntry("5609", "Beginning Level",
                beginningUrl, VOA, PUBLIC_DOMAIN, englishLang.getLangUid(),
                null, "", false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentEntry intermediateLevel = ContentScraperUtil.createOrUpdateContentEntry("5610", "Intermediate Level",
                intermediateUrl, VOA, PUBLIC_DOMAIN, englishLang.getLangUid(),
                null, "", false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentEntry advancedLevel = ContentScraperUtil.createOrUpdateContentEntry("5611", "Advanced Level",
                advancedUrl, VOA, PUBLIC_DOMAIN, englishLang.getLangUid(),
                null, "", false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentEntry usHistory = ContentScraperUtil.createOrUpdateContentEntry("6353", "US History",
                historyUrl, VOA, PUBLIC_DOMAIN, englishLang.getLangUid(),
                null, "", false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentVoa, beginningLevel, 0);
        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentVoa, intermediateLevel, 1);
        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentVoa, advancedLevel, 2);
        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentVoa, usHistory, 3);

        ContentScraperUtil.createQueueItem(queueDao, new URL(beginningUrl), beginningLevel,
                destinationDir, ScraperConstants.VoaContentType.LEVELS.getType(), runId, ScrapeQueueItem.ITEM_TYPE_INDEX);
        ContentScraperUtil.createQueueItem(queueDao, new URL(intermediateUrl), intermediateLevel,
                destinationDir, ScraperConstants.VoaContentType.LEVELS.getType(), runId, ScrapeQueueItem.ITEM_TYPE_INDEX);
        ContentScraperUtil.createQueueItem(queueDao, new URL(advancedUrl), advancedLevel,
                destinationDir, ScraperConstants.VoaContentType.LEVELS.getType(), runId, ScrapeQueueItem.ITEM_TYPE_INDEX);
        ContentScraperUtil.createQueueItem(queueDao, new URL(historyUrl), usHistory,
                destinationDir, ScraperConstants.VoaContentType.LEVELS.getType(), runId, ScrapeQueueItem.ITEM_TYPE_INDEX);

        WorkQueue.WorkQueueSource indexerSource = () -> {
            ScrapeQueueItem item = queueDao.getNextItemAndSetStatus(runId, ScrapeQueueItem.ITEM_TYPE_INDEX);
            if (item == null)
                return null;

            ContentEntry parent = contentEntryDao.findByUid(item.getSqiContentEntryParentUid());
            URL queueUrl;
            try {
                queueUrl = new URL(item.getScrapeUrl());
                return new IndexVoaScraper(queueUrl, parent, new File(item.getDestDir()),
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

            URL scrapeContentUrl;
            try {
                scrapeContentUrl = new URL(item.getScrapeUrl());
                return new VoaScraper(scrapeContentUrl, new File(item.getDestDir()),
                        parent, item.getSqiUid());
            } catch (IOException ignored) {
                throw new RuntimeException("SEVERE: invalid URL to scrape: should not be in queue:" +
                        item.getScrapeUrl());
            }
        };
        //start the indexing work queue
        CountDownLatch indexerLatch = new CountDownLatch(1);
        WorkQueue indexWorkQueue = new WorkQueue(indexerSource, 2);
        indexWorkQueue.addEmptyWorkQueueListener((srcQueu) ->
                indexerLatch.countDown());
        indexWorkQueue.start();
        CountDownLatch scraperLatch = new CountDownLatch(1);
        scrapeWorkQueue = new WorkQueue(scraperSource, 1);
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
    }


    IndexVoaScraper(URL indexerUrl, ContentEntry parentEntry, File indexLocation,
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
        if (ScraperConstants.VoaContentType.LEVELS.getType().equals(contentType)) {
            try {
                findContentInCategories(parentEntry, indexerUrl, indexLocation);
                successful = true;
            } catch (Exception e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Error creating topics for url " + indexerUrl);
            }
        } else if (ScraperConstants.VoaContentType.LESSONS.getType().equals(contentType)) {

            try {
                findLessons(parentEntry, indexLocation, indexerUrl);
                successful = true;
            } catch (Exception e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Error creating subjects for url " + indexerUrl);
            }
        }

        queueDao.updateSetStatusById(scrapeQueueItemUid, successful ? ScrapeQueueItemDao.STATUS_DONE : ScrapeQueueItemDao.STATUS_FAILED);
        queueDao.setTimeFinished(scrapeQueueItemUid, System.currentTimeMillis());
    }


    private void findContentInCategories(ContentEntry parentEntry, URL urlString, File destinationDirectory) throws IOException {

        Document categoryDocument = Jsoup.connect(urlString.toString()).get();

        Elements categoryList = categoryDocument.select("h2.section-head a");

        int categoryCount = 0;
        for (Element category : categoryList) {

            String title = category.text();

            if (Arrays.stream(CATEGORY).parallel().noneMatch(title::contains)) {

                String hrefLink = category.attr("href");
                try {

                    File categoryFolder = new File(destinationDirectory, title);
                    categoryFolder.mkdirs();

                    URL lessonListUrl = new URL(urlString, hrefLink);

                    ContentEntry categoryEntry = ContentScraperUtil.createOrUpdateContentEntry(FilenameUtils.getBaseName(hrefLink),
                            title, lessonListUrl.toString(), VOA, PUBLIC_DOMAIN, englishLang.getLangUid(),
                            null, "", false, EMPTY_STRING, EMPTY_STRING,
                            EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentEntry, categoryEntry, categoryCount++);

                    ContentScraperUtil.createQueueItem(queueDao, lessonListUrl, categoryEntry, categoryFolder,
                            ScraperConstants.VoaContentType.LESSONS.getType(), runId, ScrapeQueueItem.ITEM_TYPE_INDEX);

                } catch (IOException e) {
                    UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                    UMLogUtil.logError("Error with voa category = " + hrefLink + " with title " + title);
                }

            }


        }


    }

    private void findLessons(ContentEntry categoryEntry, File categoryFolder, URL lessonUrl) throws IOException {

        Document lessonListDoc = Jsoup.connect(lessonUrl.toString()).get();

        Elements elementList = lessonListDoc.select("div.container div.media-block-wrap div.media-block a.img-wrap");

        int lessonCount = 0;
        for (Element lessonElement : elementList) {

            String lessonHref = lessonElement.attr("href");
            URL lesson = new URL(url, lessonHref);

            String title = lessonElement.attr("title");

            ContentEntry lessonEntry = ContentScraperUtil.createOrUpdateContentEntry(FilenameUtils.getBaseName(lessonHref),
                    title, lesson.toString(), VOA, PUBLIC_DOMAIN, englishLang.getLangUid(),
                    null, "", true, EMPTY_STRING, EMPTY_STRING,
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao);

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, categoryEntry, lessonEntry, lessonCount++);

            ContentScraperUtil.createQueueItem(queueDao, lesson, lessonEntry, categoryFolder,
                    "", runId, ScrapeQueueItem.ITEM_TYPE_SCRAPE);
            scrapeWorkQueue.checkQueue();

        }

        if (lessonListDoc.hasClass("btn--load-more")) {

            String loadMoreHref = lessonListDoc.selectFirst("p.btn--load-more a").attr("href");
            ContentScraperUtil.createQueueItem(queueDao, new URL(indexerUrl, loadMoreHref), categoryEntry, categoryFolder,
                    ScraperConstants.VoaContentType.LESSONS.getType(), runId, ScrapeQueueItem.ITEM_TYPE_INDEX);

        }

    }


}
