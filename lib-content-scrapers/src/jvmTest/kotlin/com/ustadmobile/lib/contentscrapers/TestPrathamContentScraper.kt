package com.ustadmobile.lib.contentscrapers

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
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ETAG_TXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.LAST_MODIFIED_TXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.africanbooks.AsbScraper
import com.ustadmobile.lib.contentscrapers.ddl.DdlContentScraper
import com.ustadmobile.lib.contentscrapers.harscraper.TestHarScraper
import com.ustadmobile.lib.contentscrapers.khanacademy.KhanExerciseScraper
import com.ustadmobile.lib.contentscrapers.prathambooks.IndexPrathamContentScraper
import com.ustadmobile.lib.contentscrapers.ytindexer.ChildYoutubeScraper
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.buffer
import okio.source
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.kodein.di.*
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.Files
import javax.naming.InitialContext
import kotlin.random.Random


class TestPrathamContentScraper {


    private lateinit var db: UmAppDatabase

    private lateinit var di: DI
    private lateinit var endpointScope: EndpointScope
    private val endpoint = Endpoint(TestHarScraper.TEST_ENDPOINT)

    val tmpDir = Files.createTempDirectory("folder").toFile()
    val containerDir = Files.createTempDirectory("container").toFile()


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
                spy(DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
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
    }

