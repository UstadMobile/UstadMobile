package com.ustadmobile.core.torrent

import com.turn.ttorrent.tracker.Tracker
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.catalog.contenttype.EpubTypePluginCommonJvm
import com.ustadmobile.core.catalog.contenttype.H5PTypePluginCommonJvm
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.core.io.ext.addEntryToContainerFromResource
import com.ustadmobile.core.io.ext.addTorrentFileFromContainer
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.*
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.clearAllTablesAndResetSync
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import com.ustadmobile.lib.rest.ContainerDownload
import com.ustadmobile.lib.rest.TorrentFileRoute
import com.ustadmobile.lib.rest.TorrentTracker
import com.ustadmobile.util.commontest.ext.assertContainerEqualToOther
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import org.kodein.di.ktor.DIFeature
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.net.InetAddress
import java.net.URL
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.assertTrue


class TestUstadTorrentManager {


    //private lateinit var serverContainer: Container
    private lateinit var serverTrackerUrl: URL
    private lateinit var clientContainerFolder: File
    private lateinit var clientTorrentFolder: File

    private lateinit var serverContainerFolder: File
    private lateinit var serverTorrentFolder: File

    private lateinit var serverTorrentManager: UstadTorrentManager
    private lateinit var serverTorrentTracker: TorrentTracker

    private lateinit var server: ApplicationEngine

    private lateinit var clientDi: DI

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()


    private lateinit var clientDb: UmAppDatabase


    private lateinit var clientRepo: UmAppDatabase

    private lateinit var containerDownloadJob: ContainerTorrentDownloadJob

    private lateinit var epubPlugin: EpubTypePluginCommonJvm

    private lateinit var serverDb: UmAppDatabase

    private lateinit var serverRepo: UmAppDatabase

    private var fileToDownloadPath = "/com/ustadmobile/core/contentformats/english.h5p"

    @Before
    fun setup() {

        val okHttpClient = OkHttpClient()

        val httpClient = HttpClient(OkHttp) {
            install(JsonFeature)
            install(HttpTimeout)
            engine {
                preconfigured = okHttpClient
            }
        }

        val serverFolder = temporaryFolder.newFolder("serverFolder")

        val nodeIdAndAuth = NodeIdAndAuth(Random.nextInt(), randomUuid().toString())
        serverDb = DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, "UmAppDatabase")
                .addSyncCallback(nodeIdAndAuth, true)
                .build()
                .clearAllTablesAndResetSync(nodeIdAndAuth.nodeId, true)
        serverRepo = serverDb.asRepository(RepositoryConfig.repositoryConfig(Any(), "http://localhost/",
                nodeIdAndAuth.nodeId, nodeIdAndAuth.auth, httpClient, okHttpClient))
        val endpointScope = EndpointScope()

        serverTorrentFolder = File(serverFolder, "torrent")
        serverTorrentFolder.mkdirs()
        serverContainerFolder = File(serverFolder,"container")
        serverContainerFolder.mkdirs()

        val contentJobManager: ContentJobManager = mock {
        }

        serverTrackerUrl = URL("http://127.0.0.1:6677/announce")

        server = embeddedServer(Netty, 7711) {
             install(DIFeature) {
                registerContextTranslator { call: ApplicationCall ->
                    Endpoint("localhost")
                }
                bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(endpointScope).singleton {
                    serverDb
                }

                bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(endpointScope).singleton {
                    serverRepo

                }
                bind<File>(tag = DiTag.TAG_TORRENT_DIR) with scoped(endpointScope).singleton {
                    serverTorrentFolder
                }
                bind<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR) with scoped(endpointScope).singleton {
                    serverContainerFolder
                }

                bind<UstadTorrentManager>() with scoped(endpointScope).singleton {
                    serverTorrentManager = UstadTorrentManagerImpl(endpoint = context, di = di)
                    serverTorrentManager
                }
                bind<UstadCommunicationManager>() with singleton {
                    UstadCommunicationManager()
                }
                bind<Tracker>() with singleton {
                    Tracker(serverTrackerUrl.port, serverTrackerUrl.toString())
                }
                bind<TorrentTracker>() with scoped(EndpointScope.Default).singleton {
                    TorrentTracker(endpoint = context, di)
                }
                 bind<ContainerTorrentDownloadJob>() with scoped(endpointScope).singleton {
                     ContainerTorrentDownloadJob(Any(),context, di)
                 }

                 bind<ContentJobManager>() with singleton {
                     contentJobManager
                 }

                 bind<HttpClient>() with singleton {
                     httpClient
                 }

                onReady {
                    val tracker = instance<Tracker>()
                    //needed to announce urls
                    tracker.setAcceptForeignTorrents(true)
                    tracker.start(true)
                    instance<UstadCommunicationManager>().start(InetAddress.getByName(serverTrackerUrl.host))
                    GlobalScope.launch {
                        serverTorrentTracker = di.on(Endpoint("localhost")).direct.instance()
                        serverTorrentTracker.start()

                        serverTorrentManager = di.on(Endpoint("localhost")).direct.instance()
                        serverTorrentManager.start()

                        val containerTorrentDownloadJob: ContainerTorrentDownloadJob = di.on(Endpoint("localhost")).direct.instance()

                        whenever(contentJobManager.enqueueContentJob(any(), any())).then {

                            runBlocking {
                                val jobId = it.arguments[1] as Long

                                val jobItem =  serverDb.contentJobItemDao.findByJobId(jobId)
                                val job = serverDb.contentJobDao.findByUid(jobId)
                                val jobAndItem = ContentJobItemAndContentJob().apply {
                                    this.contentJob = job
                                    this.contentJobItem = jobItem
                                }

                                containerTorrentDownloadJob.processJob(jobAndItem, ProcessContext(temporaryFolder.newFolder().toDoorUri(), mutableMapOf())){

                                        }
                            }

                        }

                    }

                }
            }

