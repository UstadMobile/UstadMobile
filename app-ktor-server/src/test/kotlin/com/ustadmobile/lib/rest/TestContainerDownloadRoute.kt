package com.ustadmobile.lib.rest

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import io.ktor.server.application.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.serialization.gson.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.kodein.di.bind
import org.kodein.di.ktor.di
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

        nodeIdAndAuth = NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())

        db = DatabaseBuilder.databaseBuilder(UmAppDatabase::class, "jdbc:sqlite:build/tmp/UmAppDatabase.sqlite")
            .build()
        db.clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
        val attachmentsDir = temporaryFolder.newFolder()

        okHttpClient = OkHttpClient()

        httpClient = HttpClient(OkHttp) {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                gson()
            }
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

            di {
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
                install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation){
                    gson()
                }
            }

            httpClient.use {
                val containerEntryList: List<ContainerEntryWithMd5> = httpClient.get(
                        "http://localhost:8097/ContainerEntryList/findByContainerWithMd5?containerUid=${container.containerUid}"
                ).body()
                val containerEntries = db.containerEntryDao.findByContainerWithMd5(container.containerUid)
                containerEntries.forEach { dbEntry ->
                    Assert.assertTrue("Entry was in response", containerEntryList.any {
                        it.ceUid == dbEntry.ceUid && it.cefMd5 != null && it.cefMd5 == dbEntry.cefMd5})
                }
            }
        }
    }

}

