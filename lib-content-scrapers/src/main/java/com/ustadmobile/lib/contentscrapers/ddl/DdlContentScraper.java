package com.ustadmobile.lib.contentscrapers.ddl;

import com.neovisionaries.i18n.LanguageCode;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentCategoryDao;
import com.ustadmobile.core.db.dao.ContentCategorySchemaDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryFileStatusDao;
import com.ustadmobile.core.db.dao.LanguageDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.UMLogUtil;
import com.ustadmobile.lib.db.entities.ContentCategory;
import com.ustadmobile.lib.db.entities.ContentCategorySchema;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.Language;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.EMPTY_STRING;
import static com.ustadmobile.lib.contentscrapers.ScraperConstants.REQUEST_HEAD;
import static com.ustadmobile.lib.contentscrapers.ddl.IndexDdlContent.DDL;
import static com.ustadmobile.lib.db.entities.ContentEntry.LICENSE_TYPE_CC_BY;


/**
 * Once the resource page is opened.
 * You can download the list of files by searching with css selector - span.download-item a[href]
 * The url may contain spaces and needs to be encoded. This is done by constructing the url into a uri
 * Check if the file was downloaded before with etag or last modified
 * Create the content entry
 */
public class DdlContentScraper {

    private final String urlString;
    private final File destinationDirectory;
    private final URL url;
    private final ContentEntryDao contentEntryDao;
    private final ContentEntryFileDao contentEntryFileDao;
    private final ContentEntryContentEntryFileJoinDao contentEntryFileJoinDao;
    private final ContentEntryFileStatusDao contentFileStatusDao;
    private final ContentCategorySchemaDao categorySchemaDao;
    private final ContentCategoryDao contentCategoryDao;
    private final LanguageDao languageDao;
    private Document doc;
    ArrayList<ContentEntry> contentEntries;

    public DdlContentScraper(String url, File destination) throws MalformedURLException {
        this.urlString = url;
        this.destinationDirectory = destination;
        this.url = new URL(url);
        destinationDirectory.mkdirs();
        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repository = db.getRepository("https://localhost", "");
        contentEntryDao = repository.getContentEntryDao();
        contentEntryFileDao = repository.getContentEntryFileDao();
        contentEntryFileJoinDao = repository.getContentEntryContentEntryFileJoinDao();
        contentFileStatusDao = db.getContentEntryFileStatusDao();
        categorySchemaDao = repository.getContentCategorySchemaDao();
        contentCategoryDao = repository.getContentCategoryDao();
        languageDao = repository.getLanguageDao();
    }


    public static void main(String[] args) throws URISyntaxException {
        if (args.length < 2) {
            System.err.println("Usage: <ddl website url> <file destination><optional log{trace, debug, info, warn, error, fatal}>");
            System.exit(1);
        }
        UMLogUtil.setLevel(args.length == 3 ? args[2] : "");
        UMLogUtil.logInfo(args[0]);
        UMLogUtil.logInfo(args[1]);
        try {
            new DdlContentScraper(args[0], new File(args[1])).scrapeContent();
        } catch (IOException e) {
            UMLogUtil.logError(ExceptionUtils.getStackTrace(e));
            UMLogUtil.logError("Exception running scrapeContent ddl");
        }

    }


    public void scrapeContent() throws IOException {

        File resourceFolder = new File(destinationDirectory, FilenameUtils.getBaseName(urlString));
        resourceFolder.mkdirs();

        doc = Jsoup.connect(urlString).get();

        Elements downloadList = doc.select("span.download-item a[href]");

        String thumbnail = doc.selectFirst("aside img").attr("src");

        String lang = doc.select("html").attr("lang");
        Language langEntity = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, LanguageCode.getByCode(lang).getName());
        String description = doc.selectFirst("meta[name=description]").attr("content");
        Element authorTag = doc.selectFirst("article.resource-view-details h3:contains(Author) ~ p");
        String author = authorTag != null ? authorTag.text() : "";
        Element publisherTag = doc.selectFirst("article.resource-view-details h3:contains(Publisher) ~ p");
        String publisher = publisherTag != null ? publisherTag.text() : "";


