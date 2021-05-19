package com.ustadmobile.sharedse.network

import io.github.aakira.napier.Napier
import com.google.gson.Gson
import org.mockito.kotlin.*
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZip
import com.ustadmobile.core.io.ext.toKmpUriString
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.ext.DoorTag.Companion.TAG_REPO
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_CONNECTED_LOCAL
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_CONNECTING_LOCAL
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_DISCONNECTED
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_METERED
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_UNMETERED
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcher
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcherJvm
import com.ustadmobile.util.commontest.ext.assertContainerEqualToOther
import com.ustadmobile.util.commontest.ext.mockResponseForConcatenatedFiles2Request
import com.ustadmobile.util.test.ReverseProxyDispatcher
import com.ustadmobile.util.test.ext.baseDebugIfNotEnabled
import com.ustadmobile.util.test.extractTestResourceToFile
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.server.engine.ApplicationEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.naming.InitialContext


class DownloadJobItemRunnerTest {

    private lateinit var cloudServer: ApplicationEngine

    private lateinit var cloudMockWebServer: MockWebServer

    private lateinit var cloudMockDispatcher: ReverseProxyDispatcher

    private lateinit var peerServer: EmbeddedHTTPD

    private var peerMockDispatcher: ContainerDownloadDispatcher? = null

    private lateinit var peerMockWebServer: MockWebServer

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

    private lateinit var networkNode: NetworkNode

    private lateinit var connectivityStatus: DoorMutableLiveData<ConnectivityStatus>

    private lateinit var wifiDirectGroupInfoMessage: BleMessage

    private lateinit var groupBle: WiFiDirectGroupBle

    private lateinit var downloadJobItem: DownloadJobItem

    private lateinit var container: Container

    private lateinit var containerWDuplicates: Container

    private val MAX_LATCH_WAITING_TIME = 15000L

    private val MAX_THREAD_SLEEP_TIME = 2000L

    private lateinit var containerDownloadManager: ContainerDownloadManager

    private lateinit var connectivityStatusLiveData: DoorMutableLiveData<ConnectivityStatus>

    private lateinit var clientDi: DI

    lateinit var mockNetworkManager: NetworkManagerBle

