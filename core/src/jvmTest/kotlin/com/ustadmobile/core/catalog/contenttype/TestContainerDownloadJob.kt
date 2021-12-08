package com.ustadmobile.core.catalog.contenttype

import io.github.aakira.napier.Napier
import org.mockito.kotlin.mock
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZip
import com.ustadmobile.core.io.ext.toContainerEntryWithMd5
import com.ustadmobile.core.io.ext.toKmpUriString
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.clearAllTablesAndResetSync
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.commontest.ext.assertContainerEqualToOther
import com.ustadmobile.util.commontest.ext.mockResponseForConcatenatedFiles2Request
import com.ustadmobile.util.test.ext.baseDebugIfNotEnabled
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import okio.Buffer
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class TestContainerDownloadJob {

    class ConcatenatedResponse2Dispatcher(
            private val db: UmAppDatabase,
            val di: DI, val containerUid: Long) : Dispatcher() {

        var numTimesToFail = AtomicInteger(0)

        override fun dispatch(request: RecordedRequest): MockResponse {
            return if (request.requestUrl?.toUri().toString()
                            .contains("ContainerEntryList/findByContainerWithMd5")) {

                val list = db.containerEntryDao.findByContainer(containerUid)
                        .map { it.toContainerEntryWithMd5() }
                MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(Buffer().write(safeStringify(di, ListSerializer(ContainerEntryWithMd5.serializer()), list).toByteArray()))

            } else {
                db.mockResponseForConcatenatedFiles2Request(request).apply {
                    if (numTimesToFail.getAndDecrement() > 0) {
                        socketPolicy = SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY
                    }
                }
            }
        }
    }


    private lateinit var mockWebServer: MockWebServer

    private lateinit var dispatcher: ConcatenatedResponse2Dispatcher

    private lateinit var serverDb: UmAppDatabase

    private lateinit var serverRepo: UmAppDatabase

    private lateinit var serverHttpClient: HttpClient

    private lateinit var serverOkHttpClient: OkHttpClient

    private lateinit var container: Container

    private lateinit var clientDi: DI

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    val ustadTestRule = UstadTestRule()

    @Before
    fun setup() {
        Napier.baseDebugIfNotEnabled()
        serverOkHttpClient = OkHttpClient()
        serverHttpClient = HttpClient(OkHttp) {
            install(JsonFeature)
            install(HttpTimeout)
            engine {
                preconfigured = serverOkHttpClient
            }
        }

        val serverNodeIdAndAuth = NodeIdAndAuth(Random.nextInt(), randomUuid().toString())
        serverDb = DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, "UmAppDatabase")
                .addSyncCallback(serverNodeIdAndAuth, true)
                .build()
                .clearAllTablesAndResetSync(serverNodeIdAndAuth.nodeId, true)

        serverRepo = serverDb.asRepository(repositoryConfig(Any(), "http://localhost/dummy",
                serverNodeIdAndAuth.nodeId, serverNodeIdAndAuth.auth, serverHttpClient, serverOkHttpClient))

        container = Container().apply {
            containerUid = serverRepo.containerDao.insert(this)
        }

        val epubFile = temporaryFolder.newFile()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/contentformats/epub/test.epub")
                .writeToFile(epubFile)
        val containerTmpFolder = temporaryFolder.newFolder()
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


    }

    @After
    fun shutdown() {
        mockWebServer.shutdown()
        serverHttpClient.close()
    }

    @Test
    fun givenValidRequest_whenDownloadCalled_thenShouldDownloadContainerFiles() {
        val downloadDestDir = temporaryFolder.newFolder()

        val siteUrl = mockWebServer.url("/").toString()

        val clientDb: UmAppDatabase = clientDi.on(Endpoint(siteUrl)).direct.instance(tag = DoorTag.TAG_DB)

        val mockListener = mock<ContentJobProgressListener> { }

        val job = runBlocking {
            ContentJobItemAndContentJob().apply {
                contentJob = ContentJob().apply {
                    this.toUri = downloadDestDir.toKmpUriString()
                    this.cjIsMeteredAllowed = true
                    this.cjUid = clientDb.contentJobDao.insertAsync(this)
                }
                contentJobItem = ContentJobItem().apply {
                    this.cjiContainerUid = container.containerUid
                    this.cjiJobUid = contentJob!!.cjUid
                    this.cjiItemTotal = container.fileSize
                    this.cjiUid = clientDb.contentJobItemDao.insertJobItem(this)
                }
            }
        }

        val processContext = ContentJobProcessContext(temporaryFolder.newFolder().toDoorUri(), temporaryFolder.newFolder().toDoorUri(), params = mutableMapOf(),
                clientDi)


        val downloadJob = ContainerDownloadContentJob(Any(), Endpoint(siteUrl), clientDi)
        val result = runBlocking {  downloadJob.processJob(job, processContext, mockListener) }


        Assert.assertEquals("Result is reported as successful", JobStatus.COMPLETE,
                result.status)

        serverDb.assertContainerEqualToOther(container.containerUid, clientDb)
    }

    @Test
    fun givenDownloadIsInterrupted_whenNewRequestMade_thenDownloadShouldResume() {
        dispatcher.numTimesToFail.set(1)

        val allContainerEntryFilesToDownload = serverDb.containerEntryDao.findByContainer(container.containerUid)
                .map { it.toContainerEntryWithMd5() }

        val downloadDestDir = temporaryFolder.newFolder()

        val siteUrl = mockWebServer.url("/").toString()

        val mockListener = mock<ContentJobProgressListener> { }

        val results = mutableListOf<Int>()
        val clientDb: UmAppDatabase = clientDi.on(Endpoint(siteUrl)).direct.instance(tag = DoorTag.TAG_DB)

        for(i in 0..1) {
            Napier.d("============ ATTEMPT $i ============")
            try {

                val job = runBlocking {
                    ContentJobItemAndContentJob().apply {
                        contentJob = ContentJob().apply {
                            this.toUri = downloadDestDir.toKmpUriString()
                            this.cjIsMeteredAllowed = true
                            this.cjUid = clientDb.contentJobDao.insertAsync(this)
                        }
                        contentJobItem = ContentJobItem().apply {
                            this.cjiContainerUid = container.containerUid
                            this.cjiJobUid = contentJob!!.cjUid
                            this.cjiItemTotal = container.fileSize
                            this.cjiUid = clientDb.contentJobItemDao.insertJobItem(this)
                        }
                    }
                }

                val processContext = ContentJobProcessContext(temporaryFolder.newFolder().toDoorUri(), temporaryFolder.newFolder().toDoorUri(), params = mutableMapOf(),
                        clientDi)


                val downloadJob = ContainerDownloadContentJob(Any(), Endpoint(siteUrl), clientDi)
                val result = runBlocking {  downloadJob.processJob(job, processContext, mockListener) }

                results.add(result.status)
            }catch(e: Exception) {
                e.printStackTrace()
                throw e
            }

        }

        serverDb.assertContainerEqualToOther(container.containerUid, clientDb)
        Assert.assertEquals("First result returned paused",
                JobStatus.FAILED, results[0])
        Assert.assertEquals("Second result completed",
                JobStatus.COMPLETE, results[1])

        val mockRequest1 = mockWebServer.takeRequest()
        val mockRequest2 = mockWebServer.takeRequest()
        Assert.assertNotNull("Second request included partial response request",
                mockRequest2.getHeader("range"))
    }

}