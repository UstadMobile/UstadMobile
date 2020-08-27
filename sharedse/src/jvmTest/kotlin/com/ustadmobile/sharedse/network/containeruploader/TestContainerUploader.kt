package com.ustadmobile.sharedse.network.containeruploader

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.bindNewSqliteDataSourceIfNotExisting
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerUploadJob
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import com.ustadmobile.port.sharedse.ext.generateConcatenatedFilesResponse
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.sharedse.network.containeruploader.ContainerUploader.Companion.DEFAULT_CHUNK_SIZE
import com.ustadmobile.sharedse.network.NetworkManagerBle
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.*
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.naming.InitialContext

class TestContainerUploader {

    private lateinit var epubContainer: Container
    private lateinit var entryListStr: String
    private lateinit var containerManager: ContainerManager
    private lateinit var appDb: UmAppDatabase

    @JvmField
    @Rule
    var tmpFolderRule = TemporaryFolder()

    private lateinit var tmpFolder: File


    private lateinit var fileToUpload: File
    private lateinit var mockWebServer: MockWebServer

    private lateinit var di: DI

    private lateinit var networkManager: NetworkManagerBle

    private val context = Any()

    @Before
    fun setup() {
        networkManager = mock()
        val endpointScope = EndpointScope()
        di = DI {
            bind<NetworkManagerBle>() with singleton { networkManager }
            bind<UmAppDatabase>(tag = UmAppDatabase.TAG_DB) with scoped(endpointScope).singleton {
                val dbName = sanitizeDbNameFromUrl(context.url)
                InitialContext().bindNewSqliteDataSourceIfNotExisting(dbName)
                spy(UmAppDatabase.getInstance(Any(), dbName).also {
                    it.clearAllTables()
                })
            }
        }
        appDb = di.on(Endpoint(TEST_ENDPOINT)).direct.instance(tag = UmAppDatabase.TAG_DB)

        tmpFolder = tmpFolderRule.newFolder()
        fileToUpload = File(tmpFolder, "tincan.zip")
        UmFileUtilSe.extractResourceToFile(
                "/com/ustadmobile/port/sharedse/contentformats/ustad-tincan.zip",
                fileToUpload)


        epubContainer = Container()
        epubContainer.containerUid = appDb.containerDao.insert(epubContainer)
        containerManager = ContainerManager(epubContainer, appDb, appDb, tmpFolder.absolutePath)
        runBlocking {
            containerManager.addEntries(ContainerManager.FileEntrySource(fileToUpload, fileToUpload.name))
            val entryList = containerManager.allEntries.distinctBy { it.containerEntryFile!!.cefMd5 }
            entryListStr = entryList.joinToString(separator = ";") { it.ceCefUid.toString() }
        }
    }

    @Test
    fun givenValidHttpUrl_whenUploadCalled_thenShouldUploadFileAndReturnSuccess() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val inStream = appDb.containerEntryFileDao.generateConcatenatedFilesResponse(entryListStr).dataSrc

        val job = ContainerUploadJob()
        job.cujContainerUid = epubContainer.containerUid
        job.cujUid = appDb.containerUploadJobDao.insert(job)

        val sessionId = UUID.randomUUID().toString()
        mockWebServer.enqueue(MockResponse().setBody(sessionId))
        for (i in 0..fileToUpload.length() step DEFAULT_CHUNK_SIZE.toLong()) {
            mockWebServer.enqueue(MockResponse().setResponseCode(HttpStatusCode.NoContent.value))
        }


        val request = ContainerUploaderRequest(job.cujUid,
                entryListStr, mockWebServer.url("/upload/").toString(), TEST_ENDPOINT
        )

        val uploader = ContainerUploader(request, di = di)
        val uploadResult = runBlocking { uploader.upload() }

        val uploadedFile = File(tmpFolder, "UploadedFile")
        val fileOut = FileOutputStream(uploadedFile)
        val requestCount = mockWebServer.requestCount
        repeat(requestCount) {
            val request = mockWebServer.takeRequest(2, TimeUnit.SECONDS)
            if (request.method == "GET") {
                return@repeat
            }
            request.body.writeTo(fileOut)
        }

        Assert.assertEquals("Upload result is successful", JobStatus.COMPLETE,
                uploadResult)
        Assert.assertArrayEquals("byte array of file matches", inStream!!.readBytes(), uploadedFile.readBytes())

    }

    companion object {

        const val TEST_ENDPOINT = "http://test.localhost.com/"

    }

}