package com.ustadmobile.lib.contentscrapers.ck12;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY_NC_3;


/**
 * The CK 12 Website has a list of available subjects to download content from "https://www.ck12.org/browse/"
 * Each Subject has a list of topics that appear in different layouts.
 * Each Topic leads to a variety of content for example - Video, Text, Interactive(PLIX) or Practice Questions
 * <p>
 * Each subject is found by using the css selector - a.subject-link
 * A Folder is created for each content
 * There are 3 kinds of layout structure that could be found in each Subject.
 * <p>
 * For Elementary Subjects:
 * Selenium is needed here to get the final page source
 * Find the grade level by using css selector - li.js-grade a
 * Find the list of topics in each grade by css selector - div.topic-details-container
 * Each topic have different concepts to teach found by css selector - div.concept-track-wrapper
 * Each Concept has a list of subtopics that leads to all the variety content found by using selector - div.concept-list-container a
 * <p>
 * For Other Subjects
 * Content is found in Concepts or FlexBook Textbooks (not supported)
 * For Concepts:
 * Concepts have a list of content found using selector - div.concept-container
 * Content is categorised in list of topics and subtopics first by using selector - div.level1-inner-container to get list of topics
 * Each Topic might have their own list of subtopics identified by using checking the class
 * concept-container contains the content information to go to the variety of content - plix, video, questions
 * however if the class contains the word parent, this means there is more concept containers within the parent
 * <p>
 * Once the content url is found -
 * Selenium is needed here to wait for the page to load and click on the expand all button(which opens all the content)
 * Each content is found by the class name js-components-newspaper-Cards-Cards__cardsRow
 * Identify the type of content it is by searching the class name for js-components-newspaper-Card-Card__groupType
 * Link to the content can be found using the class js-components-newspaper-Card-Card__title
 * Once all information is found, use the groupType to identify the scraper to use.
 */
public class IndexCategoryCK12Content {

