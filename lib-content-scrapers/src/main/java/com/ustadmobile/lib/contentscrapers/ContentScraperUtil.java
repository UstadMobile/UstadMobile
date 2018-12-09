package com.ustadmobile.lib.contentscrapers;

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
import com.ustadmobile.lib.contentscrapers.buildconfig.ScraperBuildConfig;
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

import org.apache.commons.codec.digest.DigestUtils;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
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
                System.out.println("Url path " +url + " failed to download to file with base url " + baseUrl);
                e.printStackTrace();
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

        ContentEntryParentChildJoin existingParentChildJoin = dao.findParentByChildUuids(childEntry.getContentEntryUid());

        ContentEntryParentChildJoin newJoin = new ContentEntryParentChildJoin();
        newJoin.setCepcjParentContentEntryUid(parentEntry.getContentEntryUid());
        newJoin.setCepcjChildContentEntryUid(childEntry.getContentEntryUid());
        newJoin.setChildIndex(index);
        if(existingParentChildJoin == null) {
            newJoin.setCepcjUid(dao.insert(newJoin));
            return newJoin;
        }else {
            newJoin.setCepcjUid(existingParentChildJoin.getCepcjUid());
            if(!newJoin.equals(existingParentChildJoin)){
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
        if(existingParentChildJoin == null) {
            newJoin.setCepcjUid(dao.insert(newJoin));
            return newJoin;
        }else {
            newJoin.setCepcjUid(existingParentChildJoin.getCepcjUid());
            if(!newJoin.equals(existingParentChildJoin)){
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
            if(!changedCategoryEntryJoin.equals(categoryToSimulationJoin)){
                contentEntryCategoryJoinDao.update(changedCategoryEntryJoin);
            }
            categoryToSimulationJoin = changedCategoryEntryJoin;
        }
        return categoryToSimulationJoin;
    }

    public static ContentCategorySchema insertOrUpdateSchema(ContentCategorySchemaDao categorySchemeDao, String schemaName, String schemaUrl) {
        ContentCategorySchema schema = categorySchemeDao.findBySchemaUrl(schemaUrl);
        if (schema == null) {
            schema = new ContentCategorySchema();
            schema.setSchemaName(schemaName);
            schema.setSchemaUrl(schemaUrl);
            schema.setContentCategorySchemaUid(categorySchemeDao.insert(schema));
        }else{
            ContentCategorySchema changedSchema = new ContentCategorySchema();
            changedSchema.setContentCategorySchemaUid(schema.getContentCategorySchemaUid());
            changedSchema.setSchemaName(schemaName);
            changedSchema.setSchemaUrl(schemaUrl);
            if(!changedSchema.equals(schema)){
                categorySchemeDao.update(changedSchema);
            }
            schema = changedSchema;
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
            ContentCategory changedCategory = new ContentCategory();
            changedCategory.setContentCategoryUid(category.getCtnCatContentCategorySchemaUid());
            changedCategory.setCtnCatContentCategorySchemaUid(schema.getContentCategorySchemaUid());
            changedCategory.setName(categoryName);
            if(!changedCategory.equals(category)){
                categoryDao.update(changedCategory);
            }
            category = changedCategory;
        }
        return category;
    }

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
            if(!changedRelatedJoin.equals(relatedTranslationJoin)){
                contentEntryRelatedJoinDao.update(changedRelatedJoin);
            }
            relatedTranslationJoin = changedRelatedJoin;
        }
        return relatedTranslationJoin;
    }

    public static Language insertOrUpdateLanguage(LanguageDao languageDao, String langValue) {
        String threeLetterCode = "";
        String twoLetterCode = "";

        List<LanguageAlpha3Code> langAlpha3List = LanguageAlpha3Code.findByName(langValue);
        if (!langAlpha3List.isEmpty()) {
            threeLetterCode = langAlpha3List.get(0).name();
            LanguageCode code = LanguageCode.getByCode(threeLetterCode);
            if (code != null) {
                twoLetterCode = LanguageCode.getByCode(threeLetterCode).name();
            }
        }

        Language langObj = languageDao.findByName(langValue);
        if (langObj == null) {
            langObj = new Language();
            langObj.setName(langValue);
            if (!threeLetterCode.isEmpty()) {
                langObj.setIso_639_1_standard(twoLetterCode);
                langObj.setIso_639_2_standard(threeLetterCode);
            }
            langObj.setLangUid(languageDao.insert(langObj));
        } else {
            Language changedLang = new Language();
            changedLang.setLangUid(langObj.getLangUid());
            changedLang.setName(langValue);
            boolean isChanged = false;

            if(!changedLang.getName().equals(langObj.getName())){
                isChanged = true;
            }

            if (!threeLetterCode.isEmpty()) {
                changedLang.setIso_639_1_standard(twoLetterCode);
                changedLang.setIso_639_2_standard(threeLetterCode);

                if(!changedLang.getIso_639_1_standard().equals(langObj.getIso_639_1_standard())){
                    isChanged = true;
                }

                if(!changedLang.getIso_639_2_standard().equals(langObj.getIso_639_2_standard())){
                    isChanged = true;
                }

            }

            if(isChanged){
                languageDao.update(changedLang);
            }
            langObj = changedLang;

        }
        return langObj;
    }


    /**
     *
     * @param ePubFile file that was downloaded
     * @param contentEntryFileDao dao to insert the file to database
     * @param contentEntryFileStatusDao dao to insert path of file to database
     * @param contentEntry entry that is joined to file
     * @param md5 md5 of file
     * @param contentEntryContentEntryFileJoinDao file join with entry
     * @param mobileOptimized isMobileOptimized
     * @param fileType filetype of file
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
        fileStatus.setCefsUid(contentEntryFileStatusDao.insert(fileStatus));

        return contentEntryFile;
    }

    public static String getMd5(File ePubFile) throws IOException {
        FileInputStream fis = new FileInputStream(ePubFile);
        String md5EpubFile = DigestUtils.md5Hex(fis);
        fis.close();

        return md5EpubFile;
    }


    /**
     *
     * Checks if data is missing from the database by checking the file md5 and updates the database
     *
     * @param contentFile file that is already downloaded
     * @param contentEntry content entry that is joined to file
     * @param contentEntryFileDao dao to insert the missing file entry
     * @param contentEntryFileJoinDao dao to insert the missing file join entry
     * @param contentEntryFileStatusDao dao to insert the missing status path entry
     * @param fileType file type of the file downloaded
     * @param isMobileOptimized is the file mobileOptimized
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

    public static LanguageVariant insertOrUpdateLanguageVariant(LanguageVariantDao variantDao, String variant, Language language) {
        LanguageVariant languageVariant = null;
        if(!variant.isEmpty()){
            CountryCode countryCode = CountryCode.getByCode(variant);
            if(countryCode == null){
                List<CountryCode> countryList = CountryCode.findByName(variant);
                if(countryList != null && !countryList.isEmpty()){
                    countryCode = countryList.get(0);
                }
            }
            if(countryCode != null){
                String alpha2 = countryCode.getAlpha2();
                String name = countryCode.getName();
                languageVariant = variantDao.findByCode(alpha2);
                if(languageVariant == null){
                    languageVariant = new LanguageVariant();
                    languageVariant.setCountryCode(alpha2);
                    languageVariant.setName(name);
                    languageVariant.setLangUid(language.getLangUid());
                    languageVariant.setLangVariantUid(variantDao.insert(languageVariant));
                }else{
                    LanguageVariant changedVariant = new LanguageVariant();
                    changedVariant.setLangVariantUid(languageVariant.getLangVariantUid());
                    changedVariant.setCountryCode(alpha2);
                    changedVariant.setName(name);
                    changedVariant.setLangUid(language.getLangUid());
                    if(!changedVariant.equals(languageVariant)){
                        variantDao.update(languageVariant);
                    }
                    languageVariant = changedVariant;
                }
            }
        }
        return languageVariant;
    }

    public static ContentEntry checkContentEntryChanges(ContentEntry changedEntry, ContentEntry oldEntry, ContentEntryDao contentEntryDao) {
        changedEntry.setContentEntryUid(oldEntry.getContentEntryUid());
        if(!changedEntry.equals(oldEntry)){
            changedEntry.setLastModified(System.currentTimeMillis());
            contentEntryDao.update(changedEntry);
        }
        return changedEntry;
    }

    /**
     * @param id entry id
     * @param title title of entry
     * @param sourceUrl source url of entry
     * @param publisher publisher of entry
     * @param licenseType license Type of entry(predefined)
     * @param primaryLanguage primary language uid of entry
     * @param languageVariant language variant uid of entry
     * @param description description of entry
     * @param isLeaf is the entry a leaf (last child)
     * @param author author of entry
     * @param thumbnailUrl thumbnail Url of entry if exists
     * @param licenseName license name of entry
     * @param licenseUrl license Url of entry
     * @return the contententry
     */
    private static ContentEntry createContentEntryObject(String id, String title, String sourceUrl, String publisher, int licenseType,
                                        long primaryLanguage, Long languageVariant, String description, boolean isLeaf,
                                                        String author, String thumbnailUrl, String licenseName, String licenseUrl){
        ContentEntry contentEntry = new ContentEntry();
        contentEntry.setEntryId(id);
        contentEntry.setTitle(title);
        contentEntry.setSourceUrl(sourceUrl);
        contentEntry.setPublisher(publisher);
        contentEntry.setLicenseType(licenseType);
        contentEntry.setPrimaryLanguageUid(primaryLanguage);
        if(languageVariant != null){
            contentEntry.setLanguageVariantUid(languageVariant);
        }
        contentEntry.setDescription(description);
        contentEntry.setLeaf(isLeaf);
        contentEntry.setAuthor(author);
        contentEntry.setThumbnailUrl(thumbnailUrl);
        contentEntry.setLicenseName(licenseName);
        contentEntry.setLicenseUrl(licenseUrl);
        return contentEntry;
    }

    /**
     * @param id entry id
     * @param title title of entry
     * @param sourceUrl source url of entry
     * @param publisher publisher of entry
     * @param licenseType license Type of entry(predefined)
     * @param primaryLanguage primary language uid of entry
     * @param languageVariant language variant uid of entry
     * @param description description of entry
     * @param isLeaf is the entry a leaf (last child)
     * @param author author of entry
     * @param thumbnailUrl thumbnail Url of entry if exists
     * @param licenseName license name of entry
     * @param licenseUrl license Url of entry
     * @param contentEntryDao dao to insert or update
     * @return the updated content entry
     */
    public static ContentEntry createOrUpdateContentEntry(String id, String title, String sourceUrl, String publisher, int licenseType,
                                                          long primaryLanguage, Long languageVariant, String description, boolean isLeaf,
                                                          String author, String thumbnailUrl, String licenseName, String licenseUrl,
                                                          ContentEntryDao contentEntryDao){

        ContentEntry contentEntry = contentEntryDao.findBySourceUrl(sourceUrl);
        if (contentEntry == null) {
            contentEntry = createContentEntryObject(id, title,sourceUrl, publisher, licenseType, primaryLanguage,
                    languageVariant, description, isLeaf, author, thumbnailUrl, licenseName, licenseUrl);
            contentEntry.setLastModified(System.currentTimeMillis());
            contentEntry.setContentEntryUid(contentEntryDao.insert(contentEntry));
        } else {
            ContentEntry changedEntry = createContentEntryObject(id, title,sourceUrl, publisher, licenseType, primaryLanguage,
                    languageVariant, description, isLeaf, author, thumbnailUrl, licenseName, licenseUrl);
            contentEntry = ContentScraperUtil.checkContentEntryChanges(changedEntry, contentEntry, contentEntryDao);
        }
        return contentEntry;
    }
}
