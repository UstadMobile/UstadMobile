package com.ustadmobile.sharedse.network

import com.ustadmobile.port.sharedse.impl.http.StaticFileDirResponder
import com.ustadmobile.sharedse.util.ReverseProxyDispatcher
import fi.iki.elonen.router.RouterNanoHTTPD
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

// proxy - https://gist.github.com/paour/bf58afa8969640e36e9bd87f85a6c5df
class ResumableDownload2Test {

    lateinit var server : RouterNanoHTTPD

    lateinit var mockServer: MockWebServer

    lateinit var baseDir: File

    val tmpFilesToDelete = mutableListOf<File>()

    lateinit var mockDispatcher : ReverseProxyDispatcher

    @Before
    fun startServer() {
        baseDir = File(System.getProperty("user.dir"), "src/jvmTest/resources/http")
        val testFile = File(baseDir, "http/top_header_bg.jpg")

        server = RouterNanoHTTPD(0)
        server.addRoute("/static/(.*)", StaticFileDirResponder::class.java, baseDir)
        server.start()

        mockServer = MockWebServer()
        mockDispatcher = ReverseProxyDispatcher(
                HttpUrl.parse("http://localhost:${server.listeningPort}/")!!)
        mockServer.setDispatcher(mockDispatcher)
        mockServer.start()

//        println("NanoHTTPD running on ${server.listeningPort}")
//        Thread.sleep(5 * 60 * 1000)
//        println("continuing")
    }

    @After
    fun deleteTmpFile() {
        tmpFilesToDelete.forEach { it.delete() }
        tmpFilesToDelete.clear()
    }

    @After
    fun stopServers(){
        server.stop()
        mockServer.shutdown()
    }

    @Test
    fun `GIVEN serer running normally WHEN download runs SHOULD download successfully and file content should match`() {
        runBlocking {
            val port = server.listeningPort
            val downloadDstFile = File.createTempFile("resumabledl2test", "tmp")
            tmpFilesToDelete.add(downloadDstFile)
            val downloader = ResumableDownload2(mockServer.url("/static/top_header_bg.jpg").toString(),
                    downloadDstFile.absolutePath)
            downloader.download()
            val testFile = File(baseDir, "top_header_bg.jpg")
            Assert.assertTrue("Downloaded file exists", downloadDstFile.exists())
            Assert.assertEquals("Downloaded file is the same size", testFile.length(),
                    downloadDstFile.length())
            Assert.assertArrayEquals("File content is the same",
                    testFile.readBytes(), downloadDstFile.readBytes())
        }
    }

    @Test
    fun `GIVEN server that disconnects repeatedly WHEN download runs SHOULD fail after retry count is exceeded`() {
        runBlocking {
            mockDispatcher.numTimesToFail.set(7) // each second attempt runs two get requests - HEAD and GEt

            val downloadDstFile = File.createTempFile("resumabledl2test", "tmp")
            tmpFilesToDelete.add(downloadDstFile)
            val downloader = ResumableDownload2(mockServer.url("/static/top_header_bg.jpg").toString(),
                    downloadDstFile.absolutePath, retryDelay = 100)
            val successful = downloader.download()

            Assert.assertFalse(successful)

        }
    }


    @Test
    fun `GIVEN server disconnected during download WHEN download runs SHOULD resume download, return true and file content should be the same`() {
        runBlocking {
            mockDispatcher.numTimesToFail.set(1)

            val downloadDstFile = File.createTempFile("resumabledl2test", "tmp")
            tmpFilesToDelete.add(downloadDstFile)
            val downloader = ResumableDownload2(mockServer.url("/static/top_header_bg.jpg").toString(),
                    downloadDstFile.absolutePath, retryDelay = 100)
            val successful = downloader.download()

            val testFile = File(baseDir, "top_header_bg.jpg")
            Assert.assertTrue("Downloaded file exists", downloadDstFile.exists())
            Assert.assertEquals("Downloaded file is the same size", testFile.length(),
                    downloadDstFile.length())
            Assert.assertArrayEquals("File content is the same",
                    testFile.readBytes(), downloadDstFile.readBytes())

            val firstRequest = mockServer.takeRequest(15, TimeUnit.SECONDS)
            val resumeHeadRequest = mockServer.takeRequest(15, TimeUnit.SECONDS)
            val resumeGetRequest = mockServer.takeRequest(15, TimeUnit.SECONDS)

            Assert.assertTrue("After first failure, downloader makes a head request to validate",
                    resumeHeadRequest.method.equals("HEAD", ignoreCase = true))

            Assert.assertTrue("After head request to validate, downloader makes a get request",
                    resumeGetRequest.method.equals("GET", ignoreCase = true))

            Assert.assertTrue("Second get request contains a Range header",
                    resumeGetRequest.headers["Range"] != null)

            Assert.assertTrue("Download was successful after completing attempts", successful)

            Assert.assertTrue("Second download was a partial download",
                    resumeGetRequest.bodySize < downloadDstFile.length())
        }
    }

    @Test
    fun `GIVEN file does not exist WHEN download runs SHOULD fail`() {
        runBlocking {
            val downloadDstFile = File.createTempFile("resumabledl2test", "tmp")
            val downloader = ResumableDownload2(mockServer.url("/static/doesnotexist.jpg").toString(),
                    downloadDstFile.absolutePath, retryDelay = 100)
            val successful = downloader.download()
            Assert.assertFalse(successful)
        }
    }

    @Test
    fun `GIVEN server offline WHEN download runs SHOULD fail`() {
        runBlocking {
            mockServer.shutdown()

            val downloadDstFile = File.createTempFile("resumabledl2test", "tmp")
            tmpFilesToDelete.add(downloadDstFile)
            val downloader = ResumableDownload2(mockServer.url("/static/top_header_bg.jpg").toString(),
                    downloadDstFile.absolutePath, retryDelay = 100)

            val successful = downloader.download()

            Assert.assertFalse("When server is offline, retry count is exceeded and download is marked as a fail",
                    successful)
        }
    }


}