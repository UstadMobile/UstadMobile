package com.ustadmobile.lib.contentscrapers.ddl;

import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;
import com.ustadmobile.lib.contentscrapers.ScraperConstants;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.lib.util.UmUuidUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
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
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public class DdlContentScraper {

    private final String url;
    private final File destinationDirectory;
    private Document doc;
    ArrayList<OpdsEntryWithRelations> opdsFiles;

    public DdlContentScraper(String url, File destination) {
        this.url = url;
        this.destinationDirectory = destination;
        destinationDirectory.mkdirs();
    }

    public void scrapeContent() throws IOException, URISyntaxException {


        File resourceFolder = new File(destinationDirectory, FilenameUtils.getBaseName(url));
        resourceFolder.mkdirs();

        doc = Jsoup.connect(url).get();

        Elements downloadList = doc.select("span.download-item a[href]");

        opdsFiles = new ArrayList<>();
        for (Element downloadItem : downloadList) {

            String href = downloadItem.attr("href");
            URL url = new URL(href);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());

            File file = new File(resourceFolder, FilenameUtils.getName(href));
            FileUtils.copyURLToFile(uri.toURL(), file);
            String mimeType = Files.probeContentType(file.toPath());
            OpdsEntryWithRelations newEntry = new OpdsEntryWithRelations(
                    UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), url.toString(), href);
            OpdsLink newEntryLink = new OpdsLink(newEntry.getUuid(), mimeType,
                    resourceFolder.getName() + "/" + FilenameUtils.getName(href), OpdsEntry.LINK_REL_ACQUIRE);
            newEntryLink.setLength(file.length());
            newEntry.setLinks(Collections.singletonList(newEntryLink));

            opdsFiles.add(newEntry);
        }
    }

    protected ArrayList<OpdsEntryWithRelations> getOpdsFiles(){
        return opdsFiles;
    }

    public ArrayList<OpdsEntryWithRelations> getCategoryRelations(){

        ArrayList<OpdsEntryWithRelations> categoryRelations = new ArrayList<>();
        Elements subjectContainer = doc.select("article.resource-view-details h3:contains(Subject Area) ~ p");

        Elements subjectList = subjectContainer.select("a");
        for(Element subject: subjectList){

            String title = subject.attr("title");
            String href=  subject.attr("href");

            OpdsEntryWithRelations newEntry = new OpdsEntryWithRelations(
                    UmUuidUtil.encodeUuidWithAscii85(UUID.randomUUID()), href, title);

            categoryRelations.add(newEntry);

        }

        return categoryRelations;
    }


}
