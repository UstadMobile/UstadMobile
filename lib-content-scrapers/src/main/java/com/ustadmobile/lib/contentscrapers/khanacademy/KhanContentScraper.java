package com.ustadmobile.lib.contentscrapers.khanacademy;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.ustadmobile.lib.contentscrapers.edraakK12.ContentResponse;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;

public class KhanContentScraper {

    private final File destinationDirectory;

    public KhanContentScraper(File destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    public void scrapeVideoContent(String url){


        URL scrapUrl;
        try {
            scrapUrl = new URL(url);
        } catch (MalformedURLException e) {
            System.out.println("Scrap Malformed url" + url);
            throw new IllegalArgumentException("Malformed url" + url, e);
        }

        destinationDirectory.mkdirs();




    }


}
