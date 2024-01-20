package com.ustadmobile.core.domain.blob.download

/**
 * Calling EnqueueDownloadContentManifestUseCase will create a TransferJob with exactly one
 * TransferJobItem for the manifestUrl.
 *
 * The flow for downloading content is as follows:
 *
 * 1) This use case will create a TransferJob and TransferJobItem for the Manifest, and use the
 *    underlying platform scheduling mechanism (quartz/workmanager) to run DownloadContentManifestUseCase
 *
 * 2) DownloadContentManifestUseCase will fetch the ContentManifest itself, and then add TransferJobItem(s)
 *    for all items referenced in the manifest. It will then use EnqueueBlobDownloadClientUseCase to
 *    enqueue the download of all required items.
 *
 * 3) BlobDownloadClientUseCase will fetch all items, pulling them into the cache (via the lib-cache
 *    okhttp interceptor).
 *
 */
interface EnqueueContentManifestDownloadUseCase {

    /**
     * Used to enqueue the download of a ContentEntryVersion for offline use as per the manifest. This
     * will create a TransferJob and TransferJobItem for the manifest url itself (which allows
     * OfflineItemsDownloadEnqueuer to spot what needs enqueued and what has already been enqueued)
     */
    suspend operator fun invoke(
        contentEntryVersionUid: Long
    )

}

