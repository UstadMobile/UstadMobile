package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.port.sharedse.contentformats.epub.EpubTypeFilePlugin
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class EpubFileTypePluginTest {

    @JvmField
    @Rule
    val tmpFolder = TemporaryFolder()

    @Test
    fun givenValidEpubFormatFile_whenExtractEntryMetaDataFromFile_thenDataShouldMatch() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/port/sharedse/contentformats/childrens-literature.epub")
        val tempEpubFile = tmpFolder.newFile()
        tempEpubFile.copyInputStreamToFile(inputStream)

        val epubPlugin = EpubTypeFilePlugin()
        val contentEntry = epubPlugin.getContentEntry(tempEpubFile)
        Assert.assertEquals("Got ContentEntry with expected title",
                "A Textbook of Sources for Teachers and Teacher-Training Classes",
                contentEntry?.title)
    }

}