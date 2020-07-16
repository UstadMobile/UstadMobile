/*
package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.asRepository
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import org.junit.Assert
import org.junit.Test
import java.io.File
import com.ustadmobile.port.sharedse.contentformats.extractContentEntryMetadataFromFile
import kotlinx.coroutines.runBlocking
import java.nio.file.Files

class ContentTypePluginsTest {

    private val context = Any()

    */
/*TODO: this needs fixed
    @Test
    fun givenValidEpubFormatFile_whenExtractEntryMetaDataFromFile_thenDataShouldMatch() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/port/sharedse/contentformats/childrens-literature.epub")
        val tempEpubFile = File.createTempFile("importFile", "epub")
        tempEpubFile.copyInputStreamToFile(inputStream)

        val contentEntryExtracted = extractContentEntryMetadataFromFile(tempEpubFile)
        Assert.assertNotNull(contentEntryExtracted)
        Assert.assertEquals("Title received as expected",
                "A Textbook of Sources for Teachers and Teacher-Training Classes",
                contentEntryExtracted!!.contentEntry!!.title)
    }
     *//*


    @Test
    fun givenValidEpubFormatFile_whenImportContentEntryFromFile_thenContentEntryAndContainerShouldExist() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/port/sharedse/contentformats/childrens-literature.epub")
        val tempEpubFile = File.createTempFile("importFile", "epub")
        tempEpubFile.copyInputStreamToFile(inputStream)

        val containerTmpDir = Files.createTempDirectory("containerTmpDir").toFile()

        val db = UmAppDatabase.getInstance(context)
        db.clearAllTables()
        val dbRepo = db.asRepository<UmAppDatabase>(context, "http://localhost/dummy", "",
                defaultHttpClient(), containerTmpDir.absolutePath)

        runBlocking {
            //TODO: Make this more rigorous
            val (contentEntry, container) = importContentEntryFromFile(tempEpubFile, db, dbRepo,
                    containerTmpDir.absolutePath)!!
            Assert.assertNotNull(contentEntry)
            Assert.assertNotNull(container)
        }

        containerTmpDir.deleteRecursively()
        tempEpubFile.delete()
    }

    @Test
    fun givenValidEpubFormatFile_whenImported_shouldCreateNewEntry() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/port/sharedse/contentformats/childrens-literature.epub")
        val tempEpubFile = File.createTempFile("importFile", "epub")
        tempEpubFile.copyInputStreamToFile(inputStream)

        val contentEntry =  ContentTypeUtil.getContent(tempEpubFile)

        tempEpubFile.deleteOnExit()

        Assert.assertTrue("ContentEntry created successfully", contentEntry.isNotEmpty())

    }


    @Test
    fun givenValidTinCanFormatFile_whenImported_shouldCreateNewEntry() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/port/sharedse/contentformats/ustad-tincan.zip")
        val tempH5PFile = File.createTempFile("importFile", "tincan")
        tempH5PFile.copyInputStreamToFile(inputStream)

        val contentEntry =  ContentTypeUtil.getContent(tempH5PFile)

        tempH5PFile.deleteOnExit()

        Assert.assertTrue("ContentEntry created successfully", contentEntry.isNotEmpty())

    }


    @Test
    fun givenUnsupportedFileFormat_whenImported_shouldCreateNewEntry(){
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/port/sharedse/contentformats/unsupported.zip")
        val tempH5PFile = File.createTempFile("importFile", "zip")
        tempH5PFile.copyInputStreamToFile(inputStream)

        val contentEntry =  ContentTypeUtil.getContent(tempH5PFile)

        tempH5PFile.deleteOnExit()

        Assert.assertTrue("ContentEntry wasn't created successfully", contentEntry.isEmpty())

    }
}*/
