package com.ustadmobile.lib.contentscrapers.folder;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.db.dao.LanguageVariantDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LanguageList;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.entities.LanguageVariant;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EPUB_EXT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.XML_NAMESPACE;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;
import static com.ustadmobile.lib.db.entities.ContentEntry.PUBLIC_DOMAIN;


/**
 * Given a directory, create parent and child joins for each subdirectories in them.
 * If an epub is found, open the epub using Zipfile and find the container xml with path: META-INF/container.xml
 * This will contain the path to the rootfile which contains all the content inside the epub.
 * Open the rootfile using the path and extract the id, author etc to create the content entry
 */
public class IndexFolderScraper {

    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntryFileDao contentEntryFileDao;
    private ContentEntryContentEntryFileJoinDao contentEntryFileJoin;
    private ContentEntryFileStatusDao contentFileStatusDao;
    private LanguageDao languageDao;
    private Language englishLang;
    private String publisher;
    private LanguageVariantDao languageVariantDao;
    private String filePrefix = "file://";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <folder parent name> <file destination><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }
        UMLogUtil.setLevel(args.length == 3 ? args[3] : "");
        UMLogUtil.logInfo(args[0]);
        UMLogUtil.logInfo(args[1]);
        try {
            new IndexFolderScraper().findContent(args[0], new File(args[1]));
        } catch (Exception e) {
            UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logFatal("Exception running findContent Folder");
        }

    }

    public void findContent(String name, File destinationDir) throws IOException {

        publisher = name;
        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        contentEntryFileDao = repository.getContentEntryFileDao();
        contentEntryFileJoin = repository.getContentEntryContentEntryFileJoinDao();
        contentFileStatusDao = db.getContentEntryFileStatusDao();
        languageDao = repository.getLanguageDao();
        languageVariantDao = repository.getLanguageVariantDao();

        new LanguageList().addAllLanguages();

        englishLang = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, "English");

        ContentEntry masterRootParent = ContentScraperUtil.createOrUpdateContentEntry(ROOT, USTAD_MOBILE,
                ROOT, USTAD_MOBILE, LICENSE_TYPE_CC_BY, englishLang.getLangUid(), null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, EMPTY_STRING, contentEntryDao);


        ContentEntry parentFolder = ContentScraperUtil.createOrUpdateContentEntry(name, name,
                filePrefix + destinationDir.getPath(), name, PUBLIC_DOMAIN, englishLang.getLangUid(), null,
                EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING,
                EMPTY_STRING, contentEntryDao);

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, parentFolder, 7);

        browseSubFolders(destinationDir, parentFolder);

    }

    private void browseSubFolders(File destinationDir, ContentEntry parentEntry) {

        File[] fileList = destinationDir.listFiles();

        if (fileList == null || fileList.length == 0) {
            return;
        }

        int folderCount = 0;
        int fileCount = 0;
        for (File folder : fileList) {

            if (folder.isDirectory()) {

                String name = folder.getName();

                ContentEntry childEntry = ContentScraperUtil.createOrUpdateContentEntry(name, name,
                        filePrefix + folder.getPath(), name, PUBLIC_DOMAIN, englishLang.getLangUid(), null,
                        EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING,
                        EMPTY_STRING, contentEntryDao);

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentEntry, childEntry, folderCount++);

                browseSubFolders(folder, childEntry);

            } else if (folder.isFile()) {

                if (folder.getName().contains(EPUB_EXT)) {

                    try {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        factory.setNamespaceAware(true);
                        DocumentBuilder builder = factory.newDocumentBuilder();

                        ZipFile zipFile = new ZipFile(folder);
                        ZipEntry entry = zipFile.getEntry("META-INF/container.xml");

                        org.w3c.dom.Document document = builder.parse(zipFile.getInputStream(entry));

                        String path = document.getDocumentElement().getElementsByTagName("rootfile").item(0).getAttributes().getNamedItem("full-path").getNodeValue();

                        ZipEntry contentZipEntry = zipFile.getEntry(path);

                        org.w3c.dom.Document data = builder.parse(zipFile.getInputStream(contentZipEntry));
                        org.w3c.dom.Element dataElement = data.getDocumentElement();

                        NodeList titleDom = dataElement.getElementsByTagNameNS(XML_NAMESPACE, "title");
                        String title = titleDom.getLength() > 0 ? titleDom.item(0).getTextContent() : EMPTY_STRING;

                        NodeList langDom = dataElement.getElementsByTagNameNS(XML_NAMESPACE, "language");
                        String lang = langDom.getLength() > 0 ? langDom.item(0).getTextContent() : EMPTY_STRING;


                        NodeList authorDom = dataElement.getElementsByTagNameNS(XML_NAMESPACE, "creator");
                        String author = authorDom.getLength() > 0 ? authorDom.item(0).getTextContent() : EMPTY_STRING;

                        NodeList idDom = dataElement.getElementsByTagNameNS(XML_NAMESPACE, "identifier");
                        String id = idDom.getLength() > 0 ? idDom.item(0).getTextContent() : EMPTY_STRING;


                        NodeList metaList = dataElement.getElementsByTagName("meta");
                        long date = folder.lastModified();
                        for (int i = 0; i < metaList.getLength(); i++) {
                            Node meta = metaList.item(i);
                            NamedNodeMap attrs = meta.getAttributes();
                            Node property = attrs.getNamedItem("property");
                            boolean isAvailable = property != null && property.getNodeValue().contains("dcterms:modified");
                            if (isAvailable) {
                                date = ContentScraperUtil.parseServerDate(meta.getTextContent());
                            }

                        }

                        String[] country = lang.split("-");
                        String twoCode = country[0];
                        String variant = country.length > 1 ? country[1] : EMPTY_STRING;

                        Language language = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao, twoCode);
                        LanguageVariant languageVariant = ContentScraperUtil.insertOrUpdateLanguageVariant(languageVariantDao, variant, language);


                        ContentEntry childEntry = ContentScraperUtil.createOrUpdateContentEntry(id, title,
                                filePrefix + folder.getPath(), publisher, PUBLIC_DOMAIN, language.getLangUid(), languageVariant != null ? languageVariant.getLangVariantUid() : null,
                                EMPTY_STRING, true, author, EMPTY_STRING, EMPTY_STRING,
                                EMPTY_STRING, contentEntryDao);

                        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentEntry, childEntry, fileCount++);

                        long serverDate = ContentScraperUtil.getLastModifiedOfFileFromContentEntry(folder, childEntry, contentEntryFileDao);

                        if (serverDate == -1 || date > serverDate) {
                            ContentScraperUtil.insertContentEntryFile(folder, contentEntryFileDao, contentFileStatusDao,
                                    childEntry, ContentScraperUtil.getMd5(folder), contentEntryFileJoin, true,
                                    ScraperConstants.MIMETYPE_EPUB);
                        } else {

                            ContentScraperUtil.checkAndUpdateDatabaseIfFileDownloadedButNoDataFound(folder, childEntry, contentEntryFileDao,
                                    contentEntryFileJoin, contentFileStatusDao, ScraperConstants.MIMETYPE_EPUB, true);

                        }
                    } catch (Exception e) {
                        UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                        UMLogUtil.logError("Error while parsing a file " + folder.getName());
                    }

                }

            }


        }

    }

}
