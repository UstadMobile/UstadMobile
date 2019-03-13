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
    private File englishFolder;
    private File arabicFolder;
    private File mathFolder;
    private File scienceFolder;
    private File scooterFile;
    private File containerDir;

    @Before
    public void setupFolder() throws IOException {
        UmAppDatabase db = UmAppDatabase.getInstance(null);
        db.clearAllTables();

        tmpDir = Files.createTempDirectory("testIndexFolderScraper").toFile();
        containerDir = Files.createTempDirectory("container").toFile();
        englishFolder = new File(tmpDir, "English");
        englishFolder.mkdirs();

        arabicFolder = new File(tmpDir, "Arabic");
        arabicFolder.mkdirs();

        mathFolder = new File(englishFolder, "Math");
        mathFolder.mkdirs();

        scienceFolder = new File(arabicFolder, "Science");
        scienceFolder.mkdirs();

        FileUtils.copyToFile(getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/pratham/24620-a-book-for-puchku.epub"),
                new File(mathFolder, "puchku.epub"));

        FileUtils.copyToFile(getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/folder/313-Ruby And Emerald-AR.epub"),
                new File(arabicFolder, "ruby-ar.epub"));

        scooterFile = new File(scienceFolder, "scooter-en.epub");
        FileUtils.copyToFile(getClass().getResourceAsStream("/com/ustadmobile/lib/contentscrapers/folder/314-my very own scooter-EN.epub"),
                scooterFile);
    }

    @Test
    public void givenServerOnline_whenFolderIsScrapedAgain_thenShouldDownloadOnlyOnce() throws IOException {

        IndexFolderScraper scraper = new IndexFolderScraper();
        scraper.findContent("3asafeer",
                tmpDir, containerDir);

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repo = db.getRepository("https://localhost", "");

        ContentEntryDao contentEntryDao = repo.getContentEntryDao();
        ContentEntryParentChildJoinDao parentChildDaoJoin = repo.getContentEntryParentChildJoinDao();

        String filePrefix = "file://";

        ContentEntry englishEntry = contentEntryDao.findBySourceUrl(filePrefix + englishFolder.getPath());
        Assert.assertEquals("English content exists", true, englishEntry.getEntryId().equalsIgnoreCase("English"));

        ContentEntry arabicEntry = contentEntryDao.findBySourceUrl(filePrefix + arabicFolder.getPath());
        Assert.assertEquals("Arabic content exists", true, arabicEntry.getEntryId().equalsIgnoreCase("Arabic"));

        ContentEntry scienceEntry = contentEntryDao.findBySourceUrl(filePrefix + scienceFolder.getPath());
        Assert.assertEquals("Science content exists", true, scienceEntry.getEntryId().equalsIgnoreCase("Science"));

        ContentEntry mathEntry = contentEntryDao.findBySourceUrl(filePrefix + mathFolder.getPath());
        Assert.assertEquals("Math content exists", true, mathEntry.getEntryId().equalsIgnoreCase("Math"));

        ContentEntry scienceEpubEntry = contentEntryDao.findBySourceUrl(filePrefix + scooterFile.getPath());
        Assert.assertEquals("Epub in Science Folder content exists", true, scienceEpubEntry.getEntryId().equalsIgnoreCase("urn:uuid:29d919dd-24f5-4384-be78-b447c9dc299b"));

        ContentEntryParentChildJoin arabicScienceJoinEntry = parentChildDaoJoin.findParentByChildUuids(scienceEntry.getContentEntryUid());
        Assert.assertEquals("Arabic Entry is a parent of Science", true, arabicScienceJoinEntry.getCepcjParentContentEntryUid() == arabicEntry.getContentEntryUid());

        ContentEntryParentChildJoin englishMathJoinEntry = parentChildDaoJoin.findParentByChildUuids(mathEntry.getContentEntryUid());
        Assert.assertEquals("English Entry is a parent of Math", true, englishMathJoinEntry.getCepcjParentContentEntryUid() == englishEntry.getContentEntryUid());

        ContentEntryParentChildJoin scienceEpubJoinEntry = parentChildDaoJoin.findParentByChildUuids(scienceEpubEntry.getContentEntryUid());
        Assert.assertEquals("Arabic Entry is a parent of Science", true, scienceEpubJoinEntry.getCepcjParentContentEntryUid() == scienceEntry.getContentEntryUid());


    }


}
