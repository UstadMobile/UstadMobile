package com.ustadmobile.lib.contentscrapers


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao
import com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING
import com.ustadmobile.lib.contentscrapers.edraakK12.IndexEdraakK12Content
import com.ustadmobile.lib.db.entities.ScrapeRun
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
import java.io.IOException
import java.nio.file.Files


class TestIndexContent {

    //@Before
    fun setup(){
        ContentScraperUtil.checkIfPathsToDriversExist()
    }

    private val MAIN_CONTENT_CONTENT_FILE = "/com/ustadmobile/lib/contentscrapers/edraakK12/edraak-main-content.txt"


    private val DETAIL_JSON_CONTENT_FILE = "/com/ustadmobile/lib/contentscrapers/edraakK12/edraak-detail.txt"

    internal val COMPONENT_API_PREFIX = "/api/component/"

    internal val indexDispatcher: Dispatcher = object : Dispatcher() {
        @Throws(InterruptedException::class)
        override fun dispatch(request: RecordedRequest): MockResponse {

            try {

                if (request.path.contains(MAIN_CONTENT_CONTENT_FILE)) {

                    val prefixLength = COMPONENT_API_PREFIX.length
                    val fileName = request.path.substring(prefixLength,
                            request.path.indexOf(".txt", prefixLength))
                    val body = IOUtils.toString(javaClass.getResourceAsStream("$fileName.txt"), UTF_ENCODING)
                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", UTF_ENCODING.hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(body)

                    return response

                } else if (request.path.contains(DETAIL_JSON_CONTENT_FILE) || request.path.contains("5a60a25f0ed49f0498cb201d")) {

                    val body = IOUtils.toString(javaClass.getResourceAsStream(DETAIL_JSON_CONTENT_FILE), UTF_ENCODING)
                    val response = MockResponse().setResponseCode(200)
                    response.setHeader("ETag", UTF_ENCODING.hashCode())
                    if (!request.method.equals("HEAD", ignoreCase = true))
                        response.setBody(body)

                    return response

                } else if (request.path.contains("/media/")) {

                    val fileLocation = "/com/ustadmobile/lib/contentscrapers/files/" + request.path.substring(7)
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

            } catch (e: IOException) {
                e.printStackTrace()
            }

            return MockResponse().setResponseCode(404)
        }
    }


//    @Test
//    @Throws(IOException::class)
    fun givenServerOnline_whenUrlFound_FindImportedContent() {

        val db = UmAppDatabase.getInstance(Any())
        val repo = db//db.getRepository("https://localhost", "")
        val runDao = db.scrapeRunDao
        val run = ScrapeRun()
        run.scrapeRunUid = 943
        run.scrapeType = "Edraak-Test"
        //run.status = ScrapeQueueItemDao.STATUS_PENDING
        runDao.insert(run)

        val mockWebServer = MockWebServer()
        mockWebServer.setDispatcher(indexDispatcher)

        val tmpDir = Files.createTempDirectory("testedxcontentindexscraper").toFile()
        val containerDir = Files.createTempDirectory("container").toFile()

        IndexEdraakK12Content().startScrape(mockWebServer.url("/api/component/$MAIN_CONTENT_CONTENT_FILE").toString(), tmpDir, containerDir, 943)

        val contentEntryDao = repo.contentEntryDao
        val parentChildDaoJoin = repo.contentEntryParentChildJoinDao

        val parentEntry = contentEntryDao.findBySourceUrl("https://www.edraak.org/k12/")

        Assert.assertEquals(true, parentEntry!!.entryId!!.equals("https://www.edraak.org/k12/", ignoreCase = true))

        val childEntry = contentEntryDao.findBySourceUrl("5a608815f3a50d049abf68e9")

        Assert.assertEquals(true, childEntry!!.entryId!!.equals("5a608815f3a50d049abf68e9", ignoreCase = true))

        val parentChildJoinEntry = parentChildDaoJoin.findParentByChildUuids(childEntry!!.contentEntryUid)

        Assert.assertEquals(true, parentChildJoinEntry!!.cepcjParentContentEntryUid == parentEntry!!.contentEntryUid)

        val courseEntry = contentEntryDao.findBySourceUrl("5a60a25f0ed49f0498cb201d")

        Assert.assertEquals(true, courseEntry!!.entryId!!.equals("5a60a25f0ed49f0498cb201d", ignoreCase = true))


    }


}
