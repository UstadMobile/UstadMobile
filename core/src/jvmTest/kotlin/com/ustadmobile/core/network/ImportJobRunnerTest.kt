package com.ustadmobile.core.network

import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.catalog.contenttype.H5PTypePluginCommonJvm
import com.ustadmobile.core.catalog.contenttype.XapiTypePluginCommonJvm
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentformats.ContentImportManager
import com.ustadmobile.core.contentformats.ContentImportManagerImpl
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.UploadSessionManager
import com.ustadmobile.core.io.UploadSessionParams
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.core.networkmanager.ContainerUploadManager
import com.ustadmobile.core.networkmanager.ImportJobRunner
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.distinctMds5sSorted
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.ext.DoorTag.Companion.TAG_DB
import com.ustadmobile.door.ext.DoorTag.Companion.TAG_REPO
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.ContainerImportJob
import com.ustadmobile.lib.rest.ContainerUploadRoute2
import com.ustadmobile.lib.rest.TAG_UPLOAD_DIR
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.sharedse.io.extractResourceToFile
import com.ustadmobile.sharedse.network.containeruploader.ContainerUploadManagerCommonJvm
import com.ustadmobile.util.commontest.ext.assertContainerEqualToOther
import org.kodein.di.ktor.DIFeature
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.File
import java.util.*

class ImportJobRunnerTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var contentImportManager: ContentImportManager

    private lateinit var endpoint: String

    private lateinit var serverRepo: UmAppDatabase

    private lateinit var uploadServerFolder: File
    private lateinit var serverDb: UmAppDatabase
    lateinit var server: ApplicationEngine

    private val defaultPort = 8098

    private lateinit var appDb: UmAppDatabase
    private lateinit var clientFolder: File
    private lateinit var fileToUpload: File
    private lateinit var serverContainerFolder: File

    private lateinit var di: DI

    private lateinit var containerImportJob: ContainerImportJob

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var metadata: ImportedContentEntryMetaData

    @Before
    fun setup() {
        serverDb = DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, "UmAppDatabase").build()
        serverDb.clearAllTables()
        serverRepo = spy(serverDb.asRepository(Any(), "http://localhost/dummy",
                "", defaultHttpClient(), null))

        val endpointScope = EndpointScope()

        clientFolder = temporaryFolder.newFolder("upload")
        uploadServerFolder = temporaryFolder.newFolder("server")
        di = DI {
            import(ustadTestRule.diModule)
            bind<ContainerUploadManager>() with scoped(endpointScope).singleton {
                ContainerUploadManagerCommonJvm(di, context)
            }
            bind<ContentImportManager>() with scoped(endpointScope).singleton {
                ContentImportManagerImpl(listOf(H5PTypePluginCommonJvm(), XapiTypePluginCommonJvm()),
                        context, this.context, di)
            }
        }

        server = embeddedServer(Netty, port = defaultPort) {
            install(ContentNegotiation) {
                gson {
                    register(ContentType.Application.Json, GsonConverter())
                    register(ContentType.Any, GsonConverter())
                }
            }

            install(Routing) {
                ContainerUploadRoute2()
            }

            val serverEndpointScope = EndpointScope()
            install(DIFeature) {
                bind<UmAppDatabase>(tag = TAG_DB) with scoped(serverEndpointScope).singleton {
                    serverDb
                }
                bind<UmAppDatabase>(tag = TAG_REPO) with scoped(serverEndpointScope).singleton {
                    serverRepo
                }

                bind<UploadSessionManager>() with scoped(serverEndpointScope).singleton {
                    UploadSessionManager(context, di)
                }


                bind<File>(tag = TAG_UPLOAD_DIR) with scoped(serverEndpointScope).singleton {
                    uploadServerFolder
                }

                bind<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR) with scoped(serverEndpointScope).singleton {
                    serverContainerFolder = temporaryFolder.newFolder("servercontainerdir")
                    serverContainerFolder
                }

                bind<Gson>() with singleton {
                    Gson()
                }

                registerContextTranslator { call: ApplicationCall ->
                    Endpoint("localhost")
                }
            }

        }.start(wait = false)

    }

    private fun createImportJob(db: UmAppDatabase): ContainerImportJob {
        return runBlocking {
            ContainerImportJob().apply {
                cijBytesSoFar = 0
                this.cijFilePath = fileToUpload.absolutePath
                this.cijContentEntryUid = metadata.contentEntry.contentEntryUid
                this.cijMimeType = metadata.mimeType
                this.cijContainerBaseDir = clientFolder.absolutePath
                this.cijJobStatus = JobStatus.QUEUED
                cijUid = db.containerImportJobDao.insertAsync(this)
            }
        }
    }

    fun setupImportContentEntry(endpoint: Endpoint){

        appDb = di.on(endpoint).direct.instance(UmAppDatabase.TAG_DB)
        val status = ConnectivityStatus().apply {
            connectedOrConnecting = true
            connectivityState = ConnectivityStatus.STATE_UNMETERED
            wifiSsid = "wifi-mock"
        }
        appDb.connectivityStatusDao.insert(status)
        val  connectivityStatusLiveData = DoorMutableLiveData<ConnectivityStatus>()
        connectivityStatusLiveData.sendValue(status)
        val repo: UmAppDatabase = di.on(endpoint).direct.instance(UmAppDatabase.TAG_REPO)
        contentImportManager = di.on(endpoint).direct.instance()

        fileToUpload = File(clientFolder, "tincan.zip")

        runBlocking {
            extractResourceToFile(
                    "/com/ustadmobile/core/container/ustad-tincan.zip",
                    fileToUpload.path)
        }
        metadata = runBlocking {
            contentImportManager.extractMetadata(fileToUpload.path)!!
        }

        metadata.contentEntry.contentEntryUid = runBlocking {
            repo.contentEntryDao.insertAsync(metadata.contentEntry)
        }

        containerImportJob = createImportJob(appDb)
    }

    @After
    fun tearDown() {
        server.stop(0, 7000)
    }

    @Test
    fun givenAnUploadJob_whenRunnerUploads_thenContentFromServerIsSameAsDb() {
        endpoint = "http://localhost:$defaultPort/"
        setupImportContentEntry(Endpoint(endpoint))

        val runner = ImportJobRunner(containerImportJob, retryDelay, endpoint, di)

        runBlocking {
            runner.importContainer()
            runner.upload()
        }
        val containerUid = runBlocking {
            appDb.containerImportJobDao.findByUid(containerImportJob.cijUid)!!.cijContainerUid
        }

        appDb.assertContainerEqualToOther(containerUid, serverDb)
    }

    @Test
    fun givenRunnerStarts_whenFailExceedsMaxAttempt_thenShouldStopAndSetStatusToFail() {
        val mockWebServer = MockWebServer()
        mockWebServer.start()

        endpoint = mockWebServer.url("").toString()
        setupImportContentEntry(Endpoint(endpoint))

        val runner = ImportJobRunner(containerImportJob, retryDelay, endpoint, di)
        runBlocking {
            runner.importContainer()
        }

        // existing md5Sum response
        val requiredMd5s = appDb.containerEntryDao
                .findByContainerWithMd5(containerImportJob.cijContainerUid).distinctMds5sSorted()

        mockWebServer.enqueue(MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(Gson().toJson(UploadSessionParams(requiredMd5s, 0L))))

        // create session
        val sessionId = UUID.randomUUID().toString()
        mockWebServer.enqueue(MockResponse().setBody(sessionId))

        // fail to upload - server problem
        for (i in 0..(fileToUpload.length()) step (1024 * 8)) {
            mockWebServer.enqueue(MockResponse()
                    .setResponseCode(HttpStatusCode.InternalServerError.value)
                    .setBody("Server error"))
        }

        var status = 0
        runBlocking {
            runner.importContainer()
            status = runner.upload()
        }

        Assert.assertEquals("Runner failed", JobStatus.FAILED, status)
    }

    @Test
    fun givenRunnerStarts_whenServerHasAllMd5_thenShouldCallFinalizeWithoutSession() {

        endpoint = "http://localhost:$defaultPort/"
        setupImportContentEntry(Endpoint(endpoint))


        runBlocking {
            serverRepo.addEntriesToContainerFromZipResource(containerImportJob.cijContainerUid,
                this::class.java, "/com/ustadmobile/core/container/ustad-tincan.zip",
                ContainerAddOptions(uploadServerFolder.toDoorUri()))
        }

        val runner = ImportJobRunner(containerImportJob, retryDelay, endpoint, di)
        runBlocking {
            runner.importContainer()
            runner.upload()
        }
        val containerUid = runBlocking {
            appDb.containerImportJobDao.findByUid(containerImportJob.cijUid)!!.cijContainerUid
        }

        appDb.assertContainerEqualToOther(containerUid, serverDb)
    }

    companion object {
        const val retryDelay = 10L
    }


}