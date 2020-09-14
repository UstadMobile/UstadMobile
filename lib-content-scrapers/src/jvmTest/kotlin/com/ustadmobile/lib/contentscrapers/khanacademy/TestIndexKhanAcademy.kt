package com.ustadmobile.lib.contentscrapers.khanacademy

import com.google.gson.GsonBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.lib.contentscrapers.ContentScraperUtil
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.db.entities.ScrapeRun
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.Okio
import org.apache.commons.io.IOUtils
import org.apache.commons.pool2.impl.GenericObjectPool
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.chrome.ChromeDriver
import java.io.File
import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files

@ExperimentalStdlibApi
class TestIndexKhanAcademy {

    internal val dispatcher: Dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {

            try {

                if (request.path.contains("json")) {

                    val fileName = request.path.substring(5)
                    val body = IOUtils.toString(javaClass.getResourceAsStream(fileName), UTF_ENCODING)
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
                    response.setHeader("ETag", (buffer.size().toString() + "ABC").hashCode())
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
    private var mockWebServer: MockWebServer? = null
    private var driver: ChromeDriver? = null
    private var factory: GenericObjectPool<ChromeDriver>? = null

    @Before
    @Throws(Exception::class)
    fun setUpDriver() {
        ContentScraperUtil.checkIfPathsToDriversExist()
        mockWebServer = MockWebServer()
        mockWebServer!!.setDispatcher(dispatcher)

        factory = GenericObjectPool(KhanDriverFactory())
        driver = factory!!.borrowObject()
    }

    @After
    fun closeDriver() {
        driver!!.close()
        driver!!.quit()
        factory!!.close()
    }


    @Test
    @Throws(IOException::class)
    fun givenServerOnline_whenKhanContentScraped_thenShouldConvertAndDownloadAllFiles() {

        val db = UmAppDatabase.getInstance(Any())
        db.clearAllTables()
        val repo = db //db.getRepository("https://localhost", "")
        val runDao = db.scrapeRunDao
        val run = ScrapeRun()
        run.scrapeRunUid = 999
        run.scrapeType = "Khan-Test"
        run.status = ScrapeQueueItemDao.STATUS_PENDING
        runDao.insert(run)

        val tmpDir = Files.createTempDirectory("testIndexKhancontentscraper").toFile()
        val containerDir = Files.createTempDirectory("testContainer").toFile()

        KhanContentIndexer.startScrape(mockWebServer!!.url("/json/com/ustadmobile/lib/contentscrapers/khanacademy/mainpage.txt").toString(),
                tmpDir, containerDir, run.scrapeRunUid)

        val englishFolder = File(tmpDir, "en")
        Assert.assertEquals(true, englishFolder.isDirectory)

        val courseFolder = File(englishFolder, "x9b4a5e7a")
        Assert.assertEquals(true, courseFolder.isDirectory)

        val contentEntryDao = repo.contentEntryDao
        val parentChildDaoJoin = repo.contentEntryParentChildJoinDao

        val parentEntry = contentEntryDao.findBySourceUrl("https://www.khanacademy.org/")
        Assert.assertEquals("Main parent content entry exsits", true, parentEntry!!.entryId!!.equals("https://www.khanacademy.org/", ignoreCase = true))

        val subjectEntry = contentEntryDao.findBySourceUrl(mockWebServer!!.url("/json/com/ustadmobile/lib/contentscrapers/khanacademy/topicspage.txt").toString())
        val parentChildJoinEntry = parentChildDaoJoin.findParentByChildUuids(subjectEntry!!.contentEntryUid)
        Assert.assertEquals(true, parentChildJoinEntry!!.cepcjParentContentEntryUid == parentEntry!!.contentEntryUid)

        val gradeEntry = contentEntryDao.findBySourceUrl(mockWebServer!!.url("/json/com/ustadmobile/lib/contentscrapers/khanacademy/subjectpage.txt").toString())
        val gradeSubjectJoin = parentChildDaoJoin.findParentByChildUuids(gradeEntry!!.contentEntryUid)
        Assert.assertEquals(true, gradeSubjectJoin!!.cepcjParentContentEntryUid == subjectEntry!!.contentEntryUid)

        val headingTopicEntry = contentEntryDao.findBySourceUrl(mockWebServer!!.url("/json/com/ustadmobile/lib/contentscrapers/khanacademy/coursespage.txt").toString())
        val subjectHeadingJoin = parentChildDaoJoin.findParentByChildUuids(headingTopicEntry!!.contentEntryUid)
        Assert.assertEquals(true, subjectHeadingJoin!!.cepcjParentContentEntryUid == gradeEntry!!.contentEntryUid)

    }

    @Test
    @Throws(IOException::class)
    fun givenKhanAcademyChangedTheSourceOnEachPage_whenDifferent_findTheCorrectJson() {

        val gson = GsonBuilder().disableHtmlEscaping().create()
        val json = KhanContentIndexer.getJsonStringFromScript(mockWebServer!!.url("/json/com/ustadmobile/lib/contentscrapers/khanacademy/videopage.txt").toString())

        var response: SubjectListResponse? = gson.fromJson(json, SubjectListResponse::class.java)
        if (response!!.componentProps == null) {
            response = gson.fromJson(json, PropsSubjectResponse::class.java).props
        }

        Assert.assertNotNull("Got the right content", response!!.componentProps)

    }


}
