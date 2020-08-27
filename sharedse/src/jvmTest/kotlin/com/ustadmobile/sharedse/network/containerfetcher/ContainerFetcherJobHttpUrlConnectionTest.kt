package com.ustadmobile.sharedse.network.containerfetcher

import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.port.sharedse.impl.http.StaticFileDirResponder
import com.ustadmobile.sharedse.io.extractResourceToFile
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.util.test.ReverseProxyDispatcher
import fi.iki.elonen.router.RouterNanoHTTPD
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import java.io.File
import java.nio.file.Files
import com.ustadmobile.sharedse.network.containerfetcher.ContainerDownloaderJobHttpUrlConnection
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.*
import okhttp3.mockwebserver.MockResponse
import okio.Buffer
import okio.Okio
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong


class ContainerDownloaderJobHttpUrlConnectionTest {

    private lateinit var tmpDownloadDir: File

    private lateinit var mockWebServer: MockWebServer

    @JvmField
    @Rule
    var tmpFolder = TemporaryFolder()

    lateinit var httpBaseDir: File

    val tmpFilesToDelete = mutableListOf<File>()

    lateinit var mockServer: MockWebServer

    lateinit var mockDispatcher : ReverseProxyDispatcher

    lateinit var server : RouterNanoHTTPD

    private lateinit var networkManager: NetworkManagerBle

    private lateinit var di: DI

    @Before
    fun setup() {
        networkManager = mock()
        di = DI {
            bind<NetworkManagerBle>() with singleton { networkManager }
        }

        httpBaseDir = Files.createTempDirectory("ContainerDownloadJobUrlConnectionTest").toFile()
        tmpDownloadDir = Files.createTempDirectory("ContainerDownloadJobUrlConnectionTestDownloadDir").toFile()
        tmpFilesToDelete += httpBaseDir
        tmpFilesToDelete += tmpDownloadDir
        val indexResourcePath = "/http/index.html"
        val indexResource = Any::class.java.getResource(indexResourcePath)
        println(indexResource)
        runBlocking {
            extractResourceToFile(indexResourcePath,
                    File(httpBaseDir, "index.html").absolutePath)
            extractResourceToFile("/http/top_header_bg.jpg",
                    File(httpBaseDir, "top_header_bg.jpg").absolutePath)
        }

        server = RouterNanoHTTPD(0)
        server.addRoute("/static/(.*)", StaticFileDirResponder::class.java, httpBaseDir)
        server.start()

        mockServer = MockWebServer()
        mockDispatcher = ReverseProxyDispatcher(
                HttpUrl.parse("http://localhost:${server.listeningPort}/")!!)
        mockServer.setDispatcher(mockDispatcher)
        mockServer.start()
    }

    @After
    fun tearDown() {
        tmpFilesToDelete.forEach { it.deleteRecursively()}
        tmpFilesToDelete.clear()
        server.stop()
        mockServer.shutdown()
    }

    @Test
    fun givenServerRunningNormally_whenDownloadCalled_thenShouldDownloadSuccessfullyAndContentShouldMatch() {
        runBlocking {
            val downloadDest = File(tmpDownloadDir, "bg_header.jpg")
            val containerFetcherRequest = ContainerFetcherRequest(
                    mockServer.url("/static/top_header_bg.jpg").toString(),
                    downloadDest.absolutePath)
            val mockNetworkManager = mock<NetworkManagerBle> {}

            val downloader = ContainerDownloaderJobHttpUrlConnection(containerFetcherRequest,
                    null, di)
            val result = downloader.download()

            assertEquals("Job is reported as downloaded successfully",
                    JobStatus.COMPLETE, result)
            Assert.assertArrayEquals("Downloaded content is equal to original file content",
                    File(httpBaseDir, "top_header_bg.jpg").readBytes(),
                    downloadDest.readBytes())
        }
    }

    @Test
    fun givenValidHttpUrl_whenDownloadThenCancelCalled_thenShouldNotDownloadAnymore() {
        runBlocking {
            val destFile = tmpFolder.newFile()
            val request = ContainerFetcherRequest(mockServer.url("/static/top_header_bg.jpg").toString(),
                    destFile.absolutePath)


            val containerFetcher = ContainerFetcherJobHttpUrlConnection(request, null, di)
            val resultDeferred = async(Dispatchers.Default) { containerFetcher.download() }
            delay (2000)
            resultDeferred.cancelAndJoin()
            val downloadedAfterCancel = destFile.length()
            delay(1000)
            val downloadedAfterWait = destFile.length() - downloadedAfterCancel
            Assert.assertEquals("Nothing downloaded after cancellation", 0L,
                    downloadedAfterWait)
            Assert.assertTrue("Something was downloaded before cancelation", downloadedAfterCancel > 0)
        }
    }

    @Test
    fun givenServerDisconnects_whenDownloadCalled_shouldDownloadPartialContents() {
        mockDispatcher.numTimesToFail.set(2)

        runBlocking {
            val downloadDest = File(tmpDownloadDir, "bg_header.jpg")
            val downloadDestTmpFile = File(tmpDownloadDir, "bg_header.jpg.dlpart")
            val containerFetcherRequest = ContainerFetcherRequest(
                    mockServer.url("/static/top_header_bg.jpg").toString(),
                    downloadDest.absolutePath)
            val mockNetworkManager = mock<NetworkManagerBle> {}

            val downloader = ContainerDownloaderJobHttpUrlConnection(containerFetcherRequest,
                    null, di)
            val result = downloader.download()

            assertEquals("Job is reported as paused after partial download",
                    JobStatus.PAUSED, result)

            val bytesDownloaded = downloadDestTmpFile.readBytes()
            val partialOriginBytes = ByteArray(bytesDownloaded.size)
            File(httpBaseDir, "top_header_bg.jpg").readBytes()
                    .copyInto(partialOriginBytes, 0, 0, bytesDownloaded.size)

            Assert.assertArrayEquals("Downloaded content is equal to original file content",
                    bytesDownloaded, partialOriginBytes)
        }

    }

    @Test
    fun givenPartialContentAvailable_whenDownloadCalled_thenShouldResumeDownloadAndComplete() {
        mockDispatcher.numTimesToFail.set(2)

        runBlocking {
            val downloadDest = File(tmpDownloadDir, "bg_header.jpg")
            val downloadDestTmpFile = File(tmpDownloadDir, "bg_header.jpg.dlpart")
            val containerFetcherRequest = ContainerFetcherRequest(
                    mockServer.url("/static/top_header_bg.jpg").toString(),
                    downloadDest.absolutePath)
            val mockNetworkManager = mock<NetworkManagerBle> {}

            val downloader1 = ContainerDownloaderJobHttpUrlConnection(containerFetcherRequest,
                    null, di)
            val result1 = downloader1.download()

            val downloader2 = ContainerDownloaderJobHttpUrlConnection(containerFetcherRequest,
                    null, di)
            val result2 = downloader2.download()

            assertEquals("Job is reported as downloaded successfully",
                    JobStatus.COMPLETE, result2)
            Assert.assertArrayEquals("Downloaded content is equal to original file content",
                    File(httpBaseDir, "top_header_bg.jpg").readBytes(),
                    downloadDest.readBytes())
            assertEquals("Job result2 status is 206 partial content", 206,
                    downloader2.responseCode)
        }
    }


}