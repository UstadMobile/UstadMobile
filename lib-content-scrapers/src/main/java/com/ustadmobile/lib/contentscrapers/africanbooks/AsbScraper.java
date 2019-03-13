package com.ustadmobile.lib.contentscrapers.africanbooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ContentCategoryDao;
import com.ustadmobile.core.db.dao.ContentCategorySchemaDao;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.db.dao.LanguageVariantDao;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LanguageList;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.ShrinkerUtil;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentCategory;
import com.ustadmobile.lib.db.entities.ContentCategorySchema;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoin;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.entities.LanguageVariant;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.TIME_OUT_SELENIUM;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;


/**
 * African Storybooks support many languages. They can all be found in the source code on https://www.africanstorybook.org/booklist.php
 * by searching for scripts that contain the word "<option ". Each of their supported Languages haves ids which is put in a hashmap for easier access.
 * <p>
 * African story books can all be found in https://www.africanstorybook.org/booklist.php inside a script
 * To get all the books, need to read the source line by line.
 * To get the book, the line starts with parent.bookItems and the information is between curly braces { } in the format of JSON
 * Use Gson to parse the object and add to the final list
 * <p>
 * Iterate through the list, For each book:-
 * 1. Each storybook have translations which can be found in /reader.php?id=bookId
 * By using css selector li#accordianRelatedStories div.accordion-item-content a
 * you can get each translation and add a relation in the database.
 * you need to hit 3 urls
 * <p>
 * 2. /myspace/publish/epub.php?id=bookId and /make/publish/epub.php?id=bookId
 * needs to be opened using selenium and you need to wait for them to load
 * Once loaded call the url with /read/downloadepub.php?id=bookId and downloading for the epub can start
 * Epubs can fail but a retry policy of 2 is enough to get the file.
 * <p>
 * 3. Once downloaded, some epubs have some missing information
 * Open the epub, find description and image property and updateState them
 * We also need to increase the font for the epub and this is done by modifying the css and replacing the existing
 * Move on to next epub until list is complete
 */
public class AsbScraper {