    private final ContentEntryDao contentEntryDao;
    private final ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private final ContentEntryFileDao contentEntryFileDao;
    private final ContentEntryContentEntryFileJoinDao contentEntryFileJoinDao;
    private final ContentEntryContentCategoryJoinDao contentEntryCategoryJoinDao;
    private ContentEntry ck12ParentEntry;
    URL url;
    private File destinationDirectory;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: <ck12 url> <file destination>");
            System.exit(1);
        }

        System.out.println(args[0]);
        System.out.println(args[1]);
        new IndexCategoryCK12Content(args[0], new File(args[1])).findContent();
    }


    public IndexCategoryCK12Content(String urlString, File destinationDirectory) {

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            System.out.println("Index Malformed url" + urlString);
            throw new IllegalArgumentException("Malformed url" + urlString, e);
        }

        destinationDirectory.mkdirs();
        this.destinationDirectory = destinationDirectory;


        UmAppDatabase db = UmAppDatabase.getInstance(null);
        contentEntryDao = db.getContentEntryDao();
        contentParentChildJoinDao = db.getContentEntryParentChildJoinDao();
        contentEntryFileDao = db.getContentEntryFileDao();
        contentEntryFileJoinDao = db.getContentEntryContentEntryFileJoinDao();
        contentEntryCategoryJoinDao = db.getContentEntryContentCategoryJoinDao();

        ck12ParentEntry = contentEntryDao.findBySourceUrl("https://www.ck12.org/");
        if (ck12ParentEntry == null) {
            ck12ParentEntry = new ContentEntry();
            ck12ParentEntry = setContentEntryData(ck12ParentEntry, "https://www.ck12.org/",
                    "CK-12 Foundation", "https://www.ck12.org/", ScraperConstants.ENGLISH_LANG_CODE);
            ck12ParentEntry.setContentEntryUid(contentEntryDao.insert(ck12ParentEntry));
        } else {
            ck12ParentEntry = setContentEntryData(ck12ParentEntry, "https://www.ck12.org/",
                    "CK-12 Foundation", "https://www.ck12.org/", ScraperConstants.ENGLISH_LANG_CODE);
            contentEntryDao.updateContentEntry(ck12ParentEntry);
        }

    }

    private ContentEntry setContentEntryData(ContentEntry entry, String entryId, String title, String sourceUrl, String langCode) {
        entry.setEntryId(entryId);
        entry.setTitle(title);
        entry.setSourceUrl(sourceUrl);
        entry.setPublisher("CK12");
        entry.setLicenseType(LICENSE_TYPE_CC_BY_NC_3);
        entry.setPrimaryLanguage(langCode);
        return entry;
    }


    /**
     * Given a ck12 url, find the content and download it all
     *
     * @throws IOException
     */
    public void findContent() throws IOException {

        Document document = Jsoup.connect(url.toString()).get();

        Elements subjectList = document.select("a.subject-link");

        // each subject appears twice on ck12 for different layouts
        Set<String> uniqueSubjects = new HashSet<>();
        int count = 0;
        for (Element subject : subjectList) {

            String hrefLink = subject.attr("href");
            boolean isAdded = uniqueSubjects.add(hrefLink);

            if (isAdded) {

                URL subjectUrl = new URL(url, hrefLink);
                String title = subject.attr("title");

                System.out.println("Found Subject = " + title + " at url " + subjectUrl);

                ContentEntry subjectEntry = contentEntryDao.findBySourceUrl(hrefLink);
                if (subjectEntry == null) {
                    subjectEntry = new ContentEntry();
                    subjectEntry = setContentEntryData(subjectEntry, hrefLink,
                            title, hrefLink, ScraperConstants.ENGLISH_LANG_CODE);
                    subjectEntry.setContentEntryUid(contentEntryDao.insert(subjectEntry));
                } else {
                    subjectEntry = setContentEntryData(subjectEntry, hrefLink,
                            title, hrefLink, ScraperConstants.ENGLISH_LANG_CODE);
                    contentEntryDao.updateContentEntry(subjectEntry);
                }

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, ck12ParentEntry, subjectEntry, count++);

                File subjectFolder = new File(destinationDirectory, title);
                subjectFolder.mkdirs();

                browseSubjects(subjectUrl, subjectFolder, subjectEntry);

            }

        }

    }

    private void browseSubjects(URL url, File destinationDirectory, ContentEntry parent) throws IOException {

        ChromeDriver driver = ContentScraperUtil.setupChrome(true);
        try {
            driver.get(url.toString());
            WebDriverWait waitDriver = new WebDriverWait(driver, 10000);
            ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Document doc = Jsoup.parse(driver.getPageSource());

        Set<String> subCategory = new HashSet<>();
        Elements gradesList = doc.select("li.js-grade a");
        int count = 0;
        for (Element grade : gradesList) {

            String hrefLink = grade.attr("href");
            boolean isAdded = subCategory.add(hrefLink);

            if (isAdded) {

                String title = grade.text();
                URL subCategoryUrl = new URL(url, hrefLink);

                System.out.println("Opening Grade = " + title + " at url " + subCategoryUrl);

                File gradeFolder = new File(destinationDirectory, title);
                gradeFolder.mkdirs();

                ContentEntry gradeEntry = contentEntryDao.findBySourceUrl(hrefLink);
                if (gradeEntry == null) {
                    gradeEntry = new ContentEntry();
                    gradeEntry = setContentEntryData(gradeEntry, hrefLink,
                            title, hrefLink, ScraperConstants.ENGLISH_LANG_CODE);
                    gradeEntry.setContentEntryUid(contentEntryDao.insert(gradeEntry));
                } else {
                    gradeEntry = setContentEntryData(gradeEntry, hrefLink,
                            title, hrefLink, ScraperConstants.ENGLISH_LANG_CODE);
                    contentEntryDao.updateContentEntry(gradeEntry);
                }

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, gradeEntry, count++);

                browseGradeTopics(subCategoryUrl, gradeFolder, gradeEntry);
            }
        }

        Elements categoryList = doc.select("div.concept-container");

        for (Element category : categoryList) {

            String level1CategoryTitle = category.select("span.concept-name").attr("title");
            String fakePath = url.getPath() + level1CategoryTitle;

            System.out.println("Opening Heading = " + level1CategoryTitle + " at url " + fakePath);

            ContentEntry topicEntry = contentEntryDao.findBySourceUrl(fakePath);
            if (topicEntry == null) {
                topicEntry = new ContentEntry();
                topicEntry = setContentEntryData(topicEntry, fakePath,
                        level1CategoryTitle, fakePath, ScraperConstants.ENGLISH_LANG_CODE);
                topicEntry.setContentEntryUid(contentEntryDao.insert(topicEntry));
            } else {
                topicEntry = setContentEntryData(topicEntry, fakePath,
                        level1CategoryTitle, fakePath, ScraperConstants.ENGLISH_LANG_CODE);
                contentEntryDao.updateContentEntry(topicEntry);
            }

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, topicEntry, count++);

            Elements firstListCategory = categoryList.select("div.level1-inner-container");

            for (Element firstCategory : firstListCategory) {

                browseListOfTopics(firstCategory, destinationDirectory, fakePath, topicEntry);

            }
        }

        if (count == 0) {
            System.err.println("No Topics were found to browse");
        }

    }

    private void browseListOfTopics(Element firstCategory, File destinationDirectory, String fakePath, ContentEntry parent) throws IOException {

        Elements secondListCategory = firstCategory.select(":root > div > div");

        int count = 0;
        for (Element secondCategory : secondListCategory) {

            if (secondCategory.attr("class").contains("concept-container")) {

                String hrefLink = secondCategory.select("a").attr("href");
                String title = secondCategory.select("span").attr("title");

                URL contentUrl = new URL(url, hrefLink);

                System.out.println("Found Topic = " + title + " at url " + contentUrl);

                ContentEntry lastTopicEntry = contentEntryDao.findBySourceUrl(hrefLink);
                if (lastTopicEntry == null) {
                    lastTopicEntry = new ContentEntry();
                    lastTopicEntry = setContentEntryData(lastTopicEntry, hrefLink,
                            title, hrefLink, ScraperConstants.ENGLISH_LANG_CODE);
                    lastTopicEntry.setContentEntryUid(contentEntryDao.insert(lastTopicEntry));
                } else {
                    lastTopicEntry = setContentEntryData(lastTopicEntry, hrefLink,
                            title, hrefLink, ScraperConstants.ENGLISH_LANG_CODE);
                    contentEntryDao.updateContentEntry(lastTopicEntry);
                }

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, lastTopicEntry, count++);

                File topicDestination = new File(destinationDirectory, title);
                topicDestination.mkdirs();

                browseContent(contentUrl, topicDestination, lastTopicEntry);

            } else if (secondCategory.attr("class").contains("parent")) {

                String title = secondCategory.select("span").attr("title");

                String appendPath = fakePath + "/" + title;

                System.out.println("Found Parent Topic = " + title + " at url " + appendPath);

                ContentEntry subTopicEntry = contentEntryDao.findBySourceUrl(appendPath);
                if (subTopicEntry == null) {
                    subTopicEntry = new ContentEntry();
                    subTopicEntry = setContentEntryData(subTopicEntry, appendPath,
                            title, appendPath, ScraperConstants.ENGLISH_LANG_CODE);
                    subTopicEntry.setContentEntryUid(contentEntryDao.insert(subTopicEntry));
                } else {
                    subTopicEntry = setContentEntryData(subTopicEntry, appendPath,
                            title, appendPath, ScraperConstants.ENGLISH_LANG_CODE);
                    contentEntryDao.updateContentEntry(subTopicEntry);
                }

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, subTopicEntry, count++);

                browseListOfTopics(secondCategory.child(1), destinationDirectory, appendPath, subTopicEntry);

            }

        }

    }


    private void browseGradeTopics(URL subCategoryUrl, File destination, ContentEntry parent) throws IOException {

        ChromeDriver driver = ContentScraperUtil.setupChrome(true);
        try {
            driver.get(subCategoryUrl.toString());
            WebDriverWait waitDriver = new WebDriverWait(driver, 10000);
            ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Document doc = Jsoup.parse(driver.getPageSource());

        int count = 0;
        Elements headerList = doc.select("div.topic-details-container");
        for (Element header : headerList) {

            String headingTitle = header.select("div.topic-header span").attr("title");

            String fakePathTopic = subCategoryUrl.getPath() + "/" + headingTitle;

            System.out.println("Opening Heading = " + headingTitle + " at url " + fakePathTopic);


            ContentEntry headingEntry = contentEntryDao.findBySourceUrl(fakePathTopic);
            if (headingEntry == null) {
                headingEntry = new ContentEntry();
                headingEntry = setContentEntryData(headingEntry, fakePathTopic,
                        headingTitle, fakePathTopic, ScraperConstants.ENGLISH_LANG_CODE);
                headingEntry.setContentEntryUid(contentEntryDao.insert(headingEntry));
            } else {
                headingEntry = setContentEntryData(headingEntry, fakePathTopic,
                        headingTitle, fakePathTopic, ScraperConstants.ENGLISH_LANG_CODE);
                contentEntryDao.updateContentEntry(headingEntry);
            }

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, headingEntry, count++);

            Elements topicList = header.select("div.concept-track-wrapper");

            int topicCount = 0;
            for (Element topic : topicList) {

                String title = topic.selectFirst("div.concept-track-parent").attr("title");
                String fakeParentTopic = fakePathTopic + "/" + title;

                System.out.println("Found Topic = " + title + " at url " + fakeParentTopic);


                ContentEntry topicEntry = contentEntryDao.findBySourceUrl(fakeParentTopic);
                if (topicEntry == null) {
                    topicEntry = new ContentEntry();
                    topicEntry = setContentEntryData(topicEntry, fakeParentTopic,
                            title, fakeParentTopic, ScraperConstants.ENGLISH_LANG_CODE);
                    topicEntry.setContentEntryUid(contentEntryDao.insert(topicEntry));
                } else {
                    topicEntry = setContentEntryData(topicEntry, fakeParentTopic,
                            title, fakeParentTopic, ScraperConstants.ENGLISH_LANG_CODE);
                    contentEntryDao.updateContentEntry(topicEntry);
                }

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, headingEntry, topicEntry, topicCount++);

                Elements subTopicsList = topic.select("div.concept-list-container a");

                int subTopicCount = 0;
                for (Element subTopic : subTopicsList) {

                    String hrefLink = subTopic.attr("href");
                    String subTitle = subTopic.text();

                    File topicDestination = new File(destination, subTitle);
                    topicDestination.mkdirs();
                    URL contentUrl = new URL(subCategoryUrl, hrefLink);

                    System.out.println("Found SubTopic = " + subTitle + " at url " + contentUrl);

                    ContentEntry subTopicEntry = contentEntryDao.findBySourceUrl(hrefLink);
                    if (subTopicEntry == null) {
                        subTopicEntry = new ContentEntry();
                        subTopicEntry = setContentEntryData(subTopicEntry, hrefLink,
                                subTitle, hrefLink, ScraperConstants.ENGLISH_LANG_CODE);
                        subTopicEntry.setContentEntryUid(contentEntryDao.insert(subTopicEntry));
                    } else {
                        subTopicEntry = setContentEntryData(subTopicEntry, hrefLink,
                                subTitle, hrefLink, ScraperConstants.ENGLISH_LANG_CODE);
                        contentEntryDao.updateContentEntry(subTopicEntry);
                    }

                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, topicEntry, subTopicEntry, subTopicCount++);

                    browseContent(contentUrl, topicDestination, subTopicEntry);

                }


            }
        }

    }

    private void browseContent(URL contentUrl, File topicDestination, ContentEntry parent) throws IOException {

        ChromeDriver driver = ContentScraperUtil.setupChrome(true);
        try {
            driver.get(contentUrl.toString());
            WebDriverWait waitDriver = new WebDriverWait(driver, 10000);
            ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);
            waitDriver.until(ExpectedConditions.elementToBeClickable(By.cssSelector("i.icon-expand"))).click();
        } catch (TimeoutException | NoSuchElementException e) {
            e.printStackTrace();
        }

        List<WebElement> courseList = driver.findElements(By.cssSelector("div[class*=js-components-newspaper-Cards-Cards__cardsRow]"));

        int courseCount = 0;
        for (WebElement course : courseList) {

            String groupType = course.findElement(
                    By.cssSelector("div[class*=js-components-newspaper-Card-Card__groupType] span"))
                    .getText();

            String imageLink = course.findElement(
                    By.cssSelector("a[class*=js-components-newspaper-Card-Card__link]"))
                    .getAttribute("href");


            WebElement link = course.findElement(
                    By.cssSelector("h2[class*=js-components-newspaper-Card-Card__title] a"));

            String hrefLink = link.getAttribute("href");
            String title = link.getAttribute("title");


            String summary = course.findElement(
                    By.cssSelector("div[class*=js-components-newspaper-Card-Card__summary]"))
                    .getText();

            URL url = new URL(contentUrl, hrefLink);

            CK12ContentScraper scraper = new CK12ContentScraper(url.toString(), topicDestination);
            try {
                switch (groupType.toLowerCase()) {

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
                        System.out.println("found a group type not supported " + groupType);
                }
            }catch (Exception e){
                System.err.println("Unable to scrape content from " + groupType + " at url " + url);
                e.printStackTrace();
                continue;
            }

            System.out.println("Found Content = " + groupType + " at url " + url);

            ContentEntry topicEntry = contentEntryDao.findBySourceUrl(hrefLink);
            if (topicEntry == null) {
                topicEntry = new ContentEntry();
                topicEntry = setContentEntryData(topicEntry, hrefLink,
                        title, hrefLink, ScraperConstants.ENGLISH_LANG_CODE);
                topicEntry.setDescription(summary);
                topicEntry.setContentEntryUid(contentEntryDao.insert(topicEntry));
            } else {
                topicEntry = setContentEntryData(topicEntry, hrefLink,
                        title, hrefLink, ScraperConstants.ENGLISH_LANG_CODE);
                topicEntry.setDescription(summary);
                contentEntryDao.updateContentEntry(topicEntry);
            }

            ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parent, topicEntry, courseCount++);

            if(scraper.isContentUpdated()){

                File content = new File(destinationDirectory, FilenameUtils.getBaseName(url.getPath()) + ScraperConstants.ZIP_EXT);
                FileInputStream fis = new FileInputStream(content);
                String md5 = DigestUtils.md5Hex(fis);
                fis.close();

                ContentEntryFile contentEntryFile = new ContentEntryFile();
                contentEntryFile.setMimeType(ScraperConstants.MIMETYPE_ZIP);
                contentEntryFile.setFileSize(content.length());
                contentEntryFile.setLastModified(content.lastModified());
                contentEntryFile.setMd5sum(md5);
                contentEntryFile.setContentEntryFileUid(contentEntryFileDao.insert(contentEntryFile));

                ContentEntryContentEntryFileJoin fileJoin = new ContentEntryContentEntryFileJoin();
                fileJoin.setCecefjContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
                fileJoin.setCecefjContentEntryUid(topicEntry.getContentEntryUid());
                fileJoin.setCecefjUid(contentEntryFileJoinDao.insert(fileJoin));

            }

        }


    }


}
