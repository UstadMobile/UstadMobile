package com.ustadmobile.lib.contentscrapers.ddl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LanguageList;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentCategory;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;


/**
 * The DDL Website comes in 3 languages - English, Farsi and Pashto
 * To scrape all content, we would need to go to each page and traverse the list
 * First we find our the max number of pages for each language by using the css selector on a.page-link
 * Once we found the max number, open each page on ddl website with the parameters /resources/list?page= and the page number until you hit the max
 *
 * Every resource is found by searching the html with a[href] and checking if href url contains "resource/"
 * Traverse all the pages until you hit Max number and then move to next language
 */
public class IndexDdlContent {


    static final String DDL = "DDL";
    private File destinationDirectory;

    private int maxNumber;
    private ContentEntry parentDdl;
    private ContentEntry langEntry;
    private int langCount = 0;
    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntryContentCategoryJoinDao contentCategoryChildJoinDao;
    private LanguageDao languageDao;


    public static void main(String[] args) throws IOException {

        if (args.length < 2) {
            System.err.println("Usage: <ddl website url> <file destination><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }

        UMLogUtil.setLevel(args.length == 3 ? args[2] : "");

        UMLogUtil.logError(args[0]);
        UMLogUtil.logError(args[1]);
        try {
            new IndexDdlContent().findContent(args[0], new File(args[1]));
        }catch (Exception e){
            UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logFatal("Exception running findContent DDL Scraper");
        }
    }



    public void findContent(String urlString, File destinationDir) throws IOException {

        try {
            URL url = new URL(urlString);
        } catch (MalformedURLException e) {
            UMLogUtil.logError("Index Malformed url" + urlString);
            throw new IllegalArgumentException("Malformed url" + urlString, e);
        }

        destinationDir.mkdirs();
        destinationDirectory = destinationDir;

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        contentCategoryChildJoinDao = repository.getContentEntryContentCategoryJoinDao();
        languageDao = repository.getLanguageDao();

        new LanguageList().addAllLanguages();

        Language englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English");
        Language farsiLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "Persian");
        Language pashtoLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "Pashto");


        ContentEntry masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);


        parentDdl = ContentScraperUtil.createOrUpdateContentEntry("https://www.ddl.af/", "Darakht-e Danesh",
                "https://www.ddl.af/", DDL, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                "Free and open educational resources for Afghanistan", false, EMPTY_STRING,
                "https://ddl.af/storage/files/logo-dd.png", EMPTY_STRING, EMPTY_STRING, contentEntryDao);


        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, parentDdl, 5);

        browseLanguages("en", englishLang);
        browseLanguages("fa", farsiLang);
        browseLanguages("ps", pashtoLang);

    }

    private void browseLanguages(String lang, Language langEntity) throws IOException {

        Document document = Jsoup.connect("https://www.darakhtdanesh.org/" + lang + "/resources/list")
                .header("X-Requested-With", "XMLHttpRequest").get();

        Elements pageList = document.select("a.page-link");

        langEntry = ContentScraperUtil.createOrUpdateContentEntry(lang + "/resources/list", lang,
                "https://www.ddl.af/" + lang + "/resources/list", DDL, LICENSE_TYPE_CC_BY, langEntity.getLangUid(), null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentDdl, langEntry, langCount);

        maxNumber = 0;
        for (Element page : pageList) {

            String num = page.text();
            try {
                int number = Integer.parseInt(num);
                if (number > maxNumber) {
                    maxNumber = number;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        browseList(lang, 1);
        langCount++;
    }

    private void browseList(String lang, int count) throws IOException {

        if (count > maxNumber) {
            return;
        }

        Document document = Jsoup.connect("https://www.darakhtdanesh.org/" + lang + "/resources/list?page=" + count)
                .header("X-Requested-With", "XMLHttpRequest").get();

        Elements resourceList = document.select("a[href]");

        for (Element resource : resourceList) {

            String url = resource.attr("href");
            if (url.contains("resource/")) {

                DdlContentScraper scraper = new DdlContentScraper(url, destinationDirectory);
                try {
                    scraper.scrapeContent();
                    ArrayList<ContentEntry> subjectAreas = scraper.getParentSubjectAreas();
                    ArrayList<ContentEntry> contentEntryArrayList = scraper.getContentEntries();
                    ArrayList<ContentCategory> contentCategories = scraper.getContentCategories();
                    int subjectAreaCount = 0;
                    for (ContentEntry subjectArea : subjectAreas) {

                        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao,
                                langEntry, subjectArea, subjectAreaCount++);

                        int fileCount = 0;
                        for (ContentEntry contentEntry : contentEntryArrayList) {

                            ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentParentChildJoinDao,
                                    subjectArea, contentEntry, fileCount++);

                            for(ContentCategory category: contentCategories){

                                ContentScraperUtil.insertOrUpdateChildWithMultipleCategoriesJoin(
                                        contentCategoryChildJoinDao, category, contentEntry);

                            }

                        }
                    }

                } catch (IOException e) {
                    UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                    UMLogUtil.logError("Error downloading resource at " + url);
                }

            }


        }

        browseList(lang, ++count);

    }

}
