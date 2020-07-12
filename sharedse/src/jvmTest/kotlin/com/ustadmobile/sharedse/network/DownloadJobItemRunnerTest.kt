package com.ustadmobile.sharedse.network

import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.getInstance
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_DISCONNECTED
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_METERED
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_UNMETERED
import com.ustadmobile.lib.rest.LoginRoute
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.sharedse.ext.ConcatenatedHttpResponse
import com.ustadmobile.port.sharedse.ext.generateConcatenatedFilesResponse
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_CREATION_RESPONSE
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcher
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcherJvm
import com.ustadmobile.util.test.ReverseProxyDispatcher
import com.ustadmobile.util.test.checkJndiSetup
import com.ustadmobile.util.test.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.util.test.extractTestResourceToFile
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.*
import okhttp3.HttpUrl
import okhttp3.mockwebserver.*
import okio.Buffer
import okio.Okio
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.kodein.di.*
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipFile
import javax.naming.InitialContext


class DownloadJobItemRunnerTest {

    private lateinit var cloudServer: ApplicationEngine

    private lateinit var cloudMockWebServer: MockWebServer

    private lateinit var cloudMockDispatcher: ReverseProxyDispatcher

    private lateinit var peerServer: EmbeddedHTTPD

    private var peerMockDispatcher: ReverseProxyDispatcher? = null

    private var peerMockWebServer: MockWebServer? = null

//    private lateinit var mockedNetworkManager: NetworkManagerBle

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

    private lateinit var webServerTmpContentEntryWithDuplicateFile: File

    private lateinit var peerTmpContentEntryFile: File

    private lateinit var peerContainerFileTmpDir: File

    private lateinit var clientContainerDir: File

    private val TEST_CONTENT_ENTRY_FILE_UID = 1000L

    private val TEST_FILE_RESOURCE_PATH = "/com/ustadmobile/port/sharedse/networkmanager/thelittlechicks.epub"

    private val TEST_FILE_WITH_DUPLICATES = "/com/ustadmobile/port/sharedse/networkmanager/thebigchicken.epub"

    private var context = Any()

    private lateinit var mockedEntryStatusTask: BleEntryStatusTask

    private lateinit var networkNode: NetworkNode

    private lateinit var connectivityStatus: DoorMutableLiveData<ConnectivityStatus>

    private lateinit var wifiDirectGroupInfoMessage: BleMessage

    private lateinit var groupBle: WiFiDirectGroupBle

    private lateinit var downloadJobItem: DownloadJobItem

    private lateinit var container: Container

    private lateinit var containerWDuplicates: Container

    private lateinit var containerManager: ContainerManager

    private val MAX_LATCH_WAITING_TIME = 15000L

    private val MAX_THREAD_SLEEP_TIME = 2000L

    private lateinit var containerDownloadManager: ContainerDownloadManager

    private lateinit var connectivityStatusLiveData: DoorMutableLiveData<ConnectivityStatus>

    private lateinit var clientDi: DI

    lateinit var mockNetworkManager: NetworkManagerBle

    private lateinit var mockLocalAvailabilityManager: LocalAvailabilityManager

    class ContainerDownloadDispatcher(val serverDb: UmAppDatabase, val container: Container): Dispatcher() {

        val numTimesToFail = AtomicInteger(0)

        var throttleBytesPerPeriod = 0L

        var throttlePeriod = 0L

        var throttlePeriodUnit: TimeUnit = TimeUnit.MILLISECONDS


