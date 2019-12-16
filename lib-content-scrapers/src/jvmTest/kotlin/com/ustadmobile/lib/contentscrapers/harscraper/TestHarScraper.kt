package com.ustadmobile.lib.contentscrapers.harscraper

import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContainerEntryDao
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.db.entities.ContentEntry
import net.lightbody.bmp.core.har.Har
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.StringWriter
import java.lang.IllegalArgumentException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files

class TestHarScraper {


    private lateinit var db: UmAppDatabase
    private lateinit var mockWebServer: MockWebServer
    private lateinit var dispatcher: ResourceDispatcher
    private lateinit var containerDao: ContainerDao
    private lateinit var containerEntryDao: ContainerEntryDao
    private lateinit var containerEntryFileDao: ContainerEntryFileDao
    private lateinit var containerFolder: File
    private lateinit var entry: ContentEntry


    private val RESOURCE_PATH = "/com/ustadmobile/lib/contentscrapers/harcontent"


    @Before
    fun setup() {
        ContentScraperUtil.checkIfPathsToDriversExist()
        db = UmAppDatabase.getInstance(Any())
        db.clearAllTables()

        containerDao = db.containerDao
        containerEntryDao = db.containerEntryDao
        containerEntryFileDao = db.containerEntryFileDao

        dispatcher = ResourceDispatcher(RESOURCE_PATH)
        mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(dispatcher)

        containerFolder = Files.createTempDirectory("harcontainer").toFile()

        entry = ContentEntry()
        entry.leaf = true
        entry.contentEntryUid = db.contentEntryDao.insert(entry)

    }


    @Test
    fun givenUrl_whenHarScrapes_thenCreatesContainerAndFiles(){

        var url = mockWebServer.url("index.html")

        var writer = StringWriter()

        var scraper = TestChildHarScraper(containerFolder, db, entry.contentEntryUid)
        var containerManager = scraper.startHarScrape(url.toString()){
            true
        }

        scraper.proxy.har.writeTo(writer)

        var harEntry = containerManager?.getEntry("harcontent")
        var harContent = containerManager?.getInputStream(harEntry!!)?.readBytes()?.toString(UTF_8)

        Assert.assertEquals("har content matches", writer.toString(), harContent)

        val gson = GsonBuilder().disableHtmlEscaping().create()

        var har = gson.fromJson(harContent, Har::class.java)

        har.log.entries.forEach{

            if(it.response.content.text == null){
                return@forEach
            }

            var path = it.response.content.text
            var pathEntry = containerManager?.getEntry(path)

            if(pathEntry == null){
                return@forEach
            }
            var contentBytes = containerManager?.getInputStream(pathEntry)?.readBytes()

            var originalBytes: ByteArray?
            originalBytes = when {
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

        var scraper = TestChildHarScraper(containerFolder, db, entry.contentEntryUid)
        scraper.scrapeUrl(url)
        scraper.close()
    }

    @Test(expected = IllegalArgumentException::class)
    fun givenUrlLeadingTo404_whenHarScrapped_returnsIllegalArgument(){

        var url = mockWebServer.url("hello world")

        var scraper = TestChildHarScraper(containerFolder, db, entry.contentEntryUid)
        scraper.scrapeUrl(url.toString())
        scraper.close()
    }

    @Test
    fun givenUrlWithTimestamp_whenHarScrapped_requestDoesNotHaveTimestamp(){

        var url = mockWebServer.url("index.html")

        var writer = StringWriter()

        var regex = "[?&]ts=[0-9]+".toRegex()

        var scraper = TestChildHarScraper(containerFolder, db, entry.contentEntryUid)
        var containerManager = scraper.startHarScrape(url.toString(), regexes = listOf(regex)){
            true
        }

        var harEntry = containerManager?.getEntry("harcontent")
        var harContent = containerManager?.getInputStream(harEntry!!)?.readBytes()?.toString(UTF_8)

        val gson = GsonBuilder().disableHtmlEscaping().create()

        var har = gson.fromJson(harContent, Har::class.java)

        har.log.entries.forEach{

            Assert.assertTrue(!it.request.url.contains(regex))
        }

    }


}
