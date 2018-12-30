package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.lib.contentscrapers.voa.VoaScraper;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestVoaScraper {

    @Test
    public void givenServerOnline_whenVoaScraped_thenShouldConvertAndDownload() throws IOException {

        File tmpDir = Files.createTempDirectory("testVoaScraper").toFile();

        VoaScraper scraper = new VoaScraper("https://learningenglish.voanews.com/a/lets-learn-english-lesson-16-where-are-you-from/3355849.html",
                tmpDir);
        scraper.scrapeContent();

    }
}
