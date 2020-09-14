package com.ustadmobile.lib.contentscrapers.folder

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ScrapeQueueItem
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files

@ExperimentalStdlibApi
class TestFolderIndexerAndScraper {

    private lateinit var scooterFile: File
    private lateinit var englishFolder: File
    lateinit var db: UmAppDatabase

    @Rule
    @JvmField
    val tmpFileRule = TemporaryFolder()

    val tmpDir = Files.createTempDirectory("folder").toFile()
    val containerDir = Files.createTempDirectory("container").toFile()

    @Before
    fun setup(){
        db = UmAppDatabase.getInstance(Any())

        englishFolder = File(tmpDir, "english")
        englishFolder.mkdirs()

        scooterFile = File(tmpDir, "scooter-en.epub")
        FileUtils.copyToFile(javaClass.getResourceAsStream("/com/ustadmobile/lib/contentscrapers/folder/314-my-very-own-scooter-EN.epub"),
                scooterFile)

    }


    @Test
    fun givenAFolder_whenIndexed_thenCreateEntriesForFilesFound(){

        val indexer = FolderIndexer(0, 0, db, 0, 0)
        indexer.indexUrl(tmpDir.path)

        val englishEntry = db!!.contentEntryDao.findBySourceUrl(englishFolder.path)
        Assert.assertEquals("English content exists", englishFolder.name, englishEntry!!.title)

        val filEntry = db!!.contentEntryDao.findBySourceUrl(scooterFile.path)
        Assert.assertEquals("Scooter content exists", scooterFile.name, filEntry!!.title)

        val english = db!!.scrapeQueueItemDao.findExistingQueueItem(0, englishEntry.contentEntryUid)
        Assert.assertEquals(ScrapeQueueItem.ITEM_TYPE_INDEX, english!!.itemType)

        val file = db!!.scrapeQueueItemDao.findExistingQueueItem(0, filEntry.contentEntryUid)
        Assert.assertEquals(ScrapeQueueItem.ITEM_TYPE_SCRAPE, file!!.itemType)

    }

    @Test
    fun givenAFile_whenScraped_thenCreateContainer(){

        val scraper = FolderScraper(containerDir, db, 0, 0,0 )
        scraper.scrapeUrl(scooterFile.path)

        val filEntry = db.contentEntryDao.findBySourceUrl(scooterFile.path)
        Assert.assertEquals("Scooter content exists", "My Own Scooter", filEntry!!.title)

        runBlocking {
            val fileContainer = db.containerDao.findRecentContainerToBeMonitoredWithEntriesUid(listOf(filEntry.contentEntryUid))
            val container = fileContainer[0]
            Assert.assertEquals("container is epub", "application/epub+zip", container.mimeType)
        }

    }

}