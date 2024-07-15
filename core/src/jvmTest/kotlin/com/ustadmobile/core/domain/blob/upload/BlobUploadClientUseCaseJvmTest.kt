package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.BlobTransferJobItem
import com.ustadmobile.core.domain.upload.ChunkInfo
import com.ustadmobile.core.domain.upload.ChunkedUploadClientChunkGetterUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCaseKtorImpl
import com.ustadmobile.core.io.ext.readSha256
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.core.util.uuid.randomUuidAsString
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.TransferJob
import com.ustadmobile.lib.db.entities.TransferJobItem
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.io.range
import com.ustadmobile.libcache.partial.ContentRange
import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.response.ByteArrayResponse
import com.ustadmobile.util.test.ext.newFileFromResource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.any
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import java.io.File
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BlobUploadClientUseCaseJvmTest {

    private lateinit var mockWebServer: MockWebServer

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var itemsToUpload: List<BlobUploadRequestItem>

    private lateinit var  endpoint: Endpoint

    private lateinit var batchUuid: UUID

    private lateinit var mockChunkedUploadUseCase: ChunkedUploadClientUseCaseKtorImpl

    private lateinit var httpClient : HttpClient

    private lateinit var mockCache: UstadCache

    private val json = Json {
        encodeDefaults = true
    }

    private lateinit var blobFileMap: Map<String, File>

    private val testChunkSize = 20 * 1024

    private lateinit var mockDatabase: UmAppDatabase

    private lateinit var realDatabase: UmAppDatabase

    private lateinit var serverUploadResponse: BlobUploadResponse

    @BeforeTest
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        endpoint = Endpoint(mockWebServer.url("/").toString())
        val filesToUpload = (1..3).map {
            temporaryFolder.newFileFromResource(
                javaClass, "/com/ustadmobile/core/container/testfile${it}.png"
            )
        }

        val urlMapMutable = mutableMapOf<String, File>()
        itemsToUpload = filesToUpload.map { file ->
            val fileSha256 = file.inputStream().use {
                it.readSha256()
            }.encodeBase64()

            val blobUrl = "${endpoint.url}api/blob/$fileSha256"
            urlMapMutable[blobUrl] = file
            BlobUploadRequestItem(
                blobUrl = blobUrl,
                size = file.length()
            )
        }
        blobFileMap = urlMapMutable.toMap()

        batchUuid = UUID.randomUUID()
        mockChunkedUploadUseCase = mock { }
        httpClient = HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
        }

        mockCache = mock { }
        mockCache.stub {
            on { retrieve(any()) }.thenAnswer { invocation ->
                val request: HttpRequest = invocation.arguments.first() as HttpRequest
                val url = request.url
                val blobFile = blobFileMap[url]!!

                val rangeHeaderStr = request.headers["range"]
                val rangeHeader = rangeHeaderStr?.let {
                    ContentRange.parseRangeHeader(it, blobFile.length())
                }

                ByteArrayResponse(
                    request = request,
                    mimeType = "image/png",
                    responseCode = if(rangeHeader != null) 216 else 200,
                    body = if(rangeHeader != null) {
                        blobFile.inputStream().range(rangeHeader.fromByte, rangeHeader.toByte).readAllBytes()
                    }else {
                        blobFile.inputStream().readAllBytes()
                    }
                )
            }
        }
        mockDatabase = mock { }
        serverUploadResponse = BlobUploadResponse(
            itemsToUpload.map {
                BlobUploadResponseItem(
                    blobUrl = it.blobUrl,
                    uploadUuid = UUID.randomUUID().toString(),
                    fromByte = 0,
                )
            }
        )
        realDatabase = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite::memory:", nodeId = 1L)
            .build()

    }

    @AfterTest
    fun tearDown() {
        httpClient.close()
        mockWebServer.shutdown()
        realDatabase.close()
    }

    private fun UstadCache.verifyRangeRequestReceived(
        url: String,
        totalSize: Long,
        rangeStart: Long,
        rangeEnd: Long,
    ) {
        verify(this).retrieve(
            argWhere { request ->
                val rangeHeaders = try{
                    request.headers["range"]?.let {
                        ContentRange.parseRangeHeader(it, totalSize)
                    }
                }catch(e: Throwable) {
                    null
                }

                request.url == url &&
                        rangeHeaders != null &&
                        rangeHeaders.fromByte == rangeStart &&
                        /*
                         * Range header to byte is inclusive, chunk info end byte is exclusive,
                         * so must add one
                         */
                        rangeHeaders.toByte == rangeEnd
            }
        )
    }

    class TestUploadException: Exception()

    private fun ChunkedUploadClientUseCaseKtorImpl.stubToThrowException(
        throwException: (uploadUuid: String) -> Boolean
    ) {
        this.stub {
            onBlocking {
                invoke(
                    uploadUuid = any(),
                    totalSize = any(),
                    getChunk = any(),
                    remoteUrl = eq("${endpoint.url}api/blob/upload-batch-data"),
                    fromByte = any(),
                    chunkSize = any(),
                    onProgress = any(),
                    onStatusChange = any()
                )
            }.thenAnswer { invocation ->
                val uploadUuid = invocation.arguments.first() as String
                if(throwException(uploadUuid)) {
                    throw TestUploadException()
                }

                Unit
            }
        }
    }

    private fun assertMockWebServerRequestMatches(uploadRequest: BlobUploadRequest) {
        val initRequest = mockWebServer.takeRequest()
        val requestBody = initRequest.body.readUtf8()
        val batchUploadRequestReceived: BlobUploadRequest = json.decodeFromString(
            requestBody
        )
        assertEquals("POST", initRequest.method!!.toString().uppercase())
        assertEquals(uploadRequest, batchUploadRequestReceived)
    }


    @Test
    fun givenBatch_whenInvoked_thenWillRetrievePartialDataAndUpload() {
        val uploadRequest = BlobUploadRequest(
            blobs = itemsToUpload,
            batchUuid = batchUuid.toString()
        )

        val useCase = BlobUploadClientUseCaseJvm(
            mockChunkedUploadUseCase, httpClient, mockCache, json, mockDatabase, mockDatabase, endpoint, testChunkSize,
        )


        val responseStr = json.encodeToString(
            BlobUploadResponse.serializer(),
            serverUploadResponse
        )

        mockWebServer.enqueue(
            MockResponse()
                .addHeader("content-type", "application/json")
                .setBody(responseStr)
        )

        runBlocking {
            useCase(
                blobUrls = itemsToUpload.map {
                    BlobTransferJobItem(it.blobUrl, 0)
                },
                batchUuid = batchUuid.toString(),
                endpoint = endpoint,
                onProgress = { },
            )
        }

        assertMockWebServerRequestMatches(uploadRequest)

        itemsToUpload.forEach { item ->
            val blobResponseItem = serverUploadResponse.blobsToUpload.first {
                it.blobUrl == item.blobUrl
            }

            argumentCaptor<ChunkedUploadClientChunkGetterUseCase.UploadChunkGetter> {
                verifyBlocking(mockChunkedUploadUseCase) {
                    invoke(
                        uploadUuid = eq(blobResponseItem.uploadUuid),
                        totalSize = eq(item.size),
                        getChunk = capture(),
                        remoteUrl = eq("${endpoint.url}api/blob/upload-batch-data"),
                        fromByte = eq(0),
                        chunkSize = eq(testChunkSize),
                        onProgress = any(),
                        onStatusChange = any(),
                    )
                }

                val chunkGetter = firstValue

                val expectedChunkInfo = ChunkInfo(item.size, testChunkSize, 0)

                //When we call the chunk getter, it should make a range request to the server
                runBlocking {
                    val firstChunkInfo = expectedChunkInfo.first()
                    val buffer = ByteArray(testChunkSize)
                    chunkGetter.invoke(firstChunkInfo, buffer)
                    mockCache.verifyRangeRequestReceived(
                        url = item.blobUrl,
                        totalSize = item.size,
                        rangeStart = firstChunkInfo.start,
                        rangeEnd = firstChunkInfo.end - 1,
                    )
                }

            }
        }
    }

    @Test
    fun givenBatchDataRetrieved_whenOneUploadThrowsException_thenWillRethrow() {
        val responseStr = json.encodeToString(
            BlobUploadResponse.serializer(),
            serverUploadResponse
        )

        mockWebServer.enqueue(
            MockResponse()
                .addHeader("content-type", "application/json")
                .setBody(responseStr)
        )

        //Throw an exception on the upload of the last item
        mockChunkedUploadUseCase.stubToThrowException { uploadUuid ->
            uploadUuid == serverUploadResponse.blobsToUpload.last().uploadUuid
        }

        val useCase = BlobUploadClientUseCaseJvm(
            mockChunkedUploadUseCase, httpClient, mockCache, json, mockDatabase, mockDatabase, endpoint, testChunkSize,
        )
        runBlocking {
            try {
                useCase(
                    blobUrls = itemsToUpload.map {
                        BlobTransferJobItem(it.blobUrl, 0)
                    },
                    batchUuid = batchUuid.toString(),
                    endpoint = endpoint,
                    onProgress = { },
                )
                throw IllegalStateException("Shouldnt make it here")
            }catch(e: Throwable) {
                assertTrue(e is TestUploadException)
            }
        }

    }

    @Test
    fun givenTransferJobInDatabase_whenErrorOccurs_thenAttemptCountIncrementAndExceptionThrown() {
        val responseStr = json.encodeToString(
            BlobUploadResponse.serializer(),
            serverUploadResponse
        )

        mockWebServer.enqueue(
            MockResponse()
                .addHeader("content-type", "application/json")
                .setBody(responseStr)
        )

        runBlocking {
            val jobId = realDatabase.withDoorTransactionAsync {
                val transferJobUid = realDatabase.transferJobDao().insert(
                    TransferJob(
                        tjUuid = randomUuidAsString(),
                    )
                ).toInt()
                realDatabase.transferJobItemDao().insertList(
                    itemsToUpload.map {
                        TransferJobItem(
                            tjiTjUid =  transferJobUid,
                            tjiSrc = it.blobUrl,
                            tjTotalSize = it.size,
                        )
                    }
                )
                transferJobUid
            }

            mockChunkedUploadUseCase.stubToThrowException { uploadUuid ->
                uploadUuid == serverUploadResponse.blobsToUpload.last().uploadUuid
            }

            val useCase = BlobUploadClientUseCaseJvm(
                mockChunkedUploadUseCase, httpClient, mockCache, json, realDatabase, mockDatabase, endpoint, testChunkSize,
            )

            try {
                useCase(jobId)
            }catch(e: Throwable) {
                e.printStackTrace()
                assertTrue(e is TestUploadException)
                val errors = realDatabase.transferJobErrorDao().findByJobId(jobId)
                assertEquals(1, errors.size)
            }
        }
    }


}