            install(ContentNegotiation) {
                gson {
                    register(ContentType.Application.Json, GsonConverter())
                    register(ContentType.Any, GsonConverter())
                }
            }
            routing {
                UmAppDatabase_KtorRoute(true)
                ContainerDownload()
                TorrentFileRoute()
            }
        }
        server.start()

        clientDi = DI {
            import(ustadTestRule.diModule)
            bind<ContainerTorrentDownloadJob>() with scoped(ustadTestRule.endpointScope).singleton {
                ContainerTorrentDownloadJob(endpoint = context, di = di)
            }
            bind<EpubTypePluginCommonJvm>() with scoped(ustadTestRule.endpointScope).singleton {
                EpubTypePluginCommonJvm(Any(), endpoint = context, di = di)
            }
            bind<UstadTorrentManager>() with scoped(endpointScope).singleton {
                UstadTorrentManagerImpl(endpoint = context, di = di)
            }
            bind<UstadCommunicationManager>() with singleton {
                UstadCommunicationManager()
            }
            onReady {
                instance<UstadCommunicationManager>().start(InetAddress.getByName("0.0.0.0"))
            }
        }

        val accountManager: UstadAccountManager by clientDi.instance()
        accountManager.activeEndpoint = Endpoint("http://localhost:7711/")

        clientDb = clientDi.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        clientRepo = clientDi.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_REPO)
        containerDownloadJob = clientDi.on(accountManager.activeEndpoint).direct.instance()
        clientTorrentFolder = clientDi.on(accountManager.activeEndpoint).direct.instance(tag = DiTag.TAG_TORRENT_DIR)
        clientContainerFolder = clientDi.on(accountManager.activeEndpoint).direct.instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)
        epubPlugin = clientDi.on(accountManager.activeEndpoint).direct.instance()

    }

    fun createContainer(repo: UmAppDatabase, containerFolder: File): Container {
        val container = Container().apply {
            containerUid = repo.containerDao.insert(this)
        }
        runBlocking {
            repo.addEntriesToContainerFromZipResource(container.containerUid, this::class.java,
                    fileToDownloadPath,
                    ContainerAddOptions(containerFolder.toDoorUri()))
        }
        return container
    }


    @Test
    fun givenNoFilesOnClientThenContainerDownloadJobsDownloadsEverything(){

        runBlocking {
                val serverContainer = createContainer(serverRepo, serverContainerFolder)
                serverRepo.addTorrentFileFromContainer(
                        serverContainer.containerUid,
                        serverTorrentFolder.toDoorUri(), serverTrackerUrl.toString(), serverContainerFolder.toDoorUri()
                )

            serverTorrentManager.addTorrent(serverContainer.containerUid, null)
            serverTorrentTracker.addTorrentFile(File(serverTorrentFolder, "${serverContainer.containerUid}.torrent"))

            containerDownloadJob.processJob(ContentJobItemAndContentJob().apply {
                contentJobItem = ContentJobItem(cjiContainerUid = serverContainer.containerUid)
            }, ProcessContext(DoorUri.parse(""), params = mutableMapOf())){

            }
            clientDb.assertContainerEqualToOther(serverContainer.containerUid, serverDb)

        }

    }

    @Test
    fun givenSomeFilesAlreadyExistInAnotherContainerthenContainerDownloadDownloadsPartially(){

        val clientContainer = Container().apply {
            containerUid = clientRepo.containerDao.insert(this)
        }
        runBlocking {

            val serverContainer = createContainer(serverRepo, serverContainerFolder)
            serverRepo.addTorrentFileFromContainer(
                    serverContainer.containerUid,
                    serverTorrentFolder.toDoorUri(), serverTrackerUrl.toString(), serverContainerFolder.toDoorUri()
            )
            serverTorrentManager.addTorrent(serverContainer.containerUid, null)
            serverTorrentTracker.addTorrentFile(File(serverTorrentFolder, "${serverContainer.containerUid}.torrent"))


            clientRepo.addEntryToContainerFromResource(clientContainer.containerUid,
                    this::class.java, "/com/ustadmobile/core/contentformats/epub/image_1.jpg",
                    "image1", clientDi,
                    ContainerAddOptions(clientContainerFolder.toDoorUri()))
            clientRepo.addEntryToContainerFromResource(clientContainer.containerUid,
                    this::class.java, "/com/ustadmobile/core/contentformats/epub/image_2.jpg",
                    "image2", clientDi,
                    ContainerAddOptions(clientContainerFolder.toDoorUri()))
            clientRepo.addEntryToContainerFromResource(clientContainer.containerUid,
                    this::class.java, "/com/ustadmobile/core/contentformats/epub/image_3.jpg",
                    "image3", clientDi,
                    ContainerAddOptions(clientContainerFolder.toDoorUri()))

            containerDownloadJob.processJob(ContentJobItemAndContentJob().apply {
                contentJobItem = ContentJobItem(cjiContainerUid = serverContainer.containerUid)
            },
                    ProcessContext(DoorUri.parse(""), params = mutableMapOf())){

            }

            var downloadComplete = false
            val containerFiles = File(clientContainerFolder, serverContainer.containerUid.toString())
            val fileList = containerFiles.listFiles()
            fileList?.forEach {

                if(it.name.endsWith(".part")){
                    return@forEach
                }
                downloadComplete = true
            }
            assertTrue(downloadComplete)
            clientDb.assertContainerEqualToOther(serverContainer.containerUid, serverDb)
        }
    }

    @AfterTest
    fun after(){
        server.stop(10, 10)
    }

}