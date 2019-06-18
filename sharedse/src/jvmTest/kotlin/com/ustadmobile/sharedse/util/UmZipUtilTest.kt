package com.ustadmobile.sharedse.util

import com.ustadmobile.port.sharedse.util.UmZipUtils
import org.apache.commons.io.FileUtils
import org.junit.Assert
import org.junit.Test

import java.io.File
import java.io.IOException
import java.nio.file.Files

class UmZipUtilTest {

    @Test
    @Throws(IOException::class)
    fun givenAnEpubFile_whenUnzipped_thenAllFilesShouldBeAvailable() {

        val tmpDir = Files.createTempDirectory("testZipUtils").toFile()
        val targetFile = File("test.epub")
        FileUtils.copyToFile(javaClass.getResourceAsStream("/com/ustadmobile/test.epub"), targetFile)

        UmZipUtils.unzip(targetFile, tmpDir)

        Assert.assertEquals(3, tmpDir.listFiles()!!.size.toLong())


    }

}
