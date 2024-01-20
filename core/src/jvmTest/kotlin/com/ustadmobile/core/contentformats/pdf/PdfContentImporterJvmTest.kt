package com.ustadmobile.core.contentformats.pdf

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCaseJvm
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCaseCommonJvm
import com.ustadmobile.core.test.assertCachedBodyMatchesFileContent
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.newTestHttpClient
import com.ustadmobile.core.util.newTestOkHttpClient
import com.ustadmobile.core.util.newTestUstadCache
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.util.test.ResourcesDispatcher
import com.ustadmobile.util.test.ext.newFileFromResource
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PdfContentImporterJvmTest : AbstractMainDispatcherTest() {


    private lateinit var db: UmAppDatabase

    private lateinit var ustadCache: UstadCache

    private lateinit var uriHelper: UriHelper


    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var activeEndpoint: Endpoint

    private lateinit var saveLocalUrisUseCase: SaveLocalUrisAsBlobsUseCase

    private lateinit var saveAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase

    private lateinit var getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase

    private lateinit var json: Json

    private lateinit var httpClient: HttpClient

    private lateinit var okHttpClient: OkHttpClient


    @BeforeTest
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(
            UmAppDatabase::class, "jdbc:sqlite::memory:", nodeId = 1L
        ).build()

        json = Json {
            encodeDefaults = true
        }

        ustadCache = newTestUstadCache(temporaryFolder)
        okHttpClient = newTestOkHttpClient(temporaryFolder, cache = ustadCache)
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
            FileMimeTypeHelperImpl())
        getStoragePathForUrlUseCase = GetStoragePathForUrlUseCaseCommonJvm(
            httpClient = httpClient,
            cache = ustadCache,
        )
    }

    @Test
    fun givenValidPdf_whenExtractMetadataCalled_thenWillReturnMetadataEntry() {
        val testPdfFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/validPDFMetadata.pdf")

        val pdfPlugin = PdfContentImporterJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            uriHelper = uriHelper,
        )

        val metadata = runBlocking {
            pdfPlugin.extractMetadata(testPdfFile.toDoorUri(),
                "validPDFMetadata.pdf")
        }
        assertEquals("A Valid PDF for testing", metadata?.entry?.title)
        assertEquals("Varuna Singh", metadata?.entry?.author)
        assertEquals( "validPDFMetadata.pdf", metadata?.originalFilename)
    }

    @Test
    fun givenFileNotPdf_whenExtractMetadataCalled_thenWillReturnNull() {
        val testNotPdfFile = temporaryFolder.newFile()
        testNotPdfFile.writeText("Hello World")

        val pdfPlugin = PdfContentImporterJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            uriHelper = uriHelper,
        )

        val metadata = runBlocking {
            pdfPlugin.extractMetadata(testNotPdfFile.toDoorUri(),
                "testFile.txt")
        }

        assertNull(metadata)
    }

    @Test
    fun givenFileShouldBePdf_whenDataIsNotValid_thenWillThrowInvalidContentException() {
        val invalidPdf = temporaryFolder.newFile()
        invalidPdf.writeText("Hello World")

        val pdfPlugin = PdfContentImporterJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            uriHelper = uriHelper,
        )

        runBlocking {
            try {
                pdfPlugin.extractMetadata(invalidPdf.toDoorUri(), "testFile.pdf")
                throw IllegalStateException("Should not make it here")
            }catch(e: InvalidContentException) {
                assertNotNull(e)
            }
        }
    }

    @Test
    fun givenValidPdf_whenAddedToCached_thenDataShouldMatch() {
        val testPdfFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/validPDFMetadata.pdf")

        val pdfPlugin = PdfContentImporterJvm(
            endpoint = activeEndpoint,
            db = db,
            cache = ustadCache,
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            uriHelper = uriHelper,
        )

        val result = runBlocking {
            pdfPlugin.importContent(
                jobItem = ContentEntryImportJob(
                    sourceUri = testPdfFile.toDoorUri().toString(),
                    cjiOriginalFilename = "validPDFMetadata.pdf",
                ),
                progressListener =  { }
            )
        }

        val manifestUrl = result.cevManifestUrl
        val manifestResponse = ustadCache.retrieve(requestBuilder(manifestUrl!!))
        val manifest = json.decodeFromString(
            ContentManifest.serializer(),
            manifestResponse!!.bodyAsSource()!!.readString()
        )

        val pdfBlobUrl = manifest.entries.first().bodyDataUrl

        ustadCache.assertCachedBodyMatchesFileContent(
            url = pdfBlobUrl,
            file = testPdfFile,
        )
    }

    @Test
    fun givenValidPdfWithUrl_whenImported_thenDataShouldMatch() {
        val testPdfFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/validPDFMetadata.pdf")
        val mockWebServer = MockWebServer()
        mockWebServer.dispatcher = ResourcesDispatcher(this::class.java)
        try {
            val pdfPlugin = PdfContentImporterJvm(
                endpoint = activeEndpoint,
                db = db,
                cache = ustadCache,
                saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                json = json,
                getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
                uriHelper = uriHelper,
            )

            val result = runBlocking {
                pdfPlugin.importContent(
                    jobItem = ContentEntryImportJob(
                        sourceUri = testPdfFile.toDoorUri().toString(),
                        cjiOriginalFilename = mockWebServer.url("/com/ustadmobile/core/container/validPDFMetadata.pdf").toString(),
                    ),
                    progressListener =  { }
                )
            }

            val manifestUrl = result.cevManifestUrl
            val manifestResponse = ustadCache.retrieve(requestBuilder(manifestUrl!!))
            val manifest = json.decodeFromString(
                ContentManifest.serializer(),
                manifestResponse!!.bodyAsSource()!!.readString()
            )

            val pdfBlobUrl = manifest.entries.first().bodyDataUrl

            ustadCache.assertCachedBodyMatchesFileContent(
                url = pdfBlobUrl,
                file = testPdfFile,
            )
        }finally {
            mockWebServer.close()
        }
    }


}