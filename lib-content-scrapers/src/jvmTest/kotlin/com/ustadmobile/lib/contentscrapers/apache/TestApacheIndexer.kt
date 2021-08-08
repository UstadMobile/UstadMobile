package com.ustadmobile.lib.contentscrapers.apache

import org.mockito.kotlin.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.door.ext.clearAllTablesAndResetSync
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.contentscrapers.abztract.UrlScraper
import com.ustadmobile.lib.contentscrapers.folder.TestFolderIndexer
import com.ustadmobile.lib.contentscrapers.globalDisptacher
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.File
import java.nio.file.Files
import javax.naming.InitialContext
import kotlin.random.Random


class TestApacheIndexer {

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
            bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
                NodeIdAndAuth(Random.nextInt(0, Int.MAX_VALUE), randomUuid().toString())
            }

            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                val nodeIdAndAuth: NodeIdAndAuth = instance()
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, "UmAppDatabase")
                    .addSyncCallback(nodeIdAndAuth, true)
                    .build()
                    .clearAllTablesAndResetSync(nodeIdAndAuth.nodeId, true))
            }
            bind<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR) with scoped(EndpointScope.Default).singleton {
                containerDir
            }
            bind<String>(tag = DiTag.TAG_GOOGLE_API) with singleton {
                "abc"
            }
        }

        db = di.on(endpoint).direct.instance(tag = UmAppDatabase.TAG_DB)


        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = globalDisptacher

    }

    @Test
    fun givenApacheFolder_whenIndexed_createEntries() {

        val apacheIndexer = ApacheIndexer(0, 0, 0, 0, endpoint, di)
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

        val scraper = UrlScraper(0, 0, 0, endpoint, di)
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
