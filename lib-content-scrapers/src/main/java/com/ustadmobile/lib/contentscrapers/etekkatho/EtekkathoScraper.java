package com.ustadmobile.lib.contentscrapers.etekkatho;

import com.ustadmobile.lib.contentscrapers.ContentScraperUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * The etekkatho website has a single url link for download
 * This can be found by using Jsoup css selector query with th:contains(Download) ~td a[href].btn and getting the attribute value of href
 *
 * The url requires a request property header with user agent for the download to be successful
 */
public class EtekkathoScraper {

    private final URL scrapUrl;
    private final File destinationDir;
    private final File etekDirectory;
    private boolean isUpdated;
    private String mimeType;

    public EtekkathoScraper(String url, File destinationDir) throws IOException {
        scrapUrl = new URL(url);
        this.destinationDir = destinationDir;
        etekDirectory = new File(destinationDir, url.substring(url.indexOf("=") + 1));
        etekDirectory.mkdirs();
    }

    public void scrapeContent() throws IOException {

        Document document = Jsoup.connect(scrapUrl.toString()).get();

        String hrefLink = document.selectFirst("th:contains(Download) ~td a[href].btn").attr("href");
        hrefLink = hrefLink.replaceAll(" ", "_");

        URL contentUrl = new URL(scrapUrl, hrefLink);
        URLConnection conn = contentUrl.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Mobile Safari/537.36");

        isUpdated = ContentScraperUtil.isFileModified(conn, etekDirectory, FilenameUtils.getBaseName(contentUrl.getPath()));
        if (!isUpdated) {
            return;
        }

        mimeType = conn.getContentType();
        File content = new File(etekDirectory, etekDirectory.getName());

        FileUtils.copyInputStreamToFile(conn.getInputStream(), content);

    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public String getMimeType(){
        return mimeType;
    }
}
