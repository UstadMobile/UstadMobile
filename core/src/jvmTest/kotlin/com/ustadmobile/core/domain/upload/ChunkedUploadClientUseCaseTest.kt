package com.ustadmobile.core.domain.upload

import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.io.RangeInputStream
import com.ustadmobile.util.test.ext.newFileFromResource
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChunkedUploadClientUseCaseTest  {

    @JvmField
    @Rule
    val tempFolder = TemporaryFolder()

    private lateinit var httpClient: HttpClient

    private lateinit var okHttpClient: OkHttpClient

    private lateinit var uriHelper: UriHelper

    private lateinit var mockWebServer: MockWebServer

    @BeforeTest
    fun setup() {
        okHttpClient = OkHttpClient.Builder().build()

        httpClient = HttpClient(OkHttp) {
            engine {
                preconfigured = okHttpClient
            }
        }

        uriHelper = UriHelperJvm(
            mimeTypeHelperImpl = FileMimeTypeHelperImpl(),
            httpClient = httpClient,
            okHttpClient = okHttpClient,
        )

        mockWebServer = MockWebServer()
        mockWebServer.start()
        mockWebServer.dispatcher = object: Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setResponseCode(204)
            }
        }
    }

    @AfterTest
    fun tearDown() {
        httpClient.close()
        mockWebServer.shutdown()
    }


    private fun testUpload(
        @Suppress("SameParameterValue")
        resourcePath: String,
        fromByte: Long,
    ) {
        val testFile = tempFolder.newFileFromResource(javaClass, resourcePath)
        val uploader = ChunkedUploadClientUseCase(httpClient)
        val uuid = UUID.randomUUID()
        val chunkSize = (20 * 1024)
        val expectedChunkInfo = ChunkInfo(
            totalSize = testFile.length(), chunkSize = chunkSize, fromByte = fromByte
        )

        runBlocking {
            uploader(
                uploadUuid = uuid.toString(),
                localUri = testFile.toDoorUri(),
                uriHelper = uriHelper,
                remoteUrl = mockWebServer.url("/").toString(),
                chunkSize = chunkSize,
                fromByte = fromByte,
            )
        }

        val byteArrayOutput = ByteArrayOutputStream()
        if(fromByte > 0) {
            val initBytes =RangeInputStream(testFile.inputStream(), 0, fromByte - 1).use { rangeIn ->
                rangeIn.readBytes()
            }
            byteArrayOutput.writeBytes(initBytes)
        }

        val requestCount = mockWebServer.requestCount
        assertEquals(expectedChunkInfo.numChunks, requestCount)
        (0 until requestCount).forEach { index ->
            val request = mockWebServer.takeRequest()
            val requestBodyBytes = request.body.readByteArray()
            assertEquals(expectedChunkInfo[index].size, requestBodyBytes.size,
                "Chunk $index should match expected size")
            assertEquals(uuid.toString(), request.headers[HEADER_UPLOAD_UUID])
            if(index == requestCount - 1)
                assertEquals("true", request.headers[HEADER_IS_FINAL_CHUNK])

            byteArrayOutput.writeBytes(requestBodyBytes)
        }
        byteArrayOutput.flush()
        val allBytesReceived = byteArrayOutput.toByteArray()
        assertTrue(testFile.readBytes().contentEquals(allBytesReceived),
            "Concatenation of bytes upload equals the original bytes")
    }

    @Test
    fun givenFileUploadedInChunks_whenCombined_thenShouldMatch() {
        testUpload("/com/ustadmobile/core/container/testfile1.png", 0)
    }

    @Test
    fun givenPartialUpload_whenCombined_thenShouldMatch() {
        testUpload("/com/ustadmobile/core/container/testfile1.png", 10000)
    }


}