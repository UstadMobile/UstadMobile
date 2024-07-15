package com.ustadmobile.core.domain.blob.saveandmanifest

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.h5p.H5PContentImporter
import com.ustadmobile.core.contentformats.pdf.AbstractPdfContentImportCommonJvm
import com.ustadmobile.core.contentformats.xapi.XapiZipContentImporter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.xfertestnode.XferTestClient
import com.ustadmobile.core.domain.blob.xfertestnode.XferTestNode
import com.ustadmobile.core.domain.blob.xfertestnode.XferTestServer
import com.ustadmobile.core.domain.blob.xfertestnode.XferTestServerInteceptor
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentEntryUseCase
import com.ustadmobile.core.domain.upload.HEADER_UPLOAD_START_BYTE
import com.ustadmobile.core.domain.upload.HEADER_UPLOAD_UUID
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.concurrentSafeMapOf
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.flow.doorFlow
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.util.test.initNapierLog
import io.github.aakira.napier.Napier
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.request.uri
import io.ktor.server.response.respondText
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

/**
 * This is a high-fidelity integration test to test to verify that content added and manifested on
 * a client is correctly uploaded to the server. The server runs over http and no mocks are used.
 *
 * This tests the whole cycle of:
 *   1) Client manifests and stores content in its own local cache correctly
 *   2) Client uploads everything in the manifest to the server
 *   3) The server can serve all the blobs (e.g. as per ContentManifestEntry.bodyDataUrl and
 *      /api/content/path/in/manifest correctly)
 *   4) Client replicates the ContentEntryVersion entity to the server once the upload is finished
 *      (if this is not in the server database, the server will not recognize that the
 *      ContentEntryVersion is available to open, and opening it on any client will fail).
 */
class SaveLocalUriAndManifestUploadIntegrationTest{

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()


