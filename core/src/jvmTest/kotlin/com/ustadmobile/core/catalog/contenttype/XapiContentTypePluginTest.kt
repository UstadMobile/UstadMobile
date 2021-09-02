package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class XapiContentTypePluginTest {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var endpointScope: EndpointScope

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup(){
        endpointScope = EndpointScope()
        di = DI {
            import(ustadTestRule.diModule)
        }

        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = ContentDispatcher()
        mockWebServer.start()
    }

    @Test
    fun givenValidTinCanFormatFile_whenGetContentEntryCalled_thenShouldReadMetaData() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/contenttype/ustad-tincan.zip")
        val tempFile = temporaryFolder.newFile()
        tempFile.copyInputStreamToFile(inputStream!!)

        val tempFolder = temporaryFolder.newFolder("newFolder")
        val tempUri = DoorUri.parse(tempFolder.toURI().toString())

        val xapiPlugin =  XapiTypePluginCommonJvm(Any(), Endpoint("http://localhost/dummy"), di)
        val metadata = runBlocking {
            xapiPlugin.extractMetadata(DoorUri.parse(tempFile.toURI().toString()),
                    ProcessContext(tempUri, params = mutableMapOf<String,String>()))
        }!!

        Assert.assertEquals("Got expected title",
                "Ustad Mobile", metadata.entry.title)
        Assert.assertEquals("Got expected description",
            "Ustad Mobile sample tincan", metadata.entry.description)
    }

    @Test
    fun givenValidXapiLink_whenExtractMetadataAndProcessJobComplete_thenDataShouldBeDownloaded(){

        val containerTmpDir = temporaryFolder.newFolder("containerTmpDir")
        val tempFolder = temporaryFolder.newFolder("newFolder")
        val tempUri = DoorUri.parse(tempFolder.toURI().toString())
        val accountManager: UstadAccountManager by di.instance()
        val repo: UmAppDatabase = di.on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)


        val xapiPlugin = XapiTypePluginCommonJvm(Any(), accountManager.activeEndpoint, di)
        runBlocking{

            val doorUri = DoorUri.parse(mockWebServer.url("/com/ustadmobile/core/contenttype/ustad-tincan.zip").toString())
            val processContext = ProcessContext(tempUri, params = mutableMapOf<String,String>())

            val uid = repo.contentEntryDao.insert(ContentEntry().apply{
                title = "hello"
            })

            val job = ContentJobItem(sourceUri = doorUri.uri.toString(),
                    toUri = containerTmpDir.toURI().toString(),
                    cjiParentContentEntryUid = uid)

            xapiPlugin.processJob(job, processContext) {

            }

            val container = repo.containerDao.findByUid(job.cjiContainerUid)!!

            Assert.assertNotNull(container)


        }

    }



}
