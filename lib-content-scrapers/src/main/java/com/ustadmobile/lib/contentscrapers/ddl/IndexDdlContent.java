package com.ustadmobile.lib.contentscrapers.ddl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LanguageList;
import com.ustadmobile.lib.db.entities.ContentCategory;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;

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


    private URL url;
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
        if (args.length != 2) {
            System.err.println("Usage: <ddl website url> <file destination>");
            System.exit(1);
        }

        System.out.println(args[0]);
        System.out.println(args[1]);
        new IndexDdlContent().findContent(args[0], new File(args[1]));
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
        db.setMaster(true);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        contentCategoryChildJoinDao = repository.getContentEntryContentCategoryJoinDao();
        languageDao = repository.getLanguageDao();

        new LanguageList().addAllLanguages();

        Language englishLang = ContentScraperUtil.insertOrUpdateLanguage(languageDao, "English");
        Language farsiLang = ContentScraperUtil.insertOrUpdateLanguage(languageDao, "Persian");
        Language pashtoLang = ContentScraperUtil.insertOrUpdateLanguage(languageDao, "Pashto");



        ContentEntry masterRootParent = contentEntryDao.findBySourceUrl("root");
        if (masterRootParent == null) {
            masterRootParent = new ContentEntry();
            masterRootParent= setContentEntryData(masterRootParent, "root",
                    "Ustad Mobile", "root", englishLang, false, "");
            masterRootParent.setContentEntryUid(contentEntryDao.insert(masterRootParent));
        } else {
            masterRootParent = setContentEntryData(masterRootParent, "root",
                    "Ustad Mobile", "root", englishLang, false, "");
            contentEntryDao.update(masterRootParent);
        }

        parentDdl = contentEntryDao.findBySourceUrl("https://www.ddl.af/");
        if (parentDdl == null) {
            parentDdl = new ContentEntry();
            parentDdl = setContentEntryData(parentDdl, "https://www.ddl.af/",
                    "Darakht-e Danesh", "https://www.ddl.af/", englishLang, false, "Free and open educational resources for Afghanistan");
            parentDdl.setThumbnailUrl("https://www.ddl.af/storage/files/logo-dd.png");
            parentDdl.setContentEntryUid(contentEntryDao.insert(parentDdl));
        } else {
            parentDdl = setContentEntryData(parentDdl, "https://www.ddl.af/",
                    "Darakht-e Danesh", "https://www.ddl.af/", englishLang, false, "Free and open educational resources for Afghanistan");
            parentDdl.setThumbnailUrl("https://www.ddl.af/storage/files/logo-dd.png");
            contentEntryDao.update(parentDdl);
        }

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, parentDdl, 5);

        browseLanguages("en", englishLang);
        browseLanguages("fa", farsiLang);
        browseLanguages("ps", pashtoLang);

    }

    private ContentEntry setContentEntryData(ContentEntry entry, String id, String title, String sourceUrl, Language lang, boolean isLeaf, String desc) {
        entry.setEntryId(id);
        entry.setTitle(title);
        entry.setSourceUrl(sourceUrl);
        entry.setPublisher("DDL");
        entry.setLicenseType(ContentEntry.LICENSE_TYPE_CC_BY);
        entry.setPrimaryLanguageUid(lang.getLangUid());
        entry.setLeaf(isLeaf);
        entry.setDescription(desc);
        return entry;
    }

    private void browseLanguages(String lang, Language langEntity) throws IOException {

        Document document = Jsoup.connect("https://www.darakhtdanesh.org/" + lang + "/resources/list")
                .header("X-Requested-With", "XMLHttpRequest").get();

        Elements pageList = document.select("a.page-link");


        langEntry = contentEntryDao.findBySourceUrl(lang + "/resources/list");
        if (langEntry == null) {
            langEntry = new ContentEntry();
            langEntry = setContentEntryData(langEntry, lang + "/resources/list",
                    lang, lang + "/resources/list", langEntity, false, "");
            langEntry.setContentEntryUid(contentEntryDao.insert(langEntry));
        } else {
            langEntry = setContentEntryData(langEntry, lang + "/resources/list",
                    lang, lang + "/resources/list", langEntity, false, "");
            contentEntryDao.update(langEntry);
        }

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentDdl, langEntry, langCount);

        maxNumber = 0;
        for (Element page : pageList) {

            String num = page.text();
            try {
                int number = Integer.parseInt(num);
                if (number > maxNumber) {
                    maxNumber = number;
                }
            } catch (NumberFormatException e) {
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

                } catch (IOException | URISyntaxException e) {
                    System.out.println("Error downloading resource at " + url);
                    e.printStackTrace();
                }

            }


        }

        browseList(lang, ++count);

    }

}
