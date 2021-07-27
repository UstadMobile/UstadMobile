package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.ZipFile

class H5PTypePluginTest {

    private lateinit var h5pPlugin: H5PTypePluginCommonJvm

    @JvmField
    @Rule
    val tmpFolder = TemporaryFolder()

    private val context = Any()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    @Before
    fun setup(){

        di = DI {
            import(ustadTestRule.diModule)
        }
        val accountManager: UstadAccountManager by di.instance()
        db = di.on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_DB)
        repo = di.on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)
        h5pPlugin = H5PTypePluginCommonJvm(Any(), accountManager.activeEndpoint, di)
    }

    @Test
    fun givenValidH5PFile_whenExtractEntryMetaDataFromFile_thenDataShouldMatch() {
        val tempFolder = tmpFolder.newFolder()
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/contenttype/dialog-cards-620.h5p")
        val tempH5pFile = File(tempFolder, "dialog-cards-620.h5p")
        tempH5pFile.copyInputStreamToFile(inputStream)

        val contentEntry = runBlocking {
            h5pPlugin.extractMetadata(DoorUri.parse(tempH5pFile.toURI().toString()), ProcessContext(""))
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
                "/com/ustadmobile/core/contenttype/dialog-cards-620.h5p")
        val tempH5pFile = File(tempFolder, "dialog-cards-620.h5p")
        tempH5pFile.copyInputStreamToFile(inputStream)

        val containerTmpDir = tmpFolder.newFolder("containerTmpDir")
        runBlocking {
            val contentEntry = h5pPlugin.extractMetadata(DoorUri.parse(tempH5pFile.toURI().toString()), ProcessContext(""))!!

            Assert.assertNotNull(contentEntry)

            Assert.assertEquals("Got ContentEntry with expected title",
                    "Dialog Cards",
                    contentEntry.title)
            Assert.assertEquals("Got ContentEntry with expected title",
                    "Samih",
                    contentEntry.author)
            Assert.assertEquals("Got Entry with expected license",
                    ContentEntry.LICENSE_TYPE_OTHER, contentEntry.licenseType)

            val uid = repo.contentEntryDao.insert(contentEntry)

            val contentJob = ContentJobItem(fromUri = tempH5pFile.toURI().toString(),
                    toUri = containerTmpDir.toURI().toString(), cjiContentEntryUid =  uid)

            runBlocking{
                h5pPlugin.processJob(contentJob, ProcessContext(""))
            }

            val container = repo.containerDao.getMostRecentContainerForContentEntry(uid)!!

            Assert.assertNotNull(container)

            // Assert Container

            val tinCanEntry = db.containerEntryDao.findByPathInContainer(container.containerUid, "tincan.xml")
            Assert.assertNotNull(tinCanEntry)

            val file = File(tinCanEntry!!.containerEntryFile!!.cefPath)
            val xpp = XmlPullParserFactory.newInstance().newPullParser()
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
