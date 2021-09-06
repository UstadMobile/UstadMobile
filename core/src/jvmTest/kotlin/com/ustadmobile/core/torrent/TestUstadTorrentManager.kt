package com.ustadmobile.core.torrent

import com.google.gson.Gson
import com.turn.ttorrent.tracker.Tracker
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase_KtorRoute
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.core.io.ext.addEntryToContainerFromResource
import com.ustadmobile.core.io.ext.addTorrentFileFromContainer
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.RepositoryConfig
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.clearAllTablesAndResetSync
import com.ustadmobile.door.ext.toDoorUri
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
import java.io.File
import java.net.InetAddress
import java.net.URL
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.assertTrue


class TestUstadTorrentManager {


    private lateinit var containerClientFolder: File
    private lateinit var torrentClientFolder: File
    private lateinit var server: ApplicationEngine

    private lateinit var serverContainer: Container
    private lateinit var localDi: DI

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()


    private lateinit var db: UmAppDatabase


    private lateinit var repo: UmAppDatabase

    private lateinit var seedManager: UstadTorrentManager

    private lateinit var containerDownloadJob: ContainerTorrentDownloadJob

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

        val torrentServerFolder = File(serverFolder, "torrent")
        torrentServerFolder.mkdirs()
        val containerServerFolder = File(serverFolder,"container")
        containerServerFolder.mkdirs()

        serverContainer = Container().apply {
            containerUid = serverRepo.containerDao.insert(this)
        }
        runBlocking {
            serverRepo.addEntriesToContainerFromZipResource(serverContainer.containerUid, this::class.java,
                    fileToDownloadPath,
                    ContainerAddOptions(containerServerFolder.toDoorUri()))
        }

        val trackerUrl = URL("http://127.0.0.1:8000/announce")

        runBlocking {
            serverRepo.addTorrentFileFromContainer(
                    serverContainer.containerUid,
                    torrentServerFolder.toDoorUri(), trackerUrl.toString(), containerServerFolder.toDoorUri()
            )
        }

