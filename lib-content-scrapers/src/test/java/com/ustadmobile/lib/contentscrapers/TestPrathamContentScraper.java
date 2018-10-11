package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.lib.contentscrapers.africanbooks.AsbScraper;
import com.ustadmobile.lib.contentscrapers.ddl.DdlContentScraper;
import com.ustadmobile.lib.contentscrapers.ddl.IndexDdlContent;
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
    public void testasb() throws IOException {

        File tmpDir = Files.createTempDirectory("testindexAsbcontentscraper").toFile();

        AsbScraper scraper = new AsbScraper();
        scraper.findContent(tmpDir);

    }

    @Test
    public void testDdl() throws IOException {

        File tmpDir = Files.createTempDirectory("testindexDdlontentscraper").toFile();

        DdlContentScraper scraper = new DdlContentScraper("https://www.ddl.af/en/resource/4595", tmpDir);
        try {
            scraper.scrapeContent();
            scraper.getCategoryRelations();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testIndexDdl() throws IOException{


        File tmpDir = Files.createTempDirectory("testindexDdlontentscraper").toFile();

       new IndexDdlContent().findContent("https://www.darakhtdanesh.org/en/resources/list?page=2", tmpDir);

    }
}