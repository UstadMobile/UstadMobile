package com.ustadmobile.sharedse.network

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.sharedse.util.ReverseProxyDispatcher
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.extractTestResourceToFile
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipFile
import javax.naming.InitialContext


class DownloadJobItemRunnerTest {

    private lateinit var cloudServer: EmbeddedHTTPD

    private lateinit var cloudMockWebServer: MockWebServer

    private lateinit var cloudMockDispatcher: ReverseProxyDispatcher

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

    private val MAX_LATCH_WAITING_TIME = 15000L

    private val MAX_THREAD_SLEEP_TIME = 2000L

    private lateinit var downloadJobItemManager: DownloadJobItemManager


    @Before
    @Throws(IOException::class)
    fun setup() {
        checkJndiSetup()
        val c1 = InitialContext()
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
        mockedNetworkManager.setJobItemManagerList(DownloadJobItemManagerList(clientDb,
                newSingleThreadContext("DownloadJobItemRunnerTest")))

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
        networkNode.nodeId = clientDb.networkNodeDao.replace(networkNode)

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

        runBlocking {
            downloadJobItemManager = mockedNetworkManager.createNewDownloadJobItemManager(downloadJob)
        }

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

        cloudMockWebServer = MockWebServer()
        cloudMockDispatcher = ReverseProxyDispatcher(HttpUrl.parse(cloudServer.localURL)!!)
        cloudMockWebServer.setDispatcher(cloudMockDispatcher)

        cloudEndPoint = cloudMockWebServer.url("/").toString()

        val peerDb = UmAppDatabase.getInstance(context, "peerdb")
        peerDb.clearAllTables()
        val peerRepo = peerDb//.getRepository("http://localhost/dummy/", "")
        peerRepo.containerDao.insert(container)
        peerContainerFileTmpDir = UmFileUtilSe.makeTempDir("peerContainerFileTmpDir",
                "" + System.currentTimeMillis())
        val peerContainerManager = ContainerManager(container,
                peerDb, peerRepo, peerContainerFileTmpDir.absolutePath)

        peerTmpContentEntryFile = File.createTempFile("peerTmpContentEntryFile",
                "" + System.currentTimeMillis() + ".zip")
        extractTestResourceToFile(TEST_FILE_RESOURCE_PATH, peerTmpContentEntryFile)
        val peerZipFile = ZipFile(peerTmpContentEntryFile)
        addEntriesFromZipToContainer(peerTmpContentEntryFile.absolutePath, peerContainerManager)
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

    @Throws(IOException::class)
    fun assertContainersHaveSameContent(containerUid1: Long, containerUid2: Long,
                                        db1: UmAppDatabase, repo1: UmAppDatabase,
                                        db2: UmAppDatabase, repo2: UmAppDatabase) {

        val container1 = repo1.containerDao.findByUid(containerUid1)!!
        val manager1 = ContainerManager(container1, db1, repo1)

        val container2 = repo2.containerDao.findByUid(containerUid2)!!
        val manager2 = ContainerManager(container2, db2, repo2)

        Assert.assertEquals("Containers have same number of entries",
                container1.cntNumEntries.toLong(),
                db2.containerEntryDao.findByContainer(containerUid2).size.toLong())

        for (entry in manager1.allEntries) {
            val entry2 = manager2.getEntry(entry.cePath!!)
            Assert.assertNotNull("Client container also contains " + entry.cePath!!,
                    entry2)
            Assert.assertArrayEquals(
                    UMIOUtils.readStreamToByteArray(manager1.getInputStream(entry)),
                    UMIOUtils.readStreamToByteArray(manager2.getInputStream(entry2!!)))
        }
    }

    @Test
    @Throws(IOException::class)
    fun givenDownload_whenRun_shouldDownloadAndComplete() {
        runBlocking {
            var item = clientDb.downloadJobItemDao.findByUid(
                    downloadJobItem.djiUid)!!
            val jobItemRunner = DownloadJobItemRunner(context, item, mockedNetworkManager, clientDb, clientRepo,
                    cloudEndPoint, connectivityStatus)

            val startTime = System.currentTimeMillis()
            jobItemRunner.download()
            val runTime = System.currentTimeMillis() - startTime
            println("Download job completed in $runTime ms")

            item = clientDb.downloadJobItemDao.findByUid(
                    downloadJobItem.djiUid)!!

            Assert.assertEquals("File download task completed successfully",
                    JobStatus.COMPLETE.toLong(), item.djiStatus.toLong())

            Assert.assertEquals("Correct number of ContentEntry items available in client db",
                    container.cntNumEntries.toLong(),
                    clientDb.containerEntryDao.findByContainer(item.djiContainerUid).size.toLong())

            assertContainersHaveSameContent(item.djiContainerUid, item.djiContainerUid,
                    serverDb, serverRepo, clientDb, clientRepo)
        }
    }


    @Test
    @Throws(IOException::class)
    fun givenDownloadStarted_whenFailsOnce_shouldRetryAndComplete() {
        runBlocking {
            cloudMockDispatcher.numTimesToFail.set(1)

            var item = clientDb.downloadJobItemDao.findByUid(
                    downloadJobItem.djiUid)!!
            val jobItemRunner = DownloadJobItemRunner(context, item, mockedNetworkManager, clientDb, clientRepo,
                    cloudEndPoint, connectivityStatus)

            jobItemRunner.download()

            item = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!

            Assert.assertEquals("File download task retried and completed successfully",
                    JobStatus.COMPLETE.toLong(), item.djiStatus.toLong())

            Assert.assertEquals("Number of attempts = 2", 2, item.numAttempts.toLong())
            Assert.assertTrue("Number of file get requests > 2",
                    cloudMockWebServer.requestCount > 2)

            assertContainersHaveSameContent(item.djiContainerUid, item.djiContainerUid,
                    serverDb, serverRepo, clientDb, clientRepo)
        }
    }

    @Test
    fun givenDownloadStarted_whenFailsExceedMaxAttempts_shouldStopAndSetStatusToFailed() {
        runBlocking {
            cloudMockDispatcher.numTimesToFail.set(10)

            var item = clientDb.downloadJobItemDao.findByUid(
                    downloadJobItem.djiUid)!!
            val jobItemRunner = DownloadJobItemRunner(context, item, mockedNetworkManager, clientDb, clientRepo,
                    cloudEndPoint, connectivityStatus, retryDelay = 100L)

            jobItemRunner.download()

            item = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!

            Assert.assertEquals("File download task retried and completed with failure status",
                    JobStatus.FAILED, item.djiStatus)
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenDownloadUnmeteredConnectivityOnly_whenConnectivitySwitchesToMetered_shouldStopAndSetStatusToWaiting() {
        var item = clientDb.downloadJobItemDao.findByUid(
                downloadJobItem.djiUid)!!
        runBlocking {
            //set speed to 512kbps (period unit by default is milliseconds)
            cloudMockDispatcher.throttleBytesPerPeriod = (128 * 1000)
            cloudMockDispatcher.throttlePeriod = 1000


            val jobItemRunner = DownloadJobItemRunner(context, item, mockedNetworkManager, clientDb, clientRepo,
                    cloudEndPoint, connectivityStatus)

            launch {
                jobItemRunner.download()
            }

            delay(1000) //wait for 1 second

            clientDb.connectivityStatusDao.updateStateAsync(ConnectivityStatus.STATE_METERED)

            try {
                waitForLiveData(clientDb.downloadJobItemDao.getLiveStatus(
                        item.djiUid), MAX_LATCH_WAITING_TIME) { status ->
                    status == JobStatus.WAITING_FOR_CONNECTION
                }
            } catch (e: Exception) {
                println("Exception with live data cancel?")
                e.printStackTrace()
            }

        }

        item = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!

        Assert.assertEquals("File download task stopped after network status " + "change and set status to waiting",
                JobStatus.WAITING_FOR_CONNECTION, item.djiStatus)
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenDownloadStarted_whenJobIsStopped_shouldStopAndSetStatus() {
        runBlocking {
            cloudMockDispatcher.throttleBytesPerPeriod = (128 * 1000)
            cloudMockDispatcher.throttlePeriod = 1000

            var item = clientDb.downloadJobItemDao.findByUid(
                    downloadJobItem.djiUid)!!
            val jobItemRunner = DownloadJobItemRunner(context, item, mockedNetworkManager, clientDb, clientRepo,
                    cloudEndPoint, connectivityStatus)

            launch {
                jobItemRunner.download()
            }

            delay(1000)

            clientDb.downloadJobItemDao.updateStatus(item.djiUid, JobStatus.STOPPING)

            waitForLiveData(clientDb.downloadJobItemDao.getLiveStatus(
                    item.djiUid), MAX_LATCH_WAITING_TIME) { status ->
                status == JobStatus.STOPPED
            }

            item = clientDb.downloadJobItemDao.findByUid(
                    downloadJobItem.djiUid)!!

            Assert.assertEquals("File download job was stopped and status was updated",
                    JobStatus.STOPPED, item.djiStatus)
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenDownloadStartsOnMeteredConnection_whenJobSetChangedToDisableMeteredConnection_shouldStopAndSetStatus() {
        var item = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!
        var statusAfterWaitingForDownload = -1
        runBlocking {
            cloudMockDispatcher.throttleBytesPerPeriod = (128 * 1000)
            cloudMockDispatcher.throttlePeriod = 1000


            clientDb.downloadJobDao.setMeteredConnectionAllowedByJobUidSync(
                    item.djiUid, true)

            val jobItemRunner = DownloadJobItemRunner(context, item, mockedNetworkManager, clientDb, clientRepo,
                    cloudEndPoint, connectivityStatus)

            clientDb.connectivityStatusDao.updateState(ConnectivityStatus.STATE_METERED, "")

            UMLog.l(UMLog.DEBUG, 699,
                    " Running DownloadJobItemRunner for " + item.djiUid)

            launch { jobItemRunner.download() }

            waitForLiveData(clientDb.downloadJobItemDao.getLiveStatus(
                    item.djiUid), MAX_LATCH_WAITING_TIME) { status ->
                status >= JobStatus.RUNNING_MIN
            }

            delay(MAX_THREAD_SLEEP_TIME)
            statusAfterWaitingForDownload = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!.djiStatus

            clientDb.downloadJobDao.setMeteredConnectionAllowedByJobUidSync(
                    item.djiDjUid, false)

            waitForLiveData(clientDb.downloadJobItemDao.getLiveStatus(
                    item.djiUid), MAX_LATCH_WAITING_TIME) { status ->
                status == JobStatus.WAITING_FOR_CONNECTION
            }
        }

        item = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!
        assertTrue("After starting download, download was in running state before changing metered connection option",
                statusAfterWaitingForDownload >= JobStatus.RUNNING_MIN)
        assertEquals("File download job is waiting for network after changing download" +
                " set setting to use unmetered connection only",
                JobStatus.WAITING_FOR_CONNECTION, item.djiStatus)
    }

    @Test
    @Throws(InterruptedException::class)
    fun givenDownloadStarted_whenConnectionGoesOff_shouldStopAndSetStatusToWaiting() {
        var item = clientDb.downloadJobItemDao.findByUid(
                downloadJobItem.djiUid)!!
        var stateBeforeDisconnect = -1

        runBlocking {
            cloudMockDispatcher.throttleBytesPerPeriod = (128 * 1000)
            cloudMockDispatcher.throttlePeriod = 1000


            val jobItemRunner = DownloadJobItemRunner(context, item, mockedNetworkManager, clientDb, clientRepo,
                    cloudEndPoint, connectivityStatus)

            launch { jobItemRunner.download() }

            delay(MAX_THREAD_SLEEP_TIME)
            stateBeforeDisconnect = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!.djiStatus

            clientDb.connectivityStatusDao.updateState(ConnectivityStatus.STATE_DISCONNECTED, "")

            waitForLiveData(clientDb.downloadJobItemDao.getLiveStatus(
                    item.djiUid), MAX_LATCH_WAITING_TIME) { status ->
                status == JobStatus.WAITING_FOR_CONNECTION
            }
        }

        item = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!
        assertEquals("Before disconnect, job was running", JobStatus.RUNNING,
                stateBeforeDisconnect)
        assertEquals("File download job is waiting for network after the network goes off",
                JobStatus.WAITING_FOR_CONNECTION, item.djiStatus)
    }


}