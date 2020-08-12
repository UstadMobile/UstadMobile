package com.ustadmobile.sharedse.network.containerfetcher

import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.runBlocking
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
    }


    @Test
    fun givenValidHttpUrl_whenDownloadCalled_thenShouldDownloadFileAndReturnSuccess() {
        mockWebServer= MockWebServer()
        mockWebServer.start()

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


    companion object {
        const val TEST_RES_PATH = "/com/ustadmobile/core/container/testfile1.png"
    }

}