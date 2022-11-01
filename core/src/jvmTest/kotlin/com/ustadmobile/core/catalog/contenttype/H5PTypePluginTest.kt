package com.ustadmobile.core.catalog.contenttype


import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.DummyContentJobItemTransactionRunner
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.tincan.TinCanXML
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
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


    private lateinit var endpointScope: EndpointScope

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup(){
        Napier.base(DebugAntilog())
        endpointScope = EndpointScope()
        di = DI {
            import(ustadTestRule.diModule)
            bind<ContainerStorageManager>() with scoped(endpointScope).singleton {
                ContainerStorageManager(listOf(tmpFolder.newFolder()))
            }
        }



        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = ContentDispatcher()
        mockWebServer.start()
        val accountManager: UstadAccountManager by di.instance()

        db = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        val connectivityStatus = ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED, true, "NetworkSSID")
        db.connectivityStatusDao.insert(connectivityStatus)

        h5pPlugin = H5PTypePluginCommonJvm(Any(), accountManager.activeEndpoint, di)
    }

    @Test
    fun givenValidH5PFile_whenExtractEntryMetaDataFromFile_thenDataShouldMatch() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/contenttype/dialog-cards-620.h5p")
        val tempH5pFile = tmpFolder.newFile()
       tempH5pFile.copyInputStreamToFile(inputStream!!)

        val tempFolder = tmpFolder.newFolder("newFolder")
        val tempUri = DoorUri.parse(tempFolder.toURI().toString())

        val metadata = runBlocking {
            val h5pUri = DoorUri.parse(tempH5pFile.toURI().toString())
            val processContext = ContentJobProcessContext(h5pUri, tempUri, mutableMapOf(),
                DummyContentJobItemTransactionRunner(db), di)
            h5pPlugin.extractMetadata(h5pUri,processContext)
        }!!

        Assert.assertEquals("Got ContentEntry with expected title",
                "Dialog Cards",
                metadata.entry.title)
        Assert.assertEquals("Got ContentEntry with expected title",
                "Samih",
                metadata.entry.author)
        Assert.assertEquals("Got Entry with expected license",
                ContentEntry.LICENSE_TYPE_OTHER, metadata.entry.licenseType)
    }

    @Test
    fun givenValidH5PLink_whenImportContentEntryFromFile_thenContentEntryAndContainerShouldExist() {

        val containerTmpDir = tmpFolder.newFolder("containerTmpDir")
        val tempFolder = tmpFolder.newFolder("newFolder")
        val tempUri = DoorUri.parse(tempFolder.toURI().toString())
        val accountManager: UstadAccountManager by di.instance()
        repo = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)
        db = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_DB)

        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/contenttype/dialog-cards-620.h5p")
        val tempH5pFile = tmpFolder.newFile()
        tempH5pFile.copyInputStreamToFile(inputStream!!)
        runBlocking {

            val doorUri = DoorUri.parse(mockWebServer.url("/com/ustadmobile/core/contenttype/dialog-cards-620.h5p").toString())
            val processContext = ContentJobProcessContext(doorUri, tempUri, params = mutableMapOf(),
                DummyContentJobItemTransactionRunner(db), di)

            val uid = repo.contentEntryDao.insert(ContentEntry().apply{
                title = "hello"
            })

            val jobItem = ContentJobItem(sourceUri = doorUri.uri.toString(),
                    cjiParentContentEntryUid = uid, cjiContentEntryUid = 42)
            val job = ContentJob(toUri = containerTmpDir.toURI().toString())
            val jobAndItem = ContentJobItemAndContentJob().apply{
                this.contentJob = job
                this.contentJobItem = jobItem
            }

            h5pPlugin.processJob(jobAndItem, processContext) {

            }


            val container = repo.containerDao.findContainersForContentEntryUid(42).first()

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

            Assert.assertNotEquals("Container size is non-zero", 0L, container.fileSize)

            // walk through the zip entries and check all exists in containerEntry
            ZipFile(tempH5pFile).entries().toList()
                    .filter { !it.isDirectory }
                    .forEach {
                        Assert.assertNotNull("File exists: $it", db.containerEntryDao.findByPathInContainer(container.containerUid, "workspace/${it.name}"))
                    }
        }
    }

}
