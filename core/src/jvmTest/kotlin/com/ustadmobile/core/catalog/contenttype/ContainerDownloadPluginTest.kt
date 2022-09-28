package com.ustadmobile.core.catalog.contenttype

import io.github.aakira.napier.Napier
import org.mockito.kotlin.mock
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.DummyContentJobItemTransactionRunner
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZip
import com.ustadmobile.core.io.ext.toKmpUriString
import com.ustadmobile.core.util.ConcatenatedResponse2Dispatcher
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.toDeepLink
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.commontest.ext.assertContainerEqualToOther
import com.ustadmobile.util.test.ext.baseDebugIfNotEnabled
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.json.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.File
import kotlin.random.Random

class ContainerDownloadPluginTest {

    private lateinit var mockWebServer: MockWebServer

    private lateinit var dispatcher: ConcatenatedResponse2Dispatcher

    private lateinit var serverDb: UmAppDatabase

    private lateinit var serverRepo: UmAppDatabase

    private lateinit var serverHttpClient: HttpClient

    private lateinit var serverOkHttpClient: OkHttpClient

    private lateinit var container: Container

    private lateinit var contentEntry: ContentEntry

    private lateinit var clientDi: DI

    private lateinit var siteEndpoint: Endpoint

    private lateinit var epubFile: File

    private lateinit var containerTmpFolder: File

    private lateinit var downloadDestDir: File

    private lateinit var siteUrl: String

    private lateinit var clientDb: UmAppDatabase

    private lateinit var clientRepo: UmAppDatabase

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    val ustadTestRule = UstadTestRule()

