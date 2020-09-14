package com.ustadmobile.lib.contentscrapers.apache

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.folder.FolderScraper
import com.ustadmobile.lib.contentscrapers.globalDisptacher
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.Okio
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files

@ExperimentalStdlibApi
class TestApacheIndexer {

    @Rule
    @JvmField
    val tmpFileRule = TemporaryFolder()

    val tmpDir = Files.createTempDirectory("folder").toFile()
    val containerDir = Files.createTempDirectory("container").toFile()

    lateinit var db: UmAppDatabase

    lateinit var mockWebServer: MockWebServer


    @Before
    fun setup() {
        db = UmAppDatabase.getInstance(Any())

        mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(globalDisptacher)

    }

    @Test
    fun givenApacheFolder_whenIndexed_createEntries() {

        val apacheIndexer = ApacheIndexer(0, 0, db, 0, 0)
        apacheIndexer.indexUrl(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/apache/folder.txt").toString())

        val apacheFolderEntry = db.contentEntryDao.findBySourceUrl("http://localhost:${mockWebServer.port}/json/com/ustadmobile/lib/contentscrapers/apache/folder.txt")
        Assert.assertEquals("English content exists", "UstadMobile-app-branded-maktab-tajikistan", apacheFolderEntry!!.title)

        val buildFolder = db.contentEntryDao.findBySourceUrl("http://localhost:${mockWebServer.port}/json/com/ustadmobile/lib/contentscrapers/apache/builds/")
        Assert.assertEquals("English content exists", "builds/", buildFolder!!.title)

        val fileEntry = db.contentEntryDao.findBySourceUrl("http://localhost:${mockWebServer.port}/content/com/ustadmobile/lib/contentscrapers/folder/314-my-very-own-scooter-EN.epub")
        Assert.assertEquals("English content exists", "scooter.epub", fileEntry!!.title)

    }

    @Test
    fun givenApacheFile_whenScraped_createContainer(){

        val scraper = ApacheScraper(containerDir, db, 0, 0,0 )
        scraper.scrapeUrl(mockWebServer.url("/content/com/ustadmobile/lib/contentscrapers/folder/314-my-very-own-scooter-EN.epub").toString())

        val filEntry = db.contentEntryDao.findBySourceUrl("http://localhost:${mockWebServer.port}/content/com/ustadmobile/lib/contentscrapers/folder/314-my-very-own-scooter-EN.epub")
        Assert.assertEquals("Scooter content exists", "My Own Scooter", filEntry!!.title)

        runBlocking {
            val fileContainer = db.containerDao.findRecentContainerToBeMonitoredWithEntriesUid(listOf(filEntry.contentEntryUid))
            val container = fileContainer[0]
            Assert.assertEquals("container is epub", "application/epub+zip", container.mimeType)
        }


    }

}