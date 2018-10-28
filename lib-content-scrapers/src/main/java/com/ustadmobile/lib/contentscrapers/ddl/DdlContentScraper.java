package com.ustadmobile.lib.contentscrapers.ddl;

import com.neovisionaries.i18n.LanguageCode;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import javax.swing.text.AbstractDocument;

public class DdlContentScraper {

    private final String urlString;
    private final File destinationDirectory;
    private final URL url;
    private final ContentEntryDao contentEntryDao;
    private final ContentEntryFileDao contentEntryFileDao;
    private final ContentEntryContentEntryFileJoinDao contentEntryFileJoinDao;
    private Document doc;
    ArrayList<ContentEntry> opdsFiles;

    public DdlContentScraper(String url, File destination) throws MalformedURLException {
        this.urlString = url;
        this.destinationDirectory = destination;
        this.url = new URL(url);
        destinationDirectory.mkdirs();
        UmAppDatabase db = UmAppDatabase.getInstance(null);
        contentEntryDao = db.getContentEntryDao();
        contentEntryFileDao = db.getContentEntryFileDao();
        contentEntryFileJoinDao = db.getContentEntryContentEntryFileJoinDao();
    }

    public void scrapeContent() throws IOException, URISyntaxException {

        File resourceFolder = new File(destinationDirectory, FilenameUtils.getBaseName(urlString));
        resourceFolder.mkdirs();

        doc = Jsoup.connect(urlString).get();

        Elements downloadList = doc.select("span.download-item a[href]");

        opdsFiles = new ArrayList<>();
        for (Element downloadItem : downloadList) {

            String href = downloadItem.attr("href");
            URL fileUrl = new URL(url, href);
            // this was done to encode url that had empty spaces in the name or other illegal characters
            URI uri = new URI(fileUrl.getProtocol(), fileUrl.getUserInfo(), fileUrl.getHost(), fileUrl.getPort(), fileUrl.getPath(), fileUrl.getQuery(), fileUrl.getRef());

            URLConnection conn =  uri.toURL().openConnection();
            File resourceFile = new File(resourceFolder, FilenameUtils.getName(href));
            if (!ContentScraperUtil.isFileModified(conn, resourceFolder, FilenameUtils.getName(href))) {
                continue;
            }

            FileUtils.copyURLToFile(uri.toURL(), resourceFile);

            String thumbnail = doc.selectFirst("aside img").attr("src");

            ContentEntry contentEntry = contentEntryDao.findBySourceUrl(uri.toURL().getPath());
            if (contentEntry == null) {
                contentEntry = new ContentEntry();
                contentEntry = setContentEntryData(contentEntry, uri.toString(),
                        doc.title(), uri.toURL().getPath(), doc.select("html").attr("lang"));
                contentEntry.setThumbnailUrl(thumbnail);
                contentEntry.setContentEntryUid(contentEntryDao.insert(contentEntry));
            } else {
                contentEntry = setContentEntryData(contentEntry, uri.toString(),
                        doc.title(), uri.toURL().getPath(), doc.select("html").attr("lang"));
                contentEntry.setThumbnailUrl(thumbnail);
                contentEntryDao.updateContentEntry(contentEntry);
            }

            FileInputStream fis = new FileInputStream(resourceFile);
            String md5 = DigestUtils.md5Hex(fis);
            fis.close();

            String mimeType = Files.probeContentType(resourceFile.toPath());

            ContentEntryFile contentEntryFile = new ContentEntryFile();
            contentEntryFile.setMimeType(mimeType);
            contentEntryFile.setFileSize(resourceFile.length());
            contentEntryFile.setLastModified(resourceFile.lastModified());
            contentEntryFile.setMd5sum(md5);
            contentEntryFile.setContentEntryFileUid(contentEntryFileDao.insert(contentEntryFile));

            ContentEntryContentEntryFileJoin fileJoin = new ContentEntryContentEntryFileJoin();
            fileJoin.setCecefjContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
            fileJoin.setCecefjContentEntryUid(contentEntry.getContentEntryUid());
            fileJoin.setCecefjUid(contentEntryFileJoinDao.insert(fileJoin));

            opdsFiles.add(contentEntry);
        }
    }

    private ContentEntry setContentEntryData(ContentEntry entry, String id, String title, String sourceUrl, String lang) {
        entry.setEntryId(id);
        entry.setTitle(title);
        entry.setSourceUrl(sourceUrl);
        entry.setPublisher("DDL");
        entry.setLicenseType(ContentEntry.LICENSE_TYPE_CC_BY);
        entry.setPrimaryLanguage(lang);
        return entry;
    }


    protected ArrayList<ContentEntry> getOpdsFiles() {
        return opdsFiles;
    }

    public ArrayList<ContentEntry> getCategoryRelations() {

        ArrayList<ContentEntry> categoryRelations = new ArrayList<>();
        Elements subjectContainer = doc.select("article.resource-view-details h3:contains(Subject Area) ~ p");

        Elements subjectList = subjectContainer.select("a");
        for (Element subject : subjectList) {

            String title = subject.attr("title");
            String href = subject.attr("href");

            ContentEntry contentEntry = contentEntryDao.findBySourceUrl(href);
            if (contentEntry == null) {
                contentEntry = new ContentEntry();
                contentEntry = setContentEntryData(contentEntry, href,
                        title, href, doc.select("html").attr("lang"));
                contentEntry.setContentEntryUid(contentEntryDao.insert(contentEntry));
            } else {
                contentEntry = setContentEntryData(contentEntry, href,
                        title, href, doc.select("html").attr("lang"));
                contentEntryDao.updateContentEntry(contentEntry);
            }

            categoryRelations.add(contentEntry);

        }

        return categoryRelations;
    }


}
