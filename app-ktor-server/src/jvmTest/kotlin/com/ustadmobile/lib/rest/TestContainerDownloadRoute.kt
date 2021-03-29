package com.ustadmobile.lib.rest

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.kodein.di.bind
import org.kodein.di.ktor.DIFeature
import org.kodein.di.registerContextTranslator
import org.kodein.di.scoped
import org.kodein.di.singleton
import java.io.File

/**
 * Needs updated to include the Download itself. This is mostly just an adapter for
 * generateConcatenatedResponse2 which is thoroughly tested.
 */
class TestContainerDownloadRoute {

    lateinit var server: ApplicationEngine

    lateinit var db: UmAppDatabase

    lateinit var repo: UmAppDatabase

    lateinit var container: Container

    lateinit var containerTmpDir: File

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Before
    fun setup() {
        //make the data path if needed (UmRestApplication does this in di onReady)
        val dataDir = File("data")
        val singletonDataDir = File(dataDir, "singleton")

        singletonDataDir.takeIf { !it.exists() }?.mkdirs()

        db = DatabaseBuilder.databaseBuilder(Any() ,UmAppDatabase::class, "UmAppDatabase").build()
        db.clearAllTables()
        val attachmentsDir = temporaryFolder.newFolder()
        repo = db.asRepository(Any(), "http://localhost/",
                "", defaultHttpClient(), attachmentsDir.absolutePath,
                null, false)
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

                registerContextTranslator { call: ApplicationCall ->
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
    }

    @Test
    fun givenContainer_WhenEntryListRequestIsMade_shouldGiveListWIthMd5s() {
        runBlocking {
            val httpClient = HttpClient(){
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

