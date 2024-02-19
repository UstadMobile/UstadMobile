package com.ustadmobile.core.domain.blob.download

import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.bodyAsDecodedText
import com.ustadmobile.lib.db.entities.CacheLockJoin
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

/**
 * ContentManifestDownload will
 *
 * 1) Get the manifest for the given ContentEntryVersion entity
 * 2) Create CacheLockJoin entities to ensure that downloaded items are retained
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
        val manifestResponse = httpClient.get(manifestUrl)
        val manifestSize = manifestResponse.headers["content-length"]?.toLong() ?: 0
        val manifest: ContentManifest = json.decodeFromString(manifestResponse.bodyAsDecodedText())

        val offlineItemUid = db.transferJobDao.findOfflineItemUidForTransferJobUid(
            transferJobUid)

        /*
         * Remove duplicate bodyDataUrls - this happens when a ContentManifest contains two or more
         * entries that contain the same data, e.g. when the original zip had two or more identical
         * files in different locations.
         */
        val bodyDataUrlsAndStorageSizeToDownload = manifest.entries.map {
            Pair(it.bodyDataUrl, it.storageSize)
        }.distinctBy { it.first }

        if(offlineItemUid != 0L) {
            db.cacheLockJoinDao.insertListAsync(
                (bodyDataUrlsAndStorageSizeToDownload + Pair(manifestUrl, manifestSize)).map {
                    CacheLockJoin(
                        cljEntityUid = contentEntryVersionUid,
                        cljTableId = ContentEntryVersion.TABLE_ID,
                        cljType = CacheLockJoin.TYPE_OFFLINE_ITEM,
                        cljUrl = it.first,
                        cljOiUid = offlineItemUid,
                        cljStatus = CacheLockJoin.STATUS_PENDING_CREATION
                    )
                }
            )
        }

        enqueueBlobDownloadClientUseCase(
            items = bodyDataUrlsAndStorageSizeToDownload.map {
                EnqueueBlobDownloadClientUseCase.EnqueueBlobDownloadItem(
                    url = it.first,
                    expectedSize = it.second,
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