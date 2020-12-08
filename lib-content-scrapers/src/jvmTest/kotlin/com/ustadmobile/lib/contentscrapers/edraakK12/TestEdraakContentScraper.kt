package com.ustadmobile.lib.contentscrapers.edraakK12

import com.google.gson.GsonBuilder
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil.checkIfPathsToDriversExist
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ARABIC_FONT_BOLD
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ARABIC_FONT_REGULAR
import com.ustadmobile.lib.contentscrapers.ScraperConstants.CONTENT_JSON
import com.ustadmobile.lib.contentscrapers.ScraperConstants.ComponentType
import com.ustadmobile.lib.contentscrapers.ScraperConstants.INDEX_HTML
import com.ustadmobile.lib.contentscrapers.ScraperConstants.JQUERY_JS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MATERIAL_CSS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.MATERIAL_JS
import com.ustadmobile.lib.contentscrapers.ScraperConstants.QUESTIONS_JSON
import com.ustadmobile.lib.contentscrapers.ScraperConstants.TINCAN_FILENAME
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.ScraperConstants.VIDEO_FILENAME_WEBM
import com.ustadmobile.lib.staging.contentscrapers.edraakK12.EdraakK12ContentScraper
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
import java.nio.charset.Charset
import java.nio.file.Files

@ExperimentalStdlibApi
class TestEdraakContentScraper {


    private val MALFORMED_COMPONENT_ID = "eada"

    private val DETAIL_JSON_CONTENT_FILE = "/com/ustadmobile/lib/contentscrapers/edraakK12/edraak-detail.txt"

    private val MAIN_CONTENT_CONTENT_FILE = "/com/ustadmobile/lib/contentscrapers/edraakK12/edraak-main-content.txt"

    private val MAIN_DETAIL_WITHOUT_TARGET_FILE = "/com/ustadmobile/lib/contentscrapers/edraakK12/edraak-detail-without-target.txt"

    private val MAIN_DETAIL_WITHOUT_CHILDREN_FILE = "/com/ustadmobile/lib/contentscrapers/edraakK12/edraak-detail-without-children.txt"

    private val MAIN_DETAIL_NO_VIDEO_FOUND = "/com/ustadmobile/lib/contentscrapers/edraakK12/edraak-detail-no-video-info.txt"

    private val MAIN_DETAIL_NO_QUESTIONS_FOUND = "/com/ustadmobile/lib/contentscrapers/edraakK12/edraak-detail-no-question-set-children.txt"

    private val VIDEO_LOCATION_FILE = "/com/ustadmobile/lib/contentscrapers/files/video.mp4"

    private val RESOURCE_PATH = "/com/ustadmobile/lib/contentscrapers/files/"

    internal val COMPONENT_API_PREFIX = "/api/component/"

    @Before
    fun setup() {
        checkIfPathsToDriversExist()
    }

