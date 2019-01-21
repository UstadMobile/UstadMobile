package com.ustadmobile.lib.contentscrapers.etekkatho;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LanguageList;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
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
import java.net.URL;
import java.util.HashMap;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;


/**
 * Etekkatho website can be scraped by accessing the page via Jsoup at http://www.etekkatho.org/subjects/
 * This page has all the list of subjects available in the website with their subheading and description
 * <p>
 * The content is placed in a table format and you get all the content via css selector: tr th[scope=row], tr td
 * The heading, subheading and description cant be identified by the html tags so its required to loop through all the elements
 * If the element has an attribute called scope then its the main heading and the next 2 elements are its subheading and the description
 * If the element has a class called span3 then its the subheading of the previous heading and the next element is its description
 * Since we create a content entry, save it into a hashMap of subheading title and content entry to be used later on
 * <p>
 * Once all content entry is made from the table. Loop through again with css selector th.span3 a
 * This will give the href link of the heading.
 * This page contains all the subheadings.
 * Use Css selector again to get the href link of all the subheadings
 * Each subheading has a list of subjects that contain title, desc, author, publisher and link
 * Loop through all the subjects dl.results-item to get the information and scrape the url
 * Need to go to the next page to get more content for the same subheading.
 * This can be found by taking href link of css selector li.next a
 */
public class IndexEtekkathoScraper {

