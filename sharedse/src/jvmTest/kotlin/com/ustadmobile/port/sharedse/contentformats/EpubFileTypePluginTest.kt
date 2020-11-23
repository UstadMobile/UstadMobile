package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.catalog.contenttype.EpubTypePluginCommonJvm
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import kotlinx.coroutines.runBlocking
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

        val epubPlugin = EpubTypePluginCommonJvm()
        runBlocking {
            val contentEntry = epubPlugin.extractMetadata(tempEpubFile.path)
            Assert.assertEquals("Got ContentEntry with expected title",
                    "A Textbook of Sources for Teachers and Teacher-Training Classes",
                    contentEntry?.title)
        }

    }

}