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
import io.ktor.server.testing.*
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
import io.ktor.application.install
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import com.ustadmobile.core.io.ContainerManifest
import com.ustadmobile.lib.db.entities.ContainerEntryWithChecksums
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.*

/**
 * Needs updated to include the Download itself. This is mostly just an adapter for
 * generateConcatenatedResponse2 which is thoroughly tested.
 */
class ContainerDownloadRouteTest {

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var nodeIdAndAuth: NodeIdAndAuth

    private lateinit var container: Container

    private lateinit var containerTmpDir: File

    private lateinit var json: Json

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

        json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        nodeIdAndAuth = NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE), randomUuid().toString())

        db = DatabaseBuilder.databaseBuilder(Any() ,UmAppDatabase::class, "UmAppDatabase")
            .build()
        db.clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
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

        containerTmpDir = temporaryFolder.newFolder("dlroutetestcontainerfiles")
        container = Container()
        container.containerUid = repo.containerDao.insert(container)

        runBlocking {
            repo.addEntriesToContainerFromZipResource(container.containerUid, repo::class.java,
                    "/testfiles/thelittlechicks.epub",
                    ContainerAddOptions(containerTmpDir.toDoorUri()))
        }
    }



    private fun <R> withTestContainerRoute(testFn: TestApplicationEngine.() -> R) {
        withTestApplication({
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
        }) {
            testFn()
        }
    }


    @After
    fun tearDown() {
        httpClient.close()
    }

    @Test
    fun givenContainer_WhenEntryListRequestIsMade_shouldGiveListWIthMd5s() {
        withTestContainerRoute {
            handleRequest(HttpMethod.Get, uri = "/ContainerEntryList/findByContainerWithMd5?containerUid=${container.containerUid}") {

            }.apply {
                val containerEntryList = json.decodeFromString(ListSerializer(ContainerEntryWithMd5.serializer()),
                    this.response.content!!)
                val containerEntries = db.containerEntryDao.findByContainerWithMd5(container.containerUid)
                containerEntries.forEach { dbEntry ->
                    Assert.assertTrue("Entry was in response", containerEntryList.any {
                        it.ceUid == dbEntry.ceUid && it.cefMd5 != null && it.cefMd5 == dbEntry.cefMd5})
                }
            }
        }
    }


    @Test
    fun givenContainer_whenContainerManifestRequestIsMade_shouldProvideContainerManifestWithMd5s() {
        withTestContainerRoute {
            handleRequest(HttpMethod.Get, uri = "/ContainerManifest/${container.containerUid}") {

            }.apply {
                val containerEntryList = db.containerEntryDao.findByContainerWithChecksums(
                    container.containerUid)
                val manifestParsed = ContainerManifest.parseFromString(response.content!!)
                Assert.assertEquals("Count of entries matches", containerEntryList.size,
                    manifestParsed.entries.size)
                Assert.assertEquals("Manifest containerUid matches", container.containerUid,
                    manifestParsed.containerUid)
                containerEntryList.forEach { dbEntry ->
                    Assert.assertTrue("Found entry in manifest", manifestParsed.entries.any {
                        it.integrity == dbEntry.cefIntegrity &&
                        it.originalMd5 == dbEntry.cefMd5 &&
                        it.pathInContainer == dbEntry.cePath &&
                        it.size == dbEntry.ceCompressedSize
                    })
                }
            }
        }
    }

    @Test
    fun givenContainer_whenContainerEntriesRequested_shouldRespondWithMatchingData() {
        withTestContainerRoute {
            val shaMessageDigest = MessageDigest.getInstance("SHA-256")
            val containerEntryList: List<ContainerEntryWithChecksums> = db.containerEntryDao.findByContainerWithChecksums(
                container.containerUid)
            containerEntryList.forEach { entry ->
                handleRequest(HttpMethod.Get,
                    "/ContainerFileMd5/${URLEncoder.encode(entry.cefMd5, "UTF-8")}"
                ) {

                }.apply {
                    val digest = shaMessageDigest.digest(response.byteContent!!)
                    val expectedDigest = Base64.getDecoder().decode(
                        entry.cefIntegrity!!.substringAfter("-"))
                    Assert.assertArrayEquals("Message digest for MD5 = ${entry.cefMd5} matches",
                        expectedDigest, digest)
                }
            }
        }
    }

}