    private static final String ETEKKATHO = "Etekkatho";
    private URL url;
    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntryFileDao contentEntryFileDao;
    private ContentEntryContentEntryFileJoinDao contentEntryFileJoin;
    private ContentEntryFileStatusDao contentFileStatusDao;
    private HashMap<String, ContentEntry> headingHashMap;
    private Language englishLang;
    private int subjectCount = 0;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <etekkatho html url> <file destination><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }

        UMLogUtil.setLevel(args.length == 3 ? args[2] : "");
        UMLogUtil.logInfo(args[0]);
        UMLogUtil.logInfo(args[1]);
        try {
            new IndexEtekkathoScraper().findContent(args[0], new File(args[1]));
        } catch (IOException e) {
            UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logFatal("Exception running findContent Etek");
        }
    }

    public void findContent(String urlString, File destinationDir) throws IOException {

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            UMLogUtil.logError("Index Malformed url" + urlString);
            throw new IllegalArgumentException("Malformed url" + urlString, e);
        }

        destinationDir.mkdirs();

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        contentEntryFileDao = repository.getContentEntryFileDao();
        contentEntryFileJoin = repository.getContentEntryContentEntryFileJoinDao();
        contentFileStatusDao = db.getContentEntryFileStatusDao();
        LanguageDao languageDao = repository.getLanguageDao();
        headingHashMap = new HashMap<>();

        new LanguageList().addAllLanguages();

        englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English");

        ContentEntry masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentEntry parentEtek = ContentScraperUtil.createOrUpdateContentEntry("http://www.etekkatho.org/subjects/", "eTekkatho",
                "http://www.etekkatho.org/", ETEKKATHO, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                "Educational resources for the Myanmar academic community", false, EMPTY_STRING,
                "http://www.etekkatho.org/img/logos/etekkatho-myanmar-lang.png",
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, parentEtek, 7);

        Document document = Jsoup.connect(urlString).get();

        Elements elements = document.select("tr th[scope=row], tr td");

        int subjectCount = 0;
        int headingCount = 0;
        ContentEntry subjectEntry = null;
        for (int i = 0; i < elements.size(); i++) {

            Element element = elements.get(i);

            if (!element.attr("scope").isEmpty()) {

                URL headingUrl = new URL(url, element.selectFirst("a").attr("href"));
                // found Main Content
                subjectEntry = ContentScraperUtil.createOrUpdateContentEntry(element.text(),
                        element.text(), headingUrl.toString(),
                        ETEKKATHO, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                        "", false, EMPTY_STRING, EMPTY_STRING,
                        EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentEtek, subjectEntry, subjectCount++);

                Element subHeadingElement = elements.get(++i);
                Element descriptionElement = elements.get(++i);

                String title = subHeadingElement.text();
                if (title.contains("*")) {
                    title = title.replace("*", "").trim();
                }

                ContentEntry subHeadingEntry = ContentScraperUtil.createOrUpdateContentEntry(title,
                        title, element.text() + "/" + title, ETEKKATHO, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                        descriptionElement.text(), false, EMPTY_STRING, EMPTY_STRING,
                        EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                headingHashMap.put(title, subHeadingEntry);

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, subjectEntry, subHeadingEntry, headingCount++);


            } else if (element.hasClass("span3")) {

                Element descriptionElement = elements.get(++i);
                String title = element.text();
                if (title.contains("*")) {
                    title = title.replace("*", "").trim();
                }

                ContentEntry subHeadingEntry = ContentScraperUtil.createOrUpdateContentEntry(element.text(),
                        title, subjectEntry.getTitle() + "/" + title,
                        ETEKKATHO, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                        descriptionElement.text(), false, EMPTY_STRING, EMPTY_STRING,
                        EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                headingHashMap.put(title, subHeadingEntry);

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, subjectEntry, subHeadingEntry, headingCount++);

            } else if (element.hasClass("span6")) {

                UMLogUtil.logError("Should not come here" + element.text());

            }

        }

        Elements subjectList = document.select("th.span3 a");
        for (Element subject : subjectList) {

            String hrefLink = subject.attr("href");
            File folder = new File(destinationDir, subject.text());
            folder.mkdirs();
            browseSubHeading(hrefLink, folder);


        }


    }

    private void browseSubHeading(String hrefLink, File folder) throws IOException {

        URL subHeadingUrl = new URL(url, hrefLink);
        Document document = Jsoup.connect(subHeadingUrl.toString()).get();

        Elements subHeadingList = document.select("div.row li a");
        for (Element subHeading : subHeadingList) {

            String subHrefLink = subHeading.attr("href");
            String title = subHeading.text();
            File subHeadingFolder = new File(folder, title);
            subHeadingFolder.mkdirs();

            ContentEntry subject = headingHashMap.get(title);
            if (subject == null) {
                UMLogUtil.logError("Subheading title was not found " + title);
                if (title.equals("Agriculture, aquaculture and the environment")) {
                    subject = headingHashMap.get("Agriculture and the environment");
                }
            }

            browseSubjects(subject, subHrefLink, subHeadingFolder);

        }


    }

    private void browseSubjects(ContentEntry contentEntry, String subHrefLink, File subHeadingFolder) throws IOException {

        URL subjectListUrl = new URL(url, subHrefLink);
        Document document = Jsoup.connect(subjectListUrl.toString()).get();

        Elements subjectList = document.select("dl.results-item");
        for (Element subject : subjectList) {

            Element titleElement = subject.selectFirst("dd.title");
            String title = titleElement != null ? titleElement.text() : EMPTY_STRING;

            Element descriptionElement = subject.selectFirst("dd.description");
            String description = descriptionElement != null ? descriptionElement.text() : EMPTY_STRING;

            Element authorElement = subject.selectFirst("dd.author");
            String author = authorElement != null ? authorElement.text() : EMPTY_STRING;

            Element publisherElement = subject.selectFirst("dd.publisher");
            String publisher = publisherElement != null ? publisherElement.text() : ETEKKATHO;

            String hrefLink = subject.selectFirst("a").attr("href");

            URL subjectUrl = new URL(url, hrefLink);
            String subjectUrlString = subjectUrl.toString();

            ContentEntry lessonEntry = ContentScraperUtil.createOrUpdateContentEntry(subjectUrl.getQuery(),
                    title, subjectUrlString, publisher, LICENSE_TYPE_CC_BY, englishLang.getLangUid(),
                    null, description, true, author, EMPTY_STRING,
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao);

            ContentScraperUtil.insertOrUpdateChildWithMultipleParentsJoin(contentParentChildJoinDao, contentEntry, lessonEntry, subjectCount++);

            EtekkathoScraper scraper = new EtekkathoScraper(subjectUrlString, subHeadingFolder);
            try {
                scraper.scrapeContent();

                String fileName = subjectUrlString.substring(subjectUrlString.indexOf("=") + 1);
                File contentFolder = new File(subHeadingFolder, fileName);
                File content = new File(contentFolder, fileName);

                if (scraper.isUpdated()) {
                    ContentScraperUtil.insertContentEntryFile(content, contentEntryFileDao, contentFileStatusDao,
                            lessonEntry, ContentScraperUtil.getMd5(content), contentEntryFileJoin, true,
                            scraper.getMimeType());

                } else {

                    ContentScraperUtil.checkAndUpdateDatabaseIfFileDownloadedButNoDataFound(content, lessonEntry, contentEntryFileDao,
                            contentEntryFileJoin, contentFileStatusDao, scraper.getMimeType(), true);

                }

            } catch (Exception e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Unable to scrape content from " + title + " at url " + subjectUrlString);
            }


        }

        Element nextLink = document.selectFirst("li.next a");
        if (nextLink != null) {
            browseSubjects(contentEntry, nextLink.attr("href"), subHeadingFolder);
        }

    }


}
