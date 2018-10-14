package com.ustadmobile.lib.contentscrapers.ddl;

import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class DdlContentScraper {

    private final String urlString;
    private final File destinationDirectory;
    private final URL url;
    private Document doc;
    ArrayList<OpdsEntryWithRelations> opdsFiles;

    public DdlContentScraper(String url, File destination) throws MalformedURLException {
        this.urlString = url;
        this.destinationDirectory = destination;
        this.url = new URL(url);
        destinationDirectory.mkdirs();
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
            URI uri = new URI(fileUrl.getProtocol(), fileUrl.getUserInfo(), fileUrl.getHost(), fileUrl.getPort(), fileUrl.getPath(), fileUrl.getQuery(), fileUrl.getRef());

            File resourceFile = new File(resourceFolder, FilenameUtils.getName(href));
            FileUtils.copyURLToFile(uri.toURL(), resourceFile);
            String mimeType = Files.probeContentType(resourceFile.toPath());
            OpdsEntryWithRelations newEntry = new OpdsEntryWithRelations(
                    UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), fileUrl.toString(), href);
            OpdsLink newEntryLink = new OpdsLink(newEntry.getUuid(), mimeType,
                    resourceFolder.getName() + "/" + FilenameUtils.getName(href), OpdsEntry.LINK_REL_ACQUIRE);
            newEntryLink.setLength(resourceFile.length());
            newEntry.setLinks(Collections.singletonList(newEntryLink));

            opdsFiles.add(newEntry);
        }
    }

    protected ArrayList<OpdsEntryWithRelations> getOpdsFiles() {
        return opdsFiles;
    }

    public ArrayList<OpdsEntryWithRelations> getCategoryRelations() {

        ArrayList<OpdsEntryWithRelations> categoryRelations = new ArrayList<>();
        Elements subjectContainer = doc.select("article.resource-view-details h3:contains(Subject Area) ~ p");

        Elements subjectList = subjectContainer.select("a");
        for (Element subject : subjectList) {

            String title = subject.attr("title");
            String href = subject.attr("href");

            OpdsEntryWithRelations newEntry = new OpdsEntryWithRelations(
                    UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), href, title);

            categoryRelations.add(newEntry);

        }

        return categoryRelations;
    }


}
