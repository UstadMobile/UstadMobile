package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.util.UMIOUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class PhetContentScraper implements ContentScraper{


    @Override
    public void convert(String url, File destinationDir) throws IOException {

        URL simulationUrl = new URL(url);
        destinationDir.mkdirs();

        Document simulationDoc = Jsoup.connect(url).get();

        if(!simulationDoc.select("div.simulation-main-image-panel a span").hasClass("html-badge")){
            throw new IllegalArgumentException("File Type not supported");
        }

        String about = simulationDoc.getElementById("about").html();
        ContentScraperUtil.writeStringToFile(about, new File(destinationDir, ScraperConstants.ABOUT_HTML));

        boolean contentUpdated = false;
        for(Element englishLink: simulationDoc.select("div.simulation-main-image-panel a.phet-button[href]")){

            String hrefLink = englishLink.attr("href");

            File englishLocation = new File(destinationDir, "en");
            englishLocation.mkdirs();

            if(hrefLink.contains("download")){
                downloadContent(simulationUrl, hrefLink, englishLocation);
                contentUpdated = true;
                break;
            }
        }

        File languageLocation = null;
        for(Element translations: simulationDoc.select("table.phet-table tr")){

            for(Element langs: translations.select("td.list-highlight-background a[href]")){

                String hrefLink = langs.attr("href");

                if(hrefLink.contains("translated")) {

                    String langCode = hrefLink.substring(hrefLink.lastIndexOf("/") + 1, hrefLink.length());
                    System.out.println(langCode);
                    languageLocation = new File(destinationDir, langCode);
                    languageLocation.mkdirs();
                    break;
                }
            }

            for(Element links: translations.select("td.img-container a[href]")){

                String hrefLink = links.attr("href");

                if(hrefLink.contains("download")){
                    downloadContent(simulationUrl, hrefLink, languageLocation);
                    contentUpdated = true;
                    break;
                }

            }

        }

        if(contentUpdated){
            ContentScraperUtil.zipDirectory(destinationDir, url.substring(url.lastIndexOf("/"), url.length()));
        }

    }

    private void downloadContent(URL simulationUrl, String hrefLink, File languageLocation) throws IOException {

        URL link = new URL(simulationUrl, hrefLink);
        System.out.println(link);
        URLConnection conn = link.openConnection();

        String eTag = conn.getHeaderField("ETag").replaceAll("\"", "");
        String lastModified = conn.getHeaderField("Last-Modified");
        System.out.println(eTag);
        System.out.println(lastModified);

        File eTagFile = new File(languageLocation, ScraperConstants.ETAG_TXT);
        File modifiedFile = new File(languageLocation, ScraperConstants.LAST_MODIFIED_TXT);

        if(eTagFile.length() > 0){

            String text = new String(Files.readAllBytes(eTagFile.toPath()));
            if(text.equalsIgnoreCase(eTag)){
                return;
            }

        }

        ContentScraperUtil.downloadContent(link, new File(languageLocation, hrefLink.substring(hrefLink.lastIndexOf("/") + 1, hrefLink.lastIndexOf("?"))));
        ContentScraperUtil.writeStringToFile(eTag, eTagFile);
        ContentScraperUtil.writeStringToFile(lastModified, modifiedFile);

    }
}
