package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.domain.blob.BlobTransferJobItem
import com.ustadmobile.util.test.ResourcesDispatcher
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.mockito.kotlin.mock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BlobDownloadClientUseCaseCommonJvmTest {

    lateinit var mockWebServer: MockWebServer

    lateinit var okHttpClient: OkHttpClient

    @BeforeTest
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = ResourcesDispatcher(this::class.java)
        mockWebServer.start()
    }

    @AfterTest
    fun tearDown() {
        mockWebServer.shutdown()
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
    }

    fun MockWebServer.takeAllRequests(): List<RecordedRequest> {
        val numRequest = requestCount
        val requests = mutableListOf<RecordedRequest>()
        for(i in 0 until numRequest){
            requests.add(takeRequest())
        }

        return requests.toList()
    }

    /**
     * Note: the heavy lifting in downloading is done by the lib-cache okhttp interceptor. The
     * BlobDownloadClientUseCase only has to pull them through the interceptor.
     */
    @Test
    fun givenListOfBlobsToDownload_whenInvoked_thenShouldRequestAll() {
        val itemsToDownload = (1..3).map {
            BlobTransferJobItem(
                blobUrl = "http://localhost:${mockWebServer.port}/com/ustadmobile/core/container/testfile${it}.png",
                transferJobItemUid = 0,
                lockIdToRelease = 0,
            )
        }

        okHttpClient = OkHttpClient.Builder().build()
        val useCase = BlobDownloadClientUseCaseCommonJvm(
            okHttpClient = okHttpClient,
            db = mock { },
            repo = null,
        )

        runBlocking {
            useCase(itemsToDownload)
        }

        val allRequests = mockWebServer.takeAllRequests()
        itemsToDownload.forEach { downloadItem ->
            assertEquals(
                1, allRequests.count { it.requestUrl?.toString() == downloadItem.blobUrl },
                message = "should be exactly one request for ${downloadItem.blobUrl}"
            )
        }
    }


}