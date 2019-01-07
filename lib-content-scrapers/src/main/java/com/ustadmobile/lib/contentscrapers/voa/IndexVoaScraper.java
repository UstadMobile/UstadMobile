package com.ustadmobile.lib.contentscrapers.voa;

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
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;
import static com.ustadmobile.lib.db.entities.ContentEntry.PUBLIC_DOMAIN;

/**
 * The VOA website can be scraped at https://learningenglish.voanews.com/
 * They are 4 categories in the website we are interested in:
 * beginning level, intermediate level, advanced level and us history
 *
 * Each of these categories have sub categories to scrape
 * The subcategory can be found using css selector h2.section-head a to find the href link
 * Each subcategory has a list of lessons that can have multiple video and audio.
 * These lessons can be found using css selector: div.container div.media-block-wrap div.media-block a.img-wrap
 */
public class IndexVoaScraper {

    private static final String VOA = "VOA";
    private URL url;
    private File destinationDirectory;
    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntryFileDao contentEntryFileDao;
    private ContentEntryContentEntryFileJoinDao contentEntryFileJoin;
    private ContentEntryFileStatusDao contentFileStatusDao;
    private LanguageDao languageDao;

    public static final String[] CATEGORY = {
            "Test Your English", "The Day in Photos",
            "Most Popular ", "Read, Listen & Learn"};
    private Language englishLang;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: <voa html url> <file destination>");
            System.exit(1);
        }

        System.out.println(args[0]);
        System.out.println(args[1]);
        try {
            new IndexVoaScraper().findContent(args[0], new File(args[1]));
        } catch (IOException e) {
            System.err.println("Exception running findContent");
            e.printStackTrace();
        }
    }


    public void findContent(String urlString, File destinationDir) throws IOException {


        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            System.out.println("Index Malformed url" + urlString);
            throw new IllegalArgumentException("Malformed url" + urlString, e);
        }

        destinationDir.mkdirs();
        destinationDirectory = destinationDir;

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        contentEntryFileDao = repository.getContentEntryFileDao();
        contentEntryFileJoin = repository.getContentEntryContentEntryFileJoinDao();
        contentFileStatusDao = db.getContentEntryFileStatusDao();
        languageDao = repository.getLanguageDao();

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

        findContentInCategories(beginningLevel, beginningUrl);
        findContentInCategories(intermediateLevel, intermediateUrl);
        findContentInCategories(advancedLevel, advancedUrl);
        findContentInCategories(usHistory, historyUrl);
    }

    private void findContentInCategories(ContentEntry parentEntry, String urlString) throws IOException {

        Document categoryDocument = Jsoup.connect(urlString).get();

        Elements categoryList = categoryDocument.select("h2.section-head a");

        int categoryCount = 0;
        for (Element category : categoryList) {

            String title = category.text();

            if (Arrays.stream(CATEGORY).parallel().noneMatch(title::contains)) {

                String hrefLink = category.attr("href");

                File categoryFolder = new File(destinationDirectory, title);
                categoryFolder.mkdirs();

                ContentEntry categoryEntry = ContentScraperUtil.createOrUpdateContentEntry(FilenameUtils.getBaseName(hrefLink),
                        title, hrefLink, VOA, PUBLIC_DOMAIN, englishLang.getLangUid(),
                        null, "", false, EMPTY_STRING, EMPTY_STRING,
                        EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentEntry, categoryEntry, categoryCount++);

                findLessons(categoryEntry, categoryFolder, hrefLink);

            }


        }


    }

    private void findLessons(ContentEntry categoryEntry, File categoryFolder, String hrefLink) throws IOException {

        URL lessonListUrl = new URL(url, hrefLink);

        Document lessonListDoc = Jsoup.connect(lessonListUrl.toString()).get();

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

            VoaScraper scraper = new VoaScraper(lesson.toString(), categoryFolder);
            try {
                scraper.scrapeContent();

                File content = new File(categoryFolder, FilenameUtils.getBaseName(lesson.getPath()) + ScraperConstants.ZIP_EXT);

                if (scraper.isContentUpdated()) {
                    ContentScraperUtil.insertContentEntryFile(content, contentEntryFileDao, contentFileStatusDao,
                            lessonEntry, ContentScraperUtil.getMd5(content), contentEntryFileJoin, true,
                            ScraperConstants.MIMETYPE_ZIP);

                } else {

                    ContentScraperUtil.checkAndUpdateDatabaseIfFileDownloadedButNoDataFound(content, lessonEntry, contentEntryFileDao,
                            contentEntryFileJoin, contentFileStatusDao, ScraperConstants.MIMETYPE_ZIP, true);

                }
            } catch (Exception e) {
                System.err.println("Unable to scrape content from " + title + " at url " + lesson.toString());
                e.printStackTrace();
            }


        }

        if (lessonListDoc.hasClass("btn--load-more")) {

            String loadMoreHref = lessonListDoc.selectFirst("p.btn--load-more a").attr("href");
            findLessons(categoryEntry, categoryFolder, loadMoreHref);

        }

    }


}
