package com.ustadmobile.lib.contentscrapers

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.checkIfPathsToDriversExist
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ETAG_TXT
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.etekkatho.EtekkathoScraper
import com.ustadmobile.lib.contentscrapers.etekkatho.IndexEtekkathoScraper
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.buffer
import okio.source
import org.apache.commons.io.IOUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.random.Random


class TestEtekScraper {

    internal val dispatcher: Dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            val requestPath = request.path ?: ""
            try {
                if (requestPath.contains("json")) {

                    val fileName: String
                    if (requestPath.contains("?handle")) {
                        fileName = requestPath.substring(5,
                            requestPath.indexOf("?handle"))
                    } else {
                        fileName = requestPath.substring(5)
                    }
                    val body = IOUtils.toString(javaClass.getResourceAsStream(fileName), UTF_ENCODING)
                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", UTF_ENCODING.hashCode())
                    response.addHeader("Content-Type","text/plain")
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(body)

                    return response

                } else if (requestPath.contains("media")) {

                    val fileLocation = requestPath.substring(6)
                    val videoIn = javaClass.getResourceAsStream(fileLocation)
                    val source = videoIn.source().buffer()
                    val buffer = Buffer()
                    source.readAll(buffer)


                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", (buffer.size.toString() + UTF_ENCODING).hashCode())
                    response.addHeader("Content-Type","image/png")
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(buffer)

                    return response
                }

            } catch (e: Exception) {
                e.printStackTrace()
                System.err.println(request.path)
            }

            return MockResponse().setResponseCode(404)
        }
    }

    @Before
    fun clearDb() {
        val nodeIdAndAuth = NodeIdAndAuth(
            Random.nextLong(0, Long.MAX_VALUE),
            randomUuid().toString())
        val db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite:build/tmp/UmAppDatabase.sqlite")
            .addSyncCallback(nodeIdAndAuth)
            .build()
            .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
        checkIfPathsToDriversExist()
    }

    @Test
    @Throws(IOException::class)
    fun givenServerOnline_whenEtekIsScrapedAgain_thenShouldDownloadOnlyOnce() {

        val tmpDir = Files.createTempDirectory("testEtekScraper").toFile()

        val mockWebServer = MockWebServer()
        mockWebServer.dispatcher = dispatcher

        val scraper = EtekkathoScraper(
                mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/etek/lesson.html?handle=123-321").toString(),
                tmpDir)
        scraper.scrapeContent()

        val lessonFolder = File(tmpDir, "123-321")
        Assert.assertEquals(true, lessonFolder.isDirectory)

        val lessonFile = File(lessonFolder, "123-321")
        Assert.assertEquals(true, lessonFile.isFile && lessonFile.exists() && lessonFile.length() > 0)

        lessonFile.delete()

        val eTagFile = File(lessonFolder, "123-321$ETAG_TXT")
        Assert.assertEquals(true, eTagFile.isFile && eTagFile.exists() && eTagFile.length() > 0)
        val modified = eTagFile.lastModified()

        scraper.scrapeContent()

        Assert.assertEquals(modified, eTagFile.lastModified())


    }

    @Test
    @Throws(IOException::class)
    fun givenWhenServerOnline_whenEtekisIndexred_AllDirectoriesAndFilesCorrectlyDownloaded() {

        val tmpDir = Files.createTempDirectory("testEtekIndexScraper").toFile()
        val containerDir = Files.createTempDirectory("container").toFile()

        val mockWebServer = MockWebServer()
        mockWebServer.dispatcher = dispatcher

        val scraper = IndexEtekkathoScraper()
        scraper.findContent(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/etek/etekhomepage.html").toString(),
                tmpDir, containerDir)

        val educationFolder = File(tmpDir, "Education")
        Assert.assertEquals(true, educationFolder.isDirectory)

        val assessmentFolder = File(educationFolder, "Educational assessment")
        Assert.assertEquals(true, assessmentFolder.isDirectory)

        val lessonFolder = File(assessmentFolder, "123-321")
        Assert.assertEquals(true, lessonFolder.isDirectory)

        val eTagFile = File(lessonFolder, "123-321$ETAG_TXT")
        Assert.assertEquals(true, eTagFile.isFile)

        Assert.assertTrue("container has the file", containerDir.listFiles()!!.isNotEmpty())

    }

}
