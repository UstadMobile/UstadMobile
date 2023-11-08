package com.ustadmobile.core.contentformats.pdf

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.test.assertCachedBodyMatchesFileContent
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.uri.UriHelperJvm
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import com.ustadmobile.libcache.FileMimeTypeHelperImpl
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.UstadCacheBuilder
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
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

    @BeforeTest
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        val accountManager: UstadAccountManager by di.instance()
        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
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
    }

    @Test
    fun givenValidPdf_whenExtractMetadataCalled_thenWillReturnMetadataEntry() {
        val testPdfFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/validPDFMetadata.pdf")

        val pdfPlugin = PdfContentImporterJvm(
            endpoint = activeEndpoint,
            di = di,
            cache = ustadCache,
            uriHelper = uriHelper
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
            di = di,
            cache = ustadCache,
            uriHelper = uriHelper
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
            di = di,
            cache = ustadCache,
            uriHelper = uriHelper
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
            di = di,
            cache = ustadCache,
            uriHelper = uriHelper
        )

        val jobAndItem = ContentJobItemAndContentJob().apply{
            this.contentJob = ContentJob()
            this.contentJobItem = ContentJobItem(
                sourceUri = testPdfFile.toDoorUri().toString(),
                cjiOriginalFilename = "validPDFMetadata.pdf",
            )
        }

        val result = runBlocking {
            pdfPlugin.addToCache(
                jobItem = jobAndItem,
                progressListener =  { }
            )
        }

        ustadCache.assertCachedBodyMatchesFileContent(
            url = result.cevUrl!!,
            file = testPdfFile,
        )
    }

}