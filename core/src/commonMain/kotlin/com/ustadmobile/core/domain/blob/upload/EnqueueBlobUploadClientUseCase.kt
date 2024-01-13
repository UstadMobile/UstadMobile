package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCaseKtorImpl
import com.ustadmobile.lib.db.entities.TransferJob

/**
 * Enqueue a blob upload. Running it is done by WorkManager on Android and by Quartz on JVM,
 * which will use BlobUploadClientUseCase. Android can use the WorkManager connectivity constraint
 * to run the upload when connectivity is next available (even if the app has been closed in the
 * meantime).
 *
 * Normally we want to trigger replication of an associated entity (e.g. PersonPicture,
 * ContentEntryVersion, etc) once all the related blobs have been uploaded. In order for this to work,
 * the TransferJobItem.tjiEntityEtag should be set to the value of @ReplicateEtag for the associated
 * entity. This is done by UpdateTransferJobItemEtagUseCase.
 *
 * UpdateTransferJobItemEtagUseCase should be updated when adding a new entity type that is connected
 * to blob upload.
 */
interface EnqueueBlobUploadClientUseCase{

    /**
     * Blob to enqueue for upload
     *
     * @param blobUrl the URL of the blob to be uploaded (mandatory)
     * @param tableId the table id of the table that should be updated on the server when the
     *                upload is complete if desired, otherwise zero. See TransferJobItem.tjiTableId
     * @param entityUid the entity uid of the entity that should be updated on the server when the
     *                 upload is complete if desired, otherwise zero. See TransferJobItem.tjiEntityUid
     * @param retentionLockIdToRelease if a retention lock was created (which happens on Android/Desktop)
     *        to ensure the item is retained until upload is complete, then this is the lock id.
     */
    data class EnqueueBlobUploadItem(
        val blobUrl: String,
        val tableId: Int = 0,
        val entityUid: Long = 0,
        val retentionLockIdToRelease: Int = 0,
    )

    /**
     * Enqueue the upload and return the transferjob. The transferjob's uid can be used to track
     * progress etc.
     */
    suspend operator fun invoke(
        items: List<EnqueueBlobUploadItem>,
        batchUuid: String,
        chunkSize: Int = ChunkedUploadClientUseCaseKtorImpl.DEFAULT_CHUNK_SIZE,
    ): TransferJob

}