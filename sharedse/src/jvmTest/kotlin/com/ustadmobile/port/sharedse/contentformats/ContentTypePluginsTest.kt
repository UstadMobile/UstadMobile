package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import org.junit.Assert
import org.junit.Test
import java.io.File

class ContentTypePluginsTest {

    private val context = Any()

    @Test
    fun givenValidEpubFormatFile_whenImported_shouldCreateNewEntry() {
        val inputStream = UstadMobileSystemImpl.instance.getAssetSync(context,
                "/com/ustadmobile/port/sharedse/contentformats/childrens-literature.epub")
        val tempEpubFile = File.createTempFile("importFile", "epub")
        tempEpubFile.copyInputStreamToFile(inputStream)

        val contentEntry =  ContentTypeUtil.getContent(tempEpubFile)

        tempEpubFile.deleteOnExit()

        Assert.assertTrue("ContentEntry created successfully", contentEntry.isNotEmpty())

    }


    @Test
    fun givenValidTinCanFormatFile_whenImported_shouldCreateNewEntry() {
        val inputStream = UstadMobileSystemImpl.instance.getAssetSync(context,
                "/com/ustadmobile/port/sharedse/contentformats/ustad-tincan.zip")
        val tempH5PFile = File.createTempFile("importFile", "tincan")
        tempH5PFile.copyInputStreamToFile(inputStream)

        val contentEntry =  ContentTypeUtil.getContent(tempH5PFile)

        tempH5PFile.deleteOnExit()

        Assert.assertTrue("ContentEntry created successfully", contentEntry.isNotEmpty())

    }


    @Test
    fun givenUnsupportedFileFormat_whenImported_shouldCreateNewEntry(){
        val inputStream = UstadMobileSystemImpl.instance.getAssetSync(context,
                "/com/ustadmobile/port/sharedse/contentformats/unsupported.zip")
        val tempH5PFile = File.createTempFile("importFile", "zip")
        tempH5PFile.copyInputStreamToFile(inputStream)

        val contentEntry =  ContentTypeUtil.getContent(tempH5PFile)

        tempH5PFile.deleteOnExit()

        Assert.assertTrue("ContentEntry wasn't created successfully", contentEntry.isEmpty())

    }
}