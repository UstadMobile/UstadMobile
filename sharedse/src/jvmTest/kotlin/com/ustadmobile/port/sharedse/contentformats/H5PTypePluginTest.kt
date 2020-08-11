package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.port.sharedse.contentformats.h5p.H5PTypeFilePlugin
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class H5PTypePluginTest {

    @JvmField
    @Rule
    val tmpFolder = TemporaryFolder()


    @Test
    fun givenValidH5PFile_whenExtractEntryMetaDataFromFile_thenDataShouldMatch() {
        val tempFolder = tmpFolder.newFolder()
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/port/sharedse/contentformats/dialog-cards-620.h5p")
        val tempH5pFile = File(tempFolder, "dialog-cards-620.h5p")
        tempH5pFile.copyInputStreamToFile(inputStream)

        val h5pPlugin =  H5PTypeFilePlugin()
        val contentEntry = h5pPlugin.getContentEntry(tempH5pFile)
        Assert.assertEquals("Got ContentEntry with expected entryId",
                "dialog-cards-620.h5p",
                contentEntry?.entryId)
        Assert.assertEquals("Got ContentEntry with expected title",
                "Dialog Cards",
                contentEntry?.title)
    }

}