    private static final String AFRICAN_STORY_BOOKS = "African Story Books";
    private final String COVER_URL = "https://www.africanstorybook.org/illustrations/covers/";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <file destination><file container destination><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }
        UMLogUtil.setLevel(args.length == 3 ? args[2] : "");

        UMLogUtil.logInfo(args[0]);
        try {
            new AsbScraper().findContent(new File(args[0]), new File(args[1]));
        } catch (IOException e) {
            UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logFatal("Exception running findContent AsbScraper");
        }
    }


    public void findContent(File destinationDir, File containerDir) throws IOException {

        URL africanBooksUrl = generateURL();

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", EMPTY_STRING);
        ContentEntryDao contentEntryDao = repository.getContentEntryDao();
        ContentEntryParentChildJoinDao contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        ContentCategorySchemaDao categorySchemeDao = repository.getContentCategorySchemaDao();
        ContentCategoryDao categoryDao = repository.getContentCategoryDao();
        ContentEntryContentCategoryJoinDao contentCategoryJoinDao = repository.getContentEntryContentCategoryJoinDao();
        LanguageDao languageDao = repository.getLanguageDao();
        LanguageVariantDao variantDao = repository.getLanguageVariantDao();
        ContentEntryRelatedEntryJoinDao relatedEntryJoinDao = repository.getContentEntryRelatedEntryJoinDao();

        ContainerDao containerDao = repository.getContainerDao();

        new LanguageList().addAllLanguages();

        String url = getAfricanStoryBookUrl();

        Document html = Jsoup.connect(url).get();

        Map<String, String> langMap = new HashMap<>();
        Elements scriptList = html.getElementsByTag("script");
        for (Element script : scriptList) {

            for (DataNode node : script.dataNodes()) {

                if (node.getWholeData().contains("<option")) {

                    String data = node.getWholeData();

                    Document langDoc = Jsoup.parse(data.substring(data.indexOf("<option "), data.lastIndexOf("</option>") + 8));
                    Elements langList = langDoc.getElementsByTag("option");
                    for (Element lang : langList) {

                        String id = lang.attr("value");
                        String value = StringUtils.capitalize(lang.text().toLowerCase());

                        String variant = EMPTY_STRING;
                        String langValue = value;
                        if (value.contains("(")) {
                            variant = StringUtils.capitalize(value.substring(value.indexOf("(") + 1, value.indexOf(")")));
                            langValue = value.substring(0, value.indexOf("(")).trim();
                        }
                        langMap.put(id, langValue);
                        Language langEntity = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, langValue);

                        if (!variant.isEmpty()) {
                            ContentScraperUtil.insertOrUpdateLanguageVariant(variantDao, variant, langEntity);
                        }
                    }
                }
            }

        }

        Language englishLang = languageDao.findByTwoCode(ScraperConstants.ENGLISH_LANG_CODE);

        ContentEntry masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ScraperConstants.ROOT, ScraperConstants.USTAD_MOBILE, ScraperConstants.ROOT,
                ScraperConstants.USTAD_MOBILE, ContentEntry.LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null, EMPTY_STRING,
                false, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, contentEntryDao);


        ContentEntry asbParentEntry = ContentScraperUtil.createOrUpdateContentEntry("https://www.africanstorybook.org/", AFRICAN_STORY_BOOKS,
                "https://www.africanstorybook.org/", AFRICAN_STORY_BOOKS, ContentEntry.LICENSE_TYPE_CC_BY,
                englishLang.getLangUid(), null, new String("Open access to picture storybooks in the languages of Africa. \n For children's literacy, enjoyment and imagination.".getBytes(), UTF_ENCODING), false, EMPTY_STRING,
                "https://www.africanstorybook.org/img/asb120.png", EMPTY_STRING, EMPTY_STRING, contentEntryDao);


        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, asbParentEntry, 4);

        InputStream inputStreamOfBooks = africanBooksUrl.openStream();
        List<AfricanBooksResponse> africanBooksList = parseBooklist(inputStreamOfBooks);

        AfricanBooksResponse bookObj;
        ContentScraperUtil.setChromeDriverLocation();
        ChromeDriver driver = ContentScraperUtil.setupChrome(true);
        WebDriverWait waitDriver = new WebDriverWait(driver, TIME_OUT_SELENIUM);
        int retry = 0;

        for (int i = 0; i < africanBooksList.size(); i++) {
            //Download the EPUB itself
            bookObj = africanBooksList.get(i);
            String bookId = bookObj.id;
            File ePubFile = new File(destinationDir, "asb" + bookId + ".epub");
            URL epubUrl = generateEPubUrl(africanBooksUrl, bookId);
            URL publishUrl = generatePublishUrl(africanBooksUrl, bookId);
            URL makeUrl = generateMakeUrl(africanBooksUrl, bookId);
            File modifiedFile = new File(destinationDir, bookId + ScraperConstants.LAST_MODIFIED_TXT);
            try {

                driver.get(publishUrl.toString());
                ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);

                driver.get(makeUrl.toString());
                ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);

                if (bookObj.lang.contains(",")) {
                    bookObj.lang = bookObj.lang.split(",")[0];
                }

                String langName = langMap.get(bookObj.lang);

                String variant = EMPTY_STRING;
                String langValue = langName;
                if (langName != null && langName.contains("(")) {
                    variant = StringUtils.capitalize(langName.substring(langName.indexOf("(") + 1, langName.indexOf(")")));
                    langValue = langName.substring(0, langName.indexOf("(")).trim();
                }

                Language language = languageDao.findByName(langValue);

                LanguageVariant languageVariant = null;
                if (!variant.isEmpty()) {
                    languageVariant = ContentScraperUtil.insertOrUpdateLanguageVariant(variantDao, variant, language);
                }
                String sourceUrl = epubUrl.getPath() + ((epubUrl.getQuery() != null && !epubUrl.getQuery().isEmpty()) ? "?" + epubUrl.getQuery() : EMPTY_STRING);

                ContentEntry childEntry = ContentScraperUtil.createOrUpdateContentEntry(sourceUrl, bookObj.title, sourceUrl, AFRICAN_STORY_BOOKS, ContentEntry.LICENSE_TYPE_CC_BY,
                        language != null ? language.getLangUid() : 0L, languageVariant == null ? null : languageVariant.getLangVariantUid(), bookObj.summary, true, bookObj.author, getCoverUrl(bookId),
                        EMPTY_STRING, EMPTY_STRING, contentEntryDao);

                Document readerDoc = Jsoup.connect(generateReaderUrl(africanBooksUrl, bookId)).get();

                Elements langList = readerDoc.select("li#accordianRelatedStories div.accordion-item-content a");

                // find the list of translations for the book we are currently on
                ContentEntry originalEntry = childEntry;
                List<ContentEntry> relatedEntries = new ArrayList<>();
                for (Element element : langList) {

                    String lang = EMPTY_STRING;
                    try {
                        String id = element.attr("onclickss");
                        id = id.substring(id.indexOf("(") + 1, id.lastIndexOf(")"));
                        String value = element.selectFirst("span").text();

                        lang = value;
                        lang = StringUtils.remove(lang, "(Original)");
                        lang = StringUtils.remove(lang, "(Adaptation)");
                        lang = StringUtils.remove(lang, "(Translation)").trim().toLowerCase();
                        lang = StringUtils.capitalize(lang);

                        String relatedVariant = EMPTY_STRING;
                        String relatedLangValue = lang;
                        if (lang.contains("(")) {
                            relatedVariant = StringUtils.capitalize(lang.substring(lang.indexOf("(") + 1, lang.indexOf(")")));
                            relatedLangValue = lang.substring(0, lang.indexOf("(")).trim();
                        }

                        Language relatedLanguage = languageDao.findByName(relatedLangValue);
                        if (relatedLanguage == null) {
                            relatedLanguage = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, lang);
                        }
                        LanguageVariant relatedLanguageVariant = null;
                        if (!variant.isEmpty()) {
                            relatedLanguageVariant = ContentScraperUtil.insertOrUpdateLanguageVariant(variantDao, relatedVariant, relatedLanguage);
                        }

                        URL content = generateEPubUrl(africanBooksUrl, id);
                        String relatedSourceUrl = content.getPath() + ((content.getQuery() != null && !content.getQuery().isEmpty()) ? "?" + content.getQuery() : EMPTY_STRING);
                        ContentEntry contentEntry = contentEntryDao.findBySourceUrl(relatedSourceUrl);
                        if (contentEntry == null) {
                            contentEntry = new ContentEntry();
                            contentEntry.setSourceUrl(relatedSourceUrl);
                            contentEntry.setPrimaryLanguageUid(relatedLanguage.getLangUid());
                            if (relatedLanguageVariant != null) {
                                contentEntry.setLanguageVariantUid(relatedLanguageVariant.getLangVariantUid());
                            }
                            contentEntry.setLeaf(true);
                            contentEntry.setContentEntryUid(contentEntryDao.insert(contentEntry));
                        }
                        relatedEntries.add(contentEntry);

                        if (value.contains("Original")) {
                            originalEntry = contentEntry;
                        }
                    } catch (NullPointerException e) {
                        UMLogUtil.logError("A translated book could not be parsed " + lang + "for book " + bookObj.title);
                    }

                }

                for (ContentEntry entry : relatedEntries) {

                    ContentScraperUtil.insertOrUpdateRelatedContentJoin(relatedEntryJoinDao, entry, originalEntry,
                            ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION);

                }
                ContentScraperUtil.insertOrUpdateRelatedContentJoin(relatedEntryJoinDao, childEntry, originalEntry,
                        ContentEntryRelatedEntryJoin.REL_TYPE_TRANSLATED_VERSION);

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, asbParentEntry, childEntry, i);

                ContentCategorySchema schema = ContentScraperUtil.insertOrUpdateSchema(categorySchemeDao,
                        "African Storybooks Reading Level", "africanstorybooks/reading/");

                ContentCategory category = ContentScraperUtil.insertOrUpdateCategoryContent(categoryDao, schema, "Reading Level " + bookObj.level);
                ContentScraperUtil.insertOrUpdateChildWithMultipleCategoriesJoin(contentCategoryJoinDao, category, childEntry);

                boolean isUpdated = ContentScraperUtil.isFileContentsUpdated(modifiedFile, bookObj.date);
                File tmpFolder = new File(UMFileUtil.stripExtensionIfPresent(ePubFile.getName()));

                if (ContentScraperUtil.fileHasContent(tmpFolder)) {
                    isUpdated = false;
                    FileUtils.deleteDirectory(tmpFolder);
                }

                if (!isUpdated) {
                    continue;
                }

                FileUtils.copyURLToFile(epubUrl, ePubFile);

                if (ePubFile.length() == 0) {
                    ContentScraperUtil.deleteFile(modifiedFile);
                    retry++;
                    if (retry == 3) {
                        retry = 0;
                        UMLogUtil.logError(ePubFile.getName() + " size 0 bytes after 3rd try: failed! for title " + bookObj.title);
                        continue;
                    }
                    i--;
                    driver.manage().deleteAllCookies();
                    continue;
                }
                retry = 0;

                if (ContentScraperUtil.fileHasContent(ePubFile)) {
                    updateAsbEpub(bookObj, ePubFile);
                }

                File tmpDir = ShrinkerUtil.shrinkEpub(ePubFile);
                ContentScraperUtil.insertContainer(containerDao, childEntry, true,
                        ScraperConstants.MIMETYPE_EPUB, ePubFile.lastModified(),
                        tmpDir, db, repository, containerDir);
                ContentScraperUtil.deleteFile(ePubFile);
                FileUtils.deleteDirectory(tmpDir);


            } catch (Exception e) {
                ContentScraperUtil.deleteFile(modifiedFile);
                retry++;
                if (retry == 3) {
                    retry = 0;
                    UMLogUtil.logError("Exception downloading/checking after 3rd try : " + ePubFile.getName() + " with title " + bookObj.title);
                    continue;
                }
                i--;
                driver.manage().deleteAllCookies();
            }
        }
        driver.close();
        driver.quit();
    }

    public String getAfricanStoryBookUrl() {
        return "https://www.africanstorybook.org/";
    }

    public String generateReaderUrl(URL url, String bookId) throws MalformedURLException {
        return new URL(url, "/reader.php?id=" + bookId).toString();
    }

    public String getCoverUrl(String bookId) {
        return COVER_URL + bookId + ScraperConstants.PNG_EXT;
    }

    public URL generatePublishUrl(URL africanBooksUrl, String bookId) throws MalformedURLException {
        return new URL(africanBooksUrl, "/myspace/publish/epub.php?id=" + bookId);
    }

    public URL generateMakeUrl(URL africanBooksUrl, String bookId) throws MalformedURLException {
        return new URL(africanBooksUrl, "/make/publish/epub.php?id=" + bookId);
    }

    public URL generateEPubUrl(URL africanBooksUrl, String bookId) throws MalformedURLException {
        return new URL(africanBooksUrl, "/read/downloadepub.php?id=" + bookId);
    }


    public URL generateURL() throws MalformedURLException {
        return new URL("https://www.africanstorybook.org/booklist.php");
    }


    protected List<AfricanBooksResponse> parseBooklist(InputStream booklistIn) throws IOException {

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        BufferedReader reader = new BufferedReader(new InputStreamReader(booklistIn, "UTF-8"));
        List<AfricanBooksResponse> retVal = new ArrayList<>();
        String line;
        boolean inList = false;
        AfricanBooksResponse currentObj;
        int parsedCounter = 0;
        int failCounter = 0;

        while ((line = reader.readLine()) != null) {
            if (!inList && !line.startsWith("<script>"))
                continue;

            if (line.startsWith("<script>")) {
                line = line.substring("<script>".length());
                inList = true;
            }

            if (line.startsWith("parent.bookItems")) {
                line = StringEscapeUtils.unescapeHtml4(line);
                String jsonStr = line.substring(line.indexOf("({") + 1,
                        line.indexOf("})") + 1);
                jsonStr = jsonStr.replace("\n", " ");
                jsonStr = jsonStr.replace("\r", " ");
                try {
                    currentObj = gson.fromJson(jsonStr, AfricanBooksResponse.class);
                    retVal.add(currentObj);
                    parsedCounter++;
                } catch (Exception e) {
                    UMLogUtil.logError("Failed to parse: " + line);
                    UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                    failCounter++;
                }
            }
        }

        UMLogUtil.logInfo("Parsed " + parsedCounter + " / failed " + failCounter + " items from booklist.php");

        reader.close();
        booklistIn.close();

        return retVal;
    }

    /**
     * EPUBs from ASB don't contain the description that is in the booklist.php file. We need to add that.
     * We also need to check to make sure the cover image is correctly specified. Sometimes the properties='cover-image'
     * is not specified on the EPUB provided by African Story Book so we need to add that.
     *
     * @param booklistEntry
     * @param epubFile
     */
    public void updateAsbEpub(AfricanBooksResponse booklistEntry, File epubFile) {
        FileSystem zipFs = null;

        BufferedReader opfReader = null;
        try {

            zipFs = FileSystems.newFileSystem(epubFile.toPath(), ClassLoader.getSystemClassLoader());
            opfReader = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(zipFs.getPath("content.opf")), "UTF-8"));
            StringBuffer opfModBuffer = new StringBuffer();
            String line;
            boolean modified = false;
            boolean hasDescription = false;

            String descTag = "<dc:description>" + StringEscapeUtils.escapeXml(booklistEntry.summary)
                    + "</dc:description>";
            while ((line = opfReader.readLine()) != null) {
                if (line.contains("dc:description")) {
                    opfModBuffer.append(descTag).append('\n');
                    hasDescription = true;
                    modified = true;
                } else if (!hasDescription && line.contains("</metadata>")) {
                    opfModBuffer.append(descTag).append("\n</metadata>\n");
                    modified = true;
                } else if (line.contains("<item id=\"cover-image\"") && !line.contains("properties=\"cover-image\"")) {
                    opfModBuffer.append(" <item id=\"cover-image\" href=\"images/cover.webp\"  media-type=\"image/webp\" properties=\"cover-image\"/>\n");
                } else {
                    opfModBuffer.append(line).append('\n');
                }
            }

            opfReader.close();

            if (modified) {
                Files.write(
                        zipFs.getPath("content.opf"), opfModBuffer.toString().getBytes(UTF_ENCODING),
                        StandardOpenOption.TRUNCATE_EXISTING);
            }

            //replace the epub.css to increase font size
            Path epubCssResPath = Paths.get(getClass().getResource("/com/ustadmobile/lib/contentscrapers/epub.css").toURI());
            Files.copy(epubCssResPath, zipFs.getPath("epub.css"), StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
        } finally {
            if (opfReader != null) {
                try {
                    opfReader.close();
                } catch (IOException e) {
                    UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                }
            }

            if (zipFs != null) {
                try {
                    zipFs.close();
                } catch (IOException e) {
                    UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                }
            }
        }
    }

}