package com.ustadmobile.sharedse.network.containerfetcher

import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.Okio
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

class ContainerFetcherJobHttpUrlConnectionTest {

    private lateinit var mockWebServer: MockWebServer

    @JvmField
    @Rule
    var tmpFolder = TemporaryFolder()

    private lateinit var di: DI

    private lateinit var networkManager: NetworkManagerBle

    @Before
    fun setup() {
        networkManager = mock()
        di = DI {
            bind<NetworkManagerBle>() with singleton { networkManager }
        }

        mockWebServer= MockWebServer()
        mockWebServer.start()
    }


    @Test
    fun givenValidHttpUrl_whenDownloadCalled_thenShouldDownloadFileAndReturnSuccess() {
        mockWebServer.enqueue(MockResponse()
            .setHeader("Content-Type", "image/png")
            .also {
                it.setBody(Buffer().apply {
                    val inStream = this::class.java.getResourceAsStream(TEST_RES_PATH)
                    writeAll(Okio.source(inStream))
                })
            })

        val destFile = tmpFolder.newFile()
        val request = ContainerFetcherRequest(mockWebServer.url("/somefile.png").toString(),
                destFile.absolutePath)


        val containerFetcher = ContainerFetcherJobHttpUrlConnection(request, null, di)
        val downloadResult = runBlocking { containerFetcher.download() }
        Assert.assertEquals("Download result is successful", JobStatus.COMPLETE,
            downloadResult)
        Assert.assertArrayEquals("Downloaded data is the same as original resource",
            this::class.java.getResourceAsStream(TEST_RES_PATH).use { it.readBytes() },
            destFile.readBytes())
    }

    @Test
    fun givenValidHttpUrl_whenDownloadThenCancelCalled_thenShouldNotDownloadAnymore() {
        mockWebServer.enqueue(MockResponse()
                .setHeader("Content-Type", "image/png")
                .throttleBody(128, 1, TimeUnit.SECONDS)
                .also {
                    it.setBody(Buffer().apply {
                        val inStream = this::class.java.getResourceAsStream(TEST_RES_PATH)
                        writeAll(Okio.source(inStream))
                    })
                })

        runBlocking {
            val destFile = tmpFolder.newFile()
            val request = ContainerFetcherRequest(mockWebServer.url("/somefile.png").toString(),
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


    companion object {
        const val TEST_RES_PATH = "/com/ustadmobile/core/container/testfile1.png"
    }

}