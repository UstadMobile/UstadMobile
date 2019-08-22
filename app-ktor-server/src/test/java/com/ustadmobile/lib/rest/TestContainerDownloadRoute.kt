package com.ustadmobile.lib.rest

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class TestContainerDownloadRoute {

    lateinit var server: ApplicationEngine

    lateinit var db: UmAppDatabase

    lateinit var container: Container

    lateinit var epubTmpFile: File

    lateinit var containerTmpDir: File

    lateinit var containerManager: ContainerManager

    @Before
    fun setup() {
        db = DatabaseBuilder.databaseBuilder(Any() ,UmAppDatabase::class, "UmAppDatabase").build()
        db.clearAllTables()
        server = embeddedServer(Netty, port = 8097) {
            install(ContentNegotiation) {
                gson {
                    register(ContentType.Application.Json, GsonConverter())
                    register(ContentType.Any, GsonConverter())
                }
            }

            install(Routing) {
                ContainerDownload(db)
            }
        }.start(wait = false)

        containerTmpDir = UmFileUtilSe.makeTempDir("testdlroute", "tmpdir")
        container = Container()
        container.containerUid = db.containerDao.insert(container)
        epubTmpFile = File.createTempFile("tmp", "epub")
        UmFileUtilSe.extractResourceToFile("/testfiles/thelittlechicks.epub",
                epubTmpFile!!)

        containerManager = ContainerManager(container, db, db, containerTmpDir.absolutePath)
        addEntriesFromZipToContainer(epubTmpFile.absolutePath, containerManager)
    }

    @After
    fun tearDown() {
        server.stop(0, 5, TimeUnit.SECONDS)
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
                containerManager.allEntries.forEach { cntMgrEntry ->
                    Assert.assertTrue("Entry was in response", containerEntryList.any {
                        it.ceUid == cntMgrEntry.ceUid && it.cefMd5 != null && it.cefMd5 == cntMgrEntry.containerEntryFile?.cefMd5})
                }
            }
        }
    }


    @Test
    fun givenContainerEntryFileUid_whenDownloadRequestIsMade_contentsShouldMatch() {
        runBlocking {
            val httpClient = HttpClient()
            val buffer = IoBuffer.Pool.borrow()
            val firstContainerEntry = containerManager.allEntries[0]
            val httpIn = httpClient.get<InputStream>(
                    "http://localhost:8097/ContainerEntryFile/${firstContainerEntry.ceCefUid}").asInput()
            val saveTmpFile = File.createTempFile("TestContainerDownloadRoute", "TestDl")
            val fileOutput = FileOutputStream(saveTmpFile).asOutput()
            do {
                buffer.resetForWrite()
                val rc = httpIn.readAvailable(buffer)
                if (rc == -1) break
                fileOutput.writeFully(buffer)
            } while (true)

            httpIn.close()
            fileOutput.flush()
            fileOutput.close()
            httpClient.close()

            val downloadedBytes = FileInputStream(saveTmpFile).readBytes()
            val containerEntryBytes = FileInputStream(firstContainerEntry.containerEntryFile!!.cefPath!!).readBytes()

            Assert.assertArrayEquals("Bytes download = bytes from files",containerEntryBytes,
                    downloadedBytes)
        }
    }

}

