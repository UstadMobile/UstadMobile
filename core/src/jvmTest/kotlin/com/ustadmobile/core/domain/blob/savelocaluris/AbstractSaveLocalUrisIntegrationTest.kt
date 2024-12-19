package com.ustadmobile.core.domain.blob.savelocaluris

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.upload.BlobUploadServerUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseCommonJvm
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseJvm
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.lib.rest.CacheRoute
import com.ustadmobile.lib.rest.api.blob.BlobUploadServerRoute
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.util.test.initNapierLog
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock
import java.io.File

abstract class AbstractSaveLocalUrisIntegrationTest {


    protected lateinit var clientCache: UstadCache

    protected lateinit var serverCache: UstadCache

    protected lateinit var uriHelper: UriHelper

    protected lateinit var httpClient: HttpClient

    protected lateinit var okHttpClient: OkHttpClient

    protected lateinit var clientCacheDir: File

    protected lateinit var serverCacheDir: File

    protected lateinit var ktorServer: ApplicationEngine

    protected val json = Json {
        encodeDefaults = true
    }

    protected lateinit var learningSpace: LearningSpace

    protected lateinit var blobUploadServerUseCase: BlobUploadServerUseCase

    protected lateinit var mockUmAppDatabase: UmAppDatabase

    protected lateinit var serverSaveLocalUriAsBlobUseCase: SaveLocalUrisAsBlobsUseCase

    protected lateinit var serverRootTmpDir: File

    protected lateinit var serverDeleteUrisUseCase: DeleteUrisUseCase

    protected lateinit var serverIsTempFileUseCase: IsTempFileCheckerUseCase

    protected lateinit var clientRootTmpDir: File

    protected lateinit var clientDeleteUrisUseCase: DeleteUrisUseCase

    protected lateinit var clientIsTempFileUseCase: IsTempFileCheckerUseCase


    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    open fun setup() {
        initNapierLog()
        learningSpace = LearningSpace("http://localhost:8094/")
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
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
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
            learningSpace = learningSpace,
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

            install(ContentNegotiation) {
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


}