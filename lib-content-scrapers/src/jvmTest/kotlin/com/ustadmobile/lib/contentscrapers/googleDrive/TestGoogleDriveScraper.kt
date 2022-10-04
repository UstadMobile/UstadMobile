package com.ustadmobile.lib.contentscrapers.googleDrive

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.contentscrapers.googledrive.GoogleDriveScraper
import com.ustadmobile.lib.contentscrapers.harscraper.TestHarScraper
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.buffer
import okio.source
import org.apache.commons.io.IOUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import org.mockito.kotlin.spy
import java.io.File
import java.nio.file.Files
import javax.naming.InitialContext
import kotlin.random.Random


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
    private val endpoint = Endpoint(TestHarScraper.TEST_ENDPOINT)

    @Before
    fun setup() {
        endpointScope = EndpointScope()

        di = DI {
            bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
                NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                val nodeIdAndAuth : NodeIdAndAuth = instance()
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(
                    DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
                            "jdbc:sqlite:build/tmp/UmAppDatabase.sqlite")
                    .addSyncCallback(nodeIdAndAuth)
                    .build()
                    .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId))
            }
            bind<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR) with scoped(EndpointScope.Default).singleton {
                containerDir
            }
            bind<String>(tag = DiTag.TAG_GOOGLE_API) with singleton {
                "abc"
            }
        }

        db = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

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
        val source = videoIn.source().buffer()
        val buffer = Buffer()
        source.readAll(buffer)

        response = MockResponse().setResponseCode(200).setHeader("Content-Type", "application/epub+zip")
        response.setBody(buffer)

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