package com.ustadmobile.lib.contentscrapers

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.folder.IndexFolderScraper
import org.apache.commons.io.FileUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files

@ExperimentalStdlibApi
class TestFolderScraper {

    private var tmpDir: File? = null
    private var englishFolder: File? = null
    private var arabicFolder: File? = null
    private var mathFolder: File? = null
    private var scienceFolder: File? = null
    private var scooterFile: File? = null
    private var containerDir: File? = null

    @Before
    @Throws(IOException::class)
    fun setupFolder() {
        val db = UmAppDatabase.getInstance(Any())
        db.clearAllTables()


        ContentScraperUtil.checkIfPathsToDriversExist()

        tmpDir = Files.createTempDirectory("testIndexFolderScraper").toFile()
        containerDir = Files.createTempDirectory("container").toFile()
        englishFolder = File(tmpDir, "English")
        englishFolder!!.mkdirs()

        arabicFolder = File(tmpDir, "Arabic")
        arabicFolder!!.mkdirs()

        mathFolder = File(englishFolder, "Math")
        mathFolder!!.mkdirs()

        scienceFolder = File(arabicFolder, "Science")
        scienceFolder!!.mkdirs()

        FileUtils.copyToFile(javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/folder/313-Ruby And Emerald-AR.epub"),
                File(arabicFolder, "ruby-ar.epub"))

        scooterFile = File(scienceFolder, "scooter-en.epub")
        FileUtils.copyToFile(javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/folder/314-my very own scooter-EN.epub"),
                scooterFile!!)
    }

    @Test
    @Throws(IOException::class)
    fun givenServerOnline_whenFolderIsScrapedAgain_thenShouldDownloadOnlyOnce() {

        val scraper = IndexFolderScraper()
        scraper.findContent("3asafeer",
                tmpDir!!, containerDir!!)

        val db = UmAppDatabase.getInstance(Any())
        val repo = db //db.getRepository("https://localhost", "")

        val contentEntryDao = repo.contentEntryDao
        val parentChildDaoJoin = repo.contentEntryParentChildJoinDao

        val filePrefix = "file://"

        val englishEntry = contentEntryDao.findBySourceUrl(filePrefix + englishFolder!!.path)
        Assert.assertEquals("English content exists", true, englishEntry!!.entryId!!.equals("English", ignoreCase = true))

        val arabicEntry = contentEntryDao.findBySourceUrl(filePrefix + arabicFolder!!.path)
        Assert.assertEquals("Arabic content exists", true, arabicEntry!!.entryId!!.equals("Arabic", ignoreCase = true))

        val scienceEntry = contentEntryDao.findBySourceUrl(filePrefix + scienceFolder!!.path)
        Assert.assertEquals("Science content exists", true, scienceEntry!!.entryId!!.equals("Science", ignoreCase = true))

        val mathEntry = contentEntryDao.findBySourceUrl(filePrefix + mathFolder!!.path)
        Assert.assertEquals("Math content exists", true, mathEntry!!.entryId!!.equals("Math", ignoreCase = true))

        val scienceEpubEntry = contentEntryDao.findBySourceUrl(filePrefix + scooterFile!!.path)
        Assert.assertEquals("Epub in Science Folder content exists", true, scienceEpubEntry!!.entryId!!.equals("urn:uuid:29d919dd-24f5-4384-be78-b447c9dc299b", ignoreCase = true))

        val arabicScienceJoinEntry = parentChildDaoJoin.findParentByChildUuids(scienceEntry!!.contentEntryUid)
        Assert.assertEquals("Arabic Entry is a parent of Science", true, arabicScienceJoinEntry!!.cepcjParentContentEntryUid == arabicEntry!!.contentEntryUid)

        val englishMathJoinEntry = parentChildDaoJoin.findParentByChildUuids(mathEntry!!.contentEntryUid)
        Assert.assertEquals("English Entry is a parent of Math", true, englishMathJoinEntry!!.cepcjParentContentEntryUid == englishEntry!!.contentEntryUid)

        val scienceEpubJoinEntry = parentChildDaoJoin.findParentByChildUuids(scienceEpubEntry!!.contentEntryUid)
        Assert.assertEquals("Arabic Entry is a parent of Science", true, scienceEpubJoinEntry!!.cepcjParentContentEntryUid == scienceEntry!!.contentEntryUid)


    }


}