    private lateinit var mockLocalAvailabilityManager: LocalAvailabilityManager

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    class ContainerDownloadDispatcher(val serverDb: UmAppDatabase, val container: Container,
        val endpointPrefix: String = ""): Dispatcher() {

        val numTimesToFail = AtomicInteger(0)

        var throttleBytesPerPeriod = 0L

        var throttlePeriod = 0L

        var throttlePeriodUnit: TimeUnit = TimeUnit.MILLISECONDS

        var reachable = true


        override fun dispatch(request: RecordedRequest): MockResponse {
            return when {
                (endpointPrefix != "") && !request.requestUrl!!.encodedPath.startsWith(endpointPrefix) -> {
                    MockResponse()
                            .setResponseCode(404)
                            .addHeader("Content-Type", "text/plain")
                            .setBody("Not found")
                }

                request.requestUrl.toString().endsWith("/${DownloadJobItemRunner.CONTAINER_ENTRY_LIST_PATH}?containerUid=${container.containerUid}") -> {
                    val entryList = serverDb.containerEntryDao.findByContainerWithMd5(container.containerUid)
                    MockResponse()
                            .addHeader("Content-Type", "application/json")
                            .setBody(Gson().toJson(entryList))
                }

                request.requestUrl.toString().contains(ContainerEntryFileDao.ENDPOINT_CONCATENATEDFILES2) -> {
                    serverDb.mockResponseForConcatenatedFiles2Request(request)
                }

                else -> {
                    MockResponse()
                            .setResponseCode(404)
                }
            }.also {
                it.takeIf { numTimesToFail.decrementAndGet() >= 0}?.setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY)
                it.takeIf { !reachable }?.setSocketPolicy(SocketPolicy.NO_RESPONSE)
                it.takeIf { throttleBytesPerPeriod != 0L }?.throttleBody(throttleBytesPerPeriod,
                        throttlePeriod, throttlePeriodUnit)
            }
        }
    }

    @Before
    @Throws(IOException::class)
    fun setup() {
        val endpointScope = EndpointScope()
        Napier.baseDebugIfNotEnabled()

        connectivityStatusLiveData = DoorMutableLiveData(ConnectivityStatus().apply {
            connectedOrConnecting = true
            connectivityState = STATE_UNMETERED
            wifiSsid = "wifi-mock"
        })

        groupBle = WiFiDirectGroupBle("DIRECT-PeerNode", "networkPass123").also {
            it.ipAddress = "127.0.0.1"
        }

        mockNetworkManager = mock {
            on { connectivityStatus }.thenReturn(connectivityStatusLiveData)
            onBlocking { sendBleMessage(any(), any())}.thenAnswer {
                if(mockedNetworkManagerBleWorking.get()) {
                    return@thenAnswer BleMessage(NetworkManagerBleCommon.WIFI_GROUP_CREATION_RESPONSE,
                            42.toByte(), groupBle.toBytes())
                }

                Unit
            }

            on { connectToWiFi(eq(groupBle.ssid), eq(groupBle.passphrase))}.thenAnswer {
                peerMockDispatcher?.reachable = true
                connectivityStatusLiveData.sendValue(ConnectivityStatus(
                        STATE_DISCONNECTED, true, groupBle.ssid))
                GlobalScope.launch {
                    delay(100)
                    connectivityStatusLiveData.sendValue(ConnectivityStatus(
                            STATE_CONNECTING_LOCAL, true, groupBle.ssid))
                    delay(200)
                    connectivityStatusLiveData.sendValue(ConnectivityStatus(
                            STATE_CONNECTED_LOCAL, true, groupBle.ssid))
                }
                Unit
            }
        }

        clientDi = DI {
            bind<UstadMobileSystemImpl>() with singleton {
                UstadMobileSystemImpl(XmlPullParserFactory.newInstance())
            }

            bind<UstadAccountManager>() with singleton { UstadAccountManager(instance(), Any(), di) }
            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(UmAppDatabase.getInstance(Any(), dbName).also {
                    it.clearAllTables()
                })
            }

            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_REPO) with scoped(endpointScope).singleton {
                spy(instance<UmAppDatabase>(tag = TAG_DB).asRepository(repositoryConfig(Any(), context.url,
                    instance(), instance())))
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

            bind<Gson>() with singleton { Gson() }

            registerContextTranslator { account: UmAccount -> Endpoint(account.endpointUrl) }

            bind<OkHttpClient>() with singleton {
                OkHttpClient.Builder()
                    .dispatcher(okhttp3.Dispatcher().also {
                        it.maxRequests = 30
                        it.maxRequestsPerHost = 10
                    })
                    .build()
            }

            bind<HttpClient>() with singleton {
                HttpClient(OkHttp) {

                    install(JsonFeature) {
                        serializer = GsonSerializer()
                    }
                    install(HttpTimeout)

                    val dispatcher = okhttp3.Dispatcher()
                    dispatcher.maxRequests = 30
                    dispatcher.maxRequestsPerHost = 10

                    engine {
                        preconfigured = instance()
                    }

                }
            }
        }


        cloudMockWebServer = MockWebServer()
        peerMockWebServer = MockWebServer().also {
            it.start()
            groupBle.port = it.port
        }

        val accountManager: UstadAccountManager by clientDi.instance()
        accountManager.activeAccount = UmAccount(0, "guest", "",
                cloudMockWebServer.url("/").toString())

        clientDb =  clientDi.on(accountManager.activeAccount).direct.instance(tag = TAG_DB)
        clientRepo = clientDi.on(accountManager.activeAccount).direct.instance(tag = TAG_REPO)
        containerDownloadManager = clientDi.on(accountManager.activeAccount).direct.instance()

        serverDb = UmAppDatabase.getInstance(context).also {
            it.clearAllTables()
        }

        //this can be shared as needed
        val httpClient: HttpClient= clientDi.direct.instance()
        serverRepo = serverDb.asRepository(repositoryConfig(context, "http://localhost/dummy",
            httpClient, clientDi.direct.instance()))

        mockLocalAvailabilityManager = clientDi.on(accountManager.activeAccount).direct.instance()

        clientContainerDir = temporaryFolder.newFolder("clientContainerDir")

        networkNode = NetworkNode()
        networkNode.bluetoothMacAddress = "00:3F:2F:64:C6:4F"
        networkNode.lastUpdateTimeStamp = System.currentTimeMillis()
        networkNode.nodeId = clientDb.networkNodeDao.replace(networkNode)

        webServerTmpDir = temporaryFolder.newFolder("webServerTmpDir")
        webServerTmpContentEntryFile = File(webServerTmpDir, "" + TEST_CONTENT_ENTRY_FILE_UID)

        javaClass.getResourceAsStream(TEST_FILE_RESOURCE_PATH).writeToFile(webServerTmpContentEntryFile)

        containerTmpDir = temporaryFolder.newFolder("containerTmpDir")

        val contentEntry = ContentEntry()
        contentEntry.title = "Test entry"
        contentEntry.contentEntryUid = clientRepo.contentEntryDao.insert(contentEntry)

        container = Container(contentEntry)
        container.containerUid = serverRepo.containerDao.insert(container)
        runBlocking {
            serverRepo.addEntriesToContainerFromZip(container.containerUid,
                webServerTmpContentEntryFile.toDoorUri(),
                ContainerAddOptions(webServerTmpDir.toDoorUri()))
        }

        //add the container itself to the client database (would normally happen via sync/preload)
        clientRepo.containerDao.insert(container)



        val downloadJob = DownloadJob(contentEntry.contentEntryUid,
                System.currentTimeMillis())
        downloadJob.timeRequested = System.currentTimeMillis()
        downloadJob.djStatus = JobStatus.QUEUED
        downloadJob.djDestinationDir = clientContainerDir.toKmpUriString()
        downloadJob.djUid = clientDb.downloadJobDao.insert(downloadJob).toInt()

        downloadJobItem = DownloadJobItem(downloadJob, contentEntry.contentEntryUid,
                container.containerUid, container.fileSize)
        downloadJobItem.djiStatus = JobStatus.QUEUED
        downloadJobItem.downloadedSoFar = 0
        downloadJobItem.destinationFile = File(clientContainerDir,
                TEST_CONTENT_ENTRY_FILE_UID.toString()).absolutePath

        downloadJobItem.djiUid = clientDb.downloadJobItemDao.insert(downloadJobItem).toInt()


        mockedNetworkManagerBleWorking.set(true)

        mockedNetworkManagerWifiConnectWorking.set(true)

        cloudEndPoint = cloudMockWebServer.url("/").toString()

    }

    @Test
    fun givenDownload_whenRun_shouldDownloadAndComplete() {
        runBlocking {
            cloudMockWebServer.dispatcher = ContainerDownloadDispatcher(serverDb, container)

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

            serverDb.assertContainerEqualToOther(container.containerUid, clientDb)
        }
    }


    @Test
    fun givenDownloadStarted_whenFailsOnce_shouldRetryAndComplete() {
        runBlocking {
            cloudMockWebServer.dispatcher = ContainerDownloadDispatcher(serverDb, container).apply {
                numTimesToFail.set(1)
            }

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

            serverDb.assertContainerEqualToOther(container.containerUid, clientDb)
        }
    }

    @Test
    fun givenDownloadStarted_whenFailsExceedMaxAttempts_shouldStopAndSetStatusToFailed() {
        runBlocking {
            cloudMockWebServer.dispatcher = ContainerDownloadDispatcher(serverDb, container).apply {
                numTimesToFail.set(10)
            }

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

    @Test
    fun givenDownloadUnmeteredConnectivityOnly_whenConnectivitySwitchesToMetered_shouldStopAndSetStatusToQueued() {
        val item = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!
        runBlocking {
            cloudMockWebServer.dispatcher = ContainerDownloadDispatcher(serverDb, container).apply {
                //set speed to 512kbps (period unit by default is milliseconds)
                throttleBytesPerPeriod = (8 * 1000)
                throttlePeriod = 1000
            }

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

            queuedStatusLatch.await(2, TimeUnit.SECONDS)

            //Wait to make sure that nothing else happens after status is switched to queued
            delay(2000)

            argumentCaptor<DownloadJobItem>() {
                verify(containerDownloadManager, atLeastOnce()).handleDownloadJobItemUpdated(capture(), any())
                Assert.assertEquals("Final status update was jobstatus = queued", JobStatus.QUEUED,
                        lastValue.djiStatus)

            }
        }

    }

    @Test
    fun givenDownloadStarted_whenJobIsStopped_shouldStopAndSetStatus() {
        runBlocking {
            cloudMockWebServer.dispatcher = ContainerDownloadDispatcher(serverDb, container).apply {
                //set speed to 512kbps (period unit by default is milliseconds)
                throttleBytesPerPeriod = (8 * 1000)
                throttlePeriod = 1000
            }

            var item = clientDb.downloadJobItemDao.findByUid(
                    downloadJobItem.djiUid)!!
            val jobItemRunner = DownloadJobItemRunner(item, cloudEndPoint, 500, clientDi)

            val pauseCountdownLatch = CountDownLatch(1)
            whenever(containerDownloadManager.handleDownloadJobItemUpdated(any(), any())).thenAnswer {
                val downloadJobItemArg = it.arguments[0] as DownloadJobItem
                if(downloadJobItemArg.djiStatus == JobStatus.PAUSED)
                    pauseCountdownLatch.countDown()

                Unit
            }

            launch {
                jobItemRunner.download()
            }

            delay(1000)

            jobItemRunner.pause()

            pauseCountdownLatch.await(5, TimeUnit.SECONDS)
            argumentCaptor<DownloadJobItem>().apply {
                verify(containerDownloadManager, atLeastOnce()).handleDownloadJobItemUpdated(capture(), any())
                Assert.assertEquals("Last status update returns status_paused", JobStatus.PAUSED,
                        lastValue.djiStatus)
            }
        }
    }

    /**
     * DownloadJobItemRunner should stop when connectivity is lost, and then a new
     * DownloadJobItemRunner will be created when connectivity is restored. A DownloadJobItemRunner
     * must be capable of picking up from where the last one left off.
     */
    @Test
    fun givenDownloadJobItemRunnerStartedAndStopped_whenNextJobItemRunnerRuns_shouldFinishAndContentShouldMatch() {
        runBlocking {
            cloudMockWebServer.dispatcher = ContainerDownloadDispatcher(serverDb, container).apply {
                //set speed to 1Mbps (period unit by default is milliseconds)
                throttleBytesPerPeriod = (128 * 1000)
                throttlePeriod = 1000
            }

            val item = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!
            val jobItemRunner1 = DownloadJobItemRunner(item, cloudEndPoint, 500, clientDi)

            val queuedStatusDeferred = CompletableDeferred<Int>()
            whenever(containerDownloadManager.handleDownloadJobItemUpdated(any(), any())).doAnswer {
                if((it.arguments[0] as DownloadJobItem).djiStatus == JobStatus.QUEUED)
                    queuedStatusDeferred.complete(JobStatus.QUEUED)

                Unit
            }

            launch {
                jobItemRunner1.download()
            }

            delay(2000)

            connectivityStatusLiveData.sendValue(
                    ConnectivityStatus(ConnectivityStatus.STATE_DISCONNECTED, false, null))

            val statusAfterDisconnect = withTimeout(5000 * 1000) { queuedStatusDeferred.await() }

            connectivityStatusLiveData.sendValue(ConnectivityStatus(ConnectivityStatus.STATE_UNMETERED,
                    true, "wifi"))

            val downloadJobItemRunner2 = DownloadJobItemRunner(item, cloudEndPoint, 500, clientDi)

            val completedStatusDeferred = CompletableDeferred<Int>()
            whenever(containerDownloadManager.handleDownloadJobItemUpdated(any(), any())).doAnswer {
                if((it.arguments[0] as DownloadJobItem).djiStatus == JobStatus.COMPLETE)
                    completedStatusDeferred.complete(JobStatus.COMPLETE)
                Unit
            }

            downloadJobItemRunner2.download()

            val completedStatus = withTimeout(15000 * 1000) { completedStatusDeferred.await() }


            Assert.assertEquals("First download job item runner status was QUEUED after disconnect",
                    JobStatus.QUEUED, statusAfterDisconnect)
            Assert.assertEquals("File download task completed successfully",
                    JobStatus.COMPLETE, completedStatus)

            serverDb.assertContainerEqualToOther(item.djiContainerUid, clientDb)
        }
    }

    @Test
    fun givenDownloadStartsOnMeteredConnection_whenJobSetChangedToDisableMeteredConnection_shouldStopAndSetStatus() {
        var item = clientDb.downloadJobItemDao.findByUid(downloadJobItem.djiUid)!!
        var statusAfterWaitingForDownload = -1
        runBlocking {
            cloudMockWebServer.dispatcher = ContainerDownloadDispatcher(serverDb, container).apply {
                //set speed to 1Mbps (period unit by default is milliseconds)
                throttleBytesPerPeriod = (128 * 1000)
                throttlePeriod = 1000
            }


            clientDb.downloadJobDao.setMeteredConnectionAllowedByJobUidSync(
                    item.djiDjUid, true)

            connectivityStatusLiveData.sendValue(ConnectivityStatus(STATE_METERED, true, null))

            val jobItemRunner = DownloadJobItemRunner(item, cloudEndPoint, 500, clientDi)


            val runningCountdownLatch = CountDownLatch(1)
            val queuedCountdownLatch = CountDownLatch(1)
            whenever(containerDownloadManager.handleDownloadJobItemUpdated(any(), any())).doAnswer {
                when((it.arguments[0] as DownloadJobItem).djiStatus) {
                    JobStatus.RUNNING -> runningCountdownLatch.countDown()
                    JobStatus.QUEUED -> queuedCountdownLatch.countDown()
                }
            }

            launch { jobItemRunner.download() }

            runningCountdownLatch.await(5, TimeUnit.SECONDS)

            delay(2000)
            jobItemRunner.meteredDataAllowed = false

            queuedCountdownLatch.await(5, TimeUnit.SECONDS)
            argumentCaptor<DownloadJobItem>().apply {
                verify(containerDownloadManager, atLeastOnce()).handleDownloadJobItemUpdated(capture(), any())
                Assert.assertEquals("Last item status update was QUEUED", JobStatus.QUEUED,
                        lastValue.djiStatus)
            }
        }
    }

    @Test
    fun givenDownloadStarted_whenConnectionGoesOff_shouldStopAndSetStatusToWaiting() {
        var item = clientDb.downloadJobItemDao.findByUid(
                downloadJobItem.djiUid)!!

        runBlocking {
            cloudMockWebServer.dispatcher = ContainerDownloadDispatcher(serverDb, container).apply {
                //set speed to 1Mbps (period unit by default is milliseconds)
                throttleBytesPerPeriod = (128 * 1000)
                throttlePeriod = 1000
            }


            val jobItemRunner = DownloadJobItemRunner(item, cloudEndPoint, 500, clientDi)

            val runningCountdownLatch = CountDownLatch(1)
            val queuedCountdownLatch = CountDownLatch(1)
            whenever(containerDownloadManager.handleDownloadJobItemUpdated(any(), any())).doAnswer {
                when((it.arguments[0] as DownloadJobItem).djiStatus) {
                    JobStatus.RUNNING -> runningCountdownLatch.countDown()
                    JobStatus.QUEUED -> queuedCountdownLatch.countDown()
                }
            }

            launch { jobItemRunner.download() }

            //let the download runner start
            runningCountdownLatch.await(5, TimeUnit.SECONDS)
            delay(1000)

            connectivityStatusLiveData.sendValue(
                    ConnectivityStatus(STATE_DISCONNECTED, false, null))

            queuedCountdownLatch.await(5, TimeUnit.SECONDS)
            argumentCaptor<DownloadJobItem>().apply {
                verify(containerDownloadManager, atLeastOnce()).handleDownloadJobItemUpdated(capture(), any())
                Assert.assertEquals("Final status is queued", JobStatus.QUEUED,
                        lastValue.djiStatus)
            }
        }

    }


    @Test
    fun givenDownloadLocallyAvailable_whenRun_shouldDownloadFromLocalPeer() {
        runBlocking {
            val item = clientDb.downloadJobItemDao.findByUid(
                    downloadJobItem.djiUid)!!

            peerMockDispatcher  = ContainerDownloadDispatcher(serverDb, container,
                    endpointPrefix = "/${UMURLEncoder.encodeUTF8(cloudEndPoint)}/")
            peerMockWebServer.dispatcher = peerMockDispatcher!!

            cloudMockWebServer.dispatcher = ContainerDownloadDispatcher(serverDb, container)

            mockLocalAvailabilityManager.stub {
                onBlocking { findBestLocalNodeForContentEntryDownload(any()) }.thenReturn(networkNode)
            }

            val jobItemRunner = DownloadJobItemRunner(item, cloudEndPoint, 500, clientDi)

            val startTime = System.currentTimeMillis()
            jobItemRunner.download().await()

            val runTime = System.currentTimeMillis() - startTime
            println("Download job completed in $runTime ms")

            Assert.assertTrue("Request was made to peer server",
                    peerMockWebServer.requestCount >= 2)
            Assert.assertEquals("No requests sent to cloud server",
                    0, cloudMockWebServer.requestCount)


            verify(mockNetworkManager).connectToWiFi(groupBle.ssid, groupBle.passphrase)

            argumentCaptor<DownloadJobItem>().apply {
                verify(containerDownloadManager, atLeastOnce()).handleDownloadJobItemUpdated(capture(), any())
                Assert.assertEquals("Last status update was completed", JobStatus.COMPLETE,
                        lastValue.djiStatus)
            }

            serverDb.assertContainerEqualToOther(item.djiContainerUid, clientDb)
        }
    }

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

}