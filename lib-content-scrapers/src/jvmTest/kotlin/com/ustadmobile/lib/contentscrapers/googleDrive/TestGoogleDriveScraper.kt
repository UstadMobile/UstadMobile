package com.ustadmobile.lib.contentscrapers.googleDrive

import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.folder.TestFolderIndexer
import com.ustadmobile.lib.contentscrapers.googledrive.GoogleDriveScraper
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import kotlinx.coroutines.runBlocking
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
import org.kodein.di.*
import java.io.File
import java.nio.file.Files
import javax.naming.InitialContext

@ExperimentalStdlibApi
class TestGoogleDriveScraper {

    @Rule
    @JvmField
    val tmpFileRule = TemporaryFolder()

    val tmpDir = Files.createTempDirectory("folder").toFile()
    val containerDir = Files.createTempDirectory("container").toFile()

    lateinit var db: UmAppDatabase

    lateinit var mockWebServer: MockWebServer

    private lateinit var di: DI
    private lateinit var endpointScope: EndpointScope
    private val endpoint = Endpoint(TestFolderIndexer.TEST_ENDPOINT)

    @Before
    fun setup() {
        endpointScope = EndpointScope()

        di = DI {
            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(UmAppDatabase.getInstance(Any(), dbName).also {
                    it.clearAllTables()
                })
            }
            bind<File>(tag = DiTag.TAG_CONTAINER_DIR) with scoped(EndpointScope.Default).singleton {
                containerDir
            }
            bind<String>(tag = DiTag.TAG_GOOGLE_API) with singleton {
                "abc"
            }
        }

        db = di.on(endpoint).direct.instance(tag = UmAppDatabase.TAG_DB)

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

        val scraper = GoogleDriveScraper(0,0,0, endpoint, di)
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