package com.ustadmobile.lib.rest

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.core.io.ConcatenatedInputStream
import com.ustadmobile.core.io.ConcatenatedPart
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.junit.Before
import org.junit.Test
import java.io.File
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpHeaders
import io.netty.handler.codec.http.DefaultHttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.io.InputStream
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.readAvailable
import kotlinx.io.core.writeFully
import kotlinx.io.streams.asInput
import kotlinx.io.streams.asOutput
import org.junit.After
import org.junit.Assert
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class TestContainerMountRoute {

    lateinit var server: ApplicationEngine

    lateinit var db: UmAppDatabase

    lateinit var container: Container

    lateinit var epubTmpFile: File

    lateinit var containerTmpDir: File

    private val defaultPort = 8098

    private lateinit var containerManager: ContainerManager

    private var testPath: String = ""

    @Before
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(Any() ,UmAppDatabase::class, "UmAppDatabase").build()
        db.clearAllTables()
        server = embeddedServer(Netty, port = defaultPort) {
            install(ContentNegotiation) {
                gson {
                    register(ContentType.Application.Json, GsonConverter())
                    register(ContentType.Any, GsonConverter())
                }
            }

            install(Routing) {
                ContainerMountRoute(db)
            }
        }.start(wait = false)

        containerTmpDir = UmFileUtilSe.makeTempDir("testcontainermountroute", "tmpdir")
        container = Container()
        container.containerUid = db.containerDao.insert(container)
        epubTmpFile = File.createTempFile("tmp", "epub")
        UmFileUtilSe.extractResourceToFile("/testfiles/thelittlechicks.epub",
                epubTmpFile!!)

        containerManager = ContainerManager(container, db, db, containerTmpDir.absolutePath)
        addEntriesFromZipToContainer(epubTmpFile.absolutePath, containerManager)
        testPath = containerManager.allEntries[13].cePath!!
    }

    @After
    fun tearDown() {
        server.stop(0, 7, TimeUnit.SECONDS)
    }

    @Test
    fun givenMountRequest_whenNoContainerExists_shouldRespondWithNotFound() {
        runBlocking {
            val httpClient = HttpClient {
                install(JsonFeature)
            }

            httpClient.use {
                val mountResponse = httpClient.get<HttpResponse>(
                        "http://localhost:${defaultPort}/ContainerMount/${container.containerUid + 1}/epub.css")
                Assert.assertEquals("Container to be mounted was not found",HttpResponseStatus.NOT_FOUND.code(), mountResponse.status.value)
            }
        }
    }

    @Test
    fun givenMountRequest_whenContainerExistsAndFileExists_shouldMountAndServeTheFile() {
        runBlocking {
            val httpClient = HttpClient {
                install(JsonFeature)
            }

            httpClient.use {
                val mountResponse = httpClient.get<HttpResponse>(
                        "http://localhost:${defaultPort}/ContainerMount/${container.containerUid}/${testPath}")
                Assert.assertEquals("Container was mounted and requested file found",HttpResponseStatus.OK.code(), mountResponse.status.value)
            }
        }
    }

    @Test
    fun givenMountRequest_whenHeadRequestedOnExistingFile_shouldMountAndServeRequiredDetails() {
        runBlocking {
            val httpClient = HttpClient {
                install(JsonFeature)
            }

            httpClient.use {
                val mountResponse = httpClient.head<HttpResponse>(
                        "http://localhost:${defaultPort}/ContainerMount/${container.containerUid}/${testPath}")
                Assert.assertTrue("Container mounted and responded with content length",
                        200 == mountResponse.status.value && mountResponse.headers[HttpHeaders.ContentLength]!!.toInt() > 0)
            }
        }
    }


}

