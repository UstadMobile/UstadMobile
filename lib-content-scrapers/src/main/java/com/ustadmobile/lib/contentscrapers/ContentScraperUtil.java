package com.ustadmobile.lib.contentscrapers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.db.dao.ContentCategoryDao;
import com.ustadmobile.core.db.dao.ContentCategorySchemaDao;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.lib.db.entities.ContentCategory;
import com.ustadmobile.lib.db.entities.ContentCategorySchema;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.Attr;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.chromeDriverLocation;


public class ContentScraperUtil {

    private static final DateTimeFormatter LOOSE_ISO_DATE_TIME_ZONE_PARSER = DateTimeFormatter.ofPattern("[yyyyMMdd][yyyy-MM-dd][yyyy-DDD]['T'[HHmmss][HHmm][HH:mm:ss][HH:mm][.SSSSSSSSS][.SSSSSS][.SSS][.SS][.S]][OOOO][O][z][XXXXX][XXXX]['['VV']']");

    /**
     * Is the given componentType "Imported Component"
     *
     * @param component_type enum type
     * @return true if type matches ImportedComponent
     */
    public static boolean isImportedComponent(String component_type) {
        return ScraperConstants.ComponentType.IMPORTED.getType().equalsIgnoreCase(component_type);
    }


    /**
     * Given an html String, search for all tags that have src attribute to download from
     *
     * @param html           html string that might have src attributes
     * @param destinationDir location the src file will be stored
     * @param baseUrl        is needed for when the src is a path for the url
     * @returns the html with modified src pointing to its new location
     */
    public static String downloadAllResources(String html, File destinationDir, URL baseUrl) {

        if (html == null || html.isEmpty()) {
            // no string to parse
            return html;
        }

        Document doc = Jsoup.parse(html);

        Elements contentList = doc.select("[src]");
        for (Element content : contentList) {

            String url = content.attr("src");
            if ((url.contains("data:image") && url.contains("base64")) || url.contains("file://")) {
                continue;
            } else if (url.contains(ScraperConstants.brainGenieLink)) {
                String videoHtml;
                try {
                    if (url.startsWith("//")) {
                        url = "https:" + url;
                    }
                    videoHtml = Jsoup.connect(url).followRedirects(true).get().select("video").outerHtml();
                } catch (IOException e) {
                    continue;
                }
                content.parent().html(ContentScraperUtil.downloadAllResources(videoHtml, destinationDir, baseUrl));
                continue;
            } else if (url.contains("youtube")) {
                // content.parent().html("We cannot download youtube content, please watch using the link below <p></p><a href=" + url + "\"><img src=\"video-thumbnail.jpg\"/></a>");
                content.parent().html("");
                continue;
            } else if (url.contains(ScraperConstants.slideShareLink)) {
                // content.html("We cannot download slideshare content, please watch using the link below <p></p><img href=" + url + "\" src=\"video-thumbnail.jpg\"/>");
                content.parent().html("");
                continue;
                //    videoSource = Jsoup.connect(link).followRedirects(true).get().select("div.player").outerHtml();
                //   videoSource = ContentScraperUtil.downloadAllResources(videoSource, destinationDirectory, "slideshare.jpg", scrapUrl);
            }
            try {
                URL contentUrl = new URL(baseUrl, url);

                URLConnection conn = contentUrl.openConnection();
                String fileName = getFileNameFromUrl(contentUrl);
                File contentFile = new File(destinationDir, fileName);
                content.attr("src", destinationDir.getName() + "/" + contentFile.getName());

                if (!ContentScraperUtil.isFileModified(conn, destinationDir, fileName)) {
                    continue;
                }

                FileUtils.copyURLToFile(contentUrl, contentFile);

            } catch (IOException e) {
                continue;
            }

        }

        return doc.body().html();
    }


    /**
     * Given a fileName, check if the file exists. If it does, generate a new fileName
     *
     * @param destinationDirectory folder where the file will be stored
     * @param fileName             name of the file
     * @return returns the file object which is unique
     */
    public static File getUniqueFile(File destinationDirectory, String fileName) {
        int count = 0;
        File file = new File(destinationDirectory, fileName);
        while (file.exists()) {
            file = new File(destinationDirectory, count++ + fileName);
        }
        return file;
    }


