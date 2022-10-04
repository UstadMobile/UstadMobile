package com.ustadmobile.lib.contentscrapers

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.contentscrapers.ck12.CK12ContentScraper

import org.apache.commons.io.IOUtils
import org.junit.Test

import java.io.File
import java.io.IOException

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer

import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import okio.buffer
import okio.source
import org.junit.Before
import java.net.URL
import kotlin.random.Random


class TestCK12ContentScraper {
    private lateinit var db: UmAppDatabase

    private lateinit var nodeIdAndAuth: NodeIdAndAuth

    private val PRACTICE_JSON = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-practice.txt"
    private val TEST_JSON = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-test.txt"
    private val QUESTION_JSON = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-question-1.txt"
    private val ANSWER_JSON = "/com/ustadmobile/lib/contentscrapers/ck12/answer.txt"
    private val VIDEO_LOCATION_FILE = "/com/ustadmobile/lib/contentscrapers/files/video.mp4"
    private val RESOURCE_PATH = "/com/ustadmobile/lib/contentscrapers/files/"
    private val LAST_MODIFIED_FILE = "/com/ustadmobile/lib/contentscrapers/ck12/plix/last-modified.txt"


    private val SLIDESHARE_HTML = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-slideshare.txt"
    private val VIDEO_YT_HTML = "/com/ustadmobile/lib/contentscrapers/ck12/ck-12-video-yt.txt"
    private val CK_VID_HTML = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-video-genie.txt"
    private val READ_HTML = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-read.txt"
    private val MATH_JAX_HTML = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-read-mathjax.txt"

    private val PLIX_HTML = "/com/ustadmobile/lib/contentscrapers/ck12/plix/plix.txt"
    private val PLIX_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/plix/"


    internal var youtubeUrl = "https://www.ck12.org/c/biology/history-of-life/lecture/Origin-and-Evolution-of-Life/?referrer=concept_details"
    internal var ckVidUrl = "https://www.ck12.org/c/elementary-math-grade-1/add-to-10-with-images/enrichment/Overview-of-Addition-Sums-to-10/?referrer=concept_details"
    internal var slideShareUrl = "https://www.ck12.org/c/earth-science/observations-and-experiments/lecture/Inference-and-Observation-Activity/?referrer=concept_details"
    internal var mathJaxUrl = "https://www.ck12.org/c/geometry/midpoints-and-segment-bisectors/lesson/Midpoints-and-Segment-Bisectors-BSC-GEOM/?referrer=concept_details"
    internal var practiceUrl = "https://www.ck12.org/c/elementary-math-grade-1/add-to-10-with-images/asmtpractice/Add-to-10-with-Images-Practice?referrer=featured_content&collectionHandle=elementary-math-grade-1&collectionCreatorID=3&conceptCollectionHandle=elementary-math-grade-1-::-add-to-10-with-images?referrer=concept_details"
    internal var readUrl = "https://www.ck12.org/c/physical-science/chemistry-of-compounds/lesson/Chemistry-of-Compounds-MS-PS/?referrer=concept_details"
    internal var plixUrl = "https://www.ck12.org/c/trigonometry/distance-formula-and-the-pythagorean-theorem/plix/Pythagorean-Theorem-to-Determine-Distance-Tree-Shadows-53d147578e0e0876d4df82f1?referrer=concept_details"

