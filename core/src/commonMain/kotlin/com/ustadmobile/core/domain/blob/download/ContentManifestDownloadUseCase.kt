package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

/**
 * ContentManifestDownload will
 *
 * 1) Get the manifest for the given ContentEntryVersion entity
 * 2) Enqueue the download of everything in the manifest
 *
 * This will throw an exception if it is not possible to actually fetch the manifest.
 */
class ContentManifestDownloadUseCase(
    private val enqueueBlobDownloadClientUseCase: EnqueueBlobDownloadClientUseCase,
    private val db: UmAppDatabase,
    private val httpClient: HttpClient,
    private val json: Json,
) {

    /**
     *
     * @param contentEntryVersionUid the uid for the ContentEntryVersion that should be downloaded
     * @param transferJobUid the TransferJobUid that is in use for this for this download. This
     *        TransferJob must be created by EnqueueDownloadContentManifestUseCase
     */
    suspend operator fun invoke(
        contentEntryVersionUid: Long,
        transferJobUid: Int,
    ) {
        val contentEntryVersion = db.contentEntryVersionDao
            .findByUidAsync(contentEntryVersionUid)
                ?: throw IllegalArgumentException("ContentEntryVersion $contentEntryVersionUid not in db")

        val manifestUrl = contentEntryVersion.cevManifestUrl!!
        val manifest: ContentManifest = json.decodeFromString(httpClient.get(manifestUrl).bodyAsText())
        enqueueBlobDownloadClientUseCase(
            items = manifest.entries.map {
                EnqueueBlobDownloadClientUseCase.EnqueueBlobDownloadItem(
                    url = it.bodyDataUrl,
                    expectedSize = it.storageSize,
                    entityUid = contentEntryVersion.cevUid,
                    tableId = ContentEntryVersion.TABLE_ID,
                )
            },
            existingTransferJobId = transferJobUid,
        )
    }

    companion object {

        const val DEFAULT_MAX_ATTEMPTS = 5

    }

}