    /**
     * Given a url link, find the file name, if fileName does not exist in path, use the url to create the filename
     *
     * @param url download link to file
     * @return the extracted file name from url link
     */
    public static String getFileNameFromUrl(URL url) {
        String fileName = FilenameUtils.getPath(url.getPath()).replaceAll("[^a-zA-Z0-9\\.\\-]", "_") + FilenameUtils.getName(url.getPath());
        if (fileName.isEmpty()) {
            return url.getPath().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        }
        return fileName;
    }


    /**
     * Given the list, save it as json file
     *
     * @param destinationDir directory it will be saved
     * @param list           ArrayList of Objects to be parsed to a string
     * @throws IOException
     */
    public static void saveListAsJson(File destinationDir, List<?> list, String fileName) throws IOException {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String savedQuestionsJson = gson.toJson(list, ArrayList.class);
        File savedQuestionsFile = new File(destinationDir, fileName);

        FileUtils.writeStringToFile(savedQuestionsFile, savedQuestionsJson, UTF_ENCODING);
    }

    /**
     * Given the last modified time from server, check if the file that is saved is up to date with server
     *
     * @param modifiedTime the last time file was modified from server
     * @param file         the current file in our directory
     * @return true if file does not exist or modified time on server is greater than the time in the directory
     */
    public static boolean isContentUpdated(long modifiedTime, File file) {

        if (ContentScraperUtil.fileHasContent(file)) {
            return modifiedTime >= file.lastModified();
        }
        return true;
    }


    /**
     * Given a file, check it exists and has content by checking its size
     *
     * @param file that contains content
     * @return true if the size of the file is greater than 0
     */
    public static boolean fileHasContent(File file) {
        return file.exists() && file.length() > 0;
    }


    /**
     * Given an Server date, return it as a long
     *
     * @param date Date format from server
     * @return the date given in a long format
     */
    public static long parseServerDate(String date) {
        TemporalAccessor temporalAccessor = LOOSE_ISO_DATE_TIME_ZONE_PARSER.parseBest(date, ZonedDateTime::from, LocalDateTime::from, LocalDate::from);
        if (temporalAccessor instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporalAccessor).toInstant().toEpochMilli();
        }
        if (temporalAccessor instanceof LocalDateTime) {
            return ((LocalDateTime) temporalAccessor).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return ((LocalDate) temporalAccessor).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }


