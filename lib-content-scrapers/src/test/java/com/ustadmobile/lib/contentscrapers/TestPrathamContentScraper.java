package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.lib.contentscrapers.africanbooks.AsbScraper;
import com.ustadmobile.lib.contentscrapers.prathambooks.IndexPrathamContentScraper;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class TestPrathamContentScraper {


    @Test
    public void givenServerOnline_whenVideoContentScraped_thenShouldConvertAndDownload() throws IOException, URISyntaxException {

        File tmpDir = Files.createTempDirectory("testindexPrathamcontentscraper").toFile();



        IndexPrathamContentScraper contentScraper = new IndexPrathamContentScraper();
        contentScraper.findContent(tmpDir);


    }

    @Test
    public void testaf() throws IOException {

        File tmpDir = Files.createTempDirectory("testindexPrathamcontentscraper").toFile();

        AsbScraper scraper = new AsbScraper();
        scraper.findContent(tmpDir);

    }
}