        server = embeddedServer(Netty, 8089) {
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

                bind<Gson>() with singleton {
                    Gson()
                }
                bind<File>(tag = DiTag.TAG_TORRENT_DIR) with scoped(endpointScope).singleton {
                    torrentServerFolder
                }
                bind<File>(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR) with scoped(endpointScope).singleton {
                    containerServerFolder
                }

                bind<UstadTorrentManager>() with scoped(endpointScope).singleton {
                    UstadTorrentManagerImpl(endpoint = context, di = di)
                }
                bind<UstadCommunicationManager>() with singleton {
                    UstadCommunicationManager()
                }
                bind<Tracker>() with singleton {
                    Tracker(trackerUrl.port, trackerUrl.toString())
                }
                bind<TorrentTracker>() with scoped(EndpointScope.Default).singleton {
                    TorrentTracker(endpoint = context, di)
                }
                onReady {
                    val tracker = instance<Tracker>()
                    //needed to announce urls
                    tracker.setAcceptForeignTorrents(true)
                    tracker.start(true)
                    instance<UstadCommunicationManager>().start(InetAddress.getByName(trackerUrl.host))
                    GlobalScope.launch {
                        val torrentTracker: TorrentTracker = di.on(Endpoint("localhost")).direct.instance()
                        torrentTracker.start()

                        val ustadTorrentManager: UstadTorrentManager = di.on(Endpoint("localhost")).direct.instance()
                        ustadTorrentManager.start()

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

        localDi = DI {
            import(ustadTestRule.diModule)
            bind<ContainerTorrentDownloadJob>() with scoped(ustadTestRule.endpointScope).singleton {
                ContainerTorrentDownloadJob(endpoint = context, di = di)
            }
            val trackerUrl = URL("http://127.0.0.1:8000/announce")
            bind<UstadTorrentManager>() with scoped(endpointScope).singleton {
                UstadTorrentManagerImpl(endpoint = context, di = di)
            }
            bind<UstadCommunicationManager>() with singleton {
                UstadCommunicationManager()
            }
            onReady {
                instance<UstadCommunicationManager>().start(InetAddress.getByName(trackerUrl.host))
                GlobalScope.launch {
                    val ustadTorrentManager: UstadTorrentManager = di.on(Endpoint("localhost")).direct.instance()
                    ustadTorrentManager.start()

                }
            }
        }

        val accountManager: UstadAccountManager by localDi.instance()
        accountManager.activeEndpoint = Endpoint("http://localhost:8089/")

        db = localDi.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        repo = localDi.on(accountManager.activeEndpoint).direct.instance(tag = DoorTag.TAG_REPO)
        seedManager = localDi.on(accountManager.activeEndpoint).direct.instance()
        containerDownloadJob = localDi.on(accountManager.activeEndpoint).direct.instance()
        torrentClientFolder = localDi.on(accountManager.activeEndpoint).direct.instance(tag = DiTag.TAG_TORRENT_DIR)
        containerClientFolder = localDi.on(accountManager.activeEndpoint).direct.instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

        GlobalScope.launch {
            seedManager.start()
        }

    }


    @Test
    fun givenNoFilesOnClientThenContainerDownloadJobsDownloadsEverything(){

        runBlocking {

            containerDownloadJob.processJob(ContentJobItemAndContentJob().apply {
                contentJobItem = ContentJobItem(cjiContainerUid = serverContainer.containerUid)
            }, ProcessContext(DoorUri.parse(""), params = mutableMapOf())){

            }


                var downloadComplete = false
                val containerFiles = File(containerClientFolder, serverContainer.containerUid.toString())
                val fileList = containerFiles.listFiles()
                fileList?.forEach {

                    if(it.name.endsWith(".part")){
                        return@forEach
                    }
                    downloadComplete = true
                }
                assertTrue(downloadComplete)
                db.assertContainerEqualToOther(serverContainer.containerUid, serverDb)
        }

    }

    @Test
    fun givenSomeFilesAlreadyExistInAnotherContainerthenContainerDownloadDownloadsPartially(){

        val clientContainer = Container().apply {
            containerUid = repo.containerDao.insert(this)
        }
        runBlocking {
            repo.addEntryToContainerFromResource(clientContainer.containerUid,
                    this::class.java, "/com/ustadmobile/core/contentformats/epub/image_1.jpg",
                    "image1", localDi,
                    ContainerAddOptions(containerClientFolder.toDoorUri()))
            repo.addEntryToContainerFromResource(clientContainer.containerUid,
                    this::class.java, "/com/ustadmobile/core/contentformats/epub/image_2.jpg",
                    "image2", localDi,
                    ContainerAddOptions(containerClientFolder.toDoorUri()))
            repo.addEntryToContainerFromResource(clientContainer.containerUid,
                    this::class.java, "/com/ustadmobile/core/contentformats/epub/image_3.jpg",
                    "image3", localDi,
                    ContainerAddOptions(containerClientFolder.toDoorUri()))

            containerDownloadJob.processJob(ContentJobItemAndContentJob().apply {
                contentJobItem = ContentJobItem(cjiContainerUid = serverContainer.containerUid)
            },
                    ProcessContext(DoorUri.parse(""), params = mutableMapOf())){

            }

            var downloadComplete = false
            val containerFiles = File(containerClientFolder, serverContainer.containerUid.toString())
            val fileList = containerFiles.listFiles()
            fileList?.forEach {

                if(it.name.endsWith(".part")){
                    return@forEach
                }
                downloadComplete = true
            }
            assertTrue(downloadComplete)
            db.assertContainerEqualToOther(serverContainer.containerUid, serverDb)
        }
    }

    @AfterTest
    fun after(){
        server.stop(10, 10)
        runBlocking {
            seedManager.stop()
        }
    }

}