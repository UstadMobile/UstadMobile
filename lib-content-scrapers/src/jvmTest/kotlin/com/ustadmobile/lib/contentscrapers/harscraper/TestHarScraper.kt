package com.ustadmobile.lib.contentscrapers.harscraper

import com.google.gson.GsonBuilder
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.contentformats.har.HarRegexPair
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContainerEntryDao
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.io.ext.openInputStream
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import net.lightbody.bmp.core.har.Har
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import org.mockito.kotlin.spy
import java.io.File
import java.io.StringWriter
import java.nio.file.Files
import javax.naming.InitialContext
import kotlin.random.Random

class TestHarScraper {


    @Rule
    @JvmField
    val tmpFileRule = TemporaryFolder()

    val tmpDir = Files.createTempDirectory("folder").toFile()
    val containerFolder = Files.createTempDirectory("harcontainer").toFile()

    private lateinit var db: UmAppDatabase
    private lateinit var mockWebServer: MockWebServer
    private lateinit var dispatcher: ResourceDispatcher
    private lateinit var containerDao: ContainerDao
    private lateinit var containerEntryDao: ContainerEntryDao
    private lateinit var containerEntryFileDao: ContainerEntryFileDao
    private lateinit var entry: ContentEntry


    private lateinit var di: DI
    private lateinit var endpointScope: EndpointScope
    private val endpoint = Endpoint(TEST_ENDPOINT)

    private val RESOURCE_PATH = "/com/ustadmobile/lib/contentscrapers/harcontent"

    @Before
    fun setup() {
        ContentScraperUtil.checkIfPathsToDriversExist()
        endpointScope = EndpointScope()

        di = DI {
            bind<NodeIdAndAuth>() with scoped(endpointScope).singleton {
                NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                val nodeIdAndAuth: NodeIdAndAuth = instance()
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(
                    DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
                        "jdbc:sqlite:build/tmp/UmAppDatabase.sqlite")
                    .addSyncCallback(nodeIdAndAuth)
                    .build()
                    .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId))
            }
            bind<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR) with scoped(EndpointScope.Default).singleton {
                containerFolder
            }
            bind<String>(tag = DiTag.TAG_GOOGLE_API) with singleton {
                "abc"
            }
        }

        db = di.on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

        containerDao = db.containerDao
        containerEntryDao = db.containerEntryDao
        containerEntryFileDao = db.containerEntryFileDao

        dispatcher = ResourceDispatcher(RESOURCE_PATH)
        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = dispatcher

        entry = ContentEntry()
        entry.leaf = true
        entry.contentEntryUid = db.contentEntryDao.insert(entry)

    }


    @Test
    fun givenUrl_whenHarScrapes_thenCreatesContainerAndFiles(){

        var url = mockWebServer.url("index.html")

        var writer = StringWriter()

        var scraper = TestChildHarScraper(entry.contentEntryUid, endpoint, di)
        val(isContentUpdated, containerUid) = scraper.startHarScrape(url.toString()){
            true
        }

        scraper.proxy.har.writeTo(writer)

        var harEntry = db.containerEntryDao.findByPathInContainer(containerUid, "harcontent")
        var harContent = harEntry?.containerEntryFile?.openInputStream()?.readString()

        Assert.assertEquals("har content matches", writer.toString(), harContent)

        val gson = GsonBuilder().disableHtmlEscaping().create()

        var har = gson.fromJson(harContent, Har::class.java)

        har.log.entries.forEach{

            if(it.response.content.text == null){
                return@forEach
            }

            var path = it.response.content.text
            var pathEntry: ContainerEntryWithContainerEntryFile? = db.containerEntryDao.findByPathInContainer(containerUid, path)
                    ?: return@forEach

            var contentBytes =  pathEntry?.containerEntryFile?.openInputStream()?.readBytes()

            var originalBytes: ByteArray? = when {
                path.contains("index.html") -> javaClass.getResourceAsStream(UMFileUtil.joinPaths(RESOURCE_PATH,"index.html")).readBytes()
                path.contains("style.css") -> javaClass.getResourceAsStream(UMFileUtil.joinPaths(RESOURCE_PATH,"style.css")).readBytes()
                else -> byteArrayOf()
            }

            Assert.assertArrayEquals("content for entry $path exists",  originalBytes, contentBytes)

        }

        scraper.close()

    }

    @Test(expected = IllegalArgumentException::class)
    fun givenInvalidUrl_whenHarScrapped_returnsIllegalArgument(){

        var url = "hello world"

        var scraper = TestChildHarScraper(entry.contentEntryUid, endpoint, di)
        scraper.scrapeUrl(url)
        scraper.close()
    }

    @Test(expected = IllegalArgumentException::class)
    fun givenUrlLeadingTo404_whenHarScrapped_returnsIllegalArgument(){

        var url = mockWebServer.url("hello world")

        var scraper = TestChildHarScraper(entry.contentEntryUid, endpoint, di)
        scraper.scrapeUrl(url.toString())
        scraper.close()
    }

    @Test
    fun givenUrlWithTimestamp_whenHarScrapped_requestDoesNotHaveTimestamp(){

        var url = mockWebServer.url("index.html")

        var regex = "[?&]ts=[0-9]+".toRegex()

        var scraper = TestChildHarScraper(entry.contentEntryUid, endpoint, di)
        val(isContentUpdated, containerUid)  = scraper.startHarScrape(url.toString(), regexes = listOf(HarRegexPair(regex.toString(),""))){
            true
        }

        var harEntry = db.containerEntryDao.findByPathInContainer(containerUid, "harcontent")
        var harContent = harEntry?.containerEntryFile?.openInputStream()!!.readString()

        val gson = GsonBuilder().disableHtmlEscaping().create()

        var har = gson.fromJson(harContent, Har::class.java)

        var entry = har.log.entries.find {
            it.request.url.contains("pic_trull.jpg")
        }

        Assert.assertEquals("regex was found and removed",  "http://localhost:${url.port}/pic_trull.jpg?style=abc.css", entry!!.request.url)

        scraper.close()

    }

    companion object {

        const val TEST_ENDPOINT = "http://test.localhost.com/"

    }


}
