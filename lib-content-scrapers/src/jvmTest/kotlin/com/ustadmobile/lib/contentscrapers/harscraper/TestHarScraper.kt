package com.ustadmobile.lib.contentscrapers.harscraper

import com.google.gson.GsonBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContainerEntryDao
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.abztract.HarScraper
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.io.PrintWriter
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.client.ClientUtil
import net.lightbody.bmp.core.har.Har
import net.lightbody.bmp.core.har.HarContent
import okhttp3.mockwebserver.MockWebServer
import org.apache.commons.io.FileUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import sun.misc.FileURLMapper
import sun.misc.IOUtils
import java.io.File
import java.io.OutputStream
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

    }


    @Test
    fun givenUrl_whenHarScrapes_thenCreatesContainerAndFiles(){

        var url = mockWebServer.url("index.html")

        val entry = ContentEntry()
        entry.leaf = true
        entry.contentEntryUid = db.contentEntryDao.insert(entry)

        val containerFolder = Files.createTempDirectory("harcontainer").toFile()

        var writer = StringWriter()

        var scraper = TestChildHarScraper(containerFolder, db, entry.contentEntryUid)
        var containerManager = scraper.startHarScrape(url.toString()){
            it.har.writeTo(writer)
            true
        }

        var harEntry = containerManager?.getEntry("harcontent")
        var harContent = containerManager?.getInputStream(harEntry!!)?.readBytes()?.toString(UTF_8)

        Assert.assertEquals("har content matches", writer.toString(), harContent)

        val gson = GsonBuilder().disableHtmlEscaping().create()

        var har = gson.fromJson(harContent, Har::class.java)

        har.log.entries.forEach{

            var path = it.response.content.text
            var pathEntry = containerManager?.getEntry(path)
            var contentBytes = containerManager?.getInputStream(pathEntry!!)?.readBytes()

            var originalBytes: ByteArray?
            originalBytes = when {
                path.contains("index.html") -> javaClass.getResourceAsStream(UMFileUtil.joinPaths(RESOURCE_PATH,"index.html")).readBytes()
                path.contains("style.css") -> javaClass.getResourceAsStream(UMFileUtil.joinPaths(RESOURCE_PATH,"style.css")).readBytes()
                else -> byteArrayOf()
            }

            Assert.assertArrayEquals("content for entry $path exists", contentBytes, originalBytes)

        }

    }

    @Test(expected = IllegalArgumentException::class)
    fun givenInvalidUrl_whenHarScrapped_returnsIllegalArgument(){

        var url = "hello world"

        val harFolder = Files.createTempDirectory("harcontent").toFile()

        var proxy = BrowserMobProxyServer()
        proxy.start()

        var seleniumProxy = ClientUtil.createSeleniumProxy(proxy)

        seleniumProxy.noProxy = "<-loopback>"
        var driver = setupProxyWithSelenium(proxy, seleniumProxy,"test")
        scrapeUrlwithHar(proxy, driver, url, harFolder, null, null)

    }

    @Test(expected = IllegalArgumentException::class)
    fun givenUrlLeadingTo404_whenHarScrapped_returnsIllegalArgument(){

        var url = mockWebServer.url("hello world")

        val harFolder = Files.createTempDirectory("harcontent").toFile()

        var proxy = BrowserMobProxyServer()
        proxy.start()

        var seleniumProxy = ClientUtil.createSeleniumProxy(proxy)

        seleniumProxy.noProxy = "<-loopback>"
        var driver = setupProxyWithSelenium(proxy, seleniumProxy,"test")
        scrapeUrlwithHar(proxy, driver, url.toString(), harFolder, null, null)

    }


}
