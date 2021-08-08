package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.buffer
import okio.source
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

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setHeader("Content-Type", "application/epub+zip").setBody(""))
        mockWebServer.enqueue(createResponse("/com/ustadmobile/core/contenttype/childrens-literature.epub"))
        mockWebServer.enqueue(createResponse("/com/ustadmobile/core/contenttype/childrens-literature.epub"))
        

        val containerTmpDir = tmpFolder.newFolder("containerTmpDir")
        val tempFolder = tmpFolder.newFolder("newFolder")
        val tempUri = DoorUri.parse(tempFolder.toURI().toString())
        val accountManager: UstadAccountManager by di.instance()
        val repo: UmAppDatabase = di.on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)

        val epubPlugin = EpubTypePluginCommonJvm(Any(), accountManager.activeEndpoint, di)
        runBlocking{

            val doorUri = DoorUri.parse(mockWebServer.url("children.epub").toString())
            val processContext = ProcessContext(tempUri, params = mutableMapOf<String,String>())
            val isValid = epubPlugin.canProcess(doorUri, processContext)

            Assert.assertTrue("valid epub", isValid)

            val metadata = epubPlugin.extractMetadata(doorUri, processContext)!!

            Assert.assertEquals("Got ContentEntry with expected title",
                    "A Textbook of Sources for Teachers and Teacher-Training Classes",
                    metadata.entry.title)

            val uid = repo.contentEntryDao.insert(metadata.entry)

            val job = ContentJobItem(fromUri = doorUri.uri.toString(),
                                    toUri = containerTmpDir.toURI().toString(),
                                    cjiContentEntryUid = uid)

            epubPlugin.processJob(job, processContext, object: ContentJobProgressListener{
                override fun onProgress(contentJobItem: ContentJobItem) {

                }
            })

            val container = repo.containerDao.findByUid(job.cjiContainerUid)!!

            Assert.assertNotNull(container)


        }

    }

    fun createResponse(fileLocation: String): MockResponse {
        val newStream = javaClass.getResourceAsStream(fileLocation)
        val source = newStream.source().buffer()
        val buffer = Buffer()
        source.readAll(buffer)

        val response = MockResponse().setResponseCode(200).setHeader("Content-Type", "application/epub+zip")
        response.setBody(buffer)

        return response
    }

}