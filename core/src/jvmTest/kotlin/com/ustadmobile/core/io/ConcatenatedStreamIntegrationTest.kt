package com.ustadmobile.core.io

import com.ustadmobile.door.ext.md5Sum
import com.ustadmobile.door.ext.writeToFile
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.*
import java.util.zip.GZIPOutputStream

class ConcatenatedStreamIntegrationTest {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    fun File.copyAndGzipTo(dest: File) {
        FileInputStream(this).use { fileIn ->
            GZIPOutputStream(FileOutputStream(dest)).use { gzipOut ->
                fileIn.copyTo(gzipOut)
                gzipOut.flush()
            }
        }
    }

    @Test
    fun givenInputData_whenConcatenatedAndDeconcatenated_thenShouldBeTheSame() {

        val byteArrayOut = ByteArrayOutputStream()
        val concatOut = ConcatenatedOutputStream2(byteArrayOut)
        val tmpFile1 = temporaryFolder.newFile()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/controller/cat-pic0.jpg")
                .writeToFile(tmpFile1)

        val tmpFile2 = temporaryFolder.newFile()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/controller/cat-pic1.jpg")
                .writeToFile(tmpFile2)

        concatOut.putNextEntry(ConcatenatedEntry(tmpFile1.md5Sum, 0, tmpFile1.length()))
        FileInputStream(tmpFile1).use {
            it.copyTo(concatOut)
        }

        concatOut.putNextEntry(ConcatenatedEntry(tmpFile2.md5Sum,0, tmpFile2.length()))
        FileInputStream(tmpFile2).use {
            it.copyTo(concatOut)
        }

        concatOut.flush()
        concatOut.close()

        val concatBytes = byteArrayOut.toByteArray()

        val concatIn = ConcatenatedInputStream2(ByteArrayInputStream(concatBytes))
        val concatEntry1 = concatIn.getNextEntry()
        val file1Content =  concatIn.readBytes()

        val concatEntry2 = concatIn.getNextEntry()
        val file2Content = concatIn.readBytes()

        concatIn.close()

        Assert.assertArrayEquals("Content entry 1 matches", tmpFile1.readBytes(), file1Content)
        Assert.assertArrayEquals("Content entry 2 matches", tmpFile2.readBytes(), file2Content)
    }


    @Test
    fun givenGzippedInputData_whenConcatenatedAndDeconcatenated_thenShouldBeTheSame() {
        val byteArrayOut = ByteArrayOutputStream()
        val concatOut = ConcatenatedOutputStream2(byteArrayOut)
        val tmpFile1 = temporaryFolder.newFile()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/controller/cat-pic0.jpg").writeToFile(tmpFile1)
        val tmpFile1Gzip = temporaryFolder.newFile()
        tmpFile1.copyAndGzipTo(tmpFile1Gzip)

        val tmpFile2 = temporaryFolder.newFile()
        val tmpFile2Gzip = temporaryFolder.newFile()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/controller/cat-pic1.jpg").writeToFile(tmpFile2)
        tmpFile2.copyAndGzipTo(tmpFile2Gzip)

        concatOut.putNextEntry(ConcatenatedEntry(tmpFile1.md5Sum, 1, tmpFile1Gzip.length()))
        FileInputStream(tmpFile1Gzip).use {
            it.copyTo(concatOut)
        }

        concatOut.putNextEntry(ConcatenatedEntry(tmpFile2.md5Sum,1, tmpFile2Gzip.length()))
        FileInputStream(tmpFile2Gzip).use {
            it.copyTo(concatOut)
        }

        concatOut.flush()
        concatOut.close()

        val concatBytes = byteArrayOut.toByteArray()

        val concatIn = ConcatenatedInputStream2(ByteArrayInputStream(concatBytes))
        val concatEntry1 = concatIn.getNextEntry()
        val file1Content =  concatIn.readBytes()

        val concatEntry2 = concatIn.getNextEntry()
        val file2Content = concatIn.readBytes()

        Assert.assertArrayEquals("Content entry 1 matches", tmpFile1Gzip.readBytes(), file1Content)
        Assert.assertArrayEquals("Content entry 2 matches", tmpFile2Gzip.readBytes(), file2Content)

        concatIn.close()

    }
}