    private fun importAndVerifyManifest(
        contentEntryImportJob: ContentEntryImportJob,
        timeout: Int = 15,
        serverNode: XferTestServer = XferTestServer(XferTestNode(temporaryFolder, "server")),
        clientNode: XferTestClient = XferTestClient(
            XferTestNode(temporaryFolder, "client")
        )
    ) {
        try {
            val endpoint = Endpoint("http://localhost:${serverNode.port}/")

            val importContentUseCase: ImportContentEntryUseCase = clientNode.di.on(endpoint)
                .direct.instance()

            runBlocking {
                val clientDb: UmAppDatabase = clientNode.di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)
                val jobUid = clientDb.contentEntryImportJobDao().insertJobItem(contentEntryImportJob)

                val entryVersion = importContentUseCase(jobUid)
                clientNode.waitForContentUploadCompletion(
                    endpoint = endpoint,
                    contentEntryVersionUid = entryVersion.cevUid,
                    timeout = timeout
                )
                val manifestOnClient = clientNode.node.getManifest(entryVersion.cevManifestUrl!!)

                clientNode.node.assertManifestStoredOnNode(manifestOnClient, entryVersion.cevManifestUrl!!)
                serverNode.node.assertManifestStoredOnNode(manifestOnClient, entryVersion.cevManifestUrl!!)

                /*
                 * Server MUST receive the ContentEntryVersion entity in its database via replication,
                 * otherwise any clients will not know that the ContentEntryVersion exists and it will
                 * not be possible to open
                 */
                val serverDb: UmAppDatabase = serverNode.di.direct.on(endpoint)
                    .instance(tag = DoorTag.TAG_DB)
                serverDb.doorFlow(arrayOf("ContentEntryVersion")) {
                    serverDb.contentEntryVersionDao().findByUidAsync(entryVersion.cevUid)
                }.assertItemReceived(
                    timeout = timeout.seconds,
                    name = "Server should have ContentEntryVersion entity id #${entryVersion.cevUid}"
                ) {
                    it != null && it.cevManifestUrl == entryVersion.cevManifestUrl
                }

                val allManifestUrls = manifestOnClient.entries.map {
                    it.bodyDataUrl
                }.distinct() +  entryVersion.cevManifestUrl!!

                /*
                 * All urls referenced in the manifest should marked on the server to be retained
                 */
                serverDb.doorFlow(arrayOf("CacheLockJoin")) {
                    serverDb.cacheLockJoinDao().findByTableIdAndEntityUid(
                        tableId  = ContentEntryVersion.TABLE_ID,
                        entityUid = entryVersion.cevUid
                    )
                }.assertItemReceived { cacheLocks ->
                    val cacheLockUrlSet = cacheLocks.mapNotNull { it.cljUrl }.toSet()

                    allManifestUrls.all { it in cacheLockUrlSet }
                }

                /*
                 * All urls referenced in the manifest should not be locked on the client after the
                 * upload is finished
                 */
                allManifestUrls.forEach {
                    assertEquals(0, clientNode.node.httpCache.getLocks(it).size,
                        "Client should not have any cache retention locks for $it")
                }
            }
        }finally {
            clientNode.close()
            serverNode.close()
        }
    }


    @Test
    fun givenValidPdf_whenImportedOnClient_thenWilBeUploadedToServer() {
        val pdfFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/validPDFMetadata.pdf")
        initNapierLog()
        importAndVerifyManifest(
            ContentEntryImportJob(
                sourceUri = pdfFile.toDoorUri().toString(),
                cjiOriginalFilename = "validPDFMetadata.pdf",
                cjiPluginId = AbstractPdfContentImportCommonJvm.PLUGINID
            )
        )
    }

    @Test
    fun givenValidXapiFile_whenImportedOnClient_thenWillBeUploadedToServer() {
        val xapiFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/ustad-tincan.zip")
        importAndVerifyManifest(
            ContentEntryImportJob(
                sourceUri = xapiFile.toDoorUri().toString(),
                cjiOriginalFilename = "ustad-tincan.zip",
                cjiPluginId = XapiZipContentImporter.PLUGIN_ID,
            )
        )
    }

    //Current timing: 6-7sec
    @Test
    fun givenValidH5p_whenImportedOnClient_thenWillBeUploadedToServer() {
        val h5pFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/dialog-cards-620.h5p")
        importAndVerifyManifest(
            ContentEntryImportJob(
                sourceUri = h5pFile.toDoorUri().toString(),
                cjiOriginalFilename = "dialog-cards-620.h5p",
                cjiPluginId = H5PContentImporter.PLUGIN_ID,
            )
        )
    }

    /**
     * This interrupted upload test uses an interceptor to fail one (mid-range) upload chunk and
     * verifies that, after resuming, all verifications are passing as for any other request. It also
     * verifies that the upload was in fact resumed by ensuring there are no duplicate requests for
     * already-uploaded bytes
     */
    @Test
    fun givenValidXapiFile_whenUploadInterrupted_thenWillRetry() {
        initNapierLog()
        val failedUuid = AtomicReference<String?>(null)

        //map of upload item uuid to a list of the starting byte for the given request
        val uploadDataFromRequestMap = concurrentSafeMapOf<String, MutableList<Int>>()

        val interceptor: XferTestServerInteceptor = {
            if(call.request.uri.endsWith("/upload-batch-data")) {
                val requestUuid = call.request.header(HEADER_UPLOAD_UUID)!!
                val fromByte = call.request.header(HEADER_UPLOAD_START_BYTE)?.toInt() ?: 0

                uploadDataFromRequestMap.getOrPut(requestUuid) {
                    mutableListOf()
                }.add(fromByte)

                if(failedUuid.get() == null) {
                    /*
                     * Fail the first request where the fromByte is greater than zero e.g. force
                     * resumption
                     */
                    val failThis = (fromByte > 0) && failedUuid.getAndUpdate { prev ->
                        requestUuid
                    } == null

                    if(failThis) {
                        Napier.d("Interceptor Force Fail: $requestUuid")
                        call.respondText("Interceptor Force Fail!", status = HttpStatusCode.InternalServerError)
                        finish()
                    }
                }
            }
        }

        val xapiFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/contenttype/ustad-tincan.zip")

        importAndVerifyManifest(
            contentEntryImportJob = ContentEntryImportJob(
                sourceUri = xapiFile.toDoorUri().toString(),
                cjiOriginalFilename = "ustad-tincan.zip",
                cjiPluginId = XapiZipContentImporter.PLUGIN_ID,
            ),
            serverNode = XferTestServer(
                node = XferTestNode(temporaryFolder, "server"),
                ktorInterceptor = interceptor,
            ),
            clientNode = XferTestClient(
                node = XferTestNode(temporaryFolder, "client"),
                uploadChunkSize = 10_000,
            )
        )

        val failedUuidVal = failedUuid.get()
        assertNotNull(failedUuidVal, "One UUID upload was failed by the test")

        /*
         * Verify that this did in fact resume: the initial upload chunk should not have been
         * re-uploaded.
         */
        val startFromBytesForFailedUuid = uploadDataFromRequestMap[failedUuidVal]!!
        assertEquals(1, startFromBytesForFailedUuid.count { it == 0 })
    }


}