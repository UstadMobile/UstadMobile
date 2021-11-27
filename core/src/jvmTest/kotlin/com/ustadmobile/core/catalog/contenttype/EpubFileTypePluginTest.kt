package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.torrent.CommunicationWorkers
import com.ustadmobile.core.torrent.UstadCommunicationManager
import com.ustadmobile.core.torrent.UstadTorrentManager
import com.ustadmobile.core.torrent.UstadTorrentManagerImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.sharedse.util.UmFileUtilSe.copyInputStreamToFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.net.InetAddress
import java.net.URL

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
            val trackerUrl = URL("http://127.0.0.1:6677/announce")
            bind<UstadTorrentManager>() with scoped(endpointScope).singleton {
                UstadTorrentManagerImpl(endpoint = context, di = di)
            }
            bind<UstadCommunicationManager>() with singleton {
                UstadCommunicationManager(CommunicationWorkers())
            }
            onReady {
                instance<UstadCommunicationManager>().start(InetAddress.getByName(trackerUrl.host))
                GlobalScope.launch {
                    val ustadTorrentManager: UstadTorrentManager = di.on(Endpoint("localhost")).direct.instance()
                    ustadTorrentManager.startSeeding()
                }
            }
        }

        val accountManager: UstadAccountManager by di.instance()
        val umAppDatabase: UmAppDatabase = di.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        val connectivityStatus = ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED, true, "NetworkSSID")
        umAppDatabase.connectivityStatusDao.insert(connectivityStatus)

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
            val epubUri = DoorUri.parse(tempEpubFile.toURI().toString())
            val processContext = ContentJobProcessContext(epubUri, tempUri, params = mutableMapOf(),
                    di)
            val metadata = epubPlugin.extractMetadata(epubUri, processContext)
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
            val processContext = ContentJobProcessContext(doorUri, tempUri, mutableMapOf(), di)

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