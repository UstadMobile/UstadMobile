package com.ustadmobile.sharedse.network.containeruploader

import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.lib.rest.ResumableUploadRoute
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.sharedse.network.ContainerUploader
import com.ustadmobile.sharedse.network.ContainerUploader.Companion.CHUNK_SIZE
import com.ustadmobile.sharedse.network.NetworkManagerBle
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcherJobHttpUrlConnectionTest
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcherRequest
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.Okio
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import java.io.File
import java.util.*

class TestContainerUploader {

    private lateinit var tmpFolder: File
    private lateinit var fileToUpload: File
    private lateinit var mockWebServer: MockWebServer

    private lateinit var di: DI

    private lateinit var networkManager: NetworkManagerBle


    @Before
    fun setup() {
        networkManager = mock()
        di = DI {
            bind<NetworkManagerBle>() with singleton { networkManager }
        }
        tmpFolder = UmFileUtilSe.makeTempDir("upload", "")
        fileToUpload = File(tmpFolder, "tincan.zip")
        UmFileUtilSe.extractResourceToFile(
                "/com/ustadmobile/port/sharedse/contentformats/ustad-tincan.zip",
                fileToUpload)

    }

    @Test
    fun givenValidHttpUrl_whenUploadCalled_thenShouldUploadFileAndReturnSuccess() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val sessionId = UUID.randomUUID().toString()
        mockWebServer.enqueue(MockResponse().setBody(sessionId))
        for(i in 0..fileToUpload.length() step CHUNK_SIZE.toLong()){
            mockWebServer.enqueue(MockResponse().setResponseCode(HttpStatusCode.NoContent.value))
        }

        val request = ContainerUploaderRequest(fileToUpload.absolutePath,
                mockWebServer.url("/upload/").toString())

        val uploader = ContainerUploader(request, null, di = di)
        val downloadResult = runBlocking { uploader.upload() }
        val uploadStatus = File(tmpFolder, "tincan.zip.uploadInfo").readText()
        Assert.assertEquals("Upload result is successful", JobStatus.COMPLETE,
                downloadResult)
        Assert.assertEquals("uploadStatus Matches", uploadStatus,
                "{\"sessionId\":\"$sessionId\",\"uploadedTo\":${fileToUpload.length()}}")

    }

}