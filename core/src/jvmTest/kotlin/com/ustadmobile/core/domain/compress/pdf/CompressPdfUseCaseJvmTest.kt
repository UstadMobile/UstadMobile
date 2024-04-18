package com.ustadmobile.core.domain.compress.pdf

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.util.SysPathUtil
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.util.test.initNapierLog
import kotlinx.coroutines.runBlocking
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CompressPdfUseCaseJvmTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var tempDir: File

    private var gsPath: File? = null

    @BeforeTest
    fun setup() {
        initNapierLog()
        tempDir = temporaryFolder.newFolder()
        gsPath = SysPathUtil.findCommandInPath("gs")
        Assume.assumeTrue(gsPath?.exists() == true)
    }

    @Test
    fun givenValidPdf_whenInvoked_thenWillCompress() {
        val pdfFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/validPDFMetadata.pdf")

        val useCase = CompressPdfUseCaseJvm(gsPath = gsPath!!, workDir = tempDir)
        runBlocking {
            val result = useCase(
                pdfFile.toDoorUri().toString(),
            )

            assertNotNull(result)
            val compressedFile = DoorUri.parse(result.uri).toFile()
            assertTrue(compressedFile.exists())

            val originalPdfDoc: PDDocument = Loader.loadPDF(pdfFile)
            val compressedPdfDoc: PDDocument = Loader.loadPDF(compressedFile)
            assertEquals(originalPdfDoc.numberOfPages, compressedPdfDoc.numberOfPages,
                "Compressed and original document both have same number of pages")
        }
    }

}