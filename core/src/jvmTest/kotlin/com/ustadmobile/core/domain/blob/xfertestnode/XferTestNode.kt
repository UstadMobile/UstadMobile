package com.ustadmobile.core.domain.blob.xfertestnode

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.contentformats.epub.XhtmlFixer
import com.ustadmobile.core.contentformats.epub.XhtmlFixerJsoup
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseCommonJvm
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseJvm
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.io.useAndReadySha256
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.okhttp.UstadCacheInterceptor
import com.ustadmobile.libcache.request.requestBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Holds dependencies used by both BlobUploadTestNode
 * @param dbUrl can be used to give a custom dbUrl e.g. if debugging something, save the database for autopsy
 */
@OptIn(ExperimentalXmlUtilApi::class)
class XferTestNode(
    val temporaryFolder: TemporaryFolder,
    val name: String,
    val dbUrl: (Endpoint) -> String = { "jdbc:sqlite::memory:" }
) {

    val endpointScope = EndpointScope()

    val rootTmpDir = temporaryFolder.newFolder("$name-tmproot-server")

    val cacheDir = temporaryFolder.newFolder("$name-server-httpfiles")

    val httpCache: UstadCache

    val okHttpClient: OkHttpClient

    val httpClient: HttpClient

    private val json: Json = Json { encodeDefaults = true }

    val uriHelper: UriHelper

    val isTempFileCheckerUseCase: IsTempFileCheckerUseCase

    val deleteUrisUseCase: DeleteUrisUseCase

    val di: DI

    private val dbsToClose = mutableListOf<UmAppDatabase>()

    init {
        httpCache = UstadCacheBuilder(
            dbUrl = "jdbc:sqlite::memory:",
            storagePath = Path(cacheDir.absolutePath),
            logger = null,
            cacheName = "client",
        ).build()

        okHttpClient = OkHttpClient.Builder()
            .dispatcher(
                Dispatcher().also {
                    it.maxRequests = 30
                    it.maxRequestsPerHost = 10
                }
            )
            .addInterceptor(
                UstadCacheInterceptor(
                    cache = httpCache,
                    tmpDir = File(rootTmpDir, "okhttp-tmp"),
                    logger = NapierLoggingAdapter(),
                )
            )
            .build()

        httpClient = HttpClient(OkHttp) {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(json = json)
            }

            engine {
                preconfigured = okHttpClient
            }
        }

        uriHelper = UriHelperJvm(
            mimeTypeHelperImpl = FileMimeTypeHelperImpl(),
            httpClient = httpClient,
            okHttpClient = okHttpClient,
        )

        isTempFileCheckerUseCase = IsTempFileCheckerUseCaseJvm(rootTmpDir)
        deleteUrisUseCase = DeleteUrisUseCaseCommonJvm(isTempFileCheckerUseCase)

        di = DI {
            bind<HttpClient>() with singleton {
                httpClient
            }

            bind<OkHttpClient>() with singleton {
                okHttpClient
            }

            bind<SaveLocalUrisAsBlobsUseCase>() with scoped(endpointScope).singleton {
                SaveLocalUrisAsBlobsUseCaseJvm(
                    endpoint = context,
                    cache = httpCache,
                    uriHelper = uriHelper,
                    tmpDir = Path(rootTmpDir.absolutePath),
                    deleteUrisUseCase = deleteUrisUseCase
                )
            }

            bind<Json>() with singleton {
                json
            }

            bind<UstadCache>() with singleton {
                httpCache
            }

            bind<UriHelper>() with singleton {
                uriHelper
            }

            bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
                DatabaseBuilder.databaseBuilder(UmAppDatabase::class, dbUrl(context), nodeId = 1L)
                    .build().also {
                        dbsToClose.add(it)
                    }
            }

            bind<XML>() with singleton {
                XML {
                    defaultPolicy {
                        unknownChildHandler  = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
                    }
                }
            }

            bind<XhtmlFixer>() with singleton {
                XhtmlFixerJsoup(xml = instance())
            }

            bind<File>(tag = DiTag.TAG_TMP_DIR) with singleton {
                rootTmpDir
            }

        }
    }

    fun getManifest(url: String): ContentManifest {
        return httpCache.retrieve(requestBuilder(url))?.bodyAsSource()?.readString()?.let {
            json.decodeFromString(ContentManifest.serializer(), it)
        } ?: throw IllegalArgumentException("$name Could not find manifest for $url")
    }


    fun assertManifestStoredOnNode(
        manifest: ContentManifest,
        url: String,
    ) {
        val manifestResponse = httpCache.retrieve(requestBuilder(url))
        assertNotNull(manifestResponse, "Manifest response for $url should not be null")
        val manifestStored: ContentManifest = json.decodeFromString(manifestResponse.bodyAsSource()!!.readString())

        assertEquals(manifest.entries.size, manifestStored.entries.size,
            "Manifest stored on node should have same number of entries")
        manifest.entries.forEach { entry ->
            val cacheResponse = httpCache.retrieve(requestBuilder(entry.bodyDataUrl))
            assertNotNull(cacheResponse, "Cache response for ${entry.uri} must not be null")
            val integrityStored = sha256Integrity(
                cacheResponse.bodyAsSource()!!.buffered().useAndReadySha256())
            assertEquals(entry.integrity, integrityStored, "Integrity for ${entry.uri} " +
                    "should match integrity of actual body data")
        }
    }

    fun close() {
        httpClient.close()
        httpCache.close()
        dbsToClose.forEach {
            it.close()
        }
    }
}