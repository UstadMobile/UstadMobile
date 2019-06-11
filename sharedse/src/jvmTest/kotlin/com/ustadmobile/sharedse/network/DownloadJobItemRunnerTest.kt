package com.ustadmobile.sharedse.network

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.sharedse.container.ContainerManager
import com.ustadmobile.sharedse.util.addEntriesFromZipToContainer
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipFile
import javax.naming.InitialContext

class DownloadJobItemRunnerTest {

    private lateinit var cloudServer: EmbeddedHTTPD

    private lateinit var peerServer: EmbeddedHTTPD

    private lateinit var mockedNetworkManager: NetworkManagerBleCommon

    private var mockedNetworkManagerBleWorking = AtomicBoolean()

    private val mockedNetworkManagerWifiConnectWorking = AtomicBoolean()

    private lateinit var clientDb: UmAppDatabase

    private lateinit var clientRepo: UmAppDatabase

    private lateinit var serverDb: UmAppDatabase

    private lateinit var serverRepo: UmAppDatabase

    private lateinit var cloudEndPoint: String

    private lateinit var webServerTmpDir: File

    private lateinit var containerTmpDir: File

    private lateinit var webServerTmpContentEntryFile: File

    private lateinit var peerTmpContentEntryFile: File

    private lateinit var peerContainerFileTmpDir: File

    private lateinit var clientContainerDir: File

    private val TEST_CONTENT_ENTRY_FILE_UID = 1000L

    private val TEST_FILE_RESOURCE_PATH = "/com/ustadmobile/port/sharedse/networkmanager/thelittlechicks.epub"

    private var context = Any()

    private lateinit var entryStatusResponse: EntryStatusResponse

    //private DownloadJobItemHistory history;

    private lateinit var mockedEntryStatusTask: BleEntryStatusTask

    private lateinit var networkNode: NetworkNode

    private lateinit var connectivityStatus: ConnectivityStatus

    private lateinit var wifiDirectGroupInfoMessage: BleMessage

    private lateinit var groupBle: WiFiDirectGroupBle

    private lateinit var downloadJobItem: DownloadJobItem

    private lateinit var container: Container

    private lateinit var containerManager: ContainerManager

    private val MAX_LATCH_WAITING_TIME = 15

    private val MAX_THREAD_SLEEP_TIME = 2

    private lateinit var downloadJobItemManager: DownloadJobItemManager