    @Before
    fun setup() {
        downloadDestDir = temporaryFolder.newFolder()

        Napier.baseDebugIfNotEnabled()
        serverOkHttpClient = OkHttpClient()
        serverHttpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                gson()
            }
            install(HttpTimeout)
            engine {
                preconfigured = serverOkHttpClient
            }
        }

        val serverNodeIdAndAuth = NodeIdAndAuth(Random.nextLong(), randomUuid().toString())
        serverDb = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite:build/tmp/ContainerDownloadPluginTest.sqlite")
                .addSyncCallback(serverNodeIdAndAuth)
                .build()
                .clearAllTablesAndResetNodeId(serverNodeIdAndAuth.nodeId)

        serverRepo = serverDb.asRepository(repositoryConfig(Any(), "http://localhost/dummy",
                serverNodeIdAndAuth.nodeId, serverNodeIdAndAuth.auth, serverHttpClient, serverOkHttpClient))

        contentEntry = ContentEntry().apply {
            title = "Test Epub"
            contentEntryUid = serverRepo.contentEntryDao.insert(this)
        }

        container = Container().apply {
            containerContentEntryUid = contentEntry.contentEntryUid
            containerUid = serverRepo.containerDao.insert(this)
        }

        epubFile = temporaryFolder.newFile()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/contentformats/epub/test.epub")!!
                .writeToFile(epubFile)
        containerTmpFolder = temporaryFolder.newFolder()
        runBlocking {
            serverRepo.addEntriesToContainerFromZip(
                    container.containerUid,
                    epubFile.toDoorUri(), ContainerAddOptions(containerTmpFolder.toDoorUri()), Any()
            )
        }
        clientDi = DI {
            import(ustadTestRule.diModule)
        }

        //Create a mock web server that will serve the concatenated data
        mockWebServer = MockWebServer()
        dispatcher = ConcatenatedResponse2Dispatcher(serverDb, clientDi, container.containerUid)
        mockWebServer.dispatcher = dispatcher
        mockWebServer.start()
        siteUrl = mockWebServer.url("/").toString()

        siteEndpoint = Endpoint(mockWebServer.url("/").toString())

        clientDb = clientDi.direct.on(siteEndpoint).instance(tag = DoorTag.TAG_DB)
        clientRepo = clientDi.direct.on(siteEndpoint).instance(tag = DoorTag.TAG_REPO)
        clientRepo.contentEntryDao.insert(contentEntry)

        //Get the updated size and number of entries
        container = serverDb.containerDao.findByUid(container.containerUid)!!
        clientRepo.containerDao.insert(container)
    }

    private fun makeDownloadJobAndJobItem(
        setContainerInfo: Boolean = true
    ) : ContentJobItemAndContentJob {
        return runBlocking {
            ContentJobItemAndContentJob().apply {
                contentJob = ContentJob().apply {
                    this.toUri = downloadDestDir.toKmpUriString()
                    this.cjIsMeteredAllowed = true
                    this.cjUid = clientDb.contentJobDao.insertAsync(this)
                }
                contentJobItem = ContentJobItem().apply {
                    this.cjiContentEntryUid = contentEntry.contentEntryUid
                    if(setContainerInfo) {
                        this.cjiContainerUid = container.containerUid
                        this.cjiItemTotal = container.fileSize
                    }

                    this.cjiJobUid = contentJob!!.cjUid
                    this.cjiUid = clientDb.contentJobItemDao.insertJobItem(this)
                }
            }
        }
    }


    @After
    fun shutdown() {
        mockWebServer.shutdown()
        serverHttpClient.close()
    }

    @Test
    fun givenValidRequest_whenDownloadCalled_thenShouldDownloadContainerFiles() {
        val clientDb: UmAppDatabase = clientDi.on(Endpoint(siteUrl)).direct
            .instance(tag = DoorTag.TAG_DB)

        val mockListener = mock<ContentJobProgressListener> { }

        val job = makeDownloadJobAndJobItem()

        val processContext = ContentJobProcessContext(temporaryFolder.newFolder().toDoorUri(),
            temporaryFolder.newFolder().toDoorUri(), params = mutableMapOf(),
            DummyContentJobItemTransactionRunner(clientDb), clientDi)


        val downloadJob = ContainerDownloadPlugin(Any(), Endpoint(siteUrl), clientDi)
        val result = runBlocking {  downloadJob.processJob(job, processContext, mockListener) }


        Assert.assertEquals("Result is reported as successful", JobStatus.COMPLETE,
                result.status)

        serverDb.assertContainerEqualToOther(container.containerUid, clientDb)
    }

    @Test
    fun givenDownloadIsInterrupted_whenNewRequestMade_thenDownloadShouldResume() {
        dispatcher.numTimesToFail.set(1)

        val siteUrl = mockWebServer.url("/").toString()

        val mockListener = mock<ContentJobProgressListener> { }

        val results = mutableListOf<Int>()
        val clientDb: UmAppDatabase = clientDi.on(Endpoint(siteUrl)).direct.instance(tag = DoorTag.TAG_DB)

        val job = makeDownloadJobAndJobItem()


        val exceptions = mutableListOf<Exception>()
        for(i in 0..1) {
            Napier.d("============ ATTEMPT $i ============")
            try {
                val processContext = ContentJobProcessContext(
                    temporaryFolder.newFolder().toDoorUri(),
                    temporaryFolder.newFolder().toDoorUri(), params = mutableMapOf(),
                    DummyContentJobItemTransactionRunner(clientDb), clientDi)

                val downloadJob = ContainerDownloadPlugin(Any(), Endpoint(siteUrl), clientDi)
                val result = runBlocking {  downloadJob.processJob(job, processContext, mockListener) }

                results.add(result.status)
            }catch(e: Exception) {
                exceptions += e
                e.printStackTrace()
            }

        }

        serverDb.assertContainerEqualToOther(container.containerUid, clientDb)

        Assert.assertTrue("Exception was thrown first time", exceptions.isNotEmpty())
        Assert.assertEquals("Got one result", 1, results.size)
        Assert.assertEquals("Result return is completed", JobStatus.COMPLETE, results[0])

        mockWebServer.takeRequest()//first get list of entries request
        mockWebServer.takeRequest()//first download attempt
        mockWebServer.takeRequest() //second get list of entries request
        val mockRequest4 = mockWebServer.takeRequest()//actual second download attempt
        Assert.assertNotNull("Second request included partial response request",
                mockRequest4.getHeader("range"))
    }

    @Test
    fun givenValidSourceUri_whenExtractMetadataCalled_thenShouldReturnContentEntry() {
        val endpointUrl = mockWebServer.url("/").toString()
        val clientRepo: UmAppDatabase = clientDi.on(Endpoint(endpointUrl)).direct
            .instance(tag = DoorTag.TAG_REPO)
        val contentEntry = ContentEntry().apply {
            title = "Hello World"
            leaf = true
            contentEntryUid = clientRepo.contentEntryDao.insert(this)
        }


        val containerDownloadContentJob = ContainerDownloadPlugin(Any(),
            Endpoint(endpointUrl), clientDi)

        val contentEntryDeepLink = contentEntry.toDeepLink(Endpoint(endpointUrl))

        val metaDataExtracted = runBlocking {
            containerDownloadContentJob.extractMetadata(
                DoorUri.parse(contentEntryDeepLink), mock {  })
        }

        Assert.assertEquals("Content title matches", contentEntry.title,
            metaDataExtracted?.entry?.title)
    }

    //Test to make sure that if the ContentJobItem has only the sourceUri that everything works as
    //expected
    @Test
    fun givenValidSourceUri_whenProcessJobCalled_thenShouldSetContentEntryUidAndContainerUid() {
        val siteUrl = mockWebServer.url("/").toString()

        val clientDb: UmAppDatabase = clientDi.on(Endpoint(siteUrl)).direct.instance(tag = DoorTag.TAG_DB)

        val mockListener = mock<ContentJobProgressListener> { }

        val job = makeDownloadJobAndJobItem()

        val processContext = ContentJobProcessContext(temporaryFolder.newFolder().toDoorUri(),
            temporaryFolder.newFolder().toDoorUri(), params = mutableMapOf(),
            DummyContentJobItemTransactionRunner(clientDb), clientDi)


        val downloadJob = ContainerDownloadPlugin(Any(), Endpoint(siteUrl), clientDi)
        val result = runBlocking {  downloadJob.processJob(job, processContext, mockListener) }


        Assert.assertEquals("Result is reported as successful", JobStatus.COMPLETE,
            result.status)

        serverDb.assertContainerEqualToOther(container.containerUid, clientDb)

        val contentJobItemInDb = clientDb.contentJobItemDao.findRootJobItemByJobId(job.contentJobItem?.cjiUid ?: 0L)
        Assert.assertEquals("ContentEntryUid was set from sourceUri", contentEntry.contentEntryUid,
            contentJobItemInDb?.cjiContentEntryUid)
        Assert.assertEquals("ContainerUid was set to most recent container after looking up content entry",
            container.containerUid, contentJobItemInDb?.cjiContainerUid)
    }

    @Test
    fun givenAllContainerEntryMd5sAlreadyPresent_whenDownloaded_thenShouldSucceed() {
        val clientDb: UmAppDatabase = clientDi.on(Endpoint(siteUrl)).direct
            .instance(tag = DoorTag.TAG_DB)

        val localContainerAlreadyDownloaded = Container().apply {
            containerUid = clientDb.containerDao.insert(this)
        }

        runBlocking {
            clientDb.addEntriesToContainerFromZip(localContainerAlreadyDownloaded.containerUid,
                epubFile.toDoorUri(), ContainerAddOptions(containerTmpFolder.toDoorUri()), Any())
        }

        val job = makeDownloadJobAndJobItem()

        val processContext = ContentJobProcessContext(temporaryFolder.newFolder().toDoorUri(),
            temporaryFolder.newFolder().toDoorUri(), params = mutableMapOf(),
            DummyContentJobItemTransactionRunner(clientDb), clientDi)


        val downloadJob = ContainerDownloadPlugin(Any(), Endpoint(siteUrl), clientDi)
        val result = runBlocking {  downloadJob.processJob(job, processContext, mock { }) }


        Assert.assertEquals("Result is reported as successful", JobStatus.COMPLETE,
            result.status)

        serverDb.assertContainerEqualToOther(container.containerUid, clientDb)
    }

    @Test
    fun givenContainerNotActive_whenDownloaded_thenWillFail() {
        val inactiveContainer = Container().apply {
            containerContentEntryUid = contentEntry.contentEntryUid
            cntLastModified = systemTimeInMillis()
            containerUid = container.containerUid
        }
        clientDb.containerDao.replaceList(listOf(inactiveContainer))
        container = inactiveContainer
        clientDb.containerEntryDao.deleteByContainerUid(container.containerUid)

        val job = makeDownloadJobAndJobItem()

        val processContext = ContentJobProcessContext(temporaryFolder.newFolder().toDoorUri(),
            temporaryFolder.newFolder().toDoorUri(), params = mutableMapOf(),
            DummyContentJobItemTransactionRunner(clientDb), clientDi)


        val downloadJob = ContainerDownloadPlugin(Any(), Endpoint(siteUrl), clientDi)
        val result = runBlocking {  downloadJob.processJob(job, processContext, mock { } ) }

        Assert.assertEquals("Attempt to download inactive container fails",
            JobStatus.FAILED, result.status)
    }

    @Test
    fun givenNewContainerNotYetActive_whenDownloaded_thenWillDownloadOlderActiveContainer() {
        //put in a more recent, but inactive container
        Container().apply {
            containerContentEntryUid = contentEntry.contentEntryUid
            cntLastModified = systemTimeInMillis()
            containerUid = clientDb.containerDao.insert(this)
        }

        val job = makeDownloadJobAndJobItem(setContainerInfo = false)

        val processContext = ContentJobProcessContext(temporaryFolder.newFolder().toDoorUri(),
            temporaryFolder.newFolder().toDoorUri(), params = mutableMapOf(),
            DummyContentJobItemTransactionRunner(clientDb), clientDi)

        val downloadJob = ContainerDownloadPlugin(Any(), Endpoint(siteUrl), clientDi)
        val result = runBlocking {  downloadJob.processJob(job, processContext, mock { } ) }

        Assert.assertEquals("Downloading when there is a more recent, but inactive container, reports success",
            JobStatus.COMPLETE, result.status)

        val downloadedContainerUid = clientDb.contentJobItemDao
            .findRootJobItemByJobId(job.contentJobItem?.cjiJobUid ?: 0)?.cjiContainerUid ?: 0

        Assert.assertEquals("Download used the previous active container uid, not the most recent inactive one",
            container.containerUid, downloadedContainerUid)
    }





}