    /**
     * Given a directory, save it using the filename, download its content and save in the given directory
     *
     * @param directoryToZip location of the folder that will be zipped
     * @param filename       name of the zip
     * @param locationToSave location where the zipped folder will be placed
     * @throws IOException
     */
    public static void zipDirectory(File directoryToZip, String filename, File locationToSave) throws IOException {

        File zippedFile = new File(locationToSave, filename + ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zippedFile.toPath()), StandardCharsets.UTF_8)) {
            Path sourceDirPath = Paths.get(directoryToZip.toURI());
            Files.walk(sourceDirPath).filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString().replaceAll(Pattern.quote("\\"), "/"));
                        try {
                            out.putNextEntry(zipEntry);
                            out.write(Files.readAllBytes(path));
                            out.closeEntry();
                        } catch (Exception e) {
                            System.err.println(e.getCause());
                        }
                    });
        }


    }

    /**
     * Once Selenium is setup and you load a page, use this method to wait for the page to load completely
     *
     * @param waitDriver driver used to wait for conditions on webpage
     * @return true once wait is complete
     */
    public static boolean waitForJSandJQueryToLoad(WebDriverWait waitDriver) {

        // wait for jQuery to load
        ExpectedCondition<Boolean> jQueryLoad = driver -> {
            try {
                return ((Long) ((JavascriptExecutor) driver).executeScript("return jQuery.active") == 0);
            } catch (Exception e) {
                // no jQuery present
                return true;
            }
        };

        // wait for Javascript to load
        ExpectedCondition<Boolean> jsLoad = driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState")
                .toString().equals("complete");

        return waitDriver.until(jQueryLoad) && waitDriver.until(jsLoad);
    }


    /**
     * Generate tincan xml file
     *
     * @param destinationDirectory directory it will be saved
     * @param activityName         name of course/simulation
     * @param langCode             language of the course/simulation
     * @param fileName             name of file tincan will launch
     * @param typeText             type of tincan file - get from https://registry.tincanapi.com/
     * @param entityId             id of activity should match entry id of opds link
     * @param description          description of course/simulation
     * @param descLang             lang of description
     * @throws ParserConfigurationException fails to create an xml document
     * @throws TransformerException         fails to save the xml document in the directory
     */
    public static void generateTinCanXMLFile(File destinationDirectory, String activityName, String langCode, String fileName, String typeText, String entityId, String description, String descLang) throws TransformerException, ParserConfigurationException {

        DocumentBuilderFactory dbFactory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = dBuilder.newDocument();

        org.w3c.dom.Element rootElement = doc.createElement("tincan");
        Attr xlms = doc.createAttribute("xmlns");
        xlms.setValue("http://projecttincan.com/tincan.xsd");
        rootElement.setAttributeNode(xlms);
        doc.appendChild(rootElement);

        org.w3c.dom.Element activities = doc.createElement("activities");
        rootElement.appendChild(activities);

        org.w3c.dom.Element activityNode = doc.createElement("activity");
        Attr id = doc.createAttribute("id");
        Attr type = doc.createAttribute("type");
        id.setValue(entityId);
        type.setValue(typeText);
        activityNode.setAttributeNode(id);
        activityNode.setAttributeNode(type);
        activities.appendChild(activityNode);

        org.w3c.dom.Element nameElement = doc.createElement("name");
        nameElement.appendChild(doc.createTextNode(activityName));
        activityNode.appendChild(nameElement);

        org.w3c.dom.Element descElement = doc.createElement("description");
        Attr lang = doc.createAttribute("lang");
        lang.setValue(descLang);
        descElement.setAttributeNode(lang);
        descElement.appendChild(doc.createTextNode(description));
        activityNode.appendChild(descElement);

        org.w3c.dom.Element launchElement = doc.createElement("launch");
        Attr langLaunch = doc.createAttribute("lang");
        langLaunch.setValue(langCode);
        launchElement.setAttributeNode(langLaunch);
        launchElement.appendChild(doc.createTextNode(fileName));
        activityNode.appendChild(launchElement);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(destinationDirectory, "tincan.xml"));
        transformer.transform(source, result);

    }


    /**
     * Setup Chrome driver for selenium
     *
     * @param headless true if chrome browser is required to open
     * @return
     */
    public static ChromeDriver setupChrome(boolean headless) {

        ChromeOptions option = new ChromeOptions();
        option.setHeadless(headless);
        return new ChromeDriver(option);
    }

    public static void setChromeDriverLocation(){
        System.setProperty("webdriver.chrome.driver", chromeDriverLocation);
    }


    /**
     * check if the file has been modified by comparing eTag or modified Text from server and file in folder.
     *
     * @param conn           url from where the file is being downloaded
     * @param destinationDir location of possible eTag and last modified file
     * @param fileName
     * @return true if file modified
     * @throws IOException
     */
    public static boolean isFileModified(URLConnection conn, File destinationDir, String fileName) throws IOException {

        String eTag = conn.getHeaderField("ETag");
        if (eTag != null) {
            String text;
            eTag = eTag.replaceAll("\"", "");
            File eTagFile = new File(destinationDir, FilenameUtils.getBaseName(fileName) + ScraperConstants.ETAG_TXT);

            if (ContentScraperUtil.fileHasContent(eTagFile)) {
                text = FileUtils.readFileToString(eTagFile, UTF_ENCODING);
                FileUtils.writeStringToFile(eTagFile, eTag, ScraperConstants.UTF_ENCODING);
                return !eTag.equalsIgnoreCase(text);
            } else {
                FileUtils.writeStringToFile(eTagFile, eTag, ScraperConstants.UTF_ENCODING);
                return true;
            }

        }

        String lastModified = conn.getHeaderField("Last-Modified");
        File modifiedFile = new File(destinationDir, FilenameUtils.getBaseName(fileName) + ScraperConstants.LAST_MODIFIED_TXT);
        String text;

        if (lastModified != null) {
            if (ContentScraperUtil.fileHasContent(modifiedFile)) {
                text = FileUtils.readFileToString(modifiedFile, UTF_ENCODING);
                return !lastModified.equalsIgnoreCase(text);
            } else {
                FileUtils.writeStringToFile(modifiedFile, lastModified, ScraperConstants.UTF_ENCODING);
                return true;
            }
        }

        return true;
    }


    /**
     * Insert or Update the database for those parentChild Joins where the child have 1 parent
     *
     * @param dao
     * @param parentEntry
     * @param childEntry
     * @param index
     * @return the updated/created join
     */
    public static ContentEntryParentChildJoin insertOrUpdateParentChildJoin(ContentEntryParentChildJoinDao dao, ContentEntry parentEntry, ContentEntry childEntry, int index) {

        ContentEntryParentChildJoin parentChildJoin = dao.findParentByChildUuids(childEntry.getContentEntryUid());
        if (parentChildJoin == null) {
            parentChildJoin = new ContentEntryParentChildJoin();
            parentChildJoin.setCepcjParentContentEntryUid(parentEntry.getContentEntryUid());
            parentChildJoin.setCepcjChildContentEntryUid(childEntry.getContentEntryUid());
            parentChildJoin.setChildIndex(index);
            parentChildJoin.setCepcjUid(dao.insert(parentChildJoin));
        } else {
            parentChildJoin.setCepcjParentContentEntryUid(parentEntry.getContentEntryUid());
            parentChildJoin.setCepcjChildContentEntryUid(childEntry.getContentEntryUid());
            parentChildJoin.setChildIndex(index);
            dao.updateParentChildJoin(parentChildJoin);
        }
        return parentChildJoin;
    }


    /**
     * Insert or Update the database for those parentChildJoin where the child might have multiple parents (search by uuids of parent and child)
     *
     * @param dao         database to search
     * @param parentEntry parent entry
     * @param childEntry  child entry
     * @param index       count
     * @return the updated/created join
     */
    public static ContentEntryParentChildJoin insertOrUpdateChildWithMultipleParentsJoin(ContentEntryParentChildJoinDao dao, ContentEntry parentEntry, ContentEntry childEntry, int index) {

        ContentEntryParentChildJoin parentChildJoin = dao.findJoinByParentChildUuids(parentEntry.getContentEntryUid(), childEntry.getContentEntryUid());
        if (parentChildJoin == null) {
            parentChildJoin = new ContentEntryParentChildJoin();
            parentChildJoin.setCepcjParentContentEntryUid(parentEntry.getContentEntryUid());
            parentChildJoin.setCepcjChildContentEntryUid(childEntry.getContentEntryUid());
            parentChildJoin.setChildIndex(index);
            parentChildJoin.setCepcjUid(dao.insert(parentChildJoin));
        } else {
            parentChildJoin.setCepcjParentContentEntryUid(parentEntry.getContentEntryUid());
            parentChildJoin.setCepcjChildContentEntryUid(childEntry.getContentEntryUid());
            parentChildJoin.setChildIndex(index);
            dao.updateParentChildJoin(parentChildJoin);
        }
        return parentChildJoin;
    }


    /**
     * Insert or Update the database for those parentChildJoin where the child might have multiple categories (search by uuids of category and child)
     *
     * @param contentEntryCategoryJoinDao database to search
     * @param category                    parent entry
     * @param childEntry                  child entry
     * @return the updated/created join
     */
    public static ContentEntryContentCategoryJoin insertOrUpdateChildWithMultipleCategoriesJoin(ContentEntryContentCategoryJoinDao contentEntryCategoryJoinDao,
                                                                                                ContentCategory category, ContentEntry childEntry) {
        ContentEntryContentCategoryJoin categoryToSimlationJoin = contentEntryCategoryJoinDao.findJoinByParentChildUuids(category.getContentCategoryUid(), childEntry.getContentEntryUid());
        if (categoryToSimlationJoin == null) {
            categoryToSimlationJoin = new ContentEntryContentCategoryJoin();
            categoryToSimlationJoin.setCeccjContentCategoryUid(category.getContentCategoryUid());
            categoryToSimlationJoin.setCeccjContentEntryUid(childEntry.getContentEntryUid());
            categoryToSimlationJoin.setCeccjUid(contentEntryCategoryJoinDao.insert(categoryToSimlationJoin));

        } else {
            categoryToSimlationJoin.setCeccjContentCategoryUid(category.getContentCategoryUid());
            categoryToSimlationJoin.setCeccjContentEntryUid(childEntry.getContentEntryUid());
            contentEntryCategoryJoinDao.updateCategoryChildJoin(categoryToSimlationJoin);
        }
        return categoryToSimlationJoin;
    }

    public static ContentCategorySchema insertOrUpdateSchema(ContentCategorySchemaDao categorySchemeDao, String schemaName, String schemaUrl) {
        ContentCategorySchema schema = categorySchemeDao.findBySchemaUrl(schemaUrl);
        if (schema == null) {
            schema = new ContentCategorySchema();
            schema.setSchemaName(schemaName);
            schema.setSchemaUrl(schemaUrl);
            schema.setContentCategorySchemaUid(categorySchemeDao.insert(schema));
        }else{
            schema.setSchemaName(schemaName);
            schema.setSchemaUrl(schemaUrl);
            categorySchemeDao.updateSchema(schema);
        }
        return schema;
    }

    public static ContentCategory insertOrUpdateCategoryContent(ContentCategoryDao categoryDao, ContentCategorySchema schema, String categoryName) {
        ContentCategory category = categoryDao.findCategoryBySchemaIdAndName(schema.getContentCategorySchemaUid(), categoryName);
        if (category == null) {
            category = new ContentCategory();
            category.setCtnCatContentCategorySchemaUid(schema.getContentCategorySchemaUid());
            category.setName(categoryName);
            category.setContentCategoryUid(categoryDao.insert(category));
        } else {
            category.setCtnCatContentCategorySchemaUid(schema.getContentCategorySchemaUid());
            category.setName(categoryName);
            categoryDao.updateCategory(category);
        }
        return category;
    }

    // TODO change to primaryUuid
    public static ContentEntryRelatedEntryJoin insertOrUpdateRelatedContentJoin(ContentEntryRelatedEntryJoinDao contentEntryRelatedJoinDao, ContentEntry relatedEntry, ContentEntry parentEntry, int relatedType) {
        ContentEntryRelatedEntryJoin relatedTranslationJoin = contentEntryRelatedJoinDao.findPrimaryByTranslation(relatedEntry.getContentEntryUid());
        if (relatedTranslationJoin == null) {
            relatedTranslationJoin = new ContentEntryRelatedEntryJoin();
            relatedTranslationJoin.setCerejRelLanguageUid(relatedEntry.getPrimaryLanguageUid());
            relatedTranslationJoin.setCerejContentEntryUid(parentEntry.getContentEntryUid());
            relatedTranslationJoin.setCerejRelatedEntryUid(relatedEntry.getContentEntryUid());
            relatedTranslationJoin.setRelType(relatedType);
            relatedTranslationJoin.setCerejUid(contentEntryRelatedJoinDao.insert(relatedTranslationJoin));
        } else {
            relatedTranslationJoin.setCerejRelLanguageUid(relatedEntry.getPrimaryLanguageUid());
            relatedTranslationJoin.setCerejContentEntryUid(parentEntry.getContentEntryUid());
            relatedTranslationJoin.setCerejRelatedEntryUid(relatedEntry.getContentEntryUid());
            relatedTranslationJoin.setRelType(relatedType);
            contentEntryRelatedJoinDao.updateSimTranslationJoin(relatedTranslationJoin);
        }
        return relatedTranslationJoin;
    }

}
