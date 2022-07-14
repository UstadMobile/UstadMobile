@file:Suppress("unused")

package com.ustadmobile.lib.rest

import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.ext.addSyncCallback
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.RepositoryConfig.Companion.repositoryConfig
import com.ustadmobile.door.ext.asRepository
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.clearAllTablesAndResetNodeId
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.Container
import io.ktor.server.application.install
import io.ktor.client.HttpClient
import io.ktor.serialization.gson.GsonConverter
import io.ktor.serialization.gson.gson
import io.ktor.http.ContentType
import io.ktor.server.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.File
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.statement.HttpStatement
import io.ktor.http.HttpHeaders
import io.netty.handler.codec.http.HttpResponseStatus
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert
import kotlin.random.Random

/**
 * This test is BROKEN 16/Dec/2020
 */
class TestContainerMountRoute {

    lateinit var server: ApplicationEngine

    lateinit var db: UmAppDatabase

    lateinit var repo: UmAppDatabase

    private lateinit var nodeIdAndAuth: NodeIdAndAuth

    lateinit var container: Container

    lateinit var epubTmpFile: File

    lateinit var containerTmpDir: File

    private val defaultPort = 8098

    //private lateinit var containerManager: ContainerManager

    private var testPath: String = ""

    private lateinit var httpClient: HttpClient

    private lateinit var okHttpClient: OkHttpClient

//    //@Before
//    fun setup() {
//        okHttpClient = OkHttpClient()
//        httpClient = HttpClient(OkHttp){
//            install(JsonFeature)
//            engine {
//                preconfigured = okHttpClient
//            }
//        }
//        nodeIdAndAuth = NodeIdAndAuth(Random.nextLong(0, Long.MAX_VALUE),
//            randomUuid().toString())
//
//        db = DatabaseBuilder.databaseBuilder(Any(), UmAppDatabase::class, "UmAppDatabase")
//            .addSyncCallback(nodeIdAndAuth)
//            .build()
//            .clearAllTablesAndResetNodeId(nodeIdAndAuth.nodeId)
//
//        repo = db.asRepository(repositoryConfig(Any(), "http://localhost/dummy",
//            nodeIdAndAuth.nodeId, nodeIdAndAuth.auth, httpClient, okHttpClient))
//        server = embeddedServer(Netty, port = defaultPort) {
//            install(ContentNegotiation) {
//                gson {
//                    register(ContentType.Application.Json, GsonConverter())
//                    register(ContentType.Any, GsonConverter())
//                }
//            }
//
//            install(Routing) {
//                ContainerMountRoute()
//            }
//        }.start(wait = false)
//
//        containerTmpDir = UmFileUtilSe.makeTempDir("testcontainermountroute", "tmpdir")
//        container = Container()
//        container.containerUid = repo.containerDao.insert(container)
//
//        runBlocking {
//            repo.addEntriesToContainerFromZipResource(container.containerUid, this::class.java,
//                    "/testfiles/thelittlechicks.epub",
//                    ContainerAddOptions(storageDirUri = containerTmpDir.toDoorUri()))
//        }
//
//        testPath = db.containerEntryDao.findByContainer(container.containerUid)[13].cePath!!
//    }
//
//    //@After
//    fun tearDown() {
//        server.stop(0, 7000)
//        httpClient.close()
//    }
//
//    //@Test
//    fun givenMountRequest_whenNoContainerExists_shouldRespondWithNotFound() {
//        runBlocking {
//            val httpClient = HttpClient {
//                install(JsonFeature)
//            }
//
//            httpClient.use {
//                val mountResponse = httpClient.get<HttpStatement>(
//                        "http://localhost:${defaultPort}/ContainerMount/${container.containerUid + 1}/epub.css").execute()
//                Assert.assertEquals("Container to be mounted was not found",HttpResponseStatus.NOT_FOUND.code(), mountResponse.status.value)
//            }
//        }
//    }
//
//    //@Test
//    fun givenMountRequest_whenContainerExistsAndFileExists_shouldMountAndServeTheFile() {
//        runBlocking {
//            val httpClient = HttpClient {
//                install(JsonFeature)
//            }
//
//            httpClient.use {
//                val mountResponse = httpClient.get<HttpStatement>(
//                        "http://localhost:${defaultPort}/ContainerMount/${container.containerUid}/${testPath}").execute()
//                Assert.assertEquals("Container was mounted and requested file found",HttpResponseStatus.OK.code(), mountResponse.status.value)
//            }
//        }
//    }
//
//    //@Test
//    fun givenMountRequest_whenHeadRequestedOnExistingFile_shouldMountAndServeRequiredDetails() {
//        runBlocking {
//            val httpClient = HttpClient {
//                install(JsonFeature)
//            }
//
//            httpClient.use {
//                val mountResponse = httpClient.head<HttpStatement>(
//                        "http://localhost:${defaultPort}/ContainerMount/${container.containerUid}/${testPath}").execute()
//                Assert.assertTrue("Container mounted and responded with content length",
//                        200 == mountResponse.status.value && mountResponse.headers[HttpHeaders.ContentLength]!!.toInt() > 0)
//            }
//        }
//    }


}

