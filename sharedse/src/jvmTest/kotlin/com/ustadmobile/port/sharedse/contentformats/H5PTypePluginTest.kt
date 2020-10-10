package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.container.ZipEntrySource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.contentformats.h5p.H5PTypeFilePlugin
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kmp.io.KMPPullParser
import org.kmp.io.KMPXmlParser
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.util.zip.GZIPInputStream
import java.util.zip.ZipFile

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

        val h5pPlugin = H5PTypeFilePlugin()
        val contentEntry = h5pPlugin.getContentEntry(tempH5pFile)
//        Assert.assertEquals("Got ContentEntry with expected entryId",
//                "dialog-cards-620.h5p",
//                contentEntry?.entryId)
        Assert.assertEquals("Got ContentEntry with expected title",
                "Dialog Cards",
                contentEntry?.title)
        Assert.assertEquals("Got ContentEntry with expected title",
                "Samih",
                contentEntry?.author)
        Assert.assertEquals("Got Entry with expected license",
                ContentEntry.LICENSE_TYPE_OTHER, contentEntry?.licenseType)
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
            val (contentEntry, container) = importContentEntryFromFile(tempH5pFile, db, dbRepo,
                    containerTmpDir.absolutePath, Any())!!
            Assert.assertNotNull(contentEntry)

            // Assert Entry
            //Temporarily disabled
//            Assert.assertEquals("Got ContentEntry with expected entryId",
//                    "dialog-cards-620.h5p",
//                    contentEntry.entryId)
            Assert.assertEquals("Got ContentEntry with expected title",
                    "Dialog Cards",
                    contentEntry.title)
            Assert.assertEquals("Got ContentEntry with expected title",
                    "Samih",
                    contentEntry.author)
            Assert.assertEquals("Got Entry with expected license",
                    ContentEntry.LICENSE_TYPE_OTHER, contentEntry.licenseType)


            Assert.assertNotNull(container)

            // Assert Container

            val tinCanEntry = db.containerEntryDao.findByPathInContainer(container.containerUid, "tincan.xml")
            Assert.assertNotNull(tinCanEntry)

            val file = File(tinCanEntry!!.containerEntryFile!!.cefPath)
            val xpp = KMPXmlParser()
            xpp.setInput(GZIPInputStream(file.inputStream()), "UTF-8")
            val tinCanXml = TinCanXML.loadFromXML(xpp)

            val launchHref = tinCanXml.launchActivity?.launchUrl
            Assert.assertEquals("launch Url is index.html", launchHref, "index.html")

            val indexEntry = db.containerEntryDao.findByPathInContainer(container.containerUid, launchHref!!)
            Assert.assertNotNull(indexEntry)

            // walk through the zip entries and check all exists in containerEntry
            ZipFile(tempH5pFile).entries().toList()
                    .filter { !it.isDirectory }
                    .forEach {
                        Assert.assertNotNull("File exists: $it", db.containerEntryDao.findByPathInContainer(container.containerUid, "workspace/${it.name}"))
                    }





        }
    }

}