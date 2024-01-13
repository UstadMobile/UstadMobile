package com.ustadmobile.core.domain.blob.saveandmanifest

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.pdf.AbstractPdfContentImportCommonJvm
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.xfertestnode.XferTestClient
import com.ustadmobile.core.domain.blob.xfertestnode.XferTestNode
import com.ustadmobile.core.domain.blob.xfertestnode.XferTestServer
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentEntryUseCase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.flow.doorFlow
import com.ustadmobile.lib.db.composites.TransferJobItemStatus
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.util.test.initNapierLog
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

/**
 * This is a high-fidelity integration test to test to verify that content added and manifested on
 * a client is correctly uploaded to the server. The server runs over http and no mocks are used.
 *
 * This tests the whole cycle of:
 *   1) Client manifests and stores content in its local cache
 *   2) Client uploads everything in the manifest to the server
 *   3) The server can serve all the blobs (e.g. as per ContentManifestEntry.bodyDataUrl and
 *      /api/content/path/in/manifest correctly
 */
class SaveLocalUriAndManifestUploadIntegrationTest{

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenValidPdf_whenImportedOnClient_thenWilBeUploadedToServer() {
        initNapierLog()
        val clientNode = XferTestClient(XferTestNode(temporaryFolder, "client"))
        val serverNode = XferTestServer(XferTestNode(temporaryFolder, "server"))
        try {
            val endpoint = Endpoint("http://localhost:${serverNode.port}/")

            val importContentUseCase: ImportContentEntryUseCase = clientNode.di.on(endpoint)
                .direct.instance()

            val pdfToImport = temporaryFolder.newFileFromResource(this::class.java,
                "/com/ustadmobile/core/container/validPDFMetadata.pdf")
            val contentImportJob = ContentEntryImportJob(
                sourceUri = pdfToImport.toDoorUri().toString(),
                cjiOriginalFilename = "validPDFMetadata.pdf",
                cjiPluginId = AbstractPdfContentImportCommonJvm.PLUGINID
            )

            runBlocking {
                val clientDb: UmAppDatabase = clientNode.di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)
                val jobUid = clientDb.contentEntryImportJobDao.insertJobItem(contentImportJob)

                val entryVersion = importContentUseCase(jobUid)
                val transferJobFlow = clientDb.doorFlow(arrayOf("TransferJob", "TransferJobItem")) {
                    clientDb.transferJobDao.findJobByEntityAndTableUid(
                        tableId = ContentEntryVersion.TABLE_ID,
                        entityUid = entryVersion.cevUid,
                    )
                }

                transferJobFlow.filter {
                    it.size == 1 && it.first().tjStatus == TransferJobItemStatus.STATUS_COMPLETE_INT
                }.test(timeout = 100.seconds, name = "transfer job should complete") {
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }finally {
            clientNode.close()
            serverNode.close()
        }
    }

}