        override fun dispatch(request: RecordedRequest): MockResponse {
            return when {
                request.requestUrl.toString().endsWith("/${DownloadJobItemRunner.CONTAINER_ENTRY_LIST_PATH}?containerUid=${container.containerUid}") -> {
                    val entryList = serverDb.containerEntryDao.findByContainerWithMd5(container.containerUid)
                    MockResponse()
                            .addHeader("Content-Type", "application/json")
                            .setBody(Gson().toJson(entryList))
                }

                request.requestUrl.toString().contains(ContainerEntryFileDao.ENDPOINT_CONCATENATEDFILES) -> {
                    val concatenatedResponse: ConcatenatedHttpResponse = serverDb.containerEntryFileDao
                            .generateConcatenatedFilesResponse(request.requestUrl.toString().substringAfterLast("/"))
                    val outBuffer = Buffer()
                    val inBuffer = Okio.buffer(Okio.source(concatenatedResponse.dataSrc!!))
                    inBuffer.readFully(outBuffer, concatenatedResponse.contentLength)

                    MockResponse().also {
                        it.setBody(outBuffer)
                    }
                }

                else -> {
                    MockResponse()
                            .setResponseCode(404)
                }
            }.also {
                it.takeIf { numTimesToFail.decrementAndGet() >= 0}?.setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY)
                it.takeIf { throttleBytesPerPeriod != 0L }?.throttleBody(throttleBytesPerPeriod,
                        throttlePeriod, throttlePeriodUnit)
            }
        }
    }

    @Before
    @Throws(IOException::class)
    fun setup() {
        val endpointScope = EndpointScope()

        connectivityStatusLiveData = DoorMutableLiveData(ConnectivityStatus().apply {
            connectedOrConnecting = true
            connectivityState = STATE_UNMETERED
            wifiSsid = "wifi-mock"
        })

        mockNetworkManager = mock {
            on { connectivityStatus }.thenReturn(connectivityStatusLiveData)
        }

        clientDi = DI {
            bind<UstadMobileSystemImpl>() with singleton { UstadMobileSystemImpl.instance }

            bind<UstadAccountManager>() with singleton { UstadAccountManager(instance(), Any(), di) }
            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(UmAppDatabase.getInstance(Any(), dbName).also {
                    it.clearAllTables()
                })
            }

            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO) with scoped(endpointScope).singleton {
                spy(instance<UmAppDatabase>(tag = UmAppDatabase.TAG_DB).asRepository<UmAppDatabase>(Any(), context.url, "", defaultHttpClient(), null))
            }

            bind<ContainerDownloadManager>() with scoped(endpointScope).singleton {
                mock<ContainerDownloadManager>()
            }

            bind<CoroutineDispatcher>(tag = UstadMobileSystemCommon.TAG_MAIN_COROUTINE_CONTEXT) with singleton {
                Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            }

            bind<NetworkManagerBle>() with singleton { mockNetworkManager }

            bind<LocalAvailabilityManager>() with scoped(endpointScope).singleton {
                mock<LocalAvailabilityManager> {  }
            }

            bind<ContainerFetcher>() with singleton { ContainerFetcherJvm(di) }

            registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }
        }


        cloudMockWebServer = MockWebServer()

        val accountManager: UstadAccountManager by clientDi.instance()
        accountManager.activeAccount = UmAccount(0, "guest", "",
                cloudMockWebServer.url("/").toString())

        clientDb =  clientDi.on(accountManager.activeAccount).direct.instance(tag = TAG_DB)
        containerDownloadManager = clientDi.on(accountManager.activeAccount).direct.instance()

        serverDb = UmAppDatabase.getInstance(context)
        mockLocalAvailabilityManager = clientDi.on(accountManager.activeAccount).direct.instance()



//        clientDb = UmAppDatabase.getInstance(context, "clientdb")
//        clientDb.clearAllTables()

        groupBle = WiFiDirectGroupBle("DIRECT-PeerNode", "networkPass123")


        clientContainerDir = UmFileUtilSe.makeTempDir("clientContainerDir", "" + System.currentTimeMillis())

        //clientRepo = clientDb//.getRepository("http://localhost/dummy/", "")
//        networkNode = NetworkNode()
//        networkNode.bluetoothMacAddress = "00:3F:2F:64:C6:4F"
//        networkNode.lastUpdateTimeStamp = System.currentTimeMillis()
//        networkNode.nodeId = clientDb.networkNodeDao.replace(networkNode)

