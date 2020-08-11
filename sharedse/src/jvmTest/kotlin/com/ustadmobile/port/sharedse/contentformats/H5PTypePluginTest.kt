package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.asRepository
import com.ustadmobile.port.sharedse.contentformats.h5p.H5PTypeFilePlugin
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files

class H5PTypePluginTest {

    @JvmField
    @Rule
    val tmpFolder = TemporaryFolder()

    private val context = Any()


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
        Assert.assertEquals("Got ContentEntry with expected title",
                "Samih",
                contentEntry?.author)
    }

    @Test
    fun givenValidH5PFile_whenImportContentEntryFromFile_thenContentEntryAndContainerShouldExist() {
        val tempFolder = tmpFolder.newFolder()
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/port/sharedse/contentformats/dialog-cards-620.h5p")
        val tempH5pFile = File(tempFolder, "dialog-cards-620.h5p")
        tempH5pFile.copyInputStreamToFile(inputStream)

        val containerTmpDir = tmpFolder.newFolder("containerTmpDir")

        val db = UmAppDatabase.getInstance(context)
        db.clearAllTables()
        val dbRepo = db.asRepository<UmAppDatabase>(context, "http://localhost/dummy", "",
                defaultHttpClient(), containerTmpDir.absolutePath)

        runBlocking {
            //TODO: Make this more rigorous
            val (contentEntry, container) = importContentEntryFromFile(tempH5pFile, db, dbRepo,
                    containerTmpDir.absolutePath, Any())!!
            Assert.assertNotNull(contentEntry)
            Assert.assertNotNull(container)
        }
    }

}