package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.lib.contentscrapers.etekkatho.EtekkathoScraper;
import com.ustadmobile.lib.contentscrapers.etekkatho.IndexEtekkathoScraper;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestEtekScraper {

    @Test
    public void test() throws IOException {

        File tmpDir = Files.createTempDirectory("testEtekScraper").toFile();

        EtekkathoScraper scraper = new EtekkathoScraper("http://www.etekkatho.org/fullrecord?handle=20140417-16252485",
                tmpDir);
        scraper.scrapeContent();

    }

    @Test
    public void testIndex() throws IOException {

        File tmpDir = Files.createTempDirectory("testEtekIndexScraper").toFile();

        IndexEtekkathoScraper scraper = new IndexEtekkathoScraper();
        scraper.findContent("http://www.etekkatho.org/subjects/",
                tmpDir);
    }

}
