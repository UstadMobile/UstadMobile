package com.ustadmobile.core.network.containeruploader

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ConcatenatedEntry
import com.ustadmobile.core.io.ConcatenatedInputStream2
import com.ustadmobile.core.io.UploadSessionParams
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.core.io.ext.generateConcatenatedFilesResponse2
import com.ustadmobile.core.io.ext.toContainerEntryWithMd5
import com.ustadmobile.core.network.NetworkProgressListener
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.distinctMds5sSorted
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.SequenceInputStream
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("BlockingMethodInNonBlockingContext")
class ContainerUploader2Test {

    lateinit var di: DI

    lateinit var clientDb: UmAppDatabase

    lateinit var clientRepo: UmAppDatabase

    lateinit var mockWebServer: MockWebServer

    lateinit var mockUploadDispatcher: MockUploadDispatcher

    @JvmField
    @Rule
    val ustadTestRule = UstadTestRule()

    lateinit var container: Container

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    lateinit var siteEndpoint: Endpoint

    class MockUploadDispatcher(var startFromPos: Long = 0): Dispatcher() {

        override fun dispatch(request: RecordedRequest): MockResponse {
            return when {
                //request all entries
                request.requestUrl.toString().endsWith("/init") -> {
                    val byteArrayOut = ByteArrayOutputStream()
                    request.body.writeTo(byteArrayOut)
                    byteArrayOut.flush()

                    val inputBodyStr = byteArrayOut.toString()
                    val requestedEntries: List<ContainerEntryWithMd5> = Gson().fromJson(inputBodyStr,
                        object: TypeToken<List<ContainerEntryWithMd5>>() { } .type)

                    val uploadSessionParams = UploadSessionParams(
                            requestedEntries.distinctMds5sSorted(), startFromPos)
                    MockResponse()
                            .setBody(Gson().toJson(uploadSessionParams))
                            .setHeader("content-type", "application/json;charset=utf-8")
                }
                request.requestUrl.toString().endsWith("/data") -> {
                    MockResponse().setResponseCode(204)
                }
                request.requestUrl.toString().endsWith("/close") -> {
                    MockResponse().setResponseCode(204)
                }

                else -> {
                    MockResponse().setResponseCode(404)
                }
            }
        }
    }

    /**
     * Utility function to get all payload data
     */
    private fun MockWebServer.takeAllDataRequestPayloads() : ByteArray {
        val byteArrayOut = ByteArrayOutputStream()
        lateinit var recordedRequest: RecordedRequest

        while(takeRequest(1, TimeUnit.SECONDS)?.also { recordedRequest = it } != null) {
            if(recordedRequest.requestUrl.toString().endsWith("/data")) {
                recordedRequest.body.writeTo(byteArrayOut)
            }

            if(recordedRequest.requestUrl.toString().endsWith("/close")) {
                break
            }
        }

        byteArrayOut.flush()

        return byteArrayOut.toByteArray()
    }

    private fun ConcatenatedInputStream2.md5sInStream(): List<String> {
        val md5sInStream = mutableListOf<String>()
        lateinit var concatEntry: ConcatenatedEntry
        while(getNextEntry()?.also { concatEntry = it } != null) {
            md5sInStream += concatEntry.md5.encodeBase64()
        }

        return md5sInStream.toList()
    }


    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        mockWebServer = MockWebServer().also {
            it.start()
        }

        mockUploadDispatcher = MockUploadDispatcher()
        mockWebServer.dispatcher= mockUploadDispatcher

        val accountManager: UstadAccountManager = di.direct.instance()
        siteEndpoint = Endpoint(mockWebServer.url("/").toString())
        accountManager.activeEndpoint = siteEndpoint

        clientDb = di.on(siteEndpoint).direct.instance(tag = DoorTag.TAG_DB)
        clientRepo = di.on(siteEndpoint).direct.instance(tag = DoorTag.TAG_REPO)

        container = Container().apply {
            containerUid = clientRepo.containerDao.insert(this)
        }

        runBlocking {
            clientRepo.addEntriesToContainerFromZipResource(container.containerUid, this::class.java,
                        "/com/ustadmobile/core/contentformats/epub/test.epub",
                            ContainerAddOptions(temporaryFolder.newFolder().toDoorUri()))
        }

    }

    @Test
    fun givenValidContainer_whenUploadCalled_thenShouldUploadData() {
        val entriesToUpload = clientDb.containerEntryDao.findByContainer(container.containerUid).map {
            it.toContainerEntryWithMd5()
        }

        val uploadRequest = ContainerUploaderRequest2(UUID.randomUUID().toString(),
                entriesToUpload, mockWebServer.url("/").toString())

        val uploader = ContainerUploader2(uploadRequest, 200 * 1024, siteEndpoint,
            null, di)
        val result = runBlocking { uploader.upload() }

        //now read the stream
        val bytesReceived = mockWebServer.takeAllDataRequestPayloads()
        val concatStreamIn = ConcatenatedInputStream2(ByteArrayInputStream(bytesReceived))
        val md5sInStream = concatStreamIn.md5sInStream()

        entriesToUpload.forEach { containerEntry ->
            Assert.assertTrue("Found entry in stream uploaded to server", md5sInStream.any {
                containerEntry.cefMd5 == it
            })
        }

        Assert.assertEquals("Result is JobStatus.COMPLETE", JobStatus.COMPLETE, result)
    }


    @Test
    fun givenContainerPartiallyUploaded_whenUploadCalled_thenShouldFinish() {
        val entriesToUpload = clientDb.containerEntryDao.findByContainer(container.containerUid).map {
            it.toContainerEntryWithMd5()
        }

        mockUploadDispatcher.startFromPos = 20000

        val uploadRequest = ContainerUploaderRequest2(UUID.randomUUID().toString(),
                entriesToUpload, mockWebServer.url("/").toString())

        val mockProgressListener: NetworkProgressListener = mock { }
        val uploader = ContainerUploader2(uploadRequest, 200 * 1024, siteEndpoint,
            mockProgressListener, di)
        val result = runBlocking { uploader.upload() }

        //now read the stream
        val bytesReceived = mockWebServer.takeAllDataRequestPayloads()
        val initialBytesResponse = clientDb.containerEntryFileDao.generateConcatenatedFilesResponse2(
                entriesToUpload.distinctMds5sSorted(),
                mapOf("range" to listOf("bytes=0-${mockUploadDispatcher.startFromPos - 1}")),
                clientDb)

        val initalBytes = ByteArrayOutputStream().let {
            initialBytesResponse.writeTo(it)
            it.flush()
            it.toByteArray()
        }
        val concatStreamIn = ConcatenatedInputStream2(SequenceInputStream(
                ByteArrayInputStream(initalBytes),
                ByteArrayInputStream(bytesReceived)))
        val md5sInStream = concatStreamIn.md5sInStream()

        entriesToUpload.forEach { containerEntry ->
            Assert.assertTrue("Found entry in stream uploaded to server", md5sInStream.any {
                containerEntry.cefMd5 == it
            })
        }

        Assert.assertEquals("Result is JobStatus.COMPLETE", JobStatus.COMPLETE, result)
        verify(mockProgressListener, atLeastOnce()).onProgress(any(), any())
    }
}