    internal val dispatcher: Dispatcher = object : Dispatcher() {
        @Throws(InterruptedException::class)
        override fun dispatch(request: RecordedRequest): MockResponse {


            try {

                if (request.path.startsWith(COMPONENT_API_PREFIX)) {

                    val prefixLength = COMPONENT_API_PREFIX.length
                    val fileName = request.path.substring(prefixLength,
                            request.path.indexOf(".txt", prefixLength))
                    return MockResponse().setBody(IOUtils.toString(javaClass.getResourceAsStream("$fileName.txt"), UTF_ENCODING))

                } else if (request.path == "/media/video.mp4") {
                    val videoIn = javaClass.getResourceAsStream(VIDEO_LOCATION_FILE)
                    val source = Okio.buffer(Okio.source(videoIn))
                    val buffer = Buffer()
                    source.readAll(buffer)
                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", (buffer.size().toString() + VIDEO_LOCATION_FILE).hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.body = buffer

                    return response
                } else if (request.path.contains("picture")) {
                    val length = "/media/".length
                    val fileName = request.path.substring(request.path.indexOf("/media/") + length,
                            request.path.indexOf(".png", length))
                    val pictureIn = javaClass.getResourceAsStream("$RESOURCE_PATH$fileName.png")
                    val source = Okio.buffer(Okio.source(pictureIn))
                    val buffer = Buffer()
                    source.readAll(buffer)
                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", (buffer.size().toString() + RESOURCE_PATH).hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.body = buffer

                    return response
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }

            return MockResponse().setResponseCode(404)
        }
    }

    @Test
    @Throws(IOException::class)
    fun givenServerOnline_whenEdraakContentScraped_thenShouldConvertAndDownload() {

        val tmpDir = Files.createTempDirectory("testedxcontentscraper").toFile()
        val mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(dispatcher)
        val url = EdraakK12ContentScraper.generateUrl(mockWebServer.url("/api/").toString(), DETAIL_JSON_CONTENT_FILE, 41)

        val courseDirectory = File(tmpDir, "5a60a25f0ed49f0498cb201d")
        courseDirectory.mkdirs()

        val scraper = EdraakK12ContentScraper(url, courseDirectory)
        scraper.scrapeContent()

        val jsonFile = File(courseDirectory, CONTENT_JSON)
        Assert.assertTrue("Downloaded content info json exists", ContentScraperUtil.fileHasContent(jsonFile))
        val jsonStr = String(Files.readAllBytes(jsonFile.toPath()), Charset.defaultCharset())
        val gsonContent = GsonBuilder().disableHtmlEscaping().create().fromJson(jsonStr, ContentResponse::class.java)
        Assert.assertNotNull("Created Gson POJO Object", gsonContent)

        Assert.assertTrue("Downloaded Questions json exist", ContentScraperUtil.fileHasContent(File(courseDirectory, QUESTIONS_JSON)))

        val questionSetList = scraper.getQuestionSet(gsonContent)
        Assert.assertNotNull("Has Questions Set", questionSetList)
        Assert.assertTrue("Has more than 1 question", questionSetList?.size ?: 0 > 0)

        val video = File(courseDirectory, VIDEO_FILENAME_WEBM)
        if (ComponentType.ONLINE.type.equals(gsonContent.target_component!!.component_type!!, ignoreCase = true)) {
            Assert.assertEquals("Has Video", true, ContentScraperUtil.fileHasContent(video))
        } else {
            Assert.assertEquals("Should not have video", false, ContentScraperUtil.fileHasContent(video))
        }

        Assert.assertTrue("tincan file exists", ContentScraperUtil.fileHasContent(File(courseDirectory, TINCAN_FILENAME)))
        Assert.assertTrue("index html file exists", ContentScraperUtil.fileHasContent(File(courseDirectory, INDEX_HTML)))
        Assert.assertTrue("jquery file exists", ContentScraperUtil.fileHasContent(File(courseDirectory, JQUERY_JS)))
        Assert.assertTrue("material js file exists", ContentScraperUtil.fileHasContent(File(courseDirectory, MATERIAL_JS)))
        Assert.assertTrue("material css file exists", ContentScraperUtil.fileHasContent(File(courseDirectory, MATERIAL_CSS)))
        Assert.assertTrue("arabic font regular file exists", ContentScraperUtil.fileHasContent(File(courseDirectory, ARABIC_FONT_REGULAR)))
        Assert.assertTrue("arabic font bold file exists", ContentScraperUtil.fileHasContent(File(courseDirectory, ARABIC_FONT_BOLD)))


    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(IOException::class)
    fun givenNotImportedContent_whenEdraakContentScraped_thenShouldThrowIllegalArgumentException() {

        val tmpDir = Files.createTempDirectory("testedxcontentscraper").toFile()
        val mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(dispatcher)
        val url = EdraakK12ContentScraper.generateUrl(mockWebServer.url("/api/").toString(), MAIN_CONTENT_CONTENT_FILE, 41)

        val scraper = EdraakK12ContentScraper(url, tmpDir)
        scraper.scrapeContent()
    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(IOException::class)
    fun givenNullTargetComponent_whenEdraakContentScraped_thenShouldThrowIllegalArgumentException() {

        val tmpDir = Files.createTempDirectory("testedxcontentscraper").toFile()
        val mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(dispatcher)


        val url = EdraakK12ContentScraper.generateUrl(mockWebServer.url("/api/").toString(), MAIN_DETAIL_WITHOUT_TARGET_FILE, 41)
        val scraper = EdraakK12ContentScraper(url, tmpDir)
        scraper.scrapeContent()

    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(IOException::class)
    fun givenNullTargetComponentChildren_whenEdraakContentScraped_thenShouldThrowIllegalArgumentException() {

        val tmpDir = Files.createTempDirectory("testedxcontentscraper").toFile()
        val mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(dispatcher)

        val url = EdraakK12ContentScraper.generateUrl(mockWebServer.url("/api/").toString(), MAIN_DETAIL_WITHOUT_CHILDREN_FILE, 41)
        val scraper = EdraakK12ContentScraper(url, tmpDir)
        scraper.scrapeContent()

    }

    @Test
    @Throws(IOException::class)
    fun givenEncodedVideoListIsEmpty_whenEdraakContentScraped_thenShouldThrowIllegalArgumentException() {

        val tmpDir = Files.createTempDirectory("testedxcontentscraper").toFile()
        val mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(dispatcher)

        val url = EdraakK12ContentScraper.generateUrl(mockWebServer.url("/api/").toString(), MAIN_DETAIL_NO_VIDEO_FOUND, 41)
        val scraper = EdraakK12ContentScraper(url, tmpDir)
        scraper.scrapeContent()

        val folder = File(tmpDir, "5a60ac073d99e104fb62ce12")
        val video = File(folder, "video.mp4")
        Assert.assertEquals(false, video.exists())

    }

    @Test(expected = IllegalArgumentException::class)
    @Throws(IOException::class)
    fun givenMalformedContent_whenEdraakContentScraped_thenShouldThrowIllegalArgumentException() {


        val tmpDir = Files.createTempDirectory("testedxcontentscraper").toFile()

        val mockWebServer = MockWebServer()

        try {
            mockWebServer.enqueue(MockResponse().setBody("{id"))

            mockWebServer.start()

            val url = EdraakK12ContentScraper.generateUrl(mockWebServer.url("/api/").toString(), MALFORMED_COMPONENT_ID, 41)
            val scraper = EdraakK12ContentScraper(url, tmpDir)
            scraper.scrapeContent()

        } finally {
            mockWebServer.close()
        }
    }


    @Test
    @Throws(IOException::class)
    fun givenVideoModified_whenEdraakContentScrapedAgain_thenShouldVideoOnlyAgain() {

        val tmpDir = Files.createTempDirectory("testmodifiededraak").toFile()
        val mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(dispatcher)

        val url = EdraakK12ContentScraper.generateUrl(mockWebServer.url("/api/").toString(), DETAIL_JSON_CONTENT_FILE, 41)
        val scraper = EdraakK12ContentScraper(url, tmpDir)

        scraper.scrapeContent()

        val firstDownloadTime = File(tmpDir, VIDEO_FILENAME_WEBM).lastModified()
        //now run scrapeContent again...
        scraper.scrapeContent()

        val lastModified = File(tmpDir, VIDEO_FILENAME_WEBM).lastModified()
        //Assert that last modified dates are lower than firstDownloadCompleteTime
        Assert.assertEquals("last modified time = firstdownload time", lastModified, firstDownloadTime)

    }

    @Test
    @Throws(IOException::class)
    fun givenQuestionSetModified_whenEdraakContentScrapedAgain_thenShouldDownloadImagesOnlyAgain() {

        val tmpDir = Files.createTempDirectory("testmodifiededraak").toFile()
        val mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(dispatcher)

        val url = EdraakK12ContentScraper.generateUrl(mockWebServer.url("/api/").toString(), DETAIL_JSON_CONTENT_FILE, 41)
        val scraper = EdraakK12ContentScraper(url, tmpDir)

        scraper.scrapeContent()
        val firstDownloadTime = File(tmpDir, QUESTIONS_JSON).lastModified()
        //now run scrapeContent again...

        scraper.scrapeContent()

        val lastModified = File(tmpDir, QUESTIONS_JSON).lastModified()
        //Assert that last modified dates are lower than firstDownloadCompleteTime
        Assert.assertEquals("last modified time = firstdownload time", lastModified, firstDownloadTime)

    }
}
