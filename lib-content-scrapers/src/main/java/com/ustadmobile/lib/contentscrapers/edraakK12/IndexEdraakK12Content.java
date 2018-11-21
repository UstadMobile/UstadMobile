package com.ustadmobile.lib.contentscrapers.edraakK12;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.Language;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;


/**
 * The Edraak Website uses json to generate their website to get all the courses and all the content within them.
 * https://programs.edraak.org/api/component/5a6087f46380a6049b33fc19/?states_program_id=41
 * <p>
 * Each section of the website is made out of categories and sections which follows the structure of the json
 * <p>
 * The main json has a component type named MainContentTrack
 * This has 6 children which are the main categories found in the website, they have a component type named Section
 * <p>
 * Each Section has list of Subsections or Course Content
 * SubSections are identified by the component type named SubSection
 * SubSections has list of Course Content
 * Course Content contains a Quiz(list of questions) or a Course that has video and list a questions.
 * Courses and Quizzes are both identified with the component type named ImportedComponent
 * <p>
 * The goal of the index class is to find all the importedComponent by going to the child of each component type
 * until the component type found is ImportedComponent. Once it is found, EdraakK12ContentScraper
 * will decide if its a quiz or course and scrap its content
 */
public class IndexEdraakK12Content {

    private URL url;
    private File destinationDirectory;
    private ContentResponse response;
    private ContentEntryDao contentEntryDao;
    private ContentEntryParentChildJoinDao contentParentChildJoinDao;
    private ContentEntryFileDao contentEntryFileDao;
    private ContentEntryContentEntryFileJoinDao contentEntryFileJoin;
    private ContentEntryFileStatusDao contentFileStatusDao;
    private LanguageDao languageDao;
    private Language arabicLang;


    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: <edraak k12 json url> <file destination>");
            System.exit(1);
        }

        System.out.println(args[0]);
        System.out.println(args[1]);
        new IndexEdraakK12Content().findContent(args[0], new File(args[1]));
    }


    /**
     * Given a url and destination directory, look for importedcomponent in the response object of the url to save its content
     *
     * @param urlString      url for edraak content
     * @param destinationDir directory the content will be saved
     */
    public void findContent(String urlString, File destinationDir) {

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            System.out.println("Index Malformed url" + urlString);
            throw new IllegalArgumentException("Malformed url" + urlString, e);
        }

        destinationDir.mkdirs();
        destinationDirectory = destinationDir;

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        db.setMaster(true);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentParentChildJoinDao = repository.getContentEntryParentChildJoinDao();
        contentEntryFileDao = repository.getContentEntryFileDao();
        contentEntryFileJoin = repository.getContentEntryContentEntryFileJoinDao();
        contentFileStatusDao = repository.getContentEntryFileStatusDao();
        languageDao = repository.getLanguageDao();

        arabicLang = ContentScraperUtil.insertOrUpdateLanguage(languageDao, "Arabic");

        try {
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            response = new GsonBuilder().disableHtmlEscaping().create().fromJson(IOUtils.toString(connection.getInputStream(), UTF_ENCODING), ContentResponse.class);
        } catch (IOException | JsonSyntaxException e) {
            throw new IllegalArgumentException("JSON INVALID", e.getCause());
        }


        ContentEntry masterRootParent = contentEntryDao.findBySourceUrl("root");
        if (masterRootParent == null) {
            masterRootParent = new ContentEntry();
            masterRootParent = setContentEntryData(masterRootParent, "root",
                    "Ustad Mobile", "root", false);
            masterRootParent.setContentEntryUid(contentEntryDao.insert(masterRootParent));
        } else {
            masterRootParent = setContentEntryData(masterRootParent, "root",
                    "Ustad Mobile", "root", false);
            contentEntryDao.update(masterRootParent);
        }


        ContentEntry edraakParentEntry = contentEntryDao.findBySourceUrl("https://www.edraak.org/k12/");
        if (edraakParentEntry == null) {
            edraakParentEntry = new ContentEntry();
            edraakParentEntry = setContentEntryData(edraakParentEntry, "https://www.edraak.org/k12/",
                    "Edraak K12", "https://www.edraak.org/k12/", false);
            edraakParentEntry.setThumbnailUrl("https://www.edraak.org/static/images/logo-dark-ar.fa1399e8d134.png");
            edraakParentEntry.setLicenseType(ContentEntry.ALL_RIGHTS_RESERVED);
            edraakParentEntry.setDescription("تعليم مجانيّ\n" +
                    "إلكترونيّ باللغة العربيّة!" +
                    "\n Free Online \n" +
                    "Education, In Arabic!");
            edraakParentEntry.setContentEntryUid(contentEntryDao.insert(edraakParentEntry));
        } else {
            edraakParentEntry = setContentEntryData(edraakParentEntry, "https://www.edraak.org/k12/",
                    "Edraak K12", "https://www.edraak.org/k12/", false);
            edraakParentEntry.setThumbnailUrl("https://www.edraak.org/static/images/logo-dark-ar.fa1399e8d134.png");
            edraakParentEntry.setDescription("تعليم مجانيّ\n" +
                    "إلكترونيّ باللغة العربيّة!" +
                    "\n Free Online \n" +
                    "Education, In Arabic!");
            edraakParentEntry.setLicenseType(ContentEntry.ALL_RIGHTS_RESERVED);
            contentEntryDao.update(edraakParentEntry);
        }

        ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, masterRootParent, edraakParentEntry, 0);


        findImportedComponent(response, edraakParentEntry);

    }

    private ContentEntry setContentEntryData(ContentEntry entry, String id, String title, String sourceUrl, boolean isLeaf) {
        entry.setEntryId(id);
        entry.setTitle(title);
        entry.setSourceUrl(sourceUrl);
        entry.setPublisher("Edraak");
        entry.setPrimaryLanguageUid(arabicLang.getLangUid());
        entry.setLeaf(isLeaf);
        return entry;
    }

    private void findImportedComponent(ContentResponse parentContent, ContentEntry parentEntry) {

        if (ContentScraperUtil.isImportedComponent(parentContent.component_type)) {

            // found the last child
            EdraakK12ContentScraper scraper = new EdraakK12ContentScraper(
                    EdraakK12ContentScraper.generateUrl(url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? (":" + url.getPort()) : "") + "/api/", parentContent.id, parentContent.program == 0 ? response.program : parentContent.program),
                    destinationDirectory);
            try {
                scraper.scrapeContent();

                File content = new File(destinationDirectory, parentContent.id + ScraperConstants.ZIP_EXT);
                if (scraper.hasContentUpdated()) {

                    ContentScraperUtil.insertContentEntryFile(content, contentEntryFileDao, contentFileStatusDao, parentEntry,
                            ContentScraperUtil.getMd5(content), contentEntryFileJoin, true, ScraperConstants.MIMETYPE_ZIP);

                } else {
                    ContentScraperUtil.checkAndUpdateDatabaseIfFileDownloadedButNoDataFound(content, parentEntry, contentEntryFileDao,
                            contentEntryFileJoin, contentFileStatusDao, ScraperConstants.MIMETYPE_ZIP, true);

                }


            } catch (Exception e) {
                System.err.println(e.getCause());
                return;
            }

        } else {

            for (ContentResponse children : parentContent.children) {

                String sourceUrl = children.id;
                boolean isLeaf = ContentScraperUtil.isImportedComponent(parentContent.component_type);
                ContentEntry childEntry = contentEntryDao.findBySourceUrl(sourceUrl);
                if (childEntry == null) {
                    childEntry = new ContentEntry();
                    childEntry = setContentEntryData(childEntry, children.id, children.title, sourceUrl, isLeaf);
                    childEntry.setLicenseType(getLicenseType(children.license));
                    childEntry.setContentEntryUid(contentEntryDao.insert(childEntry));
                } else {
                    childEntry = setContentEntryData(childEntry, children.id, children.title, sourceUrl, isLeaf);
                    childEntry.setLicenseType(getLicenseType(children.license));
                    contentEntryDao.update(childEntry);
                }

                ContentScraperUtil.insertOrUpdateParentChildJoin(contentParentChildJoinDao, parentEntry, childEntry, children.child_index);

                findImportedComponent(children, childEntry);

            }

        }
    }

    private int getLicenseType(String license) {
        if (license.toLowerCase().contains("cc-by-nc-sa")) {
            return ContentEntry.LICESNE_TYPE_CC_BY_NC_SA;
        } else if (license.toLowerCase().contains("all_rights_reserved")) {
            return ContentEntry.ALL_RIGHTS_RESERVED;
        } else {
            System.err.println("License type not matched for license: " + license);
            return ContentEntry.ALL_RIGHTS_RESERVED;
        }
    }


    /**
     * Generate the url based on the different parameters
     *
     * @param contentId      unique id of the course
     * @param baseUrl        baseurl for edraak
     * @param programId      program id for the course
     * @param destinationDir directory where the course will be saved
     */
    public void findContent(String contentId, String baseUrl, int programId, File destinationDir) {
        findContent(baseUrl + "component/" + contentId + "/?states_program_id=" + programId, destinationDir);
    }


}
