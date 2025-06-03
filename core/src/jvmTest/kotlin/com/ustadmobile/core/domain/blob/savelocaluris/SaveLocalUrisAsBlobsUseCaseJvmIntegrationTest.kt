package com.ustadmobile.core.domain.blob.savelocaluris

import com.ustadmobile.core.domain.blob.BlobTransferJobItem
import com.ustadmobile.core.domain.blob.upload.BlobUploadClientUseCaseJvm
import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCaseKtorImpl
import com.ustadmobile.core.io.ext.readSha256
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import okhttp3.Request
import okhttp3.internal.headersContentLength
import org.junit.Test
import java.net.URLEncoder
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration test of uploading a local URI from a client to the server.
 */
class SaveLocalUrisAsBlobsUseCaseJvmIntegrationTest : AbstractSaveLocalUrisIntegrationTest() {

    @BeforeTest
    override fun setup() {
        super.setup()
    }

    @AfterTest
    fun tearDown() {
        clientCache.close()
        ktorServer.stop()
    }

    @Test
    fun givenLocalUris_whenInvoked_thenBlobsAreUploadedAndCanBeRetrievedViaCache() {
        val pdfFile = temporaryFolder.newFileFromResource(javaClass,
            "/com/ustadmobile/core//container/validPDFMetadata.pdf",
            "validPDFMetadata.pdf"
        )

        val saveLocalUrisAsBlobsUseCase = SaveLocalUrisAsBlobsUseCaseJvm(
            learningSpace = learningSpace,
            cache = clientCache,
            uriHelper = uriHelper,
            tmpDir = Path(clientRootTmpDir.absolutePath),
            deleteUrisUseCase = clientDeleteUrisUseCase,
        )

        val blobUploadClientUseCase = BlobUploadClientUseCaseJvm(
            chunkedUploadUseCase = ChunkedUploadClientUseCaseKtorImpl(httpClient, uriHelper),
            httpClient = httpClient,
            json = json,
            httpCache = clientCache,
            db = mockUmAppDatabase,
            repo = mockUmAppDatabase,
            learningSpace = learningSpace,
        )

        val blobsToSave = listOf(
            SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                localUri = pdfFile.toDoorUri().toString(),
                entityUid = 42,
                tableId = 1,
                deleteLocalUriAfterSave = true,
            )
        )

        runBlocking {
            val savedBlobs = saveLocalUrisAsBlobsUseCase(
                localUrisToSave = blobsToSave,
            )

            blobUploadClientUseCase(
                blobUrls = savedBlobs.map {
                    BlobTransferJobItem(it.blobUrl, transferJobItemUid = 0)
                },
                batchUuid = UUID.randomUUID().toString(),
                learningSpace = learningSpace,
                onProgress = {

                }
            )
        }

        //Should add a check on the client cache.
        val sha256 = pdfFile.inputStream().use { it.readSha256() }.encodeBase64()
        val blobHttpResponse = okHttpClient.newCall(
            Request.Builder()
                .url("${learningSpace.url}api/blob/${URLEncoder.encode(sha256, "UTF-8")}")
                .build()
        ).execute()

        assertEquals(200, blobHttpResponse.code)
        assertEquals("application/pdf", blobHttpResponse.header("content-type"))
        assertTrue(blobHttpResponse.header("cache-control")?.contains("immutable") == true)
        assertEquals(pdfFile.length(), blobHttpResponse.headersContentLength())
        val blobBodyBytes = blobHttpResponse.body!!.bytes()
        assertTrue(pdfFile.readBytes().contentEquals(blobBodyBytes))
        assertEquals(0, clientRootTmpDir.list()!!.size,
            "Client root temporary directory should be empty")
        assertEquals(0, serverRootTmpDir.list()!!.size,
            "Server root temporary directory should be empty")
    }
}