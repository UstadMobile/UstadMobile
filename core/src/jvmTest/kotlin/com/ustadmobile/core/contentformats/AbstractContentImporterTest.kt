package com.ustadmobile.core.contentformats

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCaseJvm
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCaseCommonJvm
import com.ustadmobile.core.domain.compress.list.CompressListUseCase
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.newTestHttpClient
import com.ustadmobile.core.util.newTestOkHttpClient
import com.ustadmobile.core.util.newTestUstadCache
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import io.ktor.client.HttpClient
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Provides setup and teardown of commonly required components for content importers.
 */
abstract class AbstractContentImporterTest: AbstractMainDispatcherTest() {

    protected lateinit var db: UmAppDatabase

    protected lateinit var ustadCache: UstadCache

    protected lateinit var uriHelper: UriHelper


    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    protected lateinit var activeEndpoint: Endpoint

    protected lateinit var saveLocalUrisUseCase: SaveLocalUrisAsBlobsUseCase

    protected lateinit var saveAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase

    protected lateinit var getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase

    protected lateinit var json: Json

    protected lateinit var httpClient: HttpClient

    protected lateinit var okHttpClient: OkHttpClient

    protected lateinit var rootTmpFolder: File

    protected lateinit var compressListUseCase: CompressListUseCase

    @BeforeTest
    fun setup() {
        rootTmpFolder = temporaryFolder.newFolder("video-import-test")
        db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, "jdbc:sqlite::memory:", nodeId = 1L
        ).build()

        json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        ustadCache = newTestUstadCache(temporaryFolder)
        okHttpClient = newTestOkHttpClient(temporaryFolder, cache = ustadCache, json = json)
        httpClient = okHttpClient.newTestHttpClient(json)

        activeEndpoint = Endpoint("http://localhost:8097/")


        uriHelper = UriHelperJvm(
            mimeTypeHelperImpl = FileMimeTypeHelperImpl(),
            httpClient = httpClient,
            okHttpClient = okHttpClient,
        )

        //Strictly speaking should be mocked, in reality, it's easier to just use the real thing.
        saveLocalUrisUseCase = SaveLocalUrisAsBlobsUseCaseJvm(
            endpoint = activeEndpoint,
            cache = ustadCache,
            uriHelper = uriHelper,
            tmpDir = Path(temporaryFolder.newFolder().absolutePath),
            deleteUrisUseCase = mock { }
        )

        saveAndManifestUseCase = SaveLocalUriAsBlobAndManifestUseCaseJvm(saveLocalUrisUseCase,
            FileMimeTypeHelperImpl()
        )
        getStoragePathForUrlUseCase = GetStoragePathForUrlUseCaseCommonJvm(
            okHttpClient = okHttpClient,
            cache = ustadCache,
            tmpDir = temporaryFolder.newFolder(),
        )
        compressListUseCase = CompressListUseCase(
            compressVideoUseCase = null,
            mimeTypeHelper = FileMimeTypeHelperImpl(),
            compressImageUseCase = null,
        )
    }

    @AfterTest
    fun tearDown() {
        db.close()
        ustadCache.close()
        httpClient.close()
    }

}