package com.ustadmobile.core.contentformats.pdf

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCaseJvm
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCaseJvm
import com.ustadmobile.core.test.assertCachedBodyMatchesFileContent
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
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

    private lateinit var di: DI

    private lateinit var activeEndpoint: Endpoint

    private lateinit var saveLocalUrisUseCase: SaveLocalUrisAsBlobsUseCase

    private lateinit var saveAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase

    private lateinit var json: Json


    @BeforeTest
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        val accountManager: UstadAccountManager by di.instance()
        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        json = di.direct.instance()
        activeEndpoint = accountManager.activeEndpoint

        ustadCache = UstadCacheBuilder(
            dbUrl = "jdbc:sqlite::memory:",
            storagePath = Path(temporaryFolder.newFolder().absolutePath),
        ).build()

        uriHelper = UriHelperJvm(
            mimeTypeHelperImpl = FileMimeTypeHelperImpl(),
            httpClient = di.direct.instance(),
            okHttpClient = di.direct.instance(),
        )

        //Strictly speaking should be mocked, in reality, it's easier to just use the real thing.
        saveLocalUrisUseCase = SaveLocalUrisAsBlobsUseCaseJvm(
            endpoint = activeEndpoint,
            cache = ustadCache,
            uriHelper = uriHelper,
            tmpDir = Path(temporaryFolder.newFolder().absolutePath),
        )

        saveAndManifestUseCase = SaveLocalUriAsBlobAndManifestUseCaseJvm(saveLocalUrisUseCase)
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

        val manifestUrl = result.cevSitemapUrl
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

}