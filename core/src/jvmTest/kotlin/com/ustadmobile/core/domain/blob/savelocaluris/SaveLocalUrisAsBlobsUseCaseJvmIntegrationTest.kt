package com.ustadmobile.core.domain.blob.savelocaluris

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCase
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCaseJvm
import com.ustadmobile.core.domain.blob.upload.BlobUploadServerUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseCommonJvm
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseJvm
import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCaseKtorImpl
import com.ustadmobile.core.io.ext.readSha256
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.rest.CacheRoute
import com.ustadmobile.lib.rest.api.blob.BlobUploadServerRoute
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.util.test.initNapierLog
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationServer
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.headersContentLength
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import org.junit.Test
import org.mockito.kotlin.mock
import java.net.URLEncoder
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration test of uploading a local URI from a client to the server.
 */
class SaveLocalUrisAsBlobsUseCaseJvmIntegrationTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var clientCache: UstadCache

    private lateinit var serverCache: UstadCache

    private lateinit var uriHelper: UriHelper

    private lateinit var httpClient: HttpClient

    private lateinit var okHttpClient: OkHttpClient

    private lateinit var clientCacheDir: File

    private lateinit var serverCacheDir: File

    private lateinit var ktorServer: ApplicationEngine

    private val json = Json {
        encodeDefaults = true
    }

    private lateinit var endpoint: Endpoint

    private lateinit var blobUploadServerUseCase: BlobUploadServerUseCase

    private lateinit var mockUmAppDatabase: UmAppDatabase

    private lateinit var serverSaveLocalUriAsBlobUseCase: SaveLocalUrisAsBlobsUseCase

    private lateinit var serverRootTmpDir: File

    private lateinit var serverDeleteUrisUseCase: DeleteUrisUseCase

    private lateinit var serverIsTempFileUseCase: IsTempFileCheckerUseCase

    private lateinit var clientRootTmpDir: File

    private lateinit var clientDeleteUrisUseCase: DeleteUrisUseCase

    private lateinit var clientIsTempFileUseCase: IsTempFileCheckerUseCase

    @BeforeTest
    fun setup() {
        initNapierLog()
        endpoint = Endpoint("http://localhost:8094/")
        serverRootTmpDir = temporaryFolder.newFolder("tmproot-server")
        clientCacheDir = temporaryFolder.newFolder("httpfiles-client")
        clientCache = UstadCacheBuilder(
            dbUrl = "jdbc:sqlite::memory:",
            storagePath = Path(clientCacheDir.absolutePath),
            logger = NapierLoggingAdapter(),
            cacheName = "client",
        ).build()

        serverCacheDir = temporaryFolder.newFolder("httpfiles-server")
        serverCache = UstadCacheBuilder(
            dbUrl = "jdbc:sqlite::memory:",
            storagePath = Path(serverCacheDir.absolutePath),
            cacheName = "server",
            logger = NapierLoggingAdapter()
        ).build()

        mockUmAppDatabase = mock { }


        okHttpClient = OkHttpClient.Builder().build()
        httpClient = HttpClient(OkHttp) {
            install(ClientContentNegotiation) {
                json(json = json)
            }
        }
        uriHelper = UriHelperJvm(
            mimeTypeHelperImpl = FileMimeTypeHelperImpl(),
            httpClient = httpClient,
            okHttpClient = okHttpClient,
        )

        serverIsTempFileUseCase = IsTempFileCheckerUseCaseJvm(serverRootTmpDir)
        serverDeleteUrisUseCase = DeleteUrisUseCaseCommonJvm(serverIsTempFileUseCase)

        serverSaveLocalUriAsBlobUseCase = SaveLocalUrisAsBlobsUseCaseJvm(
            endpoint = endpoint,
            cache = serverCache,
            uriHelper = uriHelper,
            tmpDir = Path(serverRootTmpDir.absolutePath),
            deleteUrisUseCase =serverDeleteUrisUseCase
        )

        blobUploadServerUseCase = BlobUploadServerUseCase(
            httpCache = serverCache,
            json = json,
            tmpDir = Path(serverCacheDir.absolutePath),
            saveLocalUrisAsBlobsUseCase = serverSaveLocalUriAsBlobUseCase,
        )

        ktorServer = embeddedServer(Netty, 8094) {

            install(ContentNegotiationServer) {
                json(json = json)
            }

            routing {
                route("api") {
                    route("blob") {
                        BlobUploadServerRoute(
                            useCase = { blobUploadServerUseCase }
                        )

                        CacheRoute(
                            cache = serverCache
                        )
                    }
                }
            }

        }
        ktorServer.start()

        clientRootTmpDir = temporaryFolder.newFolder("tmproot-client")
        clientIsTempFileUseCase = IsTempFileCheckerUseCaseJvm(clientRootTmpDir)
        clientDeleteUrisUseCase = DeleteUrisUseCaseCommonJvm(clientIsTempFileUseCase)
    }

    @AfterTest
    fun tearDown() {
        clientCache.close()
        ktorServer.stop()
    }

    @Test
    fun givenLocalUris_whenInvoked_thenBlobsAreUploadedAndCanBeRetrievedViaCache() {
        val pdfFile = temporaryFolder.newFileFromResource(javaClass,
            "/com/ustadmobile/core//container/validPDFMetadata.pdf",
            "validPDFMetadata.pdf"
        )

        val saveLocalUrisAsBlobsUseCase = SaveLocalUrisAsBlobsUseCaseJvm(
            endpoint = endpoint,
            cache = clientCache,
            uriHelper = uriHelper,
            tmpDir = Path(clientRootTmpDir.absolutePath),
            deleteUrisUseCase = clientDeleteUrisUseCase,
        )

        val blobUploadClientUseCase = BlobUploadClientUseCaseJvm(
            chunkedUploadUseCase = ChunkedUploadClientUseCaseKtorImpl(httpClient, uriHelper),
            httpClient = httpClient,
            httpCache = clientCache,
            db = mockUmAppDatabase,
            repo = mockUmAppDatabase,
            endpoint = endpoint,
        )

        val blobsToSave = listOf(
            SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                localUri = pdfFile.toDoorUri().toString(),
                entityUid = 42,
                tableId = 1,
                deleteLocalUriAfterSave = true,
            )
        )

        runBlocking {
            val savedBlobs = saveLocalUrisAsBlobsUseCase(
                localUrisToSave = blobsToSave,
            )

            blobUploadClientUseCase(
                blobUrls = savedBlobs.map {
                    BlobUploadClientUseCase.BlobTransferJobItem(it.blobUrl, transferJobItemUid = 0)
                },
                batchUuid = UUID.randomUUID().toString(),
                endpoint = endpoint,
                onProgress = {

                }
            )
        }

        //Should add a check on the client cache.
        val sha256 = pdfFile.inputStream().use { it.readSha256() }.encodeBase64()
        val blobHttpResponse = okHttpClient.newCall(
            Request.Builder()
                .url("${endpoint.url}api/blob/${URLEncoder.encode(sha256, "UTF-8")}")
                .build()
        ).execute()

        assertEquals(200, blobHttpResponse.code)
        assertEquals("application/pdf", blobHttpResponse.header("content-type"))
        assertTrue(blobHttpResponse.header("cache-control")?.contains("immutable") == true)
        assertEquals(pdfFile.length(), blobHttpResponse.headersContentLength())
        val blobBodyBytes = blobHttpResponse.body!!.bytes()
        assertTrue(pdfFile.readBytes().contentEquals(blobBodyBytes))
        assertEquals(0, clientRootTmpDir.list()!!.size,
            "Client root temporary directory should be empty")
        assertEquals(0, serverRootTmpDir.list()!!.size,
            "Server root temporary directory should be empty")
    }
}