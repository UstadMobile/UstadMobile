package com.ustadmobile.lib.rest

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.DoorSyncableDatabaseCallback2
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.clearAllTablesAndResetSync
import com.ustadmobile.door.ext.syncableTableIdMap
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.kodein.di.bind
import org.kodein.di.ktor.DIFeature
import org.kodein.di.registerContextTranslator
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.io.File
import kotlin.random.Random

/**
 * Needs updated to include the Download itself. This is mostly just an adapter for
 * generateConcatenatedResponse2 which is thoroughly tested.
 */
class TestContainerDownloadRoute {

    private lateinit var server: ApplicationEngine

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var nodeIdAndAuth: NodeIdAndAuth

    private lateinit var container: Container

    private lateinit var containerTmpDir: File

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var httpClient: HttpClient

    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setup() {
        //make the data path if needed (UmRestApplication does this in di onReady)
        val dataDir = File("data")
        val singletonDataDir = File(dataDir, "singleton")

        singletonDataDir.takeIf { !it.exists() }?.mkdirs()

        nodeIdAndAuth = NodeIdAndAuth(Random.nextInt(0, Int.MAX_VALUE), randomUuid().toString())

        db = DatabaseBuilder.databaseBuilder(Any() ,UmAppDatabase::class, "UmAppDatabase")
            .addCallback(DoorSyncableDatabaseCallback2(nodeIdAndAuth.nodeId,
                UmAppDatabase::class.syncableTableIdMap, primary = true))
            .build()
        db.clearAllTablesAndResetSync(nodeIdAndAuth.nodeId, true)
        val attachmentsDir = temporaryFolder.newFolder()

        okHttpClient = OkHttpClient()

        httpClient = HttpClient(OkHttp) {
            install(JsonFeature)
            install(HttpTimeout)
            engine {
                preconfigured = okHttpClient
            }
        }

        repo = db.asRepository(repositoryConfig(Any(), "http://localhost/",
            nodeIdAndAuth.nodeId, nodeIdAndAuth.auth, httpClient, okHttpClient
        ) {
            this.attachmentsDir = attachmentsDir.absolutePath
        })

        server = embeddedServer(Netty, port = 8097) {
            install(ContentNegotiation) {
                gson {
                    register(ContentType.Application.Json, GsonConverter())
                    register(ContentType.Any, GsonConverter())
                }
            }

            install(DIFeature) {
                bind<UmAppDatabase>(tag = DoorTag.TAG_DB) with scoped(EndpointScope.Default).singleton {
                    db
                }

                bind<UmAppDatabase>(tag = DoorTag.TAG_REPO) with scoped(EndpointScope.Default).singleton {
                    repo
                }

                registerContextTranslator { _: ApplicationCall ->
                    Endpoint("localhost")
                }
            }

            install(Routing) {
                ContainerDownload()
            }
        }.start(wait = false)

        containerTmpDir = temporaryFolder.newFolder("dlroutetestcontainerfiles")
        container = Container()
        container.containerUid = repo.containerDao.insert(container)

        runBlocking {
            repo.addEntriesToContainerFromZipResource(container.containerUid, repo::class.java,
                    "/testfiles/thelittlechicks.epub",
                    ContainerAddOptions(containerTmpDir.toDoorUri()))
        }
    }

    @After
    fun tearDown() {
        server.stop(0, 5000)
        httpClient.close()
    }

    @Test
    fun givenContainer_WhenEntryListRequestIsMade_shouldGiveListWIthMd5s() {
        runBlocking {
            val httpClient = HttpClient {
                install(JsonFeature)
            }

            httpClient.use {
                val containerEntryList = httpClient.get<List<ContainerEntryWithMd5>>(
                        "http://localhost:8097/ContainerEntryList/findByContainerWithMd5?containerUid=${container.containerUid}")
                val containerEntries = db.containerEntryDao.findByContainerWithMd5(container.containerUid)
                containerEntries.forEach { dbEntry ->
                    Assert.assertTrue("Entry was in response", containerEntryList.any {
                        it.ceUid == dbEntry.ceUid && it.cefMd5 != null && it.cefMd5 == dbEntry.cefMd5})
                }
            }
        }
    }

}

