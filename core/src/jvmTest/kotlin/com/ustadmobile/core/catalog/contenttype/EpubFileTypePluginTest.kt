package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentJob
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*

class EpubFileTypePluginTest {

    @JvmField
    @Rule
    val tmpFolder = TemporaryFolder()

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
    fun givenValidEpubFormatFile_whenExtractEntryMetaDataFromFile_thenDataShouldMatch() {

        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/contenttype/childrens-literature.epub")
        val tempEpubFile = tmpFolder.newFile()
        tempEpubFile.copyInputStreamToFile(inputStream)

        val tempFolder = tmpFolder.newFolder("newFolder")
        val tempUri = DoorUri.parse(tempFolder.toURI().toString())

        val epubPlugin = EpubTypePluginCommonJvm(Any(), Endpoint("http://localhost/dummy"), di)
        runBlocking {
            val metadata = epubPlugin.extractMetadata(DoorUri.parse(tempEpubFile.toURI().toString()), ProcessContext(tempUri, params = mutableMapOf<String,String>()))
            Assert.assertEquals("Got ContentEntry with expected title",
                    "A Textbook of Sources for Teachers and Teacher-Training Classes",
                    metadata!!.entry.title)
        }

    }

    @Test
    fun givenValidEpubLink_whenExtractMetadataAndProcessJobComplete_thenDataShouldBeDownloaded(){

        val containerTmpDir = tmpFolder.newFolder("containerTmpDir")
        val tempFolder = tmpFolder.newFolder("newFolder")
        val tempUri = DoorUri.parse(tempFolder.toURI().toString())
        val accountManager: UstadAccountManager by di.instance()
        val repo: UmAppDatabase = di.on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)


        val epubPlugin = EpubTypePluginCommonJvm(Any(), accountManager.activeEndpoint, di)
        runBlocking{

            val doorUri = DoorUri.parse(mockWebServer.url("/com/ustadmobile/core/contenttype/childrens-literature.epub").toString())
            val processContext = ProcessContext(tempUri, params = mutableMapOf<String,String>())

            val uid = repo.contentEntryDao.insert(ContentEntry().apply{
                title = "hello"
            })
            
            val jobItem = ContentJobItem(sourceUri = doorUri.uri.toString(),
                                    cjiParentContentEntryUid = uid)
            val job = ContentJob(toUri = containerTmpDir.toURI().toString())
            val jobAndItem = ContentJobItemAndContentJob().apply{
                this.contentJob = job
                this.contentJobItem = jobItem
            }

            epubPlugin.processJob(jobAndItem, processContext) {

            }

            val container = repo.containerDao.findByUid(jobItem.cjiContainerUid)!!

            Assert.assertNotNull(container)


        }

    }

}