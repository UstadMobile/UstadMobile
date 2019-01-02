package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.lib.contentscrapers.voa.IndexVoaScraper;
import com.ustadmobile.lib.contentscrapers.voa.VoaScraper;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestVoaScraper {

    @Test
    public void givenServerOnline_whenVoaScraped_thenShouldConvertAndDownload() throws IOException {

        File tmpDir = Files.createTempDirectory("testVoaScraper").toFile();

        VoaScraper scraper = new VoaScraper("https://learningenglish.voanews.com/a/november-23-2018/4671713.html",
                tmpDir);
        scraper.scrapeContent();

    }

    @Test
    public void givenServerOnline_whenVoaScrapedDownlaodTwice_thenShouldConvertAndDownload() throws IOException {

        //File tmpDir = Files.createTempDirectory("testVoaScraper").toFile();

        VoaScraper scraper = new VoaScraper("https://learningenglish.voanews.com/a/4719880.html",
                new File("C:/voa/"));
        scraper.scrapeContent();

        scraper.scrapeContent();

    }

    @Test
    public void givenServerOnline_whenIndexVoaScraped_thenShouldConvertAndDownload() throws IOException {

        File tmpDir = Files.createTempDirectory("testIndexVoaScraper").toFile();

        IndexVoaScraper scraper = new IndexVoaScraper();
        scraper.findContent("https://learningenglish.voanews.com/", tmpDir);

    }
}