    @Before
    @Throws(IOException::class)
    fun setup() {
        checkJndiSetup()
        val c1 =  InitialContext()
        val c2 = InitialContext()

        //UmAppDatabase.getInstance(context).clearAllTables()
        clientDb = UmAppDatabase.getInstance(context, "clientdb")
//        clientDb = UmAppDatabase.getInstance(context, "clientdb")
        clientDb.clearAllTables()

        webServerTmpDir = UmFileUtilSe.makeTempDir("webServerTmpDir",
                "" + System.currentTimeMillis())
        webServerTmpContentEntryFile = File(webServerTmpDir, "" + TEST_CONTENT_ENTRY_FILE_UID)

        extractTestResourceToFile(TEST_FILE_RESOURCE_PATH, webServerTmpContentEntryFile)

        containerTmpDir = UmFileUtilSe.makeTempDir("containerTmpDir",
                "" + System.currentTimeMillis())

        //mockedNetworkManager = spy<NetworkManagerBleCommon>(NetworkManagerBleCommon::class.java!!)
        mockedNetworkManager = spy<NetworkManagerBleCommon> {

        }
        mockedNetworkManager.setDatabase(clientDb)
        mockedNetworkManager.setContext(context)
        mockedNetworkManager.setJobItemManagerList(DownloadJobItemManagerList(clientDb,
                { newSingleThreadContext(it)} ))

        mockedNetworkManagerBleWorking.set(true)

        mockedNetworkManagerWifiConnectWorking.set(true)

        mockedEntryStatusTask = mock<BleEntryStatusTask> {}


        val httpd = EmbeddedHTTPD(0, context)
        httpd.start()

        //`when`(mockedNetworkManager.getHttpd()).thenReturn(httpd)

        groupBle = WiFiDirectGroupBle("DIRECT-PeerNode", "networkPass123")


        clientContainerDir = UmFileUtilSe.makeTempDir("clientContainerDir", "" + System.currentTimeMillis())

        clientRepo = clientDb//.getRepository("http://localhost/dummy/", "")
        networkNode = NetworkNode()
        networkNode.bluetoothMacAddress = "00:3F:2F:64:C6:4F"
        networkNode.lastUpdateTimeStamp = System.currentTimeMillis()
        networkNode.nodeId = clientDb.networkNodeDao.insert(networkNode)

        serverDb = UmAppDatabase.getInstance(context)
        serverDb.clearAllTables()
        serverRepo = serverDb//.getRepository("http://localhost/dummy/", "")


        val contentEntry = ContentEntry()
        contentEntry.title = "Test entry"
        contentEntry.contentEntryUid = clientDb.contentEntryDao.insert(contentEntry)

        container = Container(contentEntry)
        container.containerUid = serverDb.containerDao.insert(container)
        containerManager = ContainerManager(container, serverDb, serverRepo,
                webServerTmpDir.absolutePath)
        addEntriesFromZipToContainer(webServerTmpContentEntryFile.absolutePath, containerManager)

        //add the container itself to the client database (would normally happen via sync/preload)
        clientRepo.containerDao.insert(container)


        val downloadJob = DownloadJob(contentEntry.contentEntryUid,
                System.currentTimeMillis())
        downloadJob.timeRequested = System.currentTimeMillis()
        downloadJob.djStatus = JobStatus.QUEUED
        downloadJob.djDestinationDir = clientContainerDir.absolutePath

        downloadJobItemManager = mockedNetworkManager.createNewDownloadJobItemManager(downloadJob)

        downloadJobItem = DownloadJobItem(downloadJob, contentEntry.contentEntryUid,
                container.containerUid, container.fileSize)
        downloadJobItem.djiStatus = JobStatus.QUEUED
        downloadJobItem.downloadedSoFar = 0
        downloadJobItem.destinationFile = File(clientContainerDir,
                TEST_CONTENT_ENTRY_FILE_UID.toString()).absolutePath
        runBlocking { downloadJobItemManager.insertDownloadJobItems(listOf(downloadJobItem)) }


        connectivityStatus = ConnectivityStatus()
        connectivityStatus.connectedOrConnecting = true
        connectivityStatus.connectivityState = ConnectivityStatus.STATE_UNMETERED
        connectivityStatus.csUid = 1
        clientDb.connectivityStatusDao.insert(connectivityStatus)

        entryStatusResponse = EntryStatusResponse()
        entryStatusResponse.erContainerUid = container.containerUid
        entryStatusResponse.erNodeId = networkNode.nodeId
        entryStatusResponse.available = true
        entryStatusResponse.responseTime = System.currentTimeMillis()

        cloudServer = EmbeddedHTTPD(0, context,
                serverDb)
        cloudServer.start()

        cloudEndPoint = cloudServer.localURL

        val peerDb = UmAppDatabase.getInstance(context, "peerdb")
        peerDb.clearAllTables()
        val peerRepo = peerDb//.getRepository("http://localhost/dummy/", "")
        peerRepo.containerDao.insert(container)
        peerContainerFileTmpDir = UmFileUtilSe.makeTempDir("peerContainerFileTmpDir",
                "" + System.currentTimeMillis())
        val peerContainerManager = com.ustadmobile.port.sharedse.container.ContainerManager(container,
                peerDb, peerRepo, peerContainerFileTmpDir.absolutePath)

        peerTmpContentEntryFile = File.createTempFile("peerTmpContentEntryFile",
                "" + System.currentTimeMillis() + ".zip")
        UmFileUtilSe.extractResourceToFile(TEST_FILE_RESOURCE_PATH, peerTmpContentEntryFile)
        val peerZipFile = ZipFile(peerTmpContentEntryFile)
        peerContainerManager.addEntriesFromZip(peerZipFile, com.ustadmobile.port.sharedse.container.ContainerManager.OPTION_COPY)
        peerZipFile.close()

        peerServer = EmbeddedHTTPD(0, context, peerDb)


//        `when`(mockedNetworkManager.makeEntryStatusTask(any<T>(Any::class.java),
//                any<T>(), any<T>(NetworkNode::class.java))).thenReturn(mockedEntryStatusTask)
//
//        `when`(mockedNetworkManager.makeEntryStatusTask(any<T>(Any::class.java),
//                any(BleMessage::class.java), any<T>(NetworkNode::class.java),
//                any(BleMessageResponseListener::class.java))).thenReturn(mockedEntryStatusTask)
//
//
//        doAnswer { invocation ->
//            val bleResponseListener = invocation.getArgument(3)
//            startPeerWebServer()
//            Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_THREAD_SLEEP_TIME.toLong()))
//
//            wifiDirectGroupInfoMessage = BleMessage(WIFI_GROUP_CREATION_RESPONSE, 42.toByte(),
//                    mockedNetworkManager.getWifiGroupInfoAsBytes(groupBle))
//
//            bleResponseListener.onResponseReceived(networkNode.bluetoothMacAddress,
//                    if (mockedNetworkManagerBleWorking.get()) wifiDirectGroupInfoMessage else null,
//                    if (mockedNetworkManagerBleWorking.get())
//                        null
//                    else
//                        IOException(
//                                "BLE group details request failed"))
//            null
//        }.`when`(mockedNetworkManager).sendMessage(any<T>(Any::class.java), any(BleMessage::class.java),
//                any<T>(NetworkNode::class.java), any(BleMessageResponseListener::class.java))
//
//        doAnswer { invocation ->
//            clientDb.connectivityStatusDao
//                    .updateState(ConnectivityStatus.STATE_CONNECTING_LOCAL, groupBle.getSsid(), null!!)
//            Thread.sleep(TimeUnit.SECONDS.toMillis(1))
//
//            val state = if (mockedNetworkManagerWifiConnectWorking.get())
//                ConnectivityStatus.STATE_CONNECTED_LOCAL
//            else
//                ConnectivityStatus.STATE_DISCONNECTED
//            clientDb.connectivityStatusDao.updateState(state,
//                    if (state != ConnectivityStatus.STATE_DISCONNECTED) groupBle.getSsid() else null, null!!)
//            null
//        }.`when`(mockedNetworkManager).connectToWiFi(eq(groupBle.getSsid()), eq(groupBle.getPassphrase()))


//        doAnswer { invocation ->
//            Thread {
//                try {
//                    Thread.sleep(1000)
//                } catch (e: InterruptedException) { /*should not happen*/
//                }
//
//                clientDb.connectivityStatusDao.updateState(ConnectivityStatus.STATE_UNMETERED,
//                        "normalwifi", null!!)
//            }.start()
//            null
//        }.`when`(mockedNetworkManager).restoreWifi()

    }

    @Test
    fun testSomething() {
        Assert.assertEquals(2, 2)
    }

}