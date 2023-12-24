package com.ustadmobile.core.domain.blob.savelocaluris

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.adapters.PersonPictureAdapter
import com.ustadmobile.core.io.ext.readSha256
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.util.test.ext.newFileFromResource
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import kotlinx.io.asInputStream
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlinx.io.files.Path
import okhttp3.OkHttpClient
import org.junit.Assert
import org.mockito.kotlin.mock
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveBlobUseCaseIntegrationTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var cache: UstadCache

    private lateinit var uriHelper: UriHelper

    private lateinit var httpClient: HttpClient

    private lateinit var okHttpClient: OkHttpClient

    private lateinit var cacheDir: File

    @BeforeTest
    fun setup() {
        cacheDir = temporaryFolder.newFolder()
        cache = UstadCacheBuilder("jdbc:sqlite::memory:",
            Path(cacheDir.absolutePath), null).build()
        httpClient = HttpClient {  }
        okHttpClient = OkHttpClient.Builder().build()
        uriHelper = UriHelperJvm(
            mimeTypeHelperImpl = FileMimeTypeHelperImpl(),
            httpClient = httpClient,
            okHttpClient = okHttpClient,
        )
    }

    @AfterTest
    fun tearDown() {
        cache.close()
    }

    @Test
    fun givenBlob_whenSaved_thenCanBeRetrievedViaCache() {
        val pdfFile = temporaryFolder.newFileFromResource(javaClass,
            "/com/ustadmobile/core//container/validPDFMetadata.pdf")
        val appDb = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, "jdbc:sqlite::memory:", 1L
        ).build()

        val useCase = SaveLocalUrisAsBlobsUseCaseJvm(
            cache = cache,
            uriHelper = uriHelper,
            tmpDir = Path(cacheDir.absolutePath),
            dbProvider = { _, _ -> appDb },
            adapterProvider = { PersonPictureAdapter() },
            blobUploadClientUseCase = mock { }
        )

        val endpoint = Endpoint("http://server.com/")

        runBlocking {
            useCase(
                endpoint = endpoint,
                tableId = PersonPicture.TABLE_ID,
                blobs = listOf(
                    SaveLocalUrisAsBlobsUseCase.BlobToSave(1, pdfFile.toURI().toString())
                )
            )

            val dataSha256 = pdfFile.inputStream().readSha256()
            val cacheResponse = cache.retrieve(
                requestBuilder("${endpoint.url}api/blob/${UMURLEncoder.encodeUTF8(dataSha256.encodeBase64())}")
            )
            assertEquals(200, cacheResponse?.responseCode)
            val responseSha256 = cacheResponse!!.bodyAsSource()!!.asInputStream().readSha256()
            Assert.assertArrayEquals(dataSha256, responseSha256)
        }
    }

}