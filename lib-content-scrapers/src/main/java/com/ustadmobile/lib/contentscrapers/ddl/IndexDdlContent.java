package com.ustadmobile.lib.contentscrapers.ddl;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.apache.commons.io.FilenameUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
        contentEntryDao = db.getContentEntryDao();
        contentParentChildJoinDao = db.getContentEntryParentChildJoinDao();
        contentCategoryChildJoinDao = db.getContentEntryContentCategoryJoinDao();

        parentDdl = contentEntryDao.findBySourceUrl("https://www.ddl.af/");
        if (parentDdl == null) {
            parentDdl = new ContentEntry();
            parentDdl = setContentEntryData(parentDdl, "https://www.ddl.af/",
                    "Darakht-e Danesh", "https://www.ddl.af/", ScraperConstants.ENGLISH_LANG_CODE);
            parentDdl.setContentEntryUid(contentEntryDao.insert(parentDdl));
        } else {
            parentDdl = setContentEntryData(parentDdl, "https://www.ddl.af/",
                    "Darakht-e Danesh", "https://www.ddl.af/", ScraperConstants.ENGLISH_LANG_CODE);
            contentEntryDao.updateContentEntry(parentDdl);
        }

        browseLanguages("en");
        browseLanguages("fa");
        browseLanguages("ps");

    }

    private ContentEntry setContentEntryData(ContentEntry entry, String id, String title, String sourceUrl, String lang) {
        entry.setEntryId(id);
        entry.setTitle(title);
        entry.setSourceUrl(sourceUrl);
        entry.setPublisher("DDL");
        entry.setLicenseType(ContentEntry.LICENSE_TYPE_CC_BY);
        entry.setPrimaryLanguage(lang);
        return entry;
    }

    private void browseLanguages(String lang) throws IOException {

        Document document = Jsoup.connect("https://www.darakhtdanesh.org/" + lang + "/resources/list")
                .header("X-Requested-With", "XMLHttpRequest").get();

        Elements pageList = document.select("a.page-link");

        langEntry = contentEntryDao.findBySourceUrl("lang" + "/resources/list");
        if (langEntry == null) {
            langEntry = new ContentEntry();
            langEntry = setContentEntryData(langEntry, "lang" + "/resources/list",
                    lang, "lang" + "/resources/list", lang);
            langEntry.setContentEntryUid(contentEntryDao.insert(langEntry));
        } else {
            langEntry = setContentEntryData(langEntry, "lang" + "/resources/list",
                    lang, "lang" + "/resources/list", lang);
            contentEntryDao.updateContentEntry(langEntry);
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
                    ArrayList<ContentEntry> categories = scraper.getCategoryRelations();
                    ArrayList<ContentEntry> files = scraper.getOpdsFiles();
                    int categoryCount = 0;
                    for (ContentEntry category : categories) {

                        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao,
                                langEntry, category, categoryCount++);

                        int fileCount = 0;
                        for (ContentEntry file : files) {

                            ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentParentChildJoinDao,
                                    category, file, fileCount++);
                            ContentScraperUtil.insertOrUpdateChildWithMultipleCategoriesJoin(contentCategoryChildJoinDao,
                                    category, file);

                        }
                    }

                } catch (IOException | URISyntaxException e) {
                    System.out.println("Error downloading resource at " + url);
                }

            }


        }

        browseList(lang, ++count);

    }

}
