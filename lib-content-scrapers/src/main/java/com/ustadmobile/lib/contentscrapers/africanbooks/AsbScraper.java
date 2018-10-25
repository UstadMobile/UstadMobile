package com.ustadmobile.lib.contentscrapers.africanbooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryParentToChildJoin;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipFile;


/**
 * African story books can all be found in https://www.africanstorybook.org/booklist.php inside a script
 * To get all the books, need to read the source line by line.
 * To get the book, the line starts with parent.bookItems and the information is between curly braces { } in the format of JSON
 * Use Gson to parse the object and add to the final list
 * <p>
 * Iterate through the list, to get the book, you need to hit 2 urls
 * /myspace/publish/epub.php?id=bookId needs to be opened using selenium and you need to wait for it load
 * Once loaded call the url with /read/downloadepub.php?id=bookId and downloading for the epub can start
 * <p>
 * Once downloaded, some epubs have some missing information
 * Open the epub, find description and image property and update them
 * We also need to increase the font for the epub and this is done by modifying the css and replacing the existing
 * Move on to next epub until list is complete
 */
public class AsbScraper {

    public static final int DOWNLOAD_RETRY_INTERVAL = 10000;

    public static final int DOWNLOAD_NEXT_INTERVAL = 5000;
    private ChromeDriver driver;
    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntryFileDao contentEntryFileDao;
    private ContentEntryContentEntryFileJoinDao contentEntryFileJoinDao;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: <file destination>");
            System.exit(1);
        }

        System.out.println(args[0]);
        try {
            new AsbScraper().findContent(new File(args[0]));
        } catch (IOException e) {
            System.err.println("Exception running findContent");
            e.printStackTrace();
        }
    }


    public void findContent(File destinationDir) throws IOException {

        URL africanBooksUrl = generateURL();

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        contentEntryDao = db.getContentEntryDao();
        contentParentChildJoinDao = db.getContentEntryParentChildJoinDao();
        contentEntryFileDao = db.getContentEntryFileDao();
        contentEntryFileJoinDao = db.getContentEntryContentEntryFileJoinDao();

        ContentEntry masterRootParent = contentEntryDao.findBySourceUrl("root");
        if (masterRootParent == null) {
            masterRootParent = new ContentEntry();
            masterRootParent= setContentEntryData(masterRootParent, "root",
                    "Ustad Mobile", "root", ScraperConstants.ENGLISH_LANG_CODE);
            masterRootParent.setContentEntryUid(contentEntryDao.insert(masterRootParent));
        } else {
            masterRootParent = setContentEntryData(masterRootParent, "root",
                    "Ustad Mobile", "root", ScraperConstants.ENGLISH_LANG_CODE);
            contentEntryDao.updateContentEntry(masterRootParent);
        }


        ContentEntry asbParentEntry = contentEntryDao.findBySourceUrl("https://www.africanstorybook.org/");
        if (asbParentEntry == null) {
            asbParentEntry = new ContentEntry();
            asbParentEntry = setContentEntryData(asbParentEntry, "https://www.africanstorybook.org/",
                    "African Story Books", "https://www.africanstorybook.org/", ScraperConstants.ENGLISH_LANG_CODE);
            asbParentEntry.setContentEntryUid(contentEntryDao.insert(asbParentEntry));
        } else {
            asbParentEntry = setContentEntryData(asbParentEntry, "https://www.africanstorybook.org/",
                    "African Story Books", "https://www.africanstorybook.org/", ScraperConstants.ENGLISH_LANG_CODE);
            contentEntryDao.updateContentEntry(asbParentEntry);
        }

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, asbParentEntry, 4);

        driver = ContentScraperUtil.setupChrome(true);

        InputStream inputStreamOfBooks = africanBooksUrl.openStream();
        List<AfricanBooksResponse> africanBooksList = parseBooklist(inputStreamOfBooks);

        AfricanBooksResponse bookObj;
        WebDriverWait waitDriver = new WebDriverWait(driver, 10000);
        for (int i = 0; i < africanBooksList.size(); i++) {
            //Download the EPUB itself
            bookObj = africanBooksList.get(i);
            String bookId = bookObj.id;
            File ePubFile = new File(destinationDir, "asb" + bookId + ".epub");
            URL epubUrl = generateEPubUrl(africanBooksUrl, bookId);
            URL publishUrl = generatePublishUrl(africanBooksUrl, bookId);

            if (ePubFile.exists() && ePubFile.lastModified() > Integer.parseInt(bookObj.date)) {
                System.out.println("ASB " + bookId + " is up to date");
            } else {
                try {

                    System.out.println("Download ASB: " + bookId + " from " + epubUrl.toString() + " to " + ePubFile.getAbsolutePath());

                    driver.get(publishUrl.toString());
                    ContentScraperUtil.waitForJSandJQueryToLoad(waitDriver);

                    FileUtils.copyURLToFile(epubUrl, ePubFile);

                    if (ePubFile.length() == 0) {
                        System.out.println(ePubFile.getName() + " size 0 bytes: failed!");
                        continue;
                    }

                    ContentEntry childEntry = contentEntryDao.findBySourceUrl(epubUrl.getPath());
                    if (asbParentEntry == null) {
                        asbParentEntry = new ContentEntry();
                        asbParentEntry = setContentEntryData(asbParentEntry, epubUrl.getPath(),
                                bookObj.title, epubUrl.getPath(), bookObj.lang);
                        asbParentEntry.setContentEntryUid(contentEntryDao.insert(asbParentEntry));
                    } else {
                        asbParentEntry = setContentEntryData(asbParentEntry, epubUrl.getPath(),
                                bookObj.title, epubUrl.getPath(), bookObj.lang);
                        contentEntryDao.updateContentEntry(asbParentEntry);
                    }

                    ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, asbParentEntry, childEntry, i);

                    if (ContentScraperUtil.fileHasContent(ePubFile)) {
                        updateAsbEpub(bookObj, ePubFile);
                    }

                    FileInputStream fis = new FileInputStream(ePubFile);
                    String md5 = DigestUtils.md5Hex(fis);
                    fis.close();

                    ContentEntryFile contentEntryFile = new ContentEntryFile();
                    contentEntryFile.setMimeType(ScraperConstants.MIMETYPE_EPUB);
                    contentEntryFile.setFileSize(ePubFile.length());
                    contentEntryFile.setLastModified(ePubFile.lastModified());
                    contentEntryFile.setMd5sum(md5);
                    contentEntryFile.setContentEntryFileUid(contentEntryFileDao.insert(contentEntryFile));

                    ContentEntryContentEntryFileJoin fileJoin = new ContentEntryContentEntryFileJoin();
                    fileJoin.setCecefjContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
                    fileJoin.setCecefjContentEntryUid(childEntry.getContentEntryUid());
                    fileJoin.setCecefjUid(contentEntryFileJoinDao.insert(fileJoin));

                } catch (IOException e) {
                    System.err.println("IO Exception downloading/checking : " + ePubFile.getName());
                }
            }
        }
        driver.close();

    }

    private ContentEntry setContentEntryData(ContentEntry entry, String id, String title, String sourceUrl, String lang) {
        entry.setEntryId(id);
        entry.setTitle(title);
        entry.setSourceUrl(sourceUrl);
        entry.setPublisher("Pratham");
        entry.setLicenseType(ContentEntry.LICENSE_TYPE_CC_BY);
        entry.setPrimaryLanguage(lang);
        return entry;
    }

    public URL generatePublishUrl(URL africanBooksUrl, String bookId) throws MalformedURLException {
        return new URL(africanBooksUrl, "/myspace/publish/epub.php?id=" + bookId);
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
                    System.out.println("Failed to parse: " + line);
                    e.printStackTrace();
                    failCounter++;
                }
            }
        }

        System.out.println("Parsed " + parsedCounter + " / failed " + failCounter + " items from booklist.php");

        return retVal;
    }

    /**
     * EPUBs from ASB don't contain the description that is in the booklist.php file. We need to add that.
     * We also need to check to make sure the cover image is correctly specified. Sometimes the properties='cover-image'
     * is not specified on the EPUB provided by African Story Book so we need to add that.
     *
     * @param booklistEntry
     * @param path
     */
    public void updateAsbEpub(AfricanBooksResponse booklistEntry, File path) {
        FileSystem zipFs = null;

        BufferedReader opfReader = null;
        try {

            zipFs = FileSystems.newFileSystem(path.toPath(), ClassLoader.getSystemClassLoader());
            System.out.println("Opened filesystem for " + path);
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
                    opfModBuffer.append(" <item id=\"cover-image\" href=\"images/cover.png\"  media-type=\"image/png\" properties=\"cover-image\"/>\n");
                } else {
                    opfModBuffer.append(line).append('\n');
                }
            }

            opfReader.close();

            if (modified) {
                Files.write(
                        zipFs.getPath("content.opf"), opfModBuffer.toString().getBytes("UTF-8"),
                        StandardOpenOption.TRUNCATE_EXISTING);
            }

            //replace the epub.css to increase font size
            Path epubCssResPath = Paths.get(getClass().getResource("/com/ustadmobile/lib/contentscrapers/epub.css").toURI());
            Files.copy(epubCssResPath, zipFs.getPath("epub.css"), StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (opfReader != null) {
                try {
                    opfReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (zipFs != null) {
                try {
                    zipFs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}