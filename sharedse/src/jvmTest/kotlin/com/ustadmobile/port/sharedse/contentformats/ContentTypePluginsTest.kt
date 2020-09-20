package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.asRepository
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import org.junit.Assert
import org.junit.Test
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.nio.file.Files

class ContentTypePluginsTest {

    private val context = Any()

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()


    @Test
    fun givenValidEpubFormatFile_whenImportContentEntryFromFile_thenContentEntryAndContainerShouldExist() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/port/sharedse/contentformats/childrens-literature.epub")
        val tempEpubFile = temporaryFolder.newFile("imported.epub")
        tempEpubFile.copyInputStreamToFile(inputStream)

        val containerTmpDir = Files.createTempDirectory("containerTmpDir").toFile()

        val db = UmAppDatabase.getInstance(context)
        db.clearAllTables()
        val dbRepo = db.asRepository<UmAppDatabase>(context, "http://localhost/dummy", "",
                defaultHttpClient(), containerTmpDir.absolutePath)

        runBlocking {
            //TODO: Make this more rigorous
            val (contentEntry, container) = importContentEntryFromFile(tempEpubFile.toURI().toString(), db, dbRepo,
                    containerTmpDir.absolutePath, Any())!!
            Assert.assertNotNull(contentEntry)
            Assert.assertNotNull(container)
        }

        containerTmpDir.deleteRecursively()
        tempEpubFile.delete()
    }


    @Test
    fun givenUnsupportedFileFormat_whenImported_shouldReturnNull(){
        val emptyFile = temporaryFolder.newFile("empty.zip")

        val db = UmAppDatabase.getInstance(context)
        db.clearAllTables()

        val contentEntry =  runBlocking {
            extractContentEntryMetadataFromFile(emptyFile.toURI().toString(), db)
        }

        Assert.assertNull("Given unsupported file, extractContentEntryMetaData returns null",
            contentEntry)
    }
}