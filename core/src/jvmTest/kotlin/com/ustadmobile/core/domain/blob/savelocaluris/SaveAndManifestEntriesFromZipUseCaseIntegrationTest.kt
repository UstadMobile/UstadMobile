package com.ustadmobile.core.domain.blob.savelocaluris

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Test that saving and manifesting entries works as expected e.g. the entry is stored as a blob,
 * a CacheManifestEntry is generated linked to the URL of the blob, and the data retrieved from
 * the cache for that url matches the original blob itself.
 */
class SaveAndManifestEntriesFromZipUseCaseIntegrationTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var cache: UstadCache

    private lateinit var cacheDir: File

    private lateinit var saveLocalUrisAsBlobsUseCase: SaveLocalUrisAsBlobsUseCase

    private lateinit var endpoint: Endpoint

    private lateinit var uriHelper: UriHelper

    private lateinit var httpClient: HttpClient

    private lateinit var okHttpClient: OkHttpClient

    private lateinit var json: Json

    @BeforeTest
    fun setup() {
        cacheDir = temporaryFolder.newFolder()
        cache = UstadCacheBuilder(
            dbUrl = "jdbc:sqlite::memory:",
            storagePath = Path(cacheDir.absolutePath),
            logger = NapierLoggingAdapter(),
            cacheName = "client",
        ).build()

        endpoint = Endpoint("http://localhost:8094/")

        json = Json { encodeDefaults = true }
        okHttpClient = OkHttpClient.Builder().build()
        httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json = json)
            }
        }

        uriHelper = UriHelperJvm(
            mimeTypeHelperImpl = FileMimeTypeHelperImpl(),
            httpClient = httpClient,
            okHttpClient = okHttpClient,
        )

        saveLocalUrisAsBlobsUseCase = SaveLocalUrisAsBlobsUseCaseJvm(
            endpoint = endpoint,
            cache = cache,
            uriHelper = uriHelper,
            tmpDir = Path(temporaryFolder.newFolder().absolutePath),
        )
    }

    @Test
    fun givenZipInput_whenInvoked_thenShouldReturnValidManifest() {

    }

}