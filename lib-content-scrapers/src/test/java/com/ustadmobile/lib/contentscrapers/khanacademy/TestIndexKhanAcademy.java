package com.ustadmobile.lib.contentscrapers.khanacademy;

import com.ustadmobile.core.db.UmAppDatabase;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestIndexKhanAcademy {




    @Test
    public void givenServerOnline_whenKhanContentScraped_thenShouldConvertAndDownloadAllFiles() throws IOException {

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repo = db.getRepository("https://localhost", "");

        //File tmpDir = Files.createTempDirectory("testIndexKhancontentscraper").toFile();

        IndexKhanContentScraper indexScraper = new IndexKhanContentScraper();
        indexScraper.findContent("https://www.khanacademy.org/", new File("C:/khan/"));


    }

    @Test
    public void testPractice() throws IOException {

        File tmpDir = Files.createTempDirectory("testKhanExercisecontentscraper").toFile();

        KhanContentScraper scraper = new KhanContentScraper(tmpDir);
        scraper.scrapeExerciseContent("https://www.khanacademy.org/math/early-math/cc-early-math-counting-topic/cc-early-math-counting/e/counting-out-1-20-objects");
     //   scraper.scrapeExerciseContent("https://www.khanacademy.org/math/early-math/cc-early-math-counting-topic/cc-early-math-counting/e/counting-objects");
    }

    @Test
    public void testArticleContent() throws IOException {

        File tmpDir = Files.createTempDirectory("testKhanArticleContentScraper").toFile();

        KhanContentScraper scraper = new KhanContentScraper(tmpDir);
        scraper.scrapeArticleContent("https://www.khanacademy.org/math/early-math/cc-early-math-place-value-topic/cc-early-math-two-digit-compare/a/comparison-symbols-review");
        //


    }

}