    internal val dispatcher: Dispatcher = object : Dispatcher() {
        @Throws(InterruptedException::class)
        override fun dispatch(request: RecordedRequest): MockResponse {
            val requestPath = request.path ?: ""
            try {

                if (requestPath.contains("json")) {

                    val fileName = requestPath.substring(5)
                    val body = IOUtils.toString(javaClass.getResourceAsStream(fileName), UTF_ENCODING)
                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", UTF_ENCODING.hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(body)

                    return response

                } else if (requestPath.contains("content")) {

                    val fileLocation = requestPath.substring(8)
                    val videoIn = javaClass.getResourceAsStream(fileLocation)
                    val source = videoIn.source().buffer()
                    val buffer = Buffer()
                    source.readAll(buffer)

                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", (buffer.size.toString() + UTF_ENCODING).hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(buffer)

                    return response
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }

            return MockResponse().setResponseCode(404)
        }
    }


    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun givenServerOnline_whenPrathamSiteScraped_thenShouldFindConvertAndDownloadAllFiles() {

        val tmpDir = Files.createTempDirectory("testindexPrathamcontentscraper").toFile()
        val containerDir = Files.createTempDirectory("container").toFile()

        val mockWebServer = MockWebServer()
        mockWebServer.dispatcher = dispatcher

        val scraper = spy(IndexPrathamContentScraper())
        doReturn(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/pratham/prathamonebook.txt")
            .toUrl()).`when`(scraper).generatePrathamUrl("1")
        doReturn(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/pratham/prathamlist.txt")
            .toUrl()).`when`(scraper).generatePrathamUrl("2")
        doReturn(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/pratham/prathamempty.txt")
            .toUrl()).`when`(scraper).generatePrathamUrl("3")
        doReturn(mockWebServer.url("/content/com/ustadmobile/lib/contentscrapers/pratham/24620-a-book-for-puchku.zip")
            .toUrl()).`when`(scraper).generatePrathamEPubFileUrl(Mockito.anyString())
        doReturn("").`when`(scraper).loginPratham()

        scraper.findContent(tmpDir, containerDir)

        val resourceFolder = File(tmpDir, "24620")
        Assert.assertEquals(true, resourceFolder.isDirectory)

        val contentFile = File(resourceFolder, "24620$ETAG_TXT")
        Assert.assertEquals(true, ContentScraperUtil.fileHasContent(contentFile))


    }

    @Test
    @Throws(IOException::class)
    fun givenServerOnline_whenAsbSiteScraped_thenShouldFindConvertAndDownloadAllFiles() {

        val tmpDir = Files.createTempDirectory("testindexAsbcontentscraper").toFile()
        val containerDir = Files.createTempDirectory("container").toFile()

        val mockWebServer = MockWebServer()
        mockWebServer.dispatcher = dispatcher

        val scraper = spy(AsbScraper())
        doReturn(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/africanbooks/abslist.txt")
            .toUrl()).whenever(scraper).generateURL()
        doReturn(mockWebServer.url("/content/com/ustadmobile/lib/contentscrapers/africanbooks/asb18187.epub")
            .toUrl()).whenever(scraper).generateEPubUrl(any(), Mockito.anyString())
        doReturn(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/africanbooks/abslist.txt")
            .toUrl()).`when`(scraper).generatePublishUrl(any(), Mockito.anyString())
        doReturn(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/africanbooks/abslist.txt")
            .toUrl()).`when`(scraper).generateMakeUrl(any(), Mockito.anyString())
        doReturn(
            mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/africanbooks/asbreader.txt")
                .toUrl()
                .toString()).`when`(scraper).generateReaderUrl(any(), Mockito.anyString())
        doReturn(
            mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/africanbooks/asburl.txt")
                .toUrl()
                .toString()).`when`(scraper).africanStoryBookUrl

        scraper.findContent(tmpDir, containerDir)

        val contentFile = File(tmpDir, "10674$LAST_MODIFIED_TXT")
        Assert.assertEquals(true, ContentScraperUtil.fileHasContent(contentFile))

    }


    @Test
    @Throws(IOException::class)
    fun givenServerOnline_whenDdlEpubScraped_thenShouldConvertAndDownload() {

        val tmpDir = Files.createTempDirectory("testindexDdlontentscraper").toFile()
        val containerDir = Files.createTempDirectory("container").toFile()

        val mockWebServer = MockWebServer()
        mockWebServer.dispatcher = dispatcher

        val scraper = DdlContentScraper(0, 0, 0, endpoint, di)
        scraper.scrapeUrl(mockWebServer.url("json/com/ustadmobile/lib/contentscrapers/ddl/ddlcontent.txt").toString())

        val contentFolder = File(tmpDir, "ddlcontent")
        Assert.assertEquals(true, contentFolder.isDirectory)

        val contentFile = File(contentFolder, "311$ETAG_TXT")
        Assert.assertEquals(true, ContentScraperUtil.fileHasContent(contentFile))

        Assert.assertTrue("container has the file", containerDir.listFiles()!!.size > 0)
    }

    @Test
    fun test() {

        val parentUrl = URL("https://tr.khanacademy.org/math")

        val jsonUrl = URL(parentUrl, "/api/internal/static/content${parentUrl.path}?lang=tr")


        val containerDir = Files.createTempDirectory("container").toFile()

        val entry = ContentEntry()
        entry.sourceUrl = "https://bg.khanacademy.org/math/early-math/cc-early-math-counting-topic/cc-early-math-counting/e/counting-out-1-20-objects"
        entry.contentEntryUid = db.contentEntryDao.insert(entry)

        val scraper = DdlContentScraper(entry.contentEntryUid, 0, 0, endpoint, di)
        scraper.scrapeUrl(entry.sourceUrl!!)

        val child = ChildYoutubeScraper(entry.contentEntryUid, 0, endpoint, di)
        child.scrapeUrl(entry.sourceUrl!!)

        val document = Jsoup.connect("https://ddl.af/fa/resource/9398/")
                .header("X-Requested-With", "XMLHttpRequest").get()

        var sourceUrl = "https://ddl.af/fa/resource/9398/"


        // val khan = KhanArticleScraper(containerDir, db, entry.contentEntryUid, 0)
        val khan = KhanExerciseScraper(entry.contentEntryUid, 0, 0, endpoint, di)
        khan.scrapeUrl(entry.sourceUrl!!)

    }

    @Test
    fun testVideo() {

        val containerDir = Files.createTempDirectory("container").toFile()


        val entry = ContentEntry()
        entry.sourceUrl = "khan-id://x2785ecf7a12bc287"
        entry.contentEntryUid = db.contentEntryDao.insert(entry)

        runBlocking {
            val khan = KhanExerciseScraper(entry.contentEntryUid, 0, 0, endpoint, di)
            khan.scrapeUrl("https://www.khanacademy.org/math/cc-fourth-grade-math/imp-place-value-and-rounding-2/imp-intro-to-place-value/e/place-value-tables")
        }


    }

    @Test
    fun testYoutube() {

        val containerDir = Files.createTempDirectory("container").toFile()

        val entry = ContentEntry()
        entry.sourceUrl = "https://bg.khanacademy.org/math/early-math/cc-early-math-counting-topic/cc-early-math-counting/e/counting-out-1-20-objects"
        entry.contentEntryUid = db.contentEntryDao.insert(entry)

        runBlocking {
            val khan = ChildYoutubeScraper(entry.contentEntryUid, 0, endpoint, di)
            khan.scrapeUrl("https://www.youtube.com/watch?v=SGUJCVVryTU")
        }


    }


}