        ContentEntry contentEntry = ContentScraperUtil.createOrUpdateContentEntry(urlString, doc.title(),
                urlString, (publisher != null && !publisher.isEmpty() ? publisher : DDL),
                LICENSE_TYPE_CC_BY, langEntity.getLangUid(), null, description, true, author,
                thumbnail, EMPTY_STRING, EMPTY_STRING, contentEntryDao);


        contentEntries = new ArrayList<>();
        for (int downloadCount = 0; downloadCount < downloadList.size(); downloadCount++) {

            Element downloadItem = downloadList.get(downloadCount);
            String href = downloadItem.attr("href");
            HttpURLConnection conn = null;
            try {
                URL fileUrl = new URL(url, href);

                // this was done to encode url that had empty spaces in the name or other illegal characters
                URI uri = new URI(fileUrl.getProtocol(), fileUrl.getUserInfo(), fileUrl.getHost(), fileUrl.getPort(), fileUrl.getPath(), fileUrl.getQuery(), fileUrl.getRef());

                conn = (HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestMethod(REQUEST_HEAD);
                File resourceFile = new File(resourceFolder, FilenameUtils.getName(href));
                String mimeType = Files.probeContentType(resourceFile.toPath());

                if (!ContentScraperUtil.isFileModified(conn, resourceFolder, FilenameUtils.getName(href)) && ContentScraperUtil.fileHasContent(resourceFile)) {

                    ContentScraperUtil.checkAndUpdateDatabaseIfFileDownloadedButNoDataFound(resourceFile, contentEntry, contentEntryFileDao,
                            contentEntryFileJoinDao, contentFileStatusDao, mimeType, true);
                    continue;
                }

                FileUtils.copyURLToFile(uri.toURL(), resourceFile);

                ContentScraperUtil.insertContentEntryFile(resourceFile, contentEntryFileDao, contentFileStatusDao, contentEntry,
                        ContentScraperUtil.getMd5(resourceFile), contentEntryFileJoinDao, true, mimeType);

                contentEntries.add(contentEntry);
            } catch(Exception e){
                UMLogUtil.logError("Error downloading resource from url " + url + "/" + href);
            }finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

    protected ArrayList<ContentEntry> getContentEntries() {
        return contentEntries;
    }

    public ArrayList<ContentEntry> getParentSubjectAreas() {

        ArrayList<ContentEntry> subjectAreaList = new ArrayList<>();
        Elements subjectContainer = doc.select("article.resource-view-details h3:contains(Subject Area) ~ p");

        Elements subjectList = subjectContainer.select("a");
        for (Element subject : subjectList) {

            String title = subject.attr("title");
            String href = subject.attr("href");

            String lang = doc.select("html").attr("lang");
            Language langEntity = ContentScraperUtil.insertOrUpdateLanguageByName(languageDao, LanguageCode.getByCode(lang).getName());


            ContentEntry contentEntry = ContentScraperUtil.createOrUpdateContentEntry(href, title, href,
                    DDL, LICENSE_TYPE_CC_BY, langEntity.getLangUid(), null,
                    EMPTY_STRING, false, EMPTY_STRING, EMPTY_STRING,
                    EMPTY_STRING, EMPTY_STRING, contentEntryDao);

            subjectAreaList.add(contentEntry);

        }

        return subjectAreaList;
    }


    public ArrayList<ContentCategory> getContentCategories() {


        ArrayList<ContentCategory> categoryRelations = new ArrayList<>();
        Elements subjectContainer = doc.select("article.resource-view-details h3:contains(Resource Level) ~ p");

        ContentCategorySchema ddlSchema = ContentScraperUtil.insertOrUpdateSchema(categorySchemaDao, "DDL Resource Level", "ddl/resource-level/");

        Elements subjectList = subjectContainer.select("a");
        for (Element subject : subjectList) {

            String title = subject.attr("title");

            ContentCategory categoryEntry = ContentScraperUtil.insertOrUpdateCategoryContent(contentCategoryDao, ddlSchema, title);

            categoryRelations.add(categoryEntry);

        }

        return categoryRelations;
    }
}
