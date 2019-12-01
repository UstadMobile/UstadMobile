package com.ustadmobile.sharedse.network

import com.ustadmobile.port.sharedse.impl.http.StaticFileDirResponder
import com.ustadmobile.sharedse.io.extractResourceToFile
import com.ustadmobile.sharedse.network.fetch.AbstractFetchListenerMpp
import com.ustadmobile.sharedse.network.fetch.DownloadMpp
import com.ustadmobile.sharedse.network.fetch.FetchMppJvmImpl
import com.ustadmobile.sharedse.network.fetch.RequestMpp
import fi.iki.elonen.router.RouterNanoHTTPD
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class FetchJvmImplTest {

    private lateinit var server : RouterNanoHTTPD

    lateinit var mockServer: MockWebServer

    private lateinit var httpTmpDir: File

    private lateinit var httpTmpFile: File

    private var destFile: File? = null

    @Before
    fun startServer() {
        httpTmpDir = Files.createTempDirectory("FethJvmImplTest").toFile()
        httpTmpFile = File(httpTmpDir, DOWNLOADBG_FILENAME)
        runBlocking {
            extractResourceToFile(DOWNLOADBG_TEST_RESPATH, httpTmpFile.absolutePath)
        }

        server = RouterNanoHTTPD(0)
        server.addRoute("/static/(.*)", StaticFileDirResponder::class.java, httpTmpDir)
        server.start()
    }

    @After
    fun tearDown() {
        server.stop()
        //httpTmpDir.deleteRecursively()
    }

    @Test
    fun givenValidUrl_whenFileEnqueued_thenShouldDownloadAndComplete() {
        val okHttpClient = OkHttpClient.Builder().build()
        val fetchImpl = FetchMppJvmImpl(okHttpClient)

        val completableDeferred = CompletableDeferred<Unit>()
        fetchImpl.addListener(object: AbstractFetchListenerMpp() {
            override fun onCompleted(download: DownloadMpp) {
                completableDeferred.complete(Unit)
            }
        })

        val downloadDestFileVal = File.createTempFile("FetchJvmImplTest", "downloadFile")
        destFile = downloadDestFileVal
        val fetchRequest = RequestMpp("http://localhost:${server.listeningPort}/static/top_header_bg.jpg",
                downloadDestFileVal.absolutePath)
        fetchImpl.enqueue(fetchRequest)
        runBlocking {
            withTimeout(10000) {completableDeferred.await() }

            Assert.assertArrayEquals("Downloaded contents = original file contents",
                    httpTmpFile.readBytes(),
                    downloadDestFileVal.readBytes())
        }
    }

    companion object {

        const val DOWNLOADBG_FILENAME = "top_header_bg.jpg"

        const val DOWNLOADBG_TEST_RESPATH = "/http/top_header_bg.jpg"
    }

}