package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.catalog.contenttype.EpubTypePluginCommonJvm
import com.ustadmobile.core.catalog.contenttype.H5PTypePluginCommonJvm
import com.ustadmobile.core.contentformats.ContentImportManager
import com.ustadmobile.core.contentformats.ContentImportManagerImpl
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import com.ustadmobile.sharedse.util.UstadTestRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kmp.io.KMPXmlParser
import org.kodein.di.*
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.ZipFile

class H5PTypePluginTest {

    @JvmField
    @Rule
    val tmpFolder = TemporaryFolder()

    private val context = Any()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var contentImportManager: ContentImportManager

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    @Before
    fun setup(){

        di = DI {
            import(ustadTestRule.diModule)
            bind<ContentImportManager>() with scoped(ustadTestRule.endpointScope!!).singleton {
                ContentImportManagerImpl(listOf(EpubTypePluginCommonJvm(), H5PTypePluginCommonJvm(context)), context, this.context, di)
            }
        }
        val accountManager: UstadAccountManager by di.instance()
        contentImportManager =  di.on(accountManager.activeAccount).direct.instance()
        db = di.on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_DB)
        repo = di.on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)

    }

    @Test
    fun givenValidH5PFile_whenExtractEntryMetaDataFromFile_thenDataShouldMatch() {
        val tempFolder = tmpFolder.newFolder()
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/port/sharedse/contentformats/dialog-cards-620.h5p")
        val tempH5pFile = File(tempFolder, "dialog-cards-620.h5p")
        tempH5pFile.copyInputStreamToFile(inputStream)

        val h5pPlugin = H5PTypePluginCommonJvm(context)
        val contentEntry = runBlocking {
            h5pPlugin.extractMetadata(tempH5pFile.path)
        }
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

        runBlocking {
            val metadata = contentImportManager.extractMetadata(tempH5pFile.path)!!
            val contentEntry = metadata.contentEntry
            val uid = repo.contentEntryDao.insertAsync(metadata.contentEntry)
            val container = contentImportManager.importFileToContainer(tempH5pFile.path,
                    metadata.mimeType, uid, containerTmpDir.absolutePath, mapOf()){

            }!!
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
