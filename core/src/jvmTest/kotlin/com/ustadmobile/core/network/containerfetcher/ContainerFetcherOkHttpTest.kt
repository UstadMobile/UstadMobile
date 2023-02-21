package com.ustadmobile.core.network.containerfetcher

import io.github.aakira.napier.Napier
import org.mockito.kotlin.mock
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZip
import com.ustadmobile.core.io.ext.toContainerEntryWithMd5
import com.ustadmobile.core.io.ext.toKmpUriString
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.linkExistingContainerEntries
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.*
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.util.commontest.ext.assertContainerEqualToOther
import com.ustadmobile.util.commontest.ext.mockResponseForConcatenatedFiles2Request
import com.ustadmobile.util.test.ext.baseDebugIfNotEnabled
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.json.*
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.rules.TemporaryFolder
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.core.util.ext.getContentEntryJsonFilesFromDir
import com.ustadmobile.core.util.ext.filterNotInDirectory
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import kotlinx.serialization.json.Json
import org.kodein.di.*


class ContainerFetcherOkHttpTest {

    class ConcatenatedResponse2Dispatcher(private val db: UmAppDatabase) : Dispatcher(){

        var numTimesToFail = AtomicInteger(0)

        override fun dispatch(request: RecordedRequest): MockResponse {
            return db.mockResponseForConcatenatedFiles2Request(request).apply {
                if(numTimesToFail.getAndDecrement() > 0) {
                    socketPolicy = SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY
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
            install(ContentNegotiation) {
                gson()
            }
            install(HttpTimeout)
            engine {
                preconfigured = serverOkHttpClient
            }
        }

        val serverNodeIdAndAuth = NodeIdAndAuth(Random.nextLong(), randomUuid().toString())
        serverDb = DatabaseBuilder.databaseBuilder(UmAppDatabase::class,
                "jdbc:sqlite:build/tmp/UmAppDatabase.sqlite")
            .addSyncCallback(serverNodeIdAndAuth)
            .build()
            .clearAllTablesAndResetNodeId(serverNodeIdAndAuth.nodeId)

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

        //Create a mock web server that will serve the concatenated data
        mockWebServer = MockWebServer()
        dispatcher = ConcatenatedResponse2Dispatcher(serverDb)
        mockWebServer.dispatcher = dispatcher
        mockWebServer.start()

        clientDi = DI {
            import(ustadTestRule.diModule)
        }
    }

    @After
    fun shutdown() {
        mockWebServer.shutdown()
        serverHttpClient.close()
    }

    @Test
    fun givenValidRequest_whenDownloadCalled_thenShouldDownloadContainerFiles() {
        val containerEntriesToDownload = serverDb.containerEntryDao.findByContainer(container.containerUid)
                .map { it.toContainerEntryWithMd5() }

        val downloadDestDir = temporaryFolder.newFolder()

        val siteUrl = mockWebServer.url("/").toString()
        val request = ContainerFetcherRequest2(containerEntriesToDownload, siteUrl, siteUrl,
            downloadDestDir.toDoorUri().toString())

        val mockListener = mock<ContainerFetcherListener2> { }
        val downloaderJob = ContainerFetcherOkHttp(request, mockListener, clientDi)

        val result = runBlocking {
            downloaderJob.download().also {
                val clientDb: UmAppDatabase = clientDi.on(Endpoint(request.siteUrl)).direct
                    .instance(tag = DoorTag.TAG_DB)
                val downloadedContainerFiles = downloadDestDir.getContentEntryJsonFilesFromDir(
                    clientDi.direct.instance())
                clientDb.withDoorTransactionAsync { txDb ->
                    txDb.containerEntryFileDao.insertListAsync(downloadedContainerFiles)
                    txDb.linkExistingContainerEntries(container.containerUid,
                        containerEntriesToDownload)
                }
            }
        }

        Assert.assertEquals("Result is reported as successful", JobStatus.COMPLETE,
            result)

        val clientDb: UmAppDatabase = clientDi.on(Endpoint(siteUrl)).direct.instance(tag = DoorTag.TAG_DB)


        serverDb.assertContainerEqualToOther(container.containerUid, clientDb)
    }

    @Test
    fun givenDownloadIsInterrupted_whenNewRequestMade_thenDownloadShouldResume() {
        dispatcher.numTimesToFail.set(1)

        val allContainerEntryFilesToDownload = serverDb.containerEntryDao.findByContainer(container.containerUid)
                .map { it.toContainerEntryWithMd5() }

        val downloadDestDir = temporaryFolder.newFolder()

        val siteUrl = mockWebServer.url("/").toString()


        val mockListener = mock<ContainerFetcherListener2> { }

        val results = mutableListOf<Int>()
        val clientDb: UmAppDatabase = clientDi.on(Endpoint(siteUrl)).direct.instance(tag = DoorTag.TAG_DB)

        for(i in 0..1) {
            Napier.d("============ ATTEMPT $i ============")
            try {
                val entriesInDb = clientDb.containerEntryFileDao.findEntriesByMd5Sums(
                        allContainerEntryFilesToDownload.mapNotNull { it.cefMd5 })
                val entriesToDownload = allContainerEntryFilesToDownload
                        .filter { entry -> ! entriesInDb.any { dbEntry -> dbEntry.cefMd5 ==  entry.cefMd5} }
                        .filterNotInDirectory(downloadDestDir)

                val request = ContainerFetcherRequest2(entriesToDownload, siteUrl, siteUrl,
                        downloadDestDir.toKmpUriString())
                val downloaderJob = ContainerFetcherOkHttp(request,
                        mockListener, clientDi)
                val result = runBlocking {
                    downloaderJob.download()
                }
                results.add(result)
            }catch(e: Exception) {
                e.printStackTrace()
            }

        }

        runBlocking {
            val fileEntriesDownloaded = downloadDestDir.getContentEntryJsonFilesFromDir(
                clientDi.direct.instance())

            clientDb.withDoorTransactionAsync { txDb ->
                txDb.containerEntryFileDao.insertListAsync(fileEntriesDownloaded)
                txDb.linkExistingContainerEntries(container.containerUid,
                    allContainerEntryFilesToDownload)
            }

        }

        serverDb.assertContainerEqualToOther(container.containerUid, clientDb)
        Assert.assertEquals("Result is completed", JobStatus.COMPLETE, results[0])

        val mockRequest1 = mockWebServer.takeRequest()
        val mockRequest2 = mockWebServer.takeRequest()
        Assert.assertNotNull("Second request included partial response request",
                mockRequest2.getHeader("range"))
    }


    //TODO
    fun givenValidRequest_whenServerProvidesCorruptData_thenShouldFail() {

    }




}