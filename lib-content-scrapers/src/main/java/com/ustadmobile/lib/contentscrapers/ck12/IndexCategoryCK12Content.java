package com.ustadmobile.lib.contentscrapers.ck12;

import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.apache.commons.exec.ExecuteException;
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
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.chromeDriverLocation;


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

    private final OpdsEntryWithRelations parentCK12;
    URL url;
    private File destinationDirectory;
    private ArrayList<OpdsEntryWithRelations> entryWithRelationsList;
    private ArrayList<OpdsEntryParentToChildJoin> parentToChildJoins;


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

        System.setProperty("webdriver.chrome.driver", chromeDriverLocation);

        destinationDirectory.mkdirs();
        this.destinationDirectory = destinationDirectory;

        entryWithRelationsList = new ArrayList<>();
        parentToChildJoins = new ArrayList<>();

        parentCK12 = new OpdsEntryWithRelations(
                UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()),
                "https://www.ck12.org/", "CK-12 Foundation");

        entryWithRelationsList.add(parentCK12);


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

                OpdsEntryWithRelations childCk12 = new OpdsEntryWithRelations(
                        UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), hrefLink, title);

                entryWithRelationsList.add(childCk12);

                OpdsEntryParentToChildJoin ck12ToSubjectsJoin = new OpdsEntryParentToChildJoin(parentCK12.getUuid(),
                        childCk12.getUuid(), count++);
                parentToChildJoins.add(ck12ToSubjectsJoin);

                File subjectFolder = new File(destinationDirectory, title);
                subjectFolder.mkdirs();

                browseSubjects(subjectUrl, subjectFolder, childCk12);

            }

        }

    }

    private void browseSubjects(URL url, File destinationDirectory, OpdsEntryWithRelations parent) throws IOException {

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        ChromeDriver driver = new ChromeDriver(chromeOptions);
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

                OpdsEntryWithRelations childCk12 = new OpdsEntryWithRelations(
                        UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), hrefLink, title);
                entryWithRelationsList.add(childCk12);
                OpdsEntryParentToChildJoin categoryToGradeJoin = new OpdsEntryParentToChildJoin(parent.getUuid(),
                        childCk12.getUuid(), count++);
                parentToChildJoins.add(categoryToGradeJoin);

                browseGradeTopics(subCategoryUrl, gradeFolder, childCk12);
            }
        }

        Elements categoryList = doc.select("div.concept-container");

        for (Element category : categoryList) {

            String level1CategoryTitle = category.select("span.concept-name").attr("title");
            String fakePath = url.getPath() + level1CategoryTitle;

            System.out.println("Opening Heading = " + level1CategoryTitle + " at url " + fakePath);

            OpdsEntryWithRelations childCk12 = new OpdsEntryWithRelations(
                    UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), fakePath, level1CategoryTitle);
            entryWithRelationsList.add(childCk12);
            OpdsEntryParentToChildJoin categoryToGradeJoin = new OpdsEntryParentToChildJoin(parent.getUuid(),
                    childCk12.getUuid(), count++);
            parentToChildJoins.add(categoryToGradeJoin);

            Elements firstListCategory = categoryList.select("div.level1-inner-container");

            for (Element firstCategory : firstListCategory) {

                browseListOfTopics(firstCategory, destinationDirectory, fakePath, childCk12);

            }
        }

        if (count == 0) {
            System.err.println("No Topics were found to browse");
        }

    }

    private void browseListOfTopics(Element firstCategory, File destinationDirectory, String fakePath, OpdsEntryWithRelations parent) throws IOException {

        Elements secondListCategory = firstCategory.select(":root > div > div");

        int count = 0;
        for (Element secondCategory : secondListCategory) {

            if (secondCategory.attr("class").contains("concept-container")) {

                String hrefLink = secondCategory.select("a").attr("href");
                String title = secondCategory.select("span").attr("title");

                URL contentUrl = new URL(url, hrefLink);

                System.out.println("Found Topic = " + title + " at url " + contentUrl);

                OpdsEntryWithRelations childCk12 = new OpdsEntryWithRelations(
                        UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), contentUrl.getPath(), title);
                entryWithRelationsList.add(childCk12);
                OpdsEntryParentToChildJoin categoryToGradeJoin = new OpdsEntryParentToChildJoin(parent.getUuid(),
                        childCk12.getUuid(), count++);
                parentToChildJoins.add(categoryToGradeJoin);

                File topicDestination = new File(destinationDirectory, title);
                topicDestination.mkdirs();

                browseContent(contentUrl, topicDestination, childCk12);

            } else if (secondCategory.attr("class").contains("parent")) {

                String title = secondCategory.select("span").attr("title");

                String appendPath = fakePath + "/" + title;

                System.out.println("Found Parent Topic = " + title + " at url " + appendPath);

                OpdsEntryWithRelations childCk12 = new OpdsEntryWithRelations(
                        UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), appendPath, title);
                entryWithRelationsList.add(childCk12);
                OpdsEntryParentToChildJoin categoryToGradeJoin = new OpdsEntryParentToChildJoin(parent.getUuid(),
                        childCk12.getUuid(), count++);
                parentToChildJoins.add(categoryToGradeJoin);

                browseListOfTopics(secondCategory.child(1), destinationDirectory, appendPath, childCk12);

            }

        }

    }


    private void browseGradeTopics(URL subCategoryUrl, File destination, OpdsEntryWithRelations parent) throws IOException {

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        ChromeDriver driver = new ChromeDriver(chromeOptions);
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

            OpdsEntryWithRelations childCk12 = new OpdsEntryWithRelations(
                    UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), fakePathTopic
                    , headingTitle);

            entryWithRelationsList.add(childCk12);
            OpdsEntryParentToChildJoin gradeToTopicJoin = new OpdsEntryParentToChildJoin(parent.getUuid(),
                    childCk12.getUuid(), count++);

            parentToChildJoins.add(gradeToTopicJoin);


            Elements topicList = header.select("div.concept-track-wrapper");

            int topicCount = 0;
            for (Element topic : topicList) {

                String title = topic.selectFirst("div.concept-track-parent").attr("title");
                String fakeParentTopic = fakePathTopic + "/" + title;

                System.out.println("Found Topic = " + title + " at url " + fakeParentTopic);

                OpdsEntryWithRelations parentTopic = new OpdsEntryWithRelations(
                        UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()),
                        fakeParentTopic, title);

                entryWithRelationsList.add(childCk12);
                OpdsEntryParentToChildJoin topicJoin = new OpdsEntryParentToChildJoin(childCk12.getUuid(),
                        parentTopic.getUuid(), topicCount++);

                parentToChildJoins.add(topicJoin);

                Elements subTopicsList = topic.select("div.concept-list-container a");

                int subTopicCount = 0;
                for (Element subTopic : subTopicsList) {

                    String hrefLink = subTopic.attr("href");
                    String subTitle = subTopic.text();

                    File topicDestination = new File(destination, subTitle);
                    topicDestination.mkdirs();
                    URL contentUrl = new URL(subCategoryUrl, hrefLink);

                    System.out.println("Found SubTopic = " + subTitle + " at url " + contentUrl);

                    OpdsEntryWithRelations topicEntry = new OpdsEntryWithRelations(
                            UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), hrefLink, subTitle);

                    entryWithRelationsList.add(topicEntry);

                    OpdsEntryParentToChildJoin subTopicJoin = new OpdsEntryParentToChildJoin(parentTopic.getUuid(),
                            topicEntry.getUuid(), subTopicCount++);

                    parentToChildJoins.add(subTopicJoin);

                    browseContent(contentUrl, topicDestination, topicEntry);

                }


            }
        }

    }

    private void browseContent(URL contentUrl, File topicDestination, OpdsEntryWithRelations parent) throws IOException {

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        ChromeDriver driver = new ChromeDriver(chromeOptions);
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

            OpdsEntryWithRelations topicEntry = new OpdsEntryWithRelations(
                    UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), hrefLink, title);

            entryWithRelationsList.add(topicEntry);

            OpdsEntryParentToChildJoin subTopicJoin = new OpdsEntryParentToChildJoin(parent.getUuid(),
                    topicEntry.getUuid(), courseCount++);

            parentToChildJoins.add(subTopicJoin);

            OpdsLink newEntryLink = new OpdsLink(topicEntry.getUuid(), "application/zip",
                    destinationDirectory.getName() + "/" + FilenameUtils.getBaseName(url.getPath()) + ".zip", OpdsEntry.LINK_REL_ACQUIRE);
            newEntryLink.setLength(new File(destinationDirectory, FilenameUtils.getBaseName(url.getPath()) + ".zip").length());
            topicEntry.setLinks(Collections.singletonList(newEntryLink));


        }


    }


}
