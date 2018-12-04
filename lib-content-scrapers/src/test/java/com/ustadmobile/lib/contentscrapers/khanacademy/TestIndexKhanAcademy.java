package com.ustadmobile.lib.contentscrapers.khanacademy;

import com.ustadmobile.core.db.UmAppDatabase;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class TestIndexKhanAcademy {




    @Test
    public void givenServerOnline_whenKhanContentScraped_thenShouldConvertAndDownloadAllFiles() throws IOException {

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repo = db.getRepository("https://localhost", "");



        File tmpDir = Files.createTempDirectory("testIndexKhancontentscraper").toFile();

        IndexKhanContentScraper indexScraper = new IndexKhanContentScraper();
        indexScraper.findContent("https://www.khanacademy.org/", tmpDir);





    }

}
