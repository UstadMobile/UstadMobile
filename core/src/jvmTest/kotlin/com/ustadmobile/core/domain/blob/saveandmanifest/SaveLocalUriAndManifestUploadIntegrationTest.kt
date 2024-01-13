package com.ustadmobile.core.domain.blob.saveandmanifest

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.pdf.AbstractPdfContentImportCommonJvm
import com.ustadmobile.core.contentformats.xapi.XapiZipContentImporter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.xfertestnode.XferTestClient
import com.ustadmobile.core.domain.blob.xfertestnode.XferTestNode
import com.ustadmobile.core.domain.blob.xfertestnode.XferTestServer
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentEntryUseCase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.util.test.ext.newFileFromResource
import com.ustadmobile.util.test.initNapierLog
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.test.Test

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


    private fun importAndVerifyManifest(
        contentEntryImportJob: ContentEntryImportJob
    ) {
        val clientNode = XferTestClient(XferTestNode(temporaryFolder, "client"))
        val serverNode = XferTestServer(XferTestNode(temporaryFolder, "server"))
        try {
            val endpoint = Endpoint("http://localhost:${serverNode.port}/")

            val importContentUseCase: ImportContentEntryUseCase = clientNode.di.on(endpoint)
                .direct.instance()

            runBlocking {
                val clientDb: UmAppDatabase = clientNode.di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)
                val jobUid = clientDb.contentEntryImportJobDao.insertJobItem(contentEntryImportJob)

                val entryVersion = importContentUseCase(jobUid)
                clientNode.waitForContentUploadCompletion(endpoint, entryVersion.cevUid)
                val manifestOnClient = clientNode.node.getManifest(entryVersion.cevSitemapUrl!!)

                clientNode.node.assertManifestStoredOnNode(manifestOnClient, entryVersion.cevSitemapUrl!!)
                serverNode.node.assertManifestStoredOnNode(manifestOnClient, entryVersion.cevSitemapUrl!!)
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

}