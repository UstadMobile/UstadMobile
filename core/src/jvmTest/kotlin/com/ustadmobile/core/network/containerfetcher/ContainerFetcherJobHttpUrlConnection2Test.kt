package com.ustadmobile.core.network.containerfetcher

import com.github.aakira.napier.Napier
import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZip
import com.ustadmobile.core.io.ext.generateConcatenatedFilesResponse2
import com.ustadmobile.core.io.ext.toContainerEntryWithMd5
import com.ustadmobile.core.io.ext.toKmpUriString
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.base64StringToByteArray
import com.ustadmobile.door.asRepository
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toHexString
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.util.commontest.ext.assertContainerEqualToOther
import com.ustadmobile.util.commontest.ext.mockResponseForConcatenatedFiles2Request
import com.ustadmobile.util.test.ext.baseDebugIfNotEnabled
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import okio.Buffer
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.*
import java.util.concurrent.atomic.AtomicInteger

class ContainerFetcherJobHttpUrlConnection2Test {


    class ConcatenatedResponse2Dispatcher(private val db: UmAppDatabase) : Dispatcher(){

        var numTimesToFail = AtomicInteger(0)

        override fun dispatch(request: RecordedRequest): MockResponse {
            return db.mockResponseForConcatenatedFiles2Request(request).apply {
                if(numTimesToFail.getAndDecrement() > 0) {
                    socketPolicy = SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY
                }
            }
        }
    }



    fun UmAppDatabase.assertAllContainerEntryFilesPresentInOther(md5List: List<String>, otherDb: UmAppDatabase) {
        md5List.forEach { md5Base64 ->
            val md5Hex = md5Base64.base64StringToByteArray().toHexString()
            val fileEntryInThisDb = runBlocking {
                containerEntryFileDao.findEntryByMd5Sum(md5Base64)
            }
            val fileEntryInOtherDb = runBlocking {
                otherDb.containerEntryFileDao.findEntryByMd5Sum(md5Base64)
            }

            Assert.assertArrayEquals("Content of containerfileentry for $md5Base64  ($md5Hex) are the same in both db",
                File(fileEntryInThisDb!!.cefPath!!).readBytes(),
                File(fileEntryInOtherDb!!.cefPath!!).readBytes())
        }
    }


    private lateinit var mockWebServer: MockWebServer

    private lateinit var dispatcher: ConcatenatedResponse2Dispatcher

    private lateinit var serverDb: UmAppDatabase

    private lateinit var serverRepo: UmAppDatabase

    private lateinit var serverHttpClient: HttpClient

    private lateinit var container: Container

    private lateinit var clientDi: DI

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    val ustadTestRule = UstadTestRule()

    @Before
    fun setup() {
        Napier.baseDebugIfNotEnabled()
        serverHttpClient = HttpClient() {
            install(JsonFeature)
            install(HttpTimeout)
        }

        serverDb = UmAppDatabase.getInstance(Any(), "UmAppDatabase").also {
            it.clearAllTables()
        }
        serverRepo = serverDb.asRepository(Any(), "http://localhost/dummy", "",
            serverHttpClient, null)

        container = Container().apply {
            containerUid = serverRepo.containerDao.insert(this)
        }

        val epubFile = temporaryFolder.newFile()
        this::class.java.getResourceAsStream("/com/ustadmobile/core/contentformats/epub/test.epub")
                .writeToFile(epubFile)
        val containerTmpFolder = temporaryFolder.newFolder()
        runBlocking {
            serverRepo.addEntriesToContainerFromZip(container.containerUid,
                epubFile.toKmpUriString(), ContainerAddOptions(containerTmpFolder.toKmpUriString()))
        }

        //Create a mock web server that will serve the concatenated data
        mockWebServer = MockWebServer()
        dispatcher = ConcatenatedResponse2Dispatcher(serverDb)
        mockWebServer.setDispatcher(dispatcher)
        mockWebServer.start()

        clientDi = DI {
            import(ustadTestRule.diModule)
        }
    }

    @After
    fun shutdown() {
        mockWebServer.shutdown()
    }

    @Test
    fun givenValidRequest_whenDownloadCalled_thenShouldDownloadContainerFiles() {
        val containerEntriesToDownload = serverDb.containerEntryDao.findByContainer(container.containerUid)
                .map { it.toContainerEntryWithMd5() }
        val md5List = containerEntriesToDownload.map { it.cefMd5!! }

        val downloadDestDir = temporaryFolder.newFolder()

        val siteUrl = mockWebServer.url("/").toString()
        val request = ContainerFetcherRequest2(containerEntriesToDownload, siteUrl,
            downloadDestDir.toKmpUriString())

        val mockListener = mock<ContainerFetcherListener2> { }
        val downloaderJob = ContainerFetcherJobHttpUrlConnection2(request,
            mockListener, clientDi)

        val result = runBlocking { downloaderJob.download() }

        Assert.assertEquals("Result is reported as successful", JobStatus.COMPLETE,
            result)

        val clientDb: UmAppDatabase = clientDi.on(Endpoint(siteUrl)).direct.instance(tag = DoorTag.TAG_DB)


        serverDb.assertAllContainerEntryFilesPresentInOther(md5List, clientDb)
        serverDb.assertContainerEqualToOther(container.containerUid, clientDb)
    }

    @Test
    fun givenDownloadIsInterrupted_whenNewRequestMade_thenDownloadShouldResume() {
        dispatcher.numTimesToFail.set(1)

        val allContainerEntryFilesToDownload = serverDb.containerEntryDao.findByContainer(container.containerUid)
                .map { it.toContainerEntryWithMd5() }

        val downloadDestDir = temporaryFolder.newFolder()

        val siteUrl = mockWebServer.url("/").toString()


        val mockListener = mock<ContainerFetcherListener2> { }

        val results = mutableListOf<Int>()
        val clientDb: UmAppDatabase = clientDi.on(Endpoint(siteUrl)).direct.instance(tag = DoorTag.TAG_DB)

        for(i in 0..1) {
            Napier.d("============ ATTEMPT $i ============")
            try {
                val entriesInDb = clientDb.containerEntryFileDao.findEntriesByMd5Sums(
                        allContainerEntryFilesToDownload.mapNotNull { it.cefMd5 })
                val entriesToDownload = allContainerEntryFilesToDownload
                        .filter { entry -> ! entriesInDb.any { dbEntry -> dbEntry.cefMd5 ==  entry.cefMd5} }

                val request = ContainerFetcherRequest2(entriesToDownload, siteUrl, downloadDestDir.toKmpUriString())
                val downloaderJob = ContainerFetcherJobHttpUrlConnection2(request,
                        mockListener, clientDi)
                val result = runBlocking { downloaderJob.download() }
                results.add(result)
            }catch(e: Exception) {
                e.printStackTrace()
                throw e
            }

        }

        Assert.assertEquals("First result returned paused",
                0, results[0])
        Assert.assertEquals("Second result completed",
                JobStatus.COMPLETE, results[1])

        val mockRequest1 = mockWebServer.takeRequest()
        val mockRequest2 = mockWebServer.takeRequest()
        Assert.assertNotNull("Second request included partial response request",
                mockRequest2.getHeader("range"))


        serverDb.assertAllContainerEntryFilesPresentInOther(
                allContainerEntryFilesToDownload.map { it.cefMd5!! }, clientDb)
        serverDb.assertContainerEqualToOther(container.containerUid, clientDb)
    }


    fun givenValidRequest_whenServerProvidesCorruptData_thenShouldFail() {

    }




}