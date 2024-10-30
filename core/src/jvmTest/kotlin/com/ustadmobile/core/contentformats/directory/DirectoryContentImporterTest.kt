package com.ustadmobile.core.contentformats.directory

import com.ustadmobile.core.contentformats.AbstractContentImporterTest
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentformats.ContentImportersManager
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueContentEntryImportUseCase
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.any
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

    @BeforeTest
    fun setUp() {
        contentImportersManager = mock()
        enqueueContentEntryImportUseCase = mock()

        directoryContentImporter = DirectoryContentImporter(
            endpoint = activeEndpoint,
            db = db,
            getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
            contentImportersManager = contentImportersManager,
            enqueueContentEntryImportUseCase = enqueueContentEntryImportUseCase,
            uriHelper = uriHelper
        )
    }

    @Test
    fun givenValidFolder_whenExtractMetadataCalled_thenReturnsValidMetadata() {
        val temporaryFolder = temporaryFolder.newFolder("test")
        val pdfFile = File(temporaryFolder, "file.pdf")
        this::class.java.getResourceAsStream("/com/ustadmobile/core/container/validPDFMetadata.pdf")!!
            .writeToFile(pdfFile)

        // Validate the directory and extract metadata
        val metadataResults = runBlocking {
            directoryContentImporter.extractMetadata(
                uri = temporaryFolder.toDoorUri(),
                originalFilename = temporaryFolder.name
            )
        }

        assertEquals("test", metadataResults.originalFilename)
        assertEquals(false, metadataResults.entry.leaf)

    }

    @Test
    fun givenDirectoryWithValidFiles_whenImportContentCalled_thenEnqueuesImportJobs() {
        val temporaryFolder = temporaryFolder.newFolder("testFolder")
        val pdfFile = File(temporaryFolder, "file.pdf")
        this::class.java.getResourceAsStream("/com/ustadmobile/core/container/validPDFMetadata.pdf")!!
            .writeToFile(pdfFile)


        // Mock behavior for extractMetadata
        runBlocking {
            whenever(contentImportersManager.extractMetadata(any(), any())).thenAnswer {
                val entryName = it.arguments[1] as String
                MetadataResult(
                    entry = mock(),
                    importerId = directoryContentImporter.importerId,
                    originalFilename = entryName
                )
            }
        }


        // Create a mock progress listener
        val mockProgressListener = mock<ContentImportProgressListener>()

        // Create a ContentEntryImportJob for the import
        val jobItem = ContentEntryImportJob(
            sourceUri = temporaryFolder.toDoorUri().toString(),
            cjiOriginalFilename = temporaryFolder.name,
            cjiPluginId = directoryContentImporter.importerId
        )

        val result = runBlocking {
            directoryContentImporter.importContent(jobItem, mockProgressListener)
        }

        assertNotNull(result, "Import result should not be null")
        assertEquals(ContentEntryVersion.TYPE_DIRECTORY, result.cevContentType)

        runBlocking {
            verify(enqueueContentEntryImportUseCase, times(1)).invoke(any())
        }
    }

    @Test
    fun givenDirectoryWithVideoFile_whenImportContentCalled_thenEnqueuesVideoImportJob() {
        val temporaryFolder = temporaryFolder.newFolder("videoTestFolder")
        val videoFile = File(temporaryFolder, "BigBuckBunny.mp4")
        this::class.java.getResourceAsStream("/com/ustadmobile/core/container/BigBuckBunny.mp4")!!
            .writeToFile(videoFile)

        // Mock behavior for extractMetadata for the video file
        runBlocking {
            whenever(contentImportersManager.extractMetadata(any(), any())).thenAnswer {
                val entryName = it.arguments[1] as String
                MetadataResult(
                    entry = mock(),
                    importerId = directoryContentImporter.importerId,
                    originalFilename = entryName
                )
            }
        }

        val mockProgressListener = mock<ContentImportProgressListener>()
        val jobItem = ContentEntryImportJob(
            sourceUri = temporaryFolder.toDoorUri().toString(),
            cjiOriginalFilename = temporaryFolder.name,
            cjiPluginId = directoryContentImporter.importerId
        )
        runBlocking {
            val result = directoryContentImporter.importContent(jobItem, mockProgressListener)

            assertNotNull(result, "Import result should not be null")
            assertEquals(ContentEntryVersion.TYPE_DIRECTORY, result.cevContentType)

            verify(enqueueContentEntryImportUseCase, times(1)).invoke(any())
        }
    }
}