//        serverDb = UmAppDatabase.getInstance(context)
//        serverDb.clearAllTables()
//        serverRepo = serverDb//.getRepository("http://localhost/dummy/", "")

        webServerTmpDir = UmFileUtilSe.makeTempDir("webServerTmpDir",
                "" + System.currentTimeMillis())
        webServerTmpContentEntryFile = File(webServerTmpDir, "" + TEST_CONTENT_ENTRY_FILE_UID)

        extractTestResourceToFile(TEST_FILE_RESOURCE_PATH, webServerTmpContentEntryFile)

        containerTmpDir = UmFileUtilSe.makeTempDir("containerTmpDir",
                "" + System.currentTimeMillis())

        //mockedNetworkManager = spy<NetworkManagerBleCommon>(NetworkManagerBleCommon::class.java!!)

        val contentEntry = ContentEntry()
        contentEntry.title = "Test entry"
        contentEntry.contentEntryUid = clientDb.contentEntryDao.insert(contentEntry)

        container = Container(contentEntry)
        container.containerUid = serverDb.containerDao.insert(container)
        containerManager = ContainerManager(container, serverDb, serverDb,
                webServerTmpDir.absolutePath)
        addEntriesFromZipToContainer(webServerTmpContentEntryFile.absolutePath, containerManager)

        //add the container itself to the client database (would normally happen via sync/preload)
        clientDb.containerDao.insert(container)



        val downloadJob = DownloadJob(contentEntry.contentEntryUid,
                System.currentTimeMillis())
        downloadJob.timeRequested = System.currentTimeMillis()
        downloadJob.djStatus = JobStatus.QUEUED
        downloadJob.djDestinationDir = clientContainerDir.absolutePath
        downloadJob.djUid = clientDb.downloadJobDao.insert(downloadJob).toInt()

        downloadJobItem = DownloadJobItem(downloadJob, contentEntry.contentEntryUid,
                container.containerUid, container.fileSize)
        downloadJobItem.djiStatus = JobStatus.QUEUED
        downloadJobItem.downloadedSoFar = 0
        downloadJobItem.destinationFile = File(clientContainerDir,
                TEST_CONTENT_ENTRY_FILE_UID.toString()).absolutePath

        downloadJobItem.djiUid = clientDb.downloadJobItemDao.insert(downloadJobItem).toInt()

//        val peerDb = UmAppDatabase.getInstance(context, "peerdb")
//        peerDb.clearAllTables()
//        peerServer = EmbeddedHTTPD(0, context, peerDb)
        mockedEntryStatusTask = mock<BleEntryStatusTask> {}




        //TODO: ContainerDownloadManager should
//        val containerDownloaderImpl = ContainerFetcherBuilder(mockedNetworkManager).build()
//        whenever(mockedNetworkManager.containerFetcher).thenReturn(containerDownloaderImpl)

        mockedNetworkManagerBleWorking.set(true)

        mockedNetworkManagerWifiConnectWorking.set(true)



//        val httpd = EmbeddedHTTPD(0, context)
//        httpd.start()


//        cloudServer = EmbeddedHTTPD(0, serverDi)
//        cloudServer.start()




//        cloudMockDispatcher = ReverseProxyDispatcher(HttpUrl.parse(cloudServer.localURL)!!)
//        cloudMockWebServer.setDispatcher(cloudMockDispatcher)

        cloudEndPoint = cloudMockWebServer.url("/").toString()


