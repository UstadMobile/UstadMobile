package com.ustadmobile.core.contentformats.directory

import com.ustadmobile.core.contentformats.AbstractContentImporterTest
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.contentformats.ContentImportersManager
import com.ustadmobile.core.contentformats.epub.EpubContentImporterCommonJvm
import com.ustadmobile.core.contentformats.h5p.H5PContentImporter
import com.ustadmobile.core.contentformats.pdf.PdfContentImporterJvm
import com.ustadmobile.core.contentformats.video.VideoContentImporterCommonJvm
import com.ustadmobile.core.contentformats.xapi.XapiZipContentImporter
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueContentEntryImportUseCase
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DirectoryContentImporterTest : AbstractContentImporterTest() {

    private lateinit var directoryContentImporter: DirectoryContentImporter
    private lateinit var contentImportersManager: ContentImportersManager
    private lateinit var enqueueContentEntryImportUseCase: EnqueueContentEntryImportUseCase
    private lateinit var listDirectoryUriUseCase: ListDirectoryUriUseCase

    @BeforeTest
    fun setUp() {
        // Set up mocked importers with distinct plugin IDs
        val mockVideoImporter = mock<VideoContentImporterCommonJvm> {
            on { importerId } doReturn 101
        }
        val mockPdfImporter = mock<PdfContentImporterJvm> {
            on { importerId } doReturn 111
        }
        val mockH5PImporter = mock<H5PContentImporter> {
            on { importerId } doReturn 424
        }
        val mockEpubImporter = mock<EpubContentImporterCommonJvm> {
            on { importerId } doReturn 2
        }
        val mockXapiImporter = mock<XapiZipContentImporter> {
            on { importerId } doReturn 8
        }

        // Initialize the manager and use case mocks
        contentImportersManager = mock()
        enqueueContentEntryImportUseCase = mock()
        listDirectoryUriUseCase = mock()

        // Initialize DirectoryContentImporter with the mocked dependencies
        directoryContentImporter = DirectoryContentImporter(
            endpoint = activeEndpoint,
            db = db,
            otherContentImportersList = listOf(
                mockH5PImporter,
                mockPdfImporter,
                mockXapiImporter,
                mockVideoImporter,
                mockEpubImporter
            ),
            enqueueContentEntryImportUseCase = enqueueContentEntryImportUseCase,
            listDirectoryUriUseCase = listDirectoryUriUseCase
        )
    }

    @Test
    fun `given valid folder, when extractMetadata called, then returns valid metadata`() {
        // Arrange: Create a temporary folder and a test PDF file
        val temporaryFolder = temporaryFolder.newFolder("test")
        val pdfFile = File(temporaryFolder, "file.pdf")
        this::class.java.getResourceAsStream("/com/ustadmobile/core/container/validPDFMetadata.pdf")!!
            .writeToFile(pdfFile)

        // Set up mock response to recognize the directory
        whenever(listDirectoryUriUseCase.isDirectory(any())).thenReturn(true)

        // Act: Call extractMetadata
        val metadataResults = runBlocking {
            directoryContentImporter.extractMetadata(
                uri = temporaryFolder.toDoorUri(),
                originalFilename = temporaryFolder.name
            )
        }

        // Assert: Validate the extracted metadata
        assertEquals("test", metadataResults?.originalFilename)
        assertEquals(false, metadataResults?.entry?.leaf)
    }

    @Test
    fun `given directory with valid files, when importContent called, then enqueues import jobs`() {

        val temporaryFolder = temporaryFolder.newFolder("testFolder")
        val pdfFile = File(temporaryFolder, "file.pdf")
        this::class.java.getResourceAsStream("/com/ustadmobile/core/container/validPDFMetadata.pdf")!!
            .writeToFile(pdfFile)

        whenever(listDirectoryUriUseCase.invoke(temporaryFolder.toDoorUri().toString())).thenReturn(
            listOf(
                ListDirectoryUriUseCase.ListDirectoryItem(
                    uri = pdfFile.toDoorUri(),
                    fileName = pdfFile.name
                )
            )
        )

        val mockProgressListener = mock<ContentImportProgressListener>()
        val jobItem = ContentEntryImportJob(
            sourceUri = temporaryFolder.toDoorUri().toString(),
            cjiOriginalFilename = temporaryFolder.name,
            cjiPluginId = directoryContentImporter.importerId
        )

        val result = runBlocking {
            directoryContentImporter.importContent(jobItem, mockProgressListener)
        }

        // Assert: Validate import result
        assertNotNull(result, "Import result should not be null")
        assertEquals(ContentEntryVersion.TYPE_DIRECTORY, result.cevContentType)
    }
}
