package com.ustadmobile.lib.contentscrapers

import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.voa.VoaScraper
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.Okio
import org.apache.commons.io.IOUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.nio.file.Files

@ExperimentalStdlibApi
class TestVoaScraper {


    @Before
    fun setup(){
        ContentScraperUtil.checkIfPathsToDriversExist()
    }

    internal val dispatcher: Dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {

            try {

                if (request.path.contains("json")) {

                    val fileName = request.path.substring(5)
                    val body = IOUtils.toString(javaClass.getResourceAsStream(fileName),UTF_ENCODING)
                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag",UTF_ENCODING.hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(body)

                    return response

                } else if (request.path.contains("post")) {

                    val data = IOUtils.toString(request.body.inputStream(), UTF_ENCODING)
                    val body: String
                    if (data.contains("SelectedAnswerId")) {
                        val fileName = "/com/ustadmobile/lib/contentscrapers/voa/quizoneanswer.html"
                        body = IOUtils.toString(javaClass.getResourceAsStream(fileName), UTF_ENCODING)
                    } else {
                        val fileName = "/com/ustadmobile/lib/contentscrapers/voa/quizone.html"
                        body = IOUtils.toString(javaClass.getResourceAsStream(fileName), UTF_ENCODING)
                    }

                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", UTF_ENCODING.hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(body)

                    return response


                } else if (request.path.contains("content")) {

                    val fileLocation = request.path.substring(8)
                    val videoIn = javaClass.getResourceAsStream(fileLocation)
                    val source = Okio.buffer(Okio.source(videoIn))
                    val buffer = Buffer()
                    source.readAll(buffer)

                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", UTF_ENCODING.hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.body = buffer

                    return response

                }

            } catch (e: Exception) {
                e.printStackTrace()
                System.err.println(request.path)
            }

            return MockResponse().setResponseCode(404)
        }
    }


    @Test
    @Throws(IOException::class)
    fun givenServerOnline_whenVoaIsScrapedAgain_thenShouldDownloadOnlyOnce() {

        val tmpDir = Files.createTempDirectory("testVoaScraper").toFile()

        val mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(dispatcher)

        val scraper = VoaScraper(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/voa/audiovoa.html").toString(),
                tmpDir)
        scraper.scrapeContent()

        val firstDownloadTime = File(tmpDir, "audiovoa.zip").lastModified()

        scraper.scrapeContent()

        Assert.assertEquals(firstDownloadTime, File(tmpDir, "audiovoa.zip").lastModified())

    }

    @Test
    @Throws(IOException::class)
    fun givenServerOnline_whenVoaContentWithNoQuizIsScrapedAgain_thenShouldDownloadContentOnlyOnce() {

        val tmpDir = Files.createTempDirectory("testVoaScraper").toFile()

        val mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(dispatcher)

        val scraper = VoaScraper(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/voa/contentnoquiz.html").toString(),
                tmpDir)
        scraper.scrapeContent()

        val firstDownloadTime = File(tmpDir, "contentnoquiz.zip").lastModified()

        scraper.scrapeContent()

        Assert.assertEquals(firstDownloadTime, File(tmpDir, "contentnoquiz.zip").lastModified())

    }

    @Test
    @Throws(IOException::class)
    fun givenServerOnline_whenVoaContentWithQuizIsScrapedAgain_thenShouldDownloadContentOnlyOnce() {

        val tmpDir = Files.createTempDirectory("testVoaScraper").toFile()

        val mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(dispatcher)

        val scraper = VoaScraper(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/voa/testquiz.html").toString(),
                tmpDir)
        scraper.answerUrl = mockWebServer.url("/post/com/ustadmobile/lib/contentscrapers/voa/answer").toString()
        scraper.scrapeContent()

        val firstDownloadTime = File(tmpDir, "testquiz.zip").lastModified()

        scraper.scrapeContent()

        Assert.assertEquals(firstDownloadTime, File(tmpDir, "testquiz.zip").lastModified())

    }


}
