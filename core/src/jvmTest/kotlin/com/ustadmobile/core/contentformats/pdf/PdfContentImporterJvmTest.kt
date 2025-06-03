package com.ustadmobile.core.contentformats.pdf

import com.ustadmobile.core.contentformats.AbstractContentImporterTest
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.domain.compress.pdf.CompressPdfUseCase
import com.ustadmobile.core.domain.compress.pdf.CompressPdfUseCaseJvm
import com.ustadmobile.core.test.assertCachedBodyMatchesFileContent
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.util.SysPathUtil
import com.ustadmobile.ihttp.request.iRequestBuilder
import com.ustadmobile.libcache.response.bodyAsString
import com.ustadmobile.util.test.ResourcesDispatcher
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PdfContentImporterJvmTest : AbstractContentImporterTest() {

    private var compressPdfUseCase: CompressPdfUseCase? = null

    @BeforeTest
    fun setupPdf() {
        val gsPath = SysPathUtil.findCommandInPath("gs")
        compressPdfUseCase = if(gsPath != null) {
            CompressPdfUseCaseJvm(gsPath, temporaryFolder.newFolder())
        }else {
            null
        }
    }

    @Test
    fun givenValidPdf_whenExtractMetadataCalled_thenWillReturnMetadataEntry() {
        val testPdfFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/validPDFMetadata.pdf")

        val pdfPlugin = PdfContentImporterJvm(
            learningSpace = activeLearningSpace,
            db = db,
            cache = ustadCache,
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            uriHelper = uriHelper,
            compressPdfUseCase = compressPdfUseCase,
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
            learningSpace = activeLearningSpace,
            db = db,
            cache = ustadCache,
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            uriHelper = uriHelper,
            compressPdfUseCase = compressPdfUseCase,
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
            learningSpace = activeLearningSpace,
            db = db,
            cache = ustadCache,
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            uriHelper = uriHelper,
            compressPdfUseCase = compressPdfUseCase,
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
            learningSpace = activeLearningSpace,
            db = db,
            cache = ustadCache,
            saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
            json = json,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            uriHelper = uriHelper,
            compressPdfUseCase = compressPdfUseCase,
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
        val manifestResponse = ustadCache.retrieve(iRequestBuilder(manifestUrl!!))
        val manifest = json.decodeFromString(
            ContentManifest.serializer(),
            manifestResponse!!.bodyAsString()!!
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
                learningSpace = activeLearningSpace,
                db = db,
                cache = ustadCache,
                saveLocalUriAsBlobAndManifestUseCase = saveAndManifestUseCase,
                json = json,
                getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
                uriHelper = uriHelper,
                compressPdfUseCase = compressPdfUseCase,
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
            val manifestResponse = ustadCache.retrieve(iRequestBuilder(manifestUrl!!))
            val manifest = json.decodeFromString(
                ContentManifest.serializer(),
                manifestResponse!!.bodyAsString()!!
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