package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.domain.blob.BlobTransferJobItem
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.util.test.ResourcesDispatcher
import com.ustadmobile.util.test.initNapierLog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BlobDownloadClientUseCaseCommonJvmTest {

    lateinit var mockWebServer: MockWebServer

    lateinit var okHttpClient: OkHttpClient

    lateinit var mockCache: UstadCache

    @BeforeTest
    fun setup() {
        initNapierLog()
        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = ResourcesDispatcher(this::class.java)
        mockWebServer.start()
        mockCache = mock {
            on { getEntries(any()) }.thenReturn(emptyMap())
        }
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
            httpCache = mockCache,
        )

        runBlocking {
            useCase(itemsToDownload)
        }

        val allRequests = mockWebServer.takeAllRequests()
        Napier.d { "BlobDownloadClientUseCaseCommonJvmTest: Recorded requests for ${allRequests.joinToString{it.requestUrl.toString() }}" }
        itemsToDownload.forEach { downloadItem ->
            assertEquals(
                1, allRequests.count {
                    //MockWebServer might consider the host domain as part of the url, so look only at the path
                    downloadItem.blobUrl.toHttpUrl().encodedPath ==  it.requestUrl?.encodedPath
                },
                message = "should be exactly one request for ${downloadItem.blobUrl}"
            )
        }
    }


}