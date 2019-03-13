package com.ustadmobile.lib.contentscrapers.folder;

import com.ustadmobile.core.contentformats.epub.ocf.OcfDocument;
import com.ustadmobile.core.contentformats.epub.opf.OpfDocument;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContainerDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.core.db.dao.LanguageVariantDao;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.LanguageList;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.contentscrapers.ShrinkerUtil;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;
import com.ustadmobile.lib.db.entities.LanguageVariant;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EPUB_EXT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.ROOT;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.USTAD_MOBILE;
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
    private LanguageDao languageDao;
    private Language englishLang;
    private String publisher;
    private LanguageVariantDao languageVariantDao;
    private String filePrefix = "file://";
    private UmAppDatabase db;
    private UmAppDatabase repository;
    private ContainerDao containerDao;
    private File containerDir;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: <folder parent name> <file destination><folder container><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }
        UMLogUtil.setLevel(args.length == 4 ? args[3] : "");
        UMLogUtil.logInfo(args[0]);
        UMLogUtil.logInfo(args[1]);
        try {
            new IndexFolderScraper().findContent(args[0], new File(args[1]), new File(args[2]));
        } catch (Exception e) {
            UMLogUtil.logFatal(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logFatal("Exception running findContent Folder");
        }

    }

    public void findContent(String name, File destinationDir, File containerDir) throws IOException {

        publisher = name;
        containerDir.mkdirs();
        this.containerDir = containerDir;
        db = UmAppDatabase.getInstance(null);
        repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        containerDao = repository.getContainerDao();
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

                    FileInputStream opfFileInputStream = null;
                    FileInputStream ocfFileInputStream = null;
                    try {

                        File tmpFolder = ShrinkerUtil.shrinkEpub(folder);
                        OcfDocument ocfDoc = new OcfDocument();
                        File ocfFile = new File(tmpFolder, Paths.get("META-INF", "container.xml").toString());
                        ocfFileInputStream = new FileInputStream(ocfFile);
                        XmlPullParser ocfParser = UstadMobileSystemImpl.getInstance()
                                .newPullParser(ocfFileInputStream);
                        ocfDoc.loadFromParser(ocfParser);

                        File opfFile = new File(tmpFolder, ocfDoc.getRootFiles().get(0).getFullPath());
                        OpfDocument document = new OpfDocument();
                        opfFileInputStream = new FileInputStream(opfFile);
                        XmlPullParser xmlPullParser = UstadMobileSystemImpl.getInstance()
                                .newPullParser(opfFileInputStream);
                        document.loadFromOPF(xmlPullParser);

                        String title = document.getTitle();
                        String lang = document.getLanguages() != null && document.getLanguages().size() > 0 ? document.getLanguages().get(0) : EMPTY_STRING;

                        StringBuilder creators = new StringBuilder();
                        for (int i = 0; i < document.getNumCreators(); i++) {
                            if (i != 0) {
                                creators.append(",");
                            }
                            creators.append(document.getCreator(i));
                        }

                        String id = document.getId();

                        long date = folder.lastModified();

                        String[] country = lang.split("-");
                        String twoCode = country[0];
                        String variant = country.length > 1 ? country[1] : EMPTY_STRING;

                        Language language = ContentScraperUtil.insertOrUpdateLanguageByTwoCode(languageDao, twoCode);
                        LanguageVariant languageVariant = ContentScraperUtil.insertOrUpdateLanguageVariant(languageVariantDao, variant, language);

                        ContentEntry childEntry = ContentScraperUtil.createOrUpdateContentEntry(id, title,
                                filePrefix + folder.getPath(), publisher, PUBLIC_DOMAIN, language != null ? language.getLangUid() : 0, languageVariant != null ? languageVariant.getLangVariantUid() : null,
                                EMPTY_STRING, true, creators.toString(), EMPTY_STRING, EMPTY_STRING,
                                EMPTY_STRING, contentEntryDao);

                        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentEntry, childEntry, fileCount++);

                        long serverDate = ContentScraperUtil.getLastModifiedOfFileFromContentEntry(childEntry, containerDao);

                        if (serverDate == -1 || date > serverDate) {
                            ContentScraperUtil.insertContainer(containerDao, childEntry, true,
                                    ScraperConstants.MIMETYPE_EPUB, tmpFolder.lastModified(), tmpFolder, db, repository,
                                    containerDir);
                            UMIOUtils.closeQuietly(opfFileInputStream);
                            UMIOUtils.closeQuietly(ocfFileInputStream);
                            FileUtils.deleteDirectory(tmpFolder);
                        }
                    } catch (Exception e) {
                        UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
                        UMLogUtil.logError("Error while parsing a file " + folder.getName());
                    } finally {
                        UMIOUtils.closeQuietly(opfFileInputStream);
                        UMIOUtils.closeQuietly(ocfFileInputStream);
                    }

                }

            }


        }

    }

}
