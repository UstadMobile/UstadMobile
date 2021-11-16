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
    fun givenValidTinCanFormatFile_whenGetContentEntryCalled_thenShouldReadMetaData() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/contenttype/ustad-tincan.zip")
        val tempFile = temporaryFolder.newFile()
        tempFile.copyInputStreamToFile(inputStream!!)

        val tempFolder = temporaryFolder.newFolder("newFolder")
        val tempUri = DoorUri.parse(tempFolder.toURI().toString())

        val xapiPlugin =  XapiTypePluginCommonJvm(Any(), Endpoint("http://localhost/dummy"), di)
        val metadata = runBlocking {
            val xapiFileUri = DoorUri.parse(tempFile.toURI().toString())
            val processContext = ContentJobProcessContext(xapiFileUri, tempUri, mutableMapOf(),
                    di)
            xapiPlugin.extractMetadata(xapiFileUri, processContext)
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
            val processContext = ContentJobProcessContext(doorUri, tempUri, mutableMapOf(),
                    di)

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

            xapiPlugin.processJob(jobAndItem, processContext) {

            }

            val container = repo.containerDao.findByUid(jobItem.cjiContainerUid)!!

            Assert.assertNotNull(container)


        }

    }



}
