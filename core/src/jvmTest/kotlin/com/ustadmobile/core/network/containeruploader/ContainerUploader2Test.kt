package com.ustadmobile.core.network.containeruploader

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ConcatenatedEntry
import com.ustadmobile.core.io.ConcatenatedInputStream2
import com.ustadmobile.core.io.ext.addEntriesToContainerFromZipResource
import com.ustadmobile.core.io.ext.toContainerEntryWithMd5
import com.ustadmobile.core.networkmanager.ContainerUploaderRequest2
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.sharedse.network.containeruploader.ContainerUploader
import kotlinx.coroutines.runBlocking
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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
class ContainerUploader2Test {

    lateinit var di: DI

    lateinit var clientDb: UmAppDatabase

    lateinit var clientRepo: UmAppDatabase

    lateinit var mockWebServer: MockWebServer

    @JvmField
    @Rule
    val ustadTestRule = UstadTestRule()

    lateinit var container: Container

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    lateinit var siteEndpoint: Endpoint

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        mockWebServer = MockWebServer().also {
            it.start()
        }

        val accountManager: UstadAccountManager = di.direct.instance()
        siteEndpoint = Endpoint(mockWebServer.url("/").toString())
        accountManager.activeAccount.endpointUrl = siteEndpoint.url

        clientDb = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_DB)
        clientRepo = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)

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

        val uploader = ContainerUploader2(uploadRequest, 200 * 1024, siteEndpoint, di)
        val result = runBlocking { uploader.upload() }

        lateinit var recordedRequest: RecordedRequest
        val byteArrayOut = ByteArrayOutputStream()
        while(mockWebServer.takeRequest()?.also { recordedRequest = it } != null) {
            if(recordedRequest.requestUrl.toString().endsWith("/data")) {
                recordedRequest.body.writeTo(byteArrayOut)
            }

            if(recordedRequest.requestUrl.toString().endsWith("/close")) {
                break
            }
        }

        byteArrayOut.flush()
        //now read the stream
        val concatStreamIn = ConcatenatedInputStream2(ByteArrayInputStream(byteArrayOut.toByteArray()))
        val md5sInStream = mutableListOf<String>()
        lateinit var concatEntry: ConcatenatedEntry
        while(concatStreamIn.getNextEntry()?.also { concatEntry = it } != null) {
            md5sInStream += concatEntry.md5.encodeBase64()
        }

        entriesToUpload.forEach { containerEntry ->
            Assert.assertTrue("Found entry in stream uploaded to server", md5sInStream.any {
                containerEntry.cefMd5 == it
            })
        }
    }


}