package com.ustadmobile.core.domain.blob.saveandmanifest

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseCommonJvm
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseJvm
import com.ustadmobile.core.io.ext.readSha256
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.ihttp.request.iRequestBuilder
import com.ustadmobile.util.test.ext.newFileFromResource
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test that saving and manifesting entries works as expected e.g. the entry is stored as a blob,
 * a CacheManifestEntry is generated linked to the URL of the blob, and the data retrieved from
 * the cache for that url matches the original blob itself.
 */
class SaveLocalUriAsBlobAndManifestUseCaseJvmTest {

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

    private lateinit var testFiles: List<File>

    private lateinit var isTempFileUseCase: IsTempFileCheckerUseCase

    private lateinit var deleteUrisUseCase: DeleteUrisUseCase

    private lateinit var tmpRoot: File

    @BeforeTest
    fun setup() {
        tmpRoot = temporaryFolder.newFolder("tmproot")
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
        isTempFileUseCase = IsTempFileCheckerUseCaseJvm(tmpRoot)
        deleteUrisUseCase = DeleteUrisUseCaseCommonJvm(isTempFileUseCase)

        saveLocalUrisAsBlobsUseCase = SaveLocalUrisAsBlobsUseCaseJvm(
            endpoint = endpoint,
            cache = cache,
            uriHelper = uriHelper,
            tmpDir = Path(temporaryFolder.newFolder().absolutePath),
            deleteUrisUseCase = deleteUrisUseCase,
        )

        testFiles = (1..3).map {
            temporaryFolder.newFileFromResource(this::class.java,
                "/com/ustadmobile/core/container/testfile${it}.png")
        }
    }

    @Test
    fun givenLocalUrisToManifest_whenInvoked_thenAreStoredInCacheAsPerManifestBodyDataUrl() {
        val useCase = SaveLocalUriAsBlobAndManifestUseCaseJvm(
            saveLocalUrisAsBlobsUseCase, FileMimeTypeHelperImpl()
        )

        runBlocking {
            val result = useCase(
                testFiles.map {  file ->
                    SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                        blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = file.toDoorUri().toString(),
                            mimeType = "image/png"
                        ),
                        manifestUri = file.name
                    )
                }
            )

            testFiles.forEach { testFile ->
                val resultEntry = result.first {
                    it.savedBlob.localUri == testFile.toDoorUri().toString()
                }

                val cacheResponse = cache.retrieve(
                    request = iRequestBuilder(resultEntry.manifestEntry.bodyDataUrl)
                )

                assertEquals(200, cacheResponse!!.responseCode)
                assertEquals("image/png", cacheResponse.headers["content-type"])
                val sha256 = testFile.inputStream().use { it.readSha256() }
                assertEquals(sha256Integrity(sha256), cacheResponse.headers["etag"])
                assertEquals("true", cacheResponse.headers["X-Etag-Is-Integrity"])
                assertTrue(testFile.readBytes().contentEquals(cacheResponse.bodyAsSource()!!.readByteArray()))
            }
        }

    }

}