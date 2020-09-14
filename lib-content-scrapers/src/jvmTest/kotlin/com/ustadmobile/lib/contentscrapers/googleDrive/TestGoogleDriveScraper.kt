package com.ustadmobile.lib.contentscrapers.googleDrive

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ustadmobile.core.contentformats.xapi.Actor
import com.ustadmobile.core.contentformats.xapi.ContextActivity
import com.ustadmobile.core.contentformats.xapi.Statement
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.globalDisptacher
import com.ustadmobile.lib.contentscrapers.googledrive.GoogleDriveScraper
import com.ustadmobile.lib.contentscrapers.googledrive.GoogleFile
import com.ustadmobile.port.sharedse.contentformats.xapi.ContextDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementDeserializer
import com.ustadmobile.port.sharedse.contentformats.xapi.StatementSerializer
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.Okio
import org.apache.commons.io.IOUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import java.io.File
import java.nio.file.Files

@ExperimentalStdlibApi
class TestGoogleDriveScraper {

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
        db.clearAllTables()

        mockWebServer = MockWebServer()

    }

    @Test
    fun givenGoogleDriveLink_whenScraped_createContainer(){

        val data = javaClass.getResourceAsStream(File("/com/ustadmobile/lib/contentscrapers/googleDrive/file.txt").toString())
        val body = IOUtils.toString(data, ScraperConstants.UTF_ENCODING)
        var response = MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json")
        response.setBody(body)

        mockWebServer.enqueue(response)

        val videoIn = javaClass.getResourceAsStream(File("/com/ustadmobile/lib/contentscrapers/folder/314-my-very-own-scooter-EN.epub").toString())
        val source = Okio.buffer(Okio.source(videoIn))
        val buffer = Buffer()
        source.readAll(buffer)

        response = MockResponse().setResponseCode(200).setHeader("Content-Type", "application/epub+zip")
        response.body = buffer

        mockWebServer.enqueue(response)

        val scraper = GoogleDriveScraper(containerDir, db, 0,0,0)
        val url = mockWebServer.url("/https://www.googleapis.com/drive/v3/files/0B__cZKkvYaJvckpRSFEtSWFVRkk").toString()
        scraper.scrapeUrl(url)

        val filEntry = db.contentEntryDao.findBySourceUrl("https://www.googleapis.com/drive/v3/files/0B__cZKkvYaJvckpRSFEtSWFVRkk")
        Assert.assertEquals("Scooter content exists", "My Own Scooter", filEntry!!.title)

        runBlocking {
            val fileContainer = db.containerDao.findRecentContainerToBeMonitoredWithEntriesUid(listOf(filEntry.contentEntryUid))
            val container = fileContainer[0]
            Assert.assertEquals("container is epub", "application/epub+zip", container.mimeType)
        }


    }



}