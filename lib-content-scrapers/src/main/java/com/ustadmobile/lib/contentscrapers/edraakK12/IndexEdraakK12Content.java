package com.ustadmobile.lib.contentscrapers.edraakK12;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao;
import com.ustadmobile.core.db.dao.ScrapeRunDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LanguageList;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.entities.ScrapeQueueItem;
import com.ustadmobile.lib.db.entities.ScrapeRun;
import com.ustadmobile.port.sharedse.util.WorkQueue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static com.ustadmobile.lib.db.entities.ContentEntry.ALL_RIGHTS_RESERVED;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;


/**
 * The Edraak Website uses json to generate their website to get all the courses and all the content within them.
 * https://programs.edraak.org/api/component/5a6087f46380a6049b33fc19/?states_program_id=41
 * <p>
 * Each section of the website is made out of categories and sections which follows the structure of the json
 * <p>
 * The main json has a component type named MainContentTrack
 * This has 6 children which are the main categories found in the website, they have a component type named Section
 * <p>
 * Each Section has list of Subsections or Course Content
 * SubSections are identified by the component type named SubSection
 * SubSections has list of Course Content
 * Course Content contains a Quiz(list of questions) or a Course that has video and list a questions.
 * Courses and Quizzes are both identified with the component type named ImportedComponent
 * <p>
 * The goal of the index class is to find all the importedComponent by going to the child of each component type
 * until the component type found is ImportedComponent. Once it is found, EdraakK12ContentScraper
 * will decide if its a quiz or course and scrap its content
 */
public class IndexEdraakK12Content {

    private static final String ROOT_URL = "https://programs.edraak.org/api/component/5a6087f46380a6049b33fc19/?states_program_id=41";

    public static final String EDRAAK = "Edraak";
    private static URL url;
    private static File destinationDirectory;
    private static ContentResponse response;
    private static ContentEntryDao contentEntryDao;
    private static ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private static Language arabicLang;
    private static ScrapeQueueItemDao queueDao;
    private static WorkQueue scrapeWorkQueue;
    private static int runId;
    private static File containerDirectory;


    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <file destination><file container><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }
        UMLogUtil.setLevel(args.length == 3 ? args[2] : "");
        UMLogUtil.logInfo(args[0]);

        try {
            ScrapeRunDao runDao = UmAppDatabase.getInstance(null).getScrapeRunDao();

            runId = runDao.findPendingRunIdByScraperType(ScrapeRunDao.SCRAPE_TYPE_EDRAAK);
            if (runId == 0) {
                runId = (int) runDao.insert(new ScrapeRun(ScrapeRunDao.SCRAPE_TYPE_EDRAAK,
                        ScrapeQueueItemDao.STATUS_PENDING));
            }

            scrapeFromRoot(new File(args[0]), new File(args[1]), runId);
        } catch (Exception e) {
            UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logError("Main method exception catch khan");
        }

    }

    public static void scrapeFromRoot(File dest, File containerDir, int runId) throws IOException {
        startScrape(ROOT_URL, dest, containerDir, runId);
    }

    public static void startScrape(String scrapeUrl, File destinationDir, File containerDir, int runIdscrape) throws IOException {
        try {
            url = new URL(scrapeUrl);
        } catch (MalformedURLException e) {
            UMLogUtil.logError("url from main is Malformed = " + scrapeUrl);
            throw new IllegalArgumentException("Malformed url" + scrapeUrl, e);
        }

        destinationDir.mkdirs();
        containerDir.mkdirs();
        containerDirectory = containerDir;
        destinationDirectory = destinationDir;
        runId = runIdscrape;

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        LanguageDao languageDao = repository.getLanguageDao();
        queueDao = db.getScrapeQueueItemDao();

        new LanguageList().addAllLanguages();

        arabicLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "Arabic");
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            response = new GsonBuilder().disableHtmlEscaping().create().fromJson(IOUtils.toString(connection.getInputStream(), UTF_ENCODING), ContentResponse.class);
        } catch (IOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("JSON INVALID", e.getCause());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        ContentEntry masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, arabicLang.getLangUid(), null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        String description = "تعليم مجانيّ\n" +
                "إلكترونيّ باللغة العربيّة!" +
                "\n Free Online \n" +
                "Education, In Arabic!";

        description = new String(description.getBytes(), UTF_ENCODING);

        ContentEntry edraakParentEntry = ContentScraperUtil.createOrUpdateContentEntry("https://www.edraak.org/k12/", "Edraak K12",
                "https://www.edraak.org/k12/", EDRAAK, ALL_RIGHTS_RESERVED, arabicLang.getLangUid(), null,
                description, false, EMPTY_STRING, "https://www.edraak.org/static/images/logo-dark-ar.fa1399e8d134.png",
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);


        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, edraakParentEntry, 0);


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
                return new EdraakK12ContentScraper(scrapeContentUrl,
                        new File(item.getDestDir()),
                        containerDir,
                        parent, item.getSqiUid());
            } catch (IOException ignored) {
                throw new RuntimeException("SEVERE: invalid URL to scrape: should not be in queue:" +
                        item.getScrapeUrl());
            }
        };

        CountDownLatch scraperLatch = new CountDownLatch(1);
        scrapeWorkQueue = new WorkQueue(scraperSource, 1);
        scrapeWorkQueue.start();

        findImportedComponent(response, edraakParentEntry);

        scrapeWorkQueue.addEmptyWorkQueueListener((scrapeQueu) ->
                scraperLatch.countDown());
        try {
            scraperLatch.await();
        } catch (InterruptedException ignored) {

        }

    }

    private static void findImportedComponent(ContentResponse parentContent, ContentEntry parentEntry) throws MalformedURLException {

        if (ContentScraperUtil.isImportedComponent(parentContent.component_type)) {

            // found the last child
            String scrapeUrl = EdraakK12ContentScraper.generateUrl(
                    url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ?
                            (":" + url.getPort()) : "") + "/api/", parentContent.id,
                    parentContent.program == 0 ? response.program : parentContent.program);

            ContentScraperUtil.createQueueItem(queueDao, new URL(scrapeUrl), parentEntry,
                    new File(destinationDirectory, parentContent.id), "",
                    runId, ScrapeQueueItem.ITEM_TYPE_SCRAPE);
            scrapeWorkQueue.checkQueue();

        } else {

            for (ContentResponse children : parentContent.children) {

                String sourceUrl = children.id;
                boolean isLeaf = ContentScraperUtil.isImportedComponent(children.component_type);

                ContentEntry childEntry = ContentScraperUtil.createOrUpdateContentEntry(children.id, children.title,
                        sourceUrl, EDRAAK, getLicenseType(children.license), arabicLang.getLangUid(), null,
                        EMPTY_STRING, isLeaf, EMPTY_STRING, EMPTY_STRING,
                        EMPTY_STRING, EMPTY_STRING, contentEntryDao);


                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentEntry, childEntry, children.child_index);

                findImportedComponent(children, childEntry);

            }

        }
    }

    private static int getLicenseType(String license) {
        if (license.toLowerCase().contains("cc-by-nc-sa")) {
            return ContentEntry.LICESNE_TYPE_CC_BY_NC_SA;
        } else if (license.toLowerCase().contains("all_rights_reserved")) {
            return ALL_RIGHTS_RESERVED;
        } else {
            UMLogUtil.logError("License type not matched for license: " + license);
            return ALL_RIGHTS_RESERVED;
        }
    }

}