//        val peerRepo = peerDb//.getRepository("http://localhost/dummy/", "")
//        peerRepo.containerDao.insert(container)
//        peerContainerFileTmpDir = UmFileUtilSe.makeTempDir("peerContainerFileTmpDir",
//                "" + System.currentTimeMillis())
//        val peerContainerManager = ContainerManager(container,
//                peerDb, peerRepo, peerContainerFileTmpDir.absolutePath)
//
//        peerTmpContentEntryFile = File.createTempFile("peerTmpContentEntryFile",
//                "" + System.currentTimeMillis() + ".zip")
//        extractTestResourceToFile(TEST_FILE_RESOURCE_PATH, peerTmpContentEntryFile)
//        val peerZipFile = ZipFile(peerTmpContentEntryFile)
//        addEntriesFromZipToContainer(peerTmpContentEntryFile.absolutePath, peerContainerManager)
//        peerZipFile.close()
    }

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


            val e1Contents: ByteArray
            try {
                e1Contents = UMIOUtils.readStreamToByteArray(manager1.getInputStream(entry))
            } catch(e1: Exception) {
                throw IOException("Exception reading entry 1 ${entry.cePath} from ${entry.containerEntryFile?.cefPath}",
                        e1)
            }


            val e2Contents: ByteArray

            try {
                e2Contents = UMIOUtils.readStreamToByteArray(manager2.getInputStream(entry2!!))
            } catch(e2: Exception) {
                throw IOException("Exception reading entry 2 ${entry.cePath} from ${entry2!!.containerEntryFile?.cefPath}",
                        e2)
            }

            Assert.assertArrayEquals("${entry.cePath} contents are the same",
                    e1Contents, e2Contents)
        }
    }

    @Test
    fun givenDownload_whenRun_shouldDownloadAndComplete() {
        runBlocking {
            cloudMockWebServer.setDispatcher(ContainerDownloadDispatcher(serverDb, container))

            var item = clientDb.downloadJobItemDao.findByUid(
                    downloadJobItem.djiUid)!!

            val jobItemRunner = DownloadJobItemRunner(item, cloudEndPoint, 500,
                    clientDi)

            val startTime = System.currentTimeMillis()
            jobItemRunner.download().await()
            val runTime = System.currentTimeMillis() - startTime
            println("Download job completed in $runTime ms")

            item = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!

            argumentCaptor<DownloadJobItem>().apply {
                verifyBlocking(containerDownloadManager, atLeastOnce()) {
                    handleDownloadJobItemUpdated(capture(), any())
                }

                Assert.assertTrue("DownloadJobItemRunner send update for status change to running",
                        allValues.any { it.djiStatus == JobStatus.RUNNING })

                Assert.assertEquals("Final status value is completed", JobStatus.COMPLETE,
                        lastValue.djiStatus)
            }

            Assert.assertEquals("Correct number of ContentEntry items available in client db",
                    container.cntNumEntries,
                    clientDb.containerEntryDao.findByContainer(item.djiContainerUid).size)

            assertContainersHaveSameContent(item.djiContainerUid, item.djiContainerUid,
                    serverDb, serverDb, clientDb, clientDb)
        }
    }


    @Test
    fun givenDownloadStarted_whenFailsOnce_shouldRetryAndComplete() {
        runBlocking {
            cloudMockWebServer.setDispatcher(ContainerDownloadDispatcher(serverDb, container).apply {
                numTimesToFail.set(1)
            })

            var item = clientDb.downloadJobItemDao.findByUid(
                    downloadJobItem.djiUid)!!
            val jobItemRunner = DownloadJobItemRunner(item, cloudEndPoint, 500, clientDi)

            jobItemRunner.download().await()

            item = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!

            argumentCaptor<DownloadJobItem>().apply {
                verifyBlocking(containerDownloadManager, atLeastOnce()) {
                    handleDownloadJobItemUpdated(capture(), any())
                }
                Assert.assertEquals("Final status value is completed", JobStatus.COMPLETE,
                        lastValue.djiStatus)
            }

            Assert.assertTrue("Number of file get requests > 2",
                    cloudMockWebServer.requestCount > 2)

            assertContainersHaveSameContent(item.djiContainerUid, item.djiContainerUid,
                    serverDb, serverDb, clientDb, clientDb)
        }
    }

    @Test
    fun givenDownloadStarted_whenFailsExceedMaxAttempts_shouldStopAndSetStatusToFailed() {
        runBlocking {
            cloudMockWebServer.setDispatcher(ContainerDownloadDispatcher(serverDb, container).apply {
                numTimesToFail.set(10)
            })

            val item = clientDb.downloadJobItemDao.findByUid(
                    downloadJobItem.djiUid)!!
            val jobItemRunner = DownloadJobItemRunner(item, cloudEndPoint, 500, clientDi)

            jobItemRunner.download().await()

            argumentCaptor<DownloadJobItem>().apply {
                verifyBlocking(containerDownloadManager, atLeastOnce()) {
                    handleDownloadJobItemUpdated(capture(), any())
                }
                Assert.assertEquals("Final status value is completed", JobStatus.FAILED,
                        lastValue.djiStatus)
            }
        }
    }

    //TODO: this does not seem to pause and stop as quickly as it should
    @Test
    fun givenDownloadUnmeteredConnectivityOnly_whenConnectivitySwitchesToMetered_shouldStopAndSetStatusToQueued() {
        var item = clientDb.downloadJobItemDao.findByUid(
                downloadJobItem.djiUid)!!
        runBlocking {
            cloudMockWebServer.setDispatcher(ContainerDownloadDispatcher(serverDb, container).apply {
                //set speed to 512kbps (period unit by default is milliseconds)
                throttleBytesPerPeriod = (64 * 1000)
                throttlePeriod = 1000
            })

            val queuedStatusLatch = CountDownLatch(1)
            whenever(containerDownloadManager.handleDownloadJobItemUpdated(any(), any())).thenAnswer{
                val argJobItem = it.arguments[0] as DownloadJobItem
                if(argJobItem.djiStatus == JobStatus.QUEUED)
                    queuedStatusLatch.countDown()
            }


            val jobItemRunner = DownloadJobItemRunner(item, cloudEndPoint, 500, clientDi)


            jobItemRunner.download()
            delay(2000) //wait for 2 seconds

            connectivityStatusLiveData.sendValue(ConnectivityStatus(STATE_METERED, true, null))

            queuedStatusLatch.await(5, TimeUnit.SECONDS)
            argumentCaptor<DownloadJobItem>() {
                verify(containerDownloadManager, atLeastOnce()).handleDownloadJobItemUpdated(capture(), any())
                Assert.assertEquals("Final status update was jobstatus = queued", JobStatus.QUEUED,
                        lastValue.djiStatus)
            }
        }

    }
