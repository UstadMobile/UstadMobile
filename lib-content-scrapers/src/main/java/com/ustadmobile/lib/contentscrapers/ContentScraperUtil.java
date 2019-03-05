package com.ustadmobile.lib.contentscrapers;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neovisionaries.i18n.CountryCode;
import com.neovisionaries.i18n.LanguageAlpha3Code;
import com.neovisionaries.i18n.LanguageCode;
import com.ustadmobile.core.db.dao.ContentCategoryDao;
import com.ustadmobile.core.db.dao.ContentCategorySchemaDao;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.db.dao.LanguageVariantDao;
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.contentscrapers.buildconfig.ScraperBuildConfig;
import com.ustadmobile.lib.contentscrapers.khanacademy.ItemData;
import com.ustadmobile.lib.contentscrapers.util.SrtFormat;
import com.ustadmobile.lib.db.entities.ContentCategory;
import com.ustadmobile.lib.db.entities.ContentCategorySchema;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentCategoryJoin;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.entities.LanguageVariant;
import com.ustadmobile.lib.db.entities.ScrapeQueueItem;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.Attr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
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

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ANDROID_USER_AGENT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_SPACE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.FORWARD_SLASH;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.GRAPHIE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN_GRAPHIE_PREFIX;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN_LOGIN_LINK;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN_PASS;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.KHAN_USERNAME;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.OPUS_EXT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.REQUEST_HEAD;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.SVG_EXT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TINCAN_FILENAME;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.WEBM_EXT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.WEBP_EXT;


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
                content.parent().html(EMPTY_STRING);
                continue;
            } else if (url.contains(ScraperConstants.slideShareLink)) {
                // content.html("We cannot download slideshare content, please watch using the link below <p></p><img href=" + url + "\" src=\"video-thumbnail.jpg\"/>");
                content.parent().html(EMPTY_STRING);
                continue;
            }

            HttpURLConnection conn = null;
            try {
                URL contentUrl = new URL(baseUrl, url);

                conn = (HttpURLConnection) contentUrl.openConnection();
                conn.setRequestMethod(REQUEST_HEAD);
                String fileName = getFileNameFromUrl(contentUrl);
                File contentFile = new File(destinationDir, fileName);

                File destinationFile = contentFile;
                String ext = FilenameUtils.getExtension(fileName);
                if (ScraperConstants.IMAGE_EXTENSIONS.contains(ext)) {
                    destinationFile = new File(UMFileUtil.stripExtensionIfPresent(contentFile.getPath()) + WEBP_EXT);
                } else if (ScraperConstants.VIDEO_EXTENSIONS.contains(ext)) {
                    destinationFile = new File(UMFileUtil.stripExtensionIfPresent(contentFile.getPath()) + WEBM_EXT);
                } else if (ScraperConstants.AUDIO_EXTENSIONS.contains(ext)) {
                    destinationFile = new File(UMFileUtil.stripExtensionIfPresent(contentFile.getPath()) + OPUS_EXT);
                }

                content.attr("src", destinationDir.getName() + "/" + destinationFile.getName());

                if (!ContentScraperUtil.isFileModified(conn, destinationDir, fileName) && fileHasContent(destinationFile)) {
                    continue;
                }
                FileUtils.copyURLToFile(contentUrl, contentFile);
                if (destinationFile.getName().endsWith(WEBP_EXT)) {
                    ShrinkerUtil.convertImageToWebp(contentFile, destinationFile);
                    contentFile.delete();
                } else if (destinationFile.getName().endsWith(WEBM_EXT)) {
                    ShrinkerUtil.convertVideoToWebM(contentFile, destinationFile);
                    contentFile.delete();
                } else if (destinationFile.getName().endsWith(OPUS_EXT)) {
                    ShrinkerUtil.convertAudioToOpos(contentFile, destinationFile);
                    contentFile.delete();
                }

            } catch (IOException e) {
                System.out.println("Url path " + url + " failed to download to file with base url " + baseUrl);
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

        }

        return doc.body().html();
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

        File zippedFile = new File(locationToSave, filename);
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zippedFile.toPath()), StandardCharsets.UTF_8)) {
            Path sourceDirPath = Paths.get(directoryToZip.toURI());
            Files.walk(sourceDirPath).filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString()
                                .replaceAll(Pattern.quote("\\"), "/"));
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

    public static Map<File, String> createContainerFromDirectory(File directory, Map<File, String> filemap){
        Path sourceDirPath = Paths.get(directory.toURI());
        try {
            Files.walk(sourceDirPath).filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        String relativePath = sourceDirPath.relativize(path).toString()
                                                            .replaceAll(Pattern.quote("\\"), "/");
                        filemap.put(path.toFile(), relativePath);

                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filemap;
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
        StreamResult result = new StreamResult(new File(destinationDirectory, TINCAN_FILENAME));
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

    /**
     * Set the system property of the driver in your machine
     */
    public static void setChromeDriverLocation() {
        System.setProperty("webdriver.chrome.driver", ScraperBuildConfig.CHROME_DRIVER_PATH);
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
            eTag = eTag.replaceAll("\"", EMPTY_STRING);
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
     * @param dao         dao to insert/updateState
     * @param parentEntry
     * @param childEntry
     * @param index
     * @return the updated/created join
     */
    public static ContentEntryParentChildJoin insertOrUpdateParentChildJoin(ContentEntryParentChildJoinDao dao, ContentEntry parentEntry, ContentEntry childEntry, int index) {

        ContentEntryParentChildJoin existingParentChildJoin = dao.findParentByChildUuids(childEntry.getContentEntryUid());

        ContentEntryParentChildJoin newJoin = new ContentEntryParentChildJoin();
        newJoin.setCepcjParentContentEntryUid(parentEntry.getContentEntryUid());
        newJoin.setCepcjChildContentEntryUid(childEntry.getContentEntryUid());
        newJoin.setChildIndex(index);
        if (existingParentChildJoin == null) {
            newJoin.setCepcjUid(dao.insert(newJoin));
            return newJoin;
        } else {
            newJoin.setCepcjUid(existingParentChildJoin.getCepcjUid());
            if (!newJoin.equals(existingParentChildJoin)) {
                dao.update(newJoin);
            }
            return newJoin;
        }
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

        ContentEntryParentChildJoin existingParentChildJoin = dao.findJoinByParentChildUuids(parentEntry.getContentEntryUid(), childEntry.getContentEntryUid());

        ContentEntryParentChildJoin newJoin = new ContentEntryParentChildJoin();
        newJoin.setCepcjParentContentEntryUid(parentEntry.getContentEntryUid());
        newJoin.setCepcjChildContentEntryUid(childEntry.getContentEntryUid());
        newJoin.setChildIndex(index);
        if (existingParentChildJoin == null) {
            newJoin.setCepcjUid(dao.insert(newJoin));
            return newJoin;
        } else {
            newJoin.setCepcjUid(existingParentChildJoin.getCepcjUid());
            if (!newJoin.equals(existingParentChildJoin)) {
                dao.update(newJoin);
            }
            return newJoin;
        }
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
        ContentEntryContentCategoryJoin categoryToSimulationJoin = contentEntryCategoryJoinDao.findJoinByParentChildUuids(category.getContentCategoryUid(), childEntry.getContentEntryUid());
        if (categoryToSimulationJoin == null) {
            categoryToSimulationJoin = new ContentEntryContentCategoryJoin();
            categoryToSimulationJoin.setCeccjContentCategoryUid(category.getContentCategoryUid());
            categoryToSimulationJoin.setCeccjContentEntryUid(childEntry.getContentEntryUid());
            categoryToSimulationJoin.setCeccjUid(contentEntryCategoryJoinDao.insert(categoryToSimulationJoin));

        } else {
            ContentEntryContentCategoryJoin changedCategoryEntryJoin = new ContentEntryContentCategoryJoin();
            changedCategoryEntryJoin.setCeccjUid(changedCategoryEntryJoin.getCeccjUid());
            changedCategoryEntryJoin.setCeccjContentCategoryUid(category.getContentCategoryUid());
            changedCategoryEntryJoin.setCeccjContentEntryUid(childEntry.getContentEntryUid());
            if (!changedCategoryEntryJoin.equals(categoryToSimulationJoin)) {
                contentEntryCategoryJoinDao.update(changedCategoryEntryJoin);
            }
            categoryToSimulationJoin = changedCategoryEntryJoin;
        }
        return categoryToSimulationJoin;
    }

    /**
     * Insert or updateState the database with a new/updated Schema
     *
     * @param categorySchemeDao dao to insert/updateState
     * @param schemaName        schema Name
     * @param schemaUrl         schema Url
     * @return the entry that was created/updated
     */
    public static ContentCategorySchema insertOrUpdateSchema(ContentCategorySchemaDao categorySchemeDao, String schemaName, String schemaUrl) {
        ContentCategorySchema schema = categorySchemeDao.findBySchemaUrl(schemaUrl);
        if (schema == null) {
            schema = new ContentCategorySchema();
            schema.setSchemaName(schemaName);
            schema.setSchemaUrl(schemaUrl);
            schema.setContentCategorySchemaUid(categorySchemeDao.insert(schema));
        } else {
            ContentCategorySchema changedSchema = new ContentCategorySchema();
            changedSchema.setContentCategorySchemaUid(schema.getContentCategorySchemaUid());
            changedSchema.setSchemaName(schemaName);
            changedSchema.setSchemaUrl(schemaUrl);
            if (!changedSchema.equals(schema)) {
                categorySchemeDao.update(changedSchema);
            }
            schema = changedSchema;
        }
        return schema;
    }

    /**
     * Insert or updateState the category that belongs in a schema
     *
     * @param categoryDao  dao to insert/updateState
     * @param schema       schema the category belongs in
     * @param categoryName name of category
     * @return the new/updated category entry
     */
    public static ContentCategory insertOrUpdateCategoryContent(ContentCategoryDao categoryDao, ContentCategorySchema schema, String categoryName) {
        ContentCategory category = categoryDao.findCategoryBySchemaIdAndName(schema.getContentCategorySchemaUid(), categoryName);
        if (category == null) {
            category = new ContentCategory();
            category.setCtnCatContentCategorySchemaUid(schema.getContentCategorySchemaUid());
            category.setName(categoryName);
            category.setContentCategoryUid(categoryDao.insert(category));
        } else {
            ContentCategory changedCategory = new ContentCategory();
            changedCategory.setContentCategoryUid(category.getCtnCatContentCategorySchemaUid());
            changedCategory.setCtnCatContentCategorySchemaUid(schema.getContentCategorySchemaUid());
            changedCategory.setName(categoryName);
            if (!changedCategory.equals(category)) {
                categoryDao.update(changedCategory);
            }
            category = changedCategory;
        }
        return category;
    }

    /**
     * Insert or updateState the relation between 2 content entry
     *
     * @param contentEntryRelatedJoinDao dao to insert/updateState
     * @param relatedEntry               related entry of parent contententry
     * @param parentEntry                parent content entry
     * @param relatedType                type of relation (Translation, related content)
     * @return
     */
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
            ContentEntryRelatedEntryJoin changedRelatedJoin = new ContentEntryRelatedEntryJoin();
            changedRelatedJoin.setCerejUid(relatedTranslationJoin.getCerejUid());
            changedRelatedJoin.setCerejRelLanguageUid(relatedEntry.getPrimaryLanguageUid());
            changedRelatedJoin.setCerejContentEntryUid(parentEntry.getContentEntryUid());
            changedRelatedJoin.setCerejRelatedEntryUid(relatedEntry.getContentEntryUid());
            changedRelatedJoin.setRelType(relatedType);
            if (!changedRelatedJoin.equals(relatedTranslationJoin)) {
                contentEntryRelatedJoinDao.update(changedRelatedJoin);
            }
            relatedTranslationJoin = changedRelatedJoin;
        }
        return relatedTranslationJoin;
    }

    /**
     * Given a language name, check if this language exists in db before adding it
     *
     * @param languageDao dao to query and insert
     * @param langName    name of the language
     * @return the entity language
     */
    public static Language insertOrUpdateLanguageByName(LanguageDao languageDao, String langName) {
        String threeLetterCode = "";
        String twoLetterCode = "";

        List<LanguageAlpha3Code> langAlpha3List = LanguageAlpha3Code.findByName(langName);
        if (!langAlpha3List.isEmpty()) {
            threeLetterCode = langAlpha3List.get(0).name();
            LanguageCode code = LanguageCode.getByCode(threeLetterCode);
            twoLetterCode = code != null ? LanguageCode.getByCode(threeLetterCode).name() : EMPTY_STRING;
        }
        Language langObj = getLanguageFromDao(langName, twoLetterCode, languageDao);
        if (langObj == null) {
            langObj = new Language();
            langObj.setName(langName);
            if (!threeLetterCode.isEmpty()) {
                langObj.setIso_639_1_standard(twoLetterCode);
                langObj.setIso_639_2_standard(threeLetterCode);
            }
            langObj.setLangUid(languageDao.insert(langObj));
        } else {
            Language changedLang = new Language();
            changedLang.setLangUid(langObj.getLangUid());
            changedLang.setName(langName);
            boolean isChanged = false;

            if (!changedLang.getName().equals(langObj.getName())) {
                isChanged = true;
            }

            if (!threeLetterCode.isEmpty()) {
                changedLang.setIso_639_1_standard(twoLetterCode);
                changedLang.setIso_639_2_standard(threeLetterCode);

                if (!changedLang.getIso_639_1_standard().equals(langObj.getIso_639_1_standard())) {
                    isChanged = true;
                }

                if (!changedLang.getIso_639_2_standard().equals(langObj.getIso_639_2_standard())) {
                    isChanged = true;
                }

            }

            if (isChanged) {
                languageDao.update(changedLang);
            }
            langObj = changedLang;

        }
        return langObj;
    }

    private static Language getLanguageFromDao(String langName, String twoLetterCode, LanguageDao dao) {
        Language lang = null;
        if (!langName.isEmpty()) {
            lang = dao.findByName(langName);
        }
        if (!twoLetterCode.isEmpty() && lang == null) {
            return dao.findByTwoCode(twoLetterCode);
        }
        return lang;
    }

    /**
     * Given a language with 2 digit code, check if this language exists in db before adding it
     *
     * @param languageDao dao to query and insert
     * @param langTwoCode two digit code of language
     * @return the entity language
     */
    public static Language insertOrUpdateLanguageByTwoCode(LanguageDao languageDao, String langTwoCode) {

        Language language = languageDao.findByTwoCode(langTwoCode);
        if (language == null) {
            language = new Language();
            language.setIso_639_1_standard(langTwoCode);
            LanguageCode nameOfLang = LanguageCode.getByCode(langTwoCode);
            if (nameOfLang != null) {
                language.setName(nameOfLang.getName());
            }
            language.setLangUid(languageDao.insert(language));
        } else {
            Language changedLang = new Language();
            changedLang.setLangUid(language.getLangUid());
            changedLang.setIso_639_1_standard(langTwoCode);
            LanguageCode nameOfLang = LanguageCode.getByCode(langTwoCode);
            if (nameOfLang != null) {
                changedLang.setName(nameOfLang.getName());
            }
            boolean isChanged = false;
            if (!language.getIso_639_1_standard().equals(changedLang.getIso_639_1_standard())) {
                isChanged = true;
            }
            if (!language.getName().equals(changedLang.getName())) {
                isChanged = true;
            }
            if (isChanged) {
                languageDao.update(changedLang);
            }
            language = changedLang;
        }
        return language;
    }


    /**
     * @param ePubFile                            file that was downloaded
     * @param contentEntryFileDao                 dao to insert the file to database
     * @param contentEntryFileStatusDao           dao to insert path of file to database
     * @param contentEntry                        entry that is joined to file
     * @param md5                                 md5 of file
     * @param contentEntryContentEntryFileJoinDao file join with entry
     * @param mobileOptimized                     isMobileOptimized
     * @param fileType                            filetype of file
     * @returns the entry file
     */
    public static ContentEntryFile insertContentEntryFile(File ePubFile, ContentEntryFileDao contentEntryFileDao,
                                                          ContentEntryFileStatusDao contentEntryFileStatusDao,
                                                          ContentEntry contentEntry, String md5,
                                                          ContentEntryContentEntryFileJoinDao contentEntryContentEntryFileJoinDao,
                                                          boolean mobileOptimized, String fileType) {

        ContentEntryFile contentEntryFile = new ContentEntryFile();
        contentEntryFile.setMimeType(fileType);
        contentEntryFile.setFileSize(ePubFile.length());
        contentEntryFile.setLastModified(ePubFile.lastModified());
        contentEntryFile.setMd5sum(md5);
        contentEntryFile.setMobileOptimized(mobileOptimized);
        contentEntryFile.setContentEntryFileUid(contentEntryFileDao.insert(contentEntryFile));

        ContentEntryContentEntryFileJoin fileJoin = new ContentEntryContentEntryFileJoin();
        fileJoin.setCecefjContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
        fileJoin.setCecefjContentEntryUid(contentEntry.getContentEntryUid());
        fileJoin.setCecefjUid(contentEntryContentEntryFileJoinDao.insert(fileJoin));

        ContentEntryFileStatus fileStatus = new ContentEntryFileStatus();
        fileStatus.setCefsContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
        fileStatus.setFilePath(ePubFile.getAbsolutePath());
        fileStatus.setCefsUid((int) contentEntryFileStatusDao.insert(fileStatus));

        return contentEntryFile;
    }

    public static String getMd5(File ePubFile) throws IOException {
        FileInputStream fis = new FileInputStream(ePubFile);
        String md5EpubFile = DigestUtils.md5Hex(fis);
        fis.close();

        return md5EpubFile;
    }


    /**
     * Checks if data is missing from the database by checking the file md5 and updates the database
     *
     * @param contentFile               file that is already downloaded
     * @param contentEntry              content entry that is joined to file
     * @param contentEntryFileDao       dao to insert the missing file entry
     * @param contentEntryFileJoinDao   dao to insert the missing file join entry
     * @param contentEntryFileStatusDao dao to insert the missing status path entry
     * @param fileType                  file type of the file downloaded
     * @param isMobileOptimized         is the file mobileOptimized
     * @throws IOException
     */
    public static void checkAndUpdateDatabaseIfFileDownloadedButNoDataFound(File contentFile, ContentEntry contentEntry,
                                                                            ContentEntryFileDao contentEntryFileDao,
                                                                            ContentEntryContentEntryFileJoinDao contentEntryFileJoinDao,
                                                                            ContentEntryFileStatusDao contentEntryFileStatusDao,
                                                                            String fileType, boolean isMobileOptimized) throws IOException {

        String md5EpubFile = ContentScraperUtil.getMd5(contentFile);

        List<ContentEntryFile> listOfFiles = contentEntryFileDao.findFilesByContentEntryUid(contentEntry.getContentEntryUid());
        if (listOfFiles == null || listOfFiles.isEmpty()) {
            ContentScraperUtil.insertContentEntryFile(contentFile, contentEntryFileDao, contentEntryFileStatusDao, contentEntry, md5EpubFile, contentEntryFileJoinDao, isMobileOptimized, fileType);
        } else {

            boolean isFileFound = false;
            // if file is found, it already exists in database and not needed to be added
            for (ContentEntryFile file : listOfFiles) {
                if (file.getMd5sum().equals(md5EpubFile)) {
                    isFileFound = true;
                    break;
                }
            }
            if (!isFileFound) {
                ContentScraperUtil.insertContentEntryFile(contentFile, contentEntryFileDao, contentEntryFileStatusDao, contentEntry, md5EpubFile, contentEntryFileJoinDao, isMobileOptimized, fileType);
            }
        }

    }

    /**
     * Check if file entry exists in the db, get the last modified date of the file otherwise return -1
     *
     * @param contentFile         current file that will be used to saved into db
     * @param contentEntry        the content entry that is linked to finding the list of files it contains
     * @param contentEntryFileDao dao to query the db
     * @return last modified of the file stored in the db or -1 if file not found
     * @throws IOException
     */
    public static long getLastModifiedOfFileFromContentEntry(File contentFile, ContentEntry contentEntry,
                                                             ContentEntryFileDao contentEntryFileDao) throws IOException {
        String md5EpubFile = ContentScraperUtil.getMd5(contentFile);

        List<ContentEntryFile> listOfFiles = contentEntryFileDao.findFilesByContentEntryUid(contentEntry.getContentEntryUid());
        if (listOfFiles != null && !listOfFiles.isEmpty()) {
            for (ContentEntryFile file : listOfFiles) {
                if (file.getMd5sum().equals(md5EpubFile)) {
                    return file.getLastModified();
                }
            }
        }
        return -1;
    }

    /**
     * Insert or updateState language variant
     *
     * @param variantDao variant dao to insert/updateState
     * @param variant    variant of the language
     * @param language   the language the variant belongs to
     * @return the language variant entry that was created/updated
     */
    public static LanguageVariant insertOrUpdateLanguageVariant(LanguageVariantDao variantDao, String variant, Language language) {
        LanguageVariant languageVariant = null;
        if (!variant.isEmpty()) {
            CountryCode countryCode = CountryCode.getByCode(variant);
            if (countryCode == null) {
                List<CountryCode> countryList = CountryCode.findByName(variant);
                if (countryList != null && !countryList.isEmpty()) {
                    countryCode = countryList.get(0);
                }
            }
            if (countryCode != null) {
                String alpha2 = countryCode.getAlpha2();
                String name = countryCode.getName();
                languageVariant = variantDao.findByCode(alpha2);
                if (languageVariant == null) {
                    languageVariant = new LanguageVariant();
                    languageVariant.setCountryCode(alpha2);
                    languageVariant.setName(name);
                    languageVariant.setLangUid(language.getLangUid());
                    languageVariant.setLangVariantUid(variantDao.insert(languageVariant));
                } else {
                    LanguageVariant changedVariant = new LanguageVariant();
                    changedVariant.setLangVariantUid(languageVariant.getLangVariantUid());
                    changedVariant.setCountryCode(alpha2);
                    changedVariant.setName(name);
                    changedVariant.setLangUid(language.getLangUid());
                    if (!changedVariant.equals(languageVariant)) {
                        variantDao.update(languageVariant);
                    }
                    languageVariant = changedVariant;
                }
            }
        }
        return languageVariant;
    }

    private static ContentEntry checkContentEntryChanges(ContentEntry changedEntry, ContentEntry oldEntry, ContentEntryDao contentEntryDao) {
        changedEntry.setContentEntryUid(oldEntry.getContentEntryUid());
        if (!changedEntry.equals(oldEntry)) {
            changedEntry.setLastModified(System.currentTimeMillis());
            contentEntryDao.update(changedEntry);
        }
        return changedEntry;
    }

    /**
     * @param id              entry id
     * @param title           title of entry
     * @param sourceUrl       source url of entry
     * @param publisher       publisher of entry
     * @param licenseType     license Type of entry(predefined)
     * @param primaryLanguage primary language uid of entry
     * @param languageVariant language variant uid of entry
     * @param description     description of entry
     * @param isLeaf          is the entry a leaf (last child)
     * @param author          author of entry
     * @param thumbnailUrl    thumbnail Url of entry if exists
     * @param licenseName     license name of entry
     * @param licenseUrl      license Url of entry
     * @return the contententry
     */
    private static ContentEntry createContentEntryObject(String id, String title, String sourceUrl, String publisher, int licenseType,
                                                         long primaryLanguage, Long languageVariant, String description, boolean isLeaf,
                                                         String author, String thumbnailUrl, String licenseName, String licenseUrl) {
        ContentEntry contentEntry = new ContentEntry();
        contentEntry.setEntryId(id);
        contentEntry.setTitle(title);
        contentEntry.setSourceUrl(sourceUrl);
        contentEntry.setPublisher(publisher);
        contentEntry.setLicenseType(licenseType);
        contentEntry.setPrimaryLanguageUid(primaryLanguage);
        if (languageVariant != null) {
            contentEntry.setLanguageVariantUid(languageVariant);
        }
        contentEntry.setDescription(description);
        contentEntry.setLeaf(isLeaf);
        contentEntry.setAuthor(author);
        contentEntry.setThumbnailUrl(thumbnailUrl);
        contentEntry.setLicenseName(licenseName);
        contentEntry.setLicenseUrl(licenseUrl);
        contentEntry.setPublik(true);
        return contentEntry;
    }

    /**
     * @param id              entry id
     * @param title           title of entry
     * @param sourceUrl       source url of entry
     * @param publisher       publisher of entry
     * @param licenseType     license Type of entry(predefined)
     * @param primaryLanguage primary language uid of entry
     * @param languageVariant language variant uid of entry
     * @param description     description of entry
     * @param isLeaf          is the entry a leaf (last child)
     * @param author          author of entry
     * @param thumbnailUrl    thumbnail Url of entry if exists
     * @param licenseName     license name of entry
     * @param licenseUrl      license Url of entry
     * @param contentEntryDao dao to insert or updateState
     * @return the updated content entry
     */
    public static ContentEntry createOrUpdateContentEntry(String id, String title, String sourceUrl, String publisher, int licenseType,
                                                          long primaryLanguage, Long languageVariant, String description, boolean isLeaf,
                                                          String author, String thumbnailUrl, String licenseName, String licenseUrl,
                                                          ContentEntryDao contentEntryDao) {

        ContentEntry contentEntry = contentEntryDao.findBySourceUrl(sourceUrl);
        if (contentEntry == null) {
            contentEntry = createContentEntryObject(id, title, sourceUrl, publisher, licenseType, primaryLanguage,
                    languageVariant, description, isLeaf, author, thumbnailUrl, licenseName, licenseUrl);
            contentEntry.setLastModified(System.currentTimeMillis());
            contentEntry.setContentEntryUid(contentEntryDao.insert(contentEntry));
        } else {
            ContentEntry changedEntry = createContentEntryObject(id, title, sourceUrl, publisher, licenseType, primaryLanguage,
                    languageVariant, description, isLeaf, author, thumbnailUrl, licenseName, licenseUrl);
            contentEntry = ContentScraperUtil.checkContentEntryChanges(changedEntry, contentEntry, contentEntryDao);
        }
        return contentEntry;
    }


    public static ScrapeQueueItem createQueueItem(ScrapeQueueItemDao queueDao, URL subjectUrl,
                                                  ContentEntry subjectEntry, File destination,
                                                  String type, int runId, int itemType) {

        ScrapeQueueItem item = queueDao.getExistingQueueItem(runId, subjectUrl.toString());
        if (item == null) {
            item = new ScrapeQueueItem();
            item.setDestDir(destination.getPath());
            item.setScrapeUrl(subjectUrl.toString());
            item.setSqiContentEntryParentUid(subjectEntry.getContentEntryUid());
            item.setStatus(ScrapeQueueItemDao.STATUS_PENDING);
            item.setContentType(type);
            item.setRunId(runId);
            item.setItemType(itemType);
            item.setTimeAdded(System.currentTimeMillis());
            queueDao.insert(item);
        }


        return null;
    }

    /**
     * Save files that are in android directory into the log index folder
     *
     * @param url       url of the resource
     * @param directory directory it will be saved
     * @param mimeType  mimeType of resource
     * @param filePath  filePath of resource
     * @param fileName
     * @return
     * @throws IOException
     */
    public static LogIndex.IndexEntry createIndexWithResourceFiles(String url, File directory, String mimeType, InputStream filePath, String fileName) throws IOException {

        URL imageUrl = new URL(url);
        File imageFolder = ContentScraperUtil.createDirectoryFromUrl(directory, imageUrl);

        File imageFile = new File(imageFolder, fileName);
        FileUtils.copyToFile(filePath, imageFile);

        return ContentScraperUtil.createIndexFromLog(imageUrl.toString(), mimeType,
                imageFolder, imageFile, null);
    }


    /**
     * Download a file from the log entry, check if it has headers, add them to url if available
     *
     * @param url         url file to download
     * @param destination destination of file
     * @param log         log details (has request headers info)
     * @return the file that was download
     * @throws IOException
     */
    public static File downloadFileFromLogIndex(URL url, File destination, LogResponse log) throws IOException {

        String fileName = ContentScraperUtil.getFileNameFromUrl(url);
        File file = new File(destination, fileName);
        if (log != null && log.message.params.response.requestHeaders != null) {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();
                for (Map.Entry<String, String> e : log.message.params.response.requestHeaders.entrySet()) {
                    if (e.getKey().equalsIgnoreCase("Accept-Encoding")) {
                        continue;
                    }
                    conn.addRequestProperty(e.getKey().replaceAll(":", EMPTY_STRING), e.getValue());
                }
                FileUtils.copyInputStreamToFile(conn.getInputStream(), file);
            } catch (IOException e) {
                UMLogUtil.logError("Error downloading file from log index with url " + url.toString());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        } else {
            FileUtils.copyURLToFile(url, file);
        }

        return file;

    }

    /**
     * Create a folder based on the url name eg. www.khanacademy.com/video/10 = folder name khanacademy
     *
     * @param destination destination of folder
     * @param url         url
     * @return
     */
    public static File createDirectoryFromUrl(File destination, URL url) {
        File urlFolder = new File(destination, url.getAuthority().replaceAll("[^a-zA-Z0-9\\.\\-]", "_"));
        urlFolder.mkdirs();
        return urlFolder;
    }

    /**
     * @param urlString    url for the log index
     * @param mimeType     mimeType of file download
     * @param urlDirectory directory of url
     * @param file         file downloaded
     * @param log          log response of index
     * @return
     */
    public static LogIndex.IndexEntry createIndexFromLog(String urlString, String mimeType, File urlDirectory, File file, LogResponse log) {
        LogIndex.IndexEntry logIndex = new LogIndex.IndexEntry();
        logIndex.url = urlString;
        logIndex.mimeType = mimeType;
        logIndex.path = urlDirectory.getName() + FORWARD_SLASH + file.getName();
        if (log != null) {
            logIndex.headers = log.message.params.response.headers;
        }
        return logIndex;
    }

    /**
     * Create a chrome driver that saves a log of all the files that was downloaded via settings
     *
     * @return Chrome Driver with Log enabled
     */
    public static ChromeDriver setupLogIndexChromeDriver() {
        DesiredCapabilities d = DesiredCapabilities.chrome();
        d.setCapability("opera.arguments", "-screenwidth 411 -screenheight 731");
        d.setPlatform(Platform.ANDROID);

        ChromeOptions options = new ChromeOptions();
        options.addArguments(ANDROID_USER_AGENT);

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        d.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        d.setCapability(ChromeOptions.CAPABILITY, options);

        return new ChromeDriver(d);
    }

    /**
     * Given a map of params, convert into a stringbuffer for post requests
     *
     * @param params params to include in post request
     * @return map converted to string
     * @throws IOException
     */
    public static StringBuffer convertMapToStringBuffer(Map<String, String> params) throws IOException {
        StringBuffer requestParams = new StringBuffer();
        for (String key : params.keySet()) {
            String value = params.get(key);
            requestParams.append(URLEncoder.encode(key, UTF_ENCODING));
            requestParams.append("=").append(
                    URLEncoder.encode(value, UTF_ENCODING));
            requestParams.append("&");
        }
        return requestParams;
    }


    /**
     * Clear the console log in chrome, wait for it to finish clearing
     *
     * @param driver
     */
    public static void clearChromeConsoleLog(ChromeDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("console.clear()");

        while (driver.manage().logs().get(LogType.PERFORMANCE).getAll().size() != 0) {
            driver.manage().timeouts().implicitlyWait(120, TimeUnit.SECONDS);
        }
    }

    public static ChromeDriver loginKhanAcademy() {

        ChromeDriver driver = ContentScraperUtil.setupLogIndexChromeDriver();

        driver.get(KHAN_LOGIN_LINK);
        WebDriverWait waitDriver = new WebDriverWait(driver, TIME_OUT_SELENIUM);
        ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);
        waitDriver.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#login-signup-root")));

        driver.findElement(By.cssSelector("div#login-signup-root input[id*=email-or-username]")).sendKeys(KHAN_USERNAME);
        driver.findElement(By.cssSelector("div#login-signup-root input[id*=text-field-1-password]")).sendKeys(KHAN_PASS);

        List<WebElement> elements = driver.findElements(By.cssSelector("div#login-signup-root div[class*=inner]"));
        for (WebElement element : elements) {
            if (element.getText().contains("Log in")) {
                element.click();
                break;
            }
        }

        waitDriver.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2[class*=moduleTitle]")));

        ContentScraperUtil.clearChromeConsoleLog(driver);

        return driver;
    }

    public static void downloadImagesFromJsonContent(Map<String, ItemData.Content.Image> images, File destDir, String scrapeUrl, List<LogIndex.IndexEntry> indexList) {
        for (String image : images.keySet()) {
            HttpURLConnection conn = null;
            try {
                image = image.replaceAll(EMPTY_SPACE, EMPTY_STRING);
                String imageUrlString = image;
                if (image.contains(GRAPHIE)) {
                    imageUrlString = KHAN_GRAPHIE_PREFIX + image.substring(image.lastIndexOf("/") + 1) + SVG_EXT;
                }

                URL imageUrl = new URL(imageUrlString);
                conn = (HttpURLConnection) imageUrl.openConnection();
                conn.setRequestMethod(REQUEST_HEAD);
                String mimeType = conn.getContentType();
                File imageFile = ContentScraperUtil.createDirectoryFromUrl(destDir, imageUrl);

                File imageContent = new File(imageFile, FilenameUtils.getName(imageUrl.getPath()));
                FileUtils.copyURLToFile(imageUrl, imageContent);

                LogIndex.IndexEntry logIndex = ContentScraperUtil.createIndexFromLog(imageUrlString, mimeType,
                        imageFile, imageContent, null);
                indexList.add(logIndex);
            } catch (MalformedURLException e) {
                UMLogUtil.logDebug(ExceptionUtils.getStackTrace(e));
            } catch (Exception e) {
                UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                UMLogUtil.logError("Error downloading an image for index log" + image + " with url " + scrapeUrl);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

        }

    }

    public static List<LogEntry> waitForNewFiles(ChromeDriver driver) {
        List<LogEntry> logs = Lists.newArrayList(driver.manage().logs().get(LogType.PERFORMANCE).getAll());
        boolean hasMore;
        do {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
            List<LogEntry> newLogs = Lists.newArrayList(driver.manage().logs().get(LogType.PERFORMANCE).getAll());
            hasMore = newLogs.size() > 0;
            UMLogUtil.logTrace("size of new logs from driver is" + newLogs.size());
            logs.addAll(newLogs);
        } while (hasMore);
        return logs;
    }

    public static void createSrtFile(List<SrtFormat> srtFormatList, File srtFile) throws IOException {

        if (srtFormatList == null || srtFormatList.isEmpty()) {
            return;
        }

        StringBuilder buffer = new StringBuilder();
        int count = 1;
        for (SrtFormat format : srtFormatList) {

            buffer.append(count++);
            buffer.append(System.lineSeparator());
            buffer.append(formatTimeInMs(format.getStartTime()));
            buffer.append(" --> ");
            buffer.append(formatTimeInMs(format.getEndTime()));
            buffer.append(System.lineSeparator());
            buffer.append(format.getText());
            buffer.append(System.lineSeparator());
            buffer.append(System.lineSeparator());

        }

        FileUtils.writeStringToFile(srtFile, buffer.toString(), UTF_ENCODING);
    }

    public static String formatTimeInMs(long timeMs) {

        long millis = timeMs % 1000;
        long second = (timeMs / 1000) % 60;
        long minute = (timeMs / (1000 * 60)) % 60;
        long hour = (timeMs / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d,%03d", hour, minute, second, millis);
    }


    public static void deleteFile(File content) {
        if (content != null) {
            if (!content.delete()) {
                UMLogUtil.logTrace("Could not delete: " + content.getPath());
            }
        }
    }
}