    @Before
    fun setup() {
        ContentScraperUtil.checkIfPathsToDriversExist()
        nodeIdAndAuth = NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())
        db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite:build/tmp/UmAppDatabase.sqlite")
            .addSyncCallback(nodeIdAndAuth)
            .build()
            .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
    }


    internal val dispatcher: Dispatcher = object : Dispatcher() {
        @Throws(InterruptedException::class)
        override fun dispatch(request: RecordedRequest): MockResponse {

            try {
                val requestPath = request.path ?: ""

                if (requestPath.startsWith(COMPONENT_API_PREFIX)) {

                    val prefixLength = COMPONENT_API_PREFIX.length
                    val fileName = requestPath.substring(prefixLength,
                        requestPath.indexOf(".txt", prefixLength))
                    val body = IOUtils.toString(javaClass.getResourceAsStream("$fileName.txt"), UTF_ENCODING)
                    return MockResponse().setBody(body)

                } else if (requestPath == "/media/video.mp4") {
                    val videoIn = javaClass.getResourceAsStream(VIDEO_LOCATION_FILE)
                    val source = videoIn.source().buffer()
                    val buffer = Buffer()
                    source.readAll(buffer)

                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", (buffer.size.toString() + VIDEO_LOCATION_FILE).hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(buffer)

                    return response
                } else if (requestPath.contains("picture")) {
                    val length = "/media/".length
                    val fileName = requestPath.substring(length,
                        requestPath.indexOf(".png", length))
                    val pictureIn = javaClass.getResourceAsStream("$RESOURCE_PATH$fileName.png")
                    val source = pictureIn.source().buffer()
                    val buffer = Buffer()
                    source.readAll(buffer)
                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", (buffer.size.toString() + RESOURCE_PATH).hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(buffer)

                    return response

                } else if (requestPath.contains("/plix/")) {
                    val length = "/plix/".length
                    val fileName = requestPath.substring(length)
                    val pictureIn = javaClass.getResourceAsStream(PLIX_PATH + fileName)
                    val source = pictureIn.source().buffer()
                    val buffer = Buffer()
                    source.readAll(buffer)
                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", (buffer.size.toString() + RESOURCE_PATH).hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(buffer)

                    return response
                } else if (requestPath.contains("json")) {

                    val start = requestPath.indexOf("/", requestPath.indexOf("/") + 1)
                    val fileName = requestPath.substring(start)
                    val body = IOUtils.toString(javaClass.getResourceAsStream(fileName), UTF_ENCODING)
                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", UTF_ENCODING.hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(body)

                    return response

                }

            } catch (e: IOException) {
                e.printStackTrace()
            }

            return MockResponse().setBody("")
        }
    }


    /*  @Test
      @Throws(IOException::class)
      fun givenServerOnline_whenVideoContentScraped_thenShouldConvertAndDownload() {

          val mockWebServer = MockWebServer()
          mockWebServer.setDispatcher(dispatcher)
          val tmpDir = Files.createTempDirectory("testCK12contentscraper").toFile()

          val scraper = CK12ContentScraper(mockWebServer.url("/c/$CK_VID_HTML").toString(), tmpDir)
          scraper.scrapeVideoContent()

          val folderName = FilenameUtils.getBaseName(CK_VID_HTML)
          val folder = File(tmpDir, folderName)
          folder.mkdirs()

          val file = File(folder, "index.html")
          Assert.assertEquals("Html for video content", true, ContentScraperUtil.fileHasContent(file))

          val asset = File(folder, "asset")
          Assert.assertEquals("asset folder created", true, asset.isDirectory)

          val thumbnail = File(asset, "$folderName-video-thumbnail.jpg")
          Assert.assertEquals("thumbnail for content", true, ContentScraperUtil.fileHasContent(thumbnail))

          val video = File(asset, "media_video.webm")
          Assert.assertEquals("video for content", true, ContentScraperUtil.fileHasContent(video))
      }

      @Test
      @Throws(IOException::class)
      fun givenServerOnline_whenSlideShareVideoContentScraped_thenMp4FileShouldNotExist() {

          val tmpDir = Files.createTempDirectory("testCK12contentscraper").toFile()
          val mockWebServer = MockWebServer()
          mockWebServer.setDispatcher(dispatcher)

          val scraper = CK12ContentScraper(mockWebServer.url("/c/$SLIDESHARE_HTML").toString(), tmpDir)
          scraper.scrapeVideoContent()

          val folderName = FilenameUtils.getBaseName(SLIDESHARE_HTML)
          val folder = File(tmpDir, folderName)
          folder.mkdirs()

          val file = File(folder, "index.html")
          Assert.assertEquals("Html for video content", true, ContentScraperUtil.fileHasContent(file))

          val asset = File(folder, "asset")
          Assert.assertEquals("asset folder created", true, asset.isDirectory)

          val thumbnail = File(asset, "$folderName-video-thumbnail.jpg")
          Assert.assertEquals("thumbnail for content", true, ContentScraperUtil.fileHasContent(thumbnail))

          val video = File(asset, "_media_video.mp4")
          Assert.assertEquals("video for content", false, ContentScraperUtil.fileHasContent(video))
      }

      @Test
      @Throws(IOException::class)
      fun givenServerOnline_whenYoutubeVideoContentScraped_thenMp4FileShouldNotExist() {

          val tmpDir = Files.createTempDirectory("testCK12contentscraper").toFile()
          val mockWebServer = MockWebServer()
          mockWebServer.setDispatcher(dispatcher)

          val scraper = CK12ContentScraper(mockWebServer.url("/c/$VIDEO_YT_HTML").toString(), tmpDir)
          scraper.scrapeVideoContent()

          val folderName = FilenameUtils.getBaseName(VIDEO_YT_HTML)
          val folder = File(tmpDir, folderName)
          folder.mkdirs()

          val file = File(folder, "index.html")
          Assert.assertEquals("Html for video content", true, ContentScraperUtil.fileHasContent(file))

          val asset = File(folder, "asset")
          Assert.assertEquals("asset folder created", true, asset.isDirectory)

          val thumbnail = File(asset, "$folderName-video-thumbnail.jpg")
          Assert.assertEquals("thumbnail for content", true, ContentScraperUtil.fileHasContent(thumbnail))

          val video = File(asset, "_media_video.mp4")
          Assert.assertEquals("video for content", false, ContentScraperUtil.fileHasContent(video))
      }


      @Test
      @Throws(IOException::class)
      fun givenServerOnline_whenScrapeReadContentScraped_thenShouldConvertAndDownload() {
          val tmpDir = Files.createTempDirectory("testCK12readcontentscraper").toFile()

          val mockWebServer = MockWebServer()
          mockWebServer.setDispatcher(dispatcher)

          val scraper = CK12ContentScraper(mockWebServer.url("/c/$MATH_JAX_HTML").toString(), tmpDir)
          scraper.scrapeReadContent()

          val folderName = FilenameUtils.getBaseName(MATH_JAX_HTML)
          val folder = File(tmpDir, folderName)
          folder.mkdirs()

          val index = File(folder, "index.html")
          Assert.assertEquals("index html to display content", true, ContentScraperUtil.fileHasContent(index))
      }


      @Test
      @Throws(IOException::class)
      fun givenServerOnline_whenReadContentScraped_thenShouldConvertAndDownload() {
          val tmpDir = Files.createTempDirectory("testCK12readcontentscraper").toFile()

          val mockWebServer = MockWebServer()
          mockWebServer.setDispatcher(dispatcher)

          val scraper = CK12ContentScraper(mockWebServer.url("/c/$READ_HTML").toString(), tmpDir)
          scraper.scrapeReadContent()

          val folderName = FilenameUtils.getBaseName(READ_HTML)
          val folder = File(tmpDir, folderName)
          folder.mkdirs()

          val index = File(folder, "index.html")
          Assert.assertEquals("index html to display content", true, ContentScraperUtil.fileHasContent(index))

      }

      @Test
      @Throws(IOException::class)
      fun givenServerOnline_whenPracticeContentScraped_thenShouldConvertAndDownload() {
          val mockWebServer = MockWebServer()
          mockWebServer.setDispatcher(dispatcher)
          val tmpDir = Files.createTempDirectory("testCK12practicecontentscraper").toFile()

          val scraper = spy(CK12ContentScraper(mockWebServer.url("/c/ck12-practice?").toString(), tmpDir))
          doReturn(mockWebServer.url("/c/$PRACTICE_JSON").toString()).`when`(scraper).generatePracticeLink(Mockito.anyString())
          doReturn(mockWebServer.url("/c/$TEST_JSON").toString()).`when`(scraper).generateTestUrl(Mockito.anyString())
          doReturn(mockWebServer.url("/c/$QUESTION_JSON").toString()).`when`(scraper).generateQuestionUrl(
                  Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())
          doReturn(IOUtils.toString(javaClass.getResourceAsStream(ANSWER_JSON), UTF_ENCODING)).`when`(scraper).extractAnswerFromEncryption(Mockito.anyString())


          scraper.scrapePracticeContent()
          val practiceFolder = File(tmpDir, "ck12-practice")
          Assert.assertEquals("directory for practice exists", true, practiceFolder.isDirectory)

          val questions = File(practiceFolder, "questions.json")
          Assert.assertEquals("download questions json exists", true, ContentScraperUtil.fileHasContent(questions))

          val index = File(practiceFolder, "index.html")
          Assert.assertEquals("index html to display questions", true, ContentScraperUtil.fileHasContent(index))

      }

      @Test
      fun testRhino() {
          val scriptEngineReader = ScriptEngineReader()
          val result = scriptEngineReader.getResult("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")
          Assert.assertEquals("result matches answer json response", result, "{\"answer\": [\"<img width=\\\"248\\\" height=\\\"64\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/35.jpg\\\" alt=\\\"\\\" />\"], \"instance\": {\"multiAnswers\": false, \"solution\": \"<p>\\n  There are 4 yellow trees and 3 green trees. There are 7 trees altogether.\\n</p>\\n<p>\\n  4 + 3 = 7\\n</p>\", \"stem\": {\"displayText\": \"<p>\\n  Which picture shows 4 + 3 = 7?\\n</p>\"}, \"responseObjects\": [{\"optionKey\": \"<img width=\\\"254\\\" height=\\\"64\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/34.jpg\\\" alt=\\\"\\\" />\", \"isCorrect\": \"F\", \"displayText\": \"<img width=\\\"254\\\" height=\\\"64\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/34.jpg\\\" alt=\\\"\\\" />\", \"orderText\": \"a\", \"displayOrder\": 1}, {\"optionKey\": \"<img width=\\\"247\\\" height=\\\"73\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/37.jpg\\\" alt=\\\"\\\" />\", \"isCorrect\": \"F\", \"displayText\": \"<img width=\\\"247\\\" height=\\\"73\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/37.jpg\\\" alt=\\\"\\\" />\", \"orderText\": \"b\", \"displayOrder\": 2}, {\"optionKey\": \"<img width=\\\"248\\\" height=\\\"64\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/35.jpg\\\" alt=\\\"\\\" />\", \"isCorrect\": \"T\", \"displayText\": \"<img width=\\\"248\\\" height=\\\"64\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/35.jpg\\\" alt=\\\"\\\" />\", \"orderText\": \"c\", \"displayOrder\": 3}, {\"optionKey\": \"<img width=\\\"258\\\" height=\\\"77\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/36.jpg\\\" alt=\\\"\\\" />\", \"isCorrect\": \"F\", \"displayText\": \"<img width=\\\"258\\\" height=\\\"77\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/36.jpg\\\" alt=\\\"\\\" />\", \"orderText\": \"d\", \"displayOrder\": 4}], \"answer\": [\"<img width=\\\"248\\\" height=\\\"64\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/35.jpg\\\" alt=\\\"\\\" />\"], \"hints\": [\"<iframe width=\\\"95%\\\" height=\\\"95%\\\" frameborder=\\\"0\\\" src=\\\"https://braingenie.ck12.org/skills/102916/video_only\\\" style=\\\"border:none\\\"><p>Your browser does not support iframes.</p> </iframe>\"]}, \"allowVariants\": false, \"questionTypeName\": \"multiple-choice\", \"answered\": false}")
      }

      @Test
      @Throws(IOException::class)
      fun givenServerOnline_whenPlixContentScraped_thenShouldConvertAndDownload() {

          val mockWebServer = MockWebServer()
          mockWebServer.setDispatcher(dispatcher)

          val tmpDir = Files.createTempDirectory("testCK12plixcontentscraper").toFile()

          val scraper = spy(CK12ContentScraper(mockWebServer.url("/c/$PLIX_HTML-53d147578e0e0876d4df82f1?").toString(), tmpDir))
          doReturn(mockWebServer.url("/c/$LAST_MODIFIED_FILE").toString()).`when`(scraper).generatePlixLink(Mockito.anyString())
          scraper.scrapePlixContent()

          val plixFolder = File(tmpDir, "plix")
          Assert.assertEquals("directory for plix exists", true, plixFolder.isDirectory)

          val indexJson = File(plixFolder, "index.json")
          Assert.assertEquals("index json for all urls and thier path exists", true, ContentScraperUtil.fileHasContent(indexJson))

      }*/

    @Test
    fun test() {
        val entry = ContentEntry()
        entry.contentEntryUid = -102
        entry.leaf = true
        db.contentEntryDao.insert(entry)

        val parent = ContentEntry()
        parent.contentEntryUid = -101
        db.contentEntryDao.insert(parent)

        val plixEntry = ContentEntry()
        plixEntry.leaf = true
        plixEntry.sourceUrl = "https://www.ck12.org/c/arithmetic/add-whole-numbers/plix/Lets-Go-Fishing-564e36599616aa13f790add9"
        plixEntry.contentEntryUid = -103
        db.contentEntryDao.insert(plixEntry)

        var containerFolder = File("/media/samih/LENOVO/content/test-container/")
        var tempDir = File("/media/samih/LENOVO/content/test-ck12/")
        var flexFolder = File("/media/samih/LENOVO/content/test-ck12/flex2/")
        var plixFolder = File("/media/samih/LENOVO/content/test-ck12/plix2/")

        var parentChildJoin = ContentEntryParentChildJoin()
        parentChildJoin.cepcjParentContentEntryUid = parent.contentEntryUid
        parentChildJoin.cepcjChildContentEntryUid = entry.contentEntryUid
        db.contentEntryParentChildJoinDao.insert(parentChildJoin)

        var videoChildJoin = ContentEntryParentChildJoin()
        videoChildJoin.cepcjParentContentEntryUid = parent.contentEntryUid
        videoChildJoin.cepcjChildContentEntryUid = plixEntry.contentEntryUid
        db.contentEntryParentChildJoinDao.insert(videoChildJoin)

        CK12ContentScraper(
                URL("https://flexbooks.ck12.org/cbook/ck-12-interactive-middle-school-math-6-for-ccss/section/1.2/primary/lesson/pictures-of-ratios-msm6-ccss"), tempDir, containerFolder, entry, "flex", 0)
                .scrapeFlexBookContent(flexFolder)

        ContentScraperUtil.insertContainer(db.containerDao, entry, true, "application/webchunk+zip", flexFolder.lastModified(), flexFolder, db, db, containerFolder)

        CK12ContentScraper(URL("https://www.ck12.org/c/arithmetic/add-whole-numbers/plix/Lets-Go-Fishing-564e36599616aa13f790add9?"), tempDir, containerFolder, plixEntry, "plix", 0)
                .scrapePlixContent(plixFolder)

        ContentScraperUtil.insertContainer(db.containerDao, plixEntry, true, "application/webchunk+zip", plixFolder.lastModified(), plixFolder, db, db, containerFolder)
    }

    @Test
    fun filename(){

        var filename = ContentScraperUtil.getFileNameFromUrl(URL("https://www.ck12.org/flx/show/interactive/user%3Ay2sxmnf1zxn0aw9uc0bjazeylm9yzw../http%3A//www.ck12.org/assessment/ui/basicmath/mathWidget.html%3Fqid%3D587c6f3fda2cfe294d852c1a%26hash%3D6a6db9294cfa6bdfa596574bff6fcf60"))

    }


    companion object {

        private val COMPONENT_API_PREFIX = "/c/"
    }


}