//
//    @Test
//    fun givenDownloadStarted_whenJobIsStopped_shouldStopAndSetStatus() {
//        runBlocking {
//            cloudMockDispatcher.throttleBytesPerPeriod = (128 * 1000)
//            cloudMockDispatcher.throttlePeriod = 1000
//
//            var item = clientDb.downloadJobItemDao.findByUid(
//                    downloadJobItem.djiUid)!!
//            val jobItemRunner = DownloadJobItemRunner(context, item, containerDownloadManager,
//                    mockedNetworkManager, clientDb,
//                    cloudEndPoint, connectivityStatus,
//                    localAvailabilityManager = mock<LocalAvailabilityManager>(),
//                    connectivityStatusLiveData = connectivityStatusLiveData)
//
//            val pauseCountdownLatch = CountDownLatch(1)
//            whenever(containerDownloadManager.handleDownloadJobItemUpdated(any(), any())).thenAnswer {
//                val downloadJobItemArg = it.arguments[0] as DownloadJobItem
//                if(downloadJobItemArg.djiStatus == JobStatus.PAUSED)
//                    pauseCountdownLatch.countDown()
//
//                Unit
//            }
//
//            launch {
//                jobItemRunner.download()
//            }
//
//            delay(1000)
//
//            jobItemRunner.pause()
//
//            pauseCountdownLatch.await(5, TimeUnit.SECONDS)
//            argumentCaptor<DownloadJobItem>().apply {
//                verify(containerDownloadManager, atLeastOnce()).handleDownloadJobItemUpdated(capture(), any())
//                Assert.assertEquals("Last status update returns status_paused", JobStatus.PAUSED,
//                        lastValue.djiStatus)
//            }
//        }
//    }
//
//    /**
//     * DownloadJobItemRunner should stop when connectivity is lost, and then a new
//     * DownloadJobItemRunner will be created when connectivity is restored. A DownloadJobItemRunner
//     * must be capable of picking up from where the last one left off.
//     */
//    @Test
//    fun givenDownloadJobItemRunnerStartedAndStopped_whenNextJobItemRunnerRuns_shouldFinishAndContentShouldMatch() {
//        runBlocking {
//            cloudMockDispatcher.throttleBytesPerPeriod = (128 * 1000)
//            cloudMockDispatcher.throttlePeriod = 1000
//
//            val item = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!
//            val jobItemRunner1 = DownloadJobItemRunner(context, item, containerDownloadManager,
//                    mockedNetworkManager, clientDb,
//                    cloudEndPoint, connectivityStatus,
//                    localAvailabilityManager = mock<LocalAvailabilityManager>(),
//                    connectivityStatusLiveData = connectivityStatusLiveData)
//
//            val queuedStatusDeferred = CompletableDeferred<Int>()
//            whenever(containerDownloadManager.handleDownloadJobItemUpdated(any(), any())).doAnswer {
//                if((it.arguments[0] as DownloadJobItem).djiStatus == JobStatus.QUEUED)
//                    queuedStatusDeferred.complete(JobStatus.QUEUED)
//
//                Unit
//            }
//
//            launch {
//                jobItemRunner1.download()
//            }
//
//            delay(2000)
//
//            connectivityStatusLiveData.sendValue(
//                    ConnectivityStatus(ConnectivityStatus.STATE_DISCONNECTED, false, null))
//
//            val statusAfterDisconnect = withTimeout(5000) { queuedStatusDeferred.await() }
//
//            connectivityStatusLiveData.sendValue(ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED,
//                    true, "wifi"))
//
//            val downloadJobItemRunner2 = DownloadJobItemRunner(context, item, containerDownloadManager,
//                    mockedNetworkManager, clientDb,
//                    cloudEndPoint, connectivityStatus,
//                    localAvailabilityManager = mock<LocalAvailabilityManager>(),
//                    connectivityStatusLiveData = connectivityStatusLiveData)
//
//            val completedStatusDeferred = CompletableDeferred<Int>()
//            whenever(containerDownloadManager.handleDownloadJobItemUpdated(any(), any())).doAnswer {
//                if((it.arguments[0] as DownloadJobItem).djiStatus == JobStatus.COMPLETE)
//                    completedStatusDeferred.complete(JobStatus.COMPLETE)
//                Unit
//            }
//
//            downloadJobItemRunner2.download()
//
//            val completedStatus = withTimeout(15000) { completedStatusDeferred.await() }
//
//
//            Assert.assertEquals("First download job item runner status was WAITING_FOR_CONNECTION after disconnect",
//                    JobStatus.QUEUED, statusAfterDisconnect)
//            Assert.assertEquals("File download task completed successfully",
//                    JobStatus.COMPLETE, completedStatus)
//
//            Assert.assertEquals("Correct number of ContainerEntry items available in client db",
//                    container.cntNumEntries,
//                    clientDb.containerEntryDao.findByContainer(item.djiContainerUid).size)
//
//            assertContainersHaveSameContent(item.djiContainerUid, item.djiContainerUid,
//                    serverDb, serverRepo, clientDb, clientRepo)
//        }
//    }
//
//    @Test
//    fun givenDownloadStartsOnMeteredConnection_whenJobSetChangedToDisableMeteredConnection_shouldStopAndSetStatus() {
//        var item = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!
//        var statusAfterWaitingForDownload = -1
//        runBlocking {
//            cloudMockDispatcher.throttleBytesPerPeriod = (128 * 1000)
//            cloudMockDispatcher.throttlePeriod = 1000
//
//
//            clientDb.downloadJobDao.setMeteredConnectionAllowedByJobUidSync(
//                    item.djiDjUid, true)
//
//            connectivityStatus = ConnectivityStatus(STATE_METERED, true, null)
//            connectivityStatusLiveData.sendValue(connectivityStatus)
//
//            val jobItemRunner = DownloadJobItemRunner(context, item, containerDownloadManager,
//                    mockedNetworkManager, clientDb,
//                    cloudEndPoint, connectivityStatus,
//                    localAvailabilityManager = mock<LocalAvailabilityManager>(),
//                    connectivityStatusLiveData = connectivityStatusLiveData)
//
//            UMLog.l(UMLog.DEBUG, 699,
//                    " Running DownloadJobItemRunner for " + item.djiUid)
//            val runningCountdownLatch = CountDownLatch(1)
//            val queuedCountdownLatch = CountDownLatch(1)
//            whenever(containerDownloadManager.handleDownloadJobItemUpdated(any(), any())).doAnswer {
//                when((it.arguments[0] as DownloadJobItem).djiStatus) {
//                    JobStatus.RUNNING -> runningCountdownLatch.countDown()
//                    JobStatus.QUEUED -> queuedCountdownLatch.countDown()
//                }
//            }
//
//            launch { jobItemRunner.download() }
//
//            runningCountdownLatch.await(5, TimeUnit.SECONDS)
//
//            delay(2000)
//            jobItemRunner.meteredDataAllowed = false
//
//            queuedCountdownLatch.await(5, TimeUnit.SECONDS)
//            argumentCaptor<DownloadJobItem>().apply {
//                verify(containerDownloadManager, atLeastOnce()).handleDownloadJobItemUpdated(capture(), any())
//                Assert.assertEquals("Last item status update was QUEUED", JobStatus.QUEUED,
//                        lastValue.djiStatus)
//            }
//        }
//    }
//
//    @Test
//    fun givenDownloadStarted_whenConnectionGoesOff_shouldStopAndSetStatusToWaiting() {
//        var item = clientDb.downloadJobItemDao.findByUid(
//                downloadJobItem.djiUid)!!
//
//        runBlocking {
//            cloudMockDispatcher.throttleBytesPerPeriod = (128 * 1000)
//            cloudMockDispatcher.throttlePeriod = 1000
//
//
//            val jobItemRunner = DownloadJobItemRunner(context, item, containerDownloadManager,
//                    mockedNetworkManager, clientDb,
//                    cloudEndPoint, connectivityStatus,
//                    localAvailabilityManager = mock<LocalAvailabilityManager>(),
//                    connectivityStatusLiveData = connectivityStatusLiveData)
//
//            val runningCountdownLatch = CountDownLatch(1)
//            val queuedCountdownLatch = CountDownLatch(1)
//            whenever(containerDownloadManager.handleDownloadJobItemUpdated(any(), any())).doAnswer {
//                when((it.arguments[0] as DownloadJobItem).djiStatus) {
//                    JobStatus.RUNNING -> runningCountdownLatch.countDown()
//                    JobStatus.QUEUED -> queuedCountdownLatch.countDown()
//                }
//            }
//
//            launch { jobItemRunner.download() }
//
//            //let the download runner start
//            runningCountdownLatch.await(5, TimeUnit.SECONDS)
//            delay(1000)
//
//            connectivityStatusLiveData.sendValue(
//                    ConnectivityStatus(STATE_DISCONNECTED, false, null))
//
//            queuedCountdownLatch.await(5, TimeUnit.SECONDS)
//            argumentCaptor<DownloadJobItem>().apply {
//                verify(containerDownloadManager, atLeastOnce()).handleDownloadJobItemUpdated(capture(), any())
//                Assert.assertEquals("Final status is queued", JobStatus.QUEUED,
//                        lastValue.djiStatus)
//            }
//        }
//
//    }
//
//
//
//    @Test
//    fun givenDownloadLocallyAvailable_whenRun_shouldDownloadFromLocalPeer() {
//        runBlocking {
//            val item = clientDb.downloadJobItemDao.findByUid(
//                    downloadJobItem.djiUid)!!
//            val mockAvailabilityManager = mock<LocalAvailabilityManager> {
//                onBlocking { findBestLocalNodeForContentEntryDownload(any()) }.thenReturn(networkNode)
//            }
//
//            val jobItemRunner = DownloadJobItemRunner(context, item, containerDownloadManager,
//                    mockedNetworkManager, clientDb,
//                    cloudEndPoint, connectivityStatus,
//                    localAvailabilityManager = mockAvailabilityManager,
//                    connectivityStatusLiveData = connectivityStatusLiveData)
//
//            val startTime = System.currentTimeMillis()
//            jobItemRunner.download().await()
//
//            val runTime = System.currentTimeMillis() - startTime
//            println("Download job completed in $runTime ms")
//
//            Assert.assertTrue("Request was made to peer server",
//                    peerMockWebServer!!.requestCount >= 2)
//            Assert.assertEquals("No requests sent to cloud server",
//                    0, cloudMockWebServer.requestCount)
//
//
//            verify(mockedNetworkManager).connectToWiFi(groupBle.ssid, groupBle.passphrase)
//
//            argumentCaptor<DownloadJobItem>().apply {
//                verify(containerDownloadManager, atLeastOnce()).handleDownloadJobItemUpdated(capture(), any())
//                Assert.assertEquals("Last status update was completed", JobStatus.COMPLETE,
//                        lastValue.djiStatus)
//            }
//
//            assertContainersHaveSameContent(item.djiContainerUid, item.djiContainerUid,
//                    serverDb, serverRepo, clientDb, clientRepo)
//        }
//
//    }
//
//    /**
//     * The container system avoids the need to download duplicate entries (files that are already
//     * present in other containers). When a download contains entries that the device already has
//     * from other containers, these items should be linked and only the new files should be downloaded.
//     */
//    @Test
//    fun givenOneItemDownloaded_whenSecondItemWithDuplicatesIsDownloaded_thenOnlyNewEntriesAreDownloadeAndBothShouldHaveContentMatchingOriginalContainers() {
//        runBlocking {
//            val container2TmpFile = File.createTempFile("DownloadJobItemRunnerTest", "c2")
//            val container2TmpDir = Files.createTempDirectory("DownloadJobItemRunnerTestC2Tmp").toFile()
//            extractTestResourceToFile(TEST_FILE_WITH_DUPLICATES, container2TmpFile)
//
//            val contentEntry2 = ContentEntry("The big chicken", "The big chicken", true, true)
//            contentEntry2.contentEntryUid = serverDb.contentEntryDao.insert(contentEntry2)
//            var container2 = Container(contentEntry2)
//            container2.containerUid = serverDb.containerDao.insert(container2)
//            val containerManager2 = ContainerManager(container2, serverDb, serverDb,
//                    container2TmpDir.absolutePath)
//            addEntriesFromZipToContainer(container2TmpFile.absolutePath, containerManager2)
//            container2 = serverDb.containerDao.findByUid(container2.containerUid)!!
//
//            clientDb.contentEntryDao.insert(contentEntry2)
//            clientDb.containerDao.insert(container2)
//
//            //Create a DownloadJob and DownloadJobItem
//            val downloadJob2 = DownloadJob(contentEntry2.contentEntryUid,
//                    System.currentTimeMillis()).also {
//                it.timeRequested = System.currentTimeMillis()
//                it.djStatus = JobStatus.QUEUED
//                it.djDestinationDir = clientContainerDir.absolutePath
//                it.djUid = clientDb.downloadJobDao.insert(it).toInt()
//            }
//
//            val downloadJobItem2 = DownloadJobItem(downloadJob2, contentEntry2.contentEntryUid,
//                    container2.containerUid, container2.fileSize).also {
//                it.djiStatus = JobStatus.QUEUED
//                it.destinationFile = File(clientContainerDir,
//                        contentEntry2.contentEntryUid.toString()).absolutePath
//            }
//
//            downloadJobItem2.djiUid = clientDb.downloadJobItemDao.insert(downloadJobItem2).toInt()
//
//            val item1 = clientDb.downloadJobItemDao.findByUid(
//                    downloadJobItem.djiUid)!!
//            val jobItemRunner1 = DownloadJobItemRunner(context, item1, containerDownloadManager,
//                    mockedNetworkManager, clientDb,
//                    cloudEndPoint, connectivityStatus,
//                    localAvailabilityManager = mock<LocalAvailabilityManager>(),
//                    connectivityStatusLiveData = connectivityStatusLiveData)
//
//            val jobItem1Status = jobItemRunner1.download().await()
//
//            //val jobItem1Status = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!.djiStatus
//
//
//            val jobItemRunner2 = DownloadJobItemRunner(context, downloadJobItem2, containerDownloadManager,
//                    mockedNetworkManager, clientDb,
//                    cloudEndPoint, connectivityStatus,
//                    localAvailabilityManager = mock<LocalAvailabilityManager>(),
//                    connectivityStatusLiveData = connectivityStatusLiveData)
//
//            val jobItem2Status = jobItemRunner2.download().await()
//
//            Assert.assertEquals("Job item 1 completes OK", JobStatus.COMPLETE, jobItem1Status)
//            Assert.assertEquals("Job item 2 completes OK", JobStatus.COMPLETE, jobItem2Status)
//
//            assertContainersHaveSameContent(container.containerUid, container.containerUid,
//                    serverDb, serverDb, clientDb, clientDb)
//            assertContainersHaveSameContent(container2.containerUid, container2.containerUid,
//                    serverDb, serverDb, clientDb, clientDb)
//        }
//    }


    companion object {

        @BeforeClass
        @JvmStatic
        fun setupLog() {
            Napier.base(DebugAntilog())
        }
    }
}