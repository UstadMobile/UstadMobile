package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.lib.contentscrapers.folder.IndexFolderScraper;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestFolderScraper {

    private File tmpDir;

    @Before
    public void setupFolder() throws IOException {
        tmpDir = Files.createTempDirectory("testIndexFolderScraper").toFile();
        File englishFolder = new File(tmpDir, "English");
        englishFolder.mkdirs();

        File arabicFolder = new File(tmpDir, "Arabic");
        arabicFolder.mkdirs();

        File mathFolder = new File(englishFolder, "Math");
        mathFolder.mkdirs();

        File scienceFolder = new File(arabicFolder, "Science");
        scienceFolder.mkdirs();

        FileUtils.copyToFile(getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/pratham/24620-a-book-for-puchku.epub"),
                new File(mathFolder, "puchku.epub"));

        FileUtils.copyToFile(getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/folder/313-Ruby And Emerald-AR.epub"),
                new File(arabicFolder, "ruby-ar.epub"));

        FileUtils.copyToFile(getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/folder/314-my very own scooter-EN.epub"),
                new File(scienceFolder, "scooter-en.epub"));
    }

    @Test
    public void givenServerOnline_whenVoaIsScrapedAgain_thenShouldDownloadOnlyOnce() throws IOException {

        IndexFolderScraper scraper = new IndexFolderScraper();
        scraper.findContent("3asafeer",
                tmpDir);

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repo = db.getRepository("https://localhost", "");

        ContentEntryDao contentEntryDao = repo.getContentEntryDao();
        ContentEntryParentChildJoinDao parentChildDaoJoin = repo.getContentEntryParentChildJoinDao();

        ContentEntry englishEntry = contentEntryDao.findBySourceUrl("English");
        Assert.assertEquals("English content exists", true, englishEntry.getEntryId().equalsIgnoreCase("English"));

        ContentEntry arabicEntry = contentEntryDao.findBySourceUrl("Arabic");
        Assert.assertEquals("Arabic content exists", true, arabicEntry.getEntryId().equalsIgnoreCase("Arabic"));

        ContentEntry scienceEntry = contentEntryDao.findBySourceUrl("Science");
        Assert.assertEquals("Science content exists", true, scienceEntry.getEntryId().equalsIgnoreCase("Science"));

        ContentEntry mathEntry = contentEntryDao.findBySourceUrl("Math");
        Assert.assertEquals("Math content exists", true, mathEntry.getEntryId().equalsIgnoreCase("Math"));

        ContentEntry scienceEpubEntry = contentEntryDao.findBySourceUrl("urn:uuid:29d919dd-24f5-4384-be78-b447c9dc299b");
        Assert.assertEquals("Epub in Science Folder content exists", true, scienceEpubEntry.getEntryId().equalsIgnoreCase("urn:uuid:29d919dd-24f5-4384-be78-b447c9dc299b"));

        ContentEntryParentChildJoin arabicScienceJoinEntry = parentChildDaoJoin.findParentByChildUuids(scienceEntry.getContentEntryUid());
        Assert.assertEquals("Arabic Entry is a parent of Science", true, arabicScienceJoinEntry.getCepcjParentContentEntryUid() == arabicEntry.getContentEntryUid());

        ContentEntryParentChildJoin englishMathJoinEntry = parentChildDaoJoin.findParentByChildUuids(mathEntry.getContentEntryUid());
        Assert.assertEquals("English Entry is a parent of Math", true, englishMathJoinEntry.getCepcjParentContentEntryUid() == englishEntry.getContentEntryUid());

        ContentEntryParentChildJoin scienceEpubJoinEntry = parentChildDaoJoin.findParentByChildUuids(scienceEpubEntry.getContentEntryUid());
        Assert.assertEquals("Arabic Entry is a parent of Science", true, scienceEpubJoinEntry.getCepcjParentContentEntryUid() == scienceEntry.getContentEntryUid());



    }


}
