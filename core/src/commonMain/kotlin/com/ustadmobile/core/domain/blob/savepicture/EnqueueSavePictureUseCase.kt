package com.ustadmobile.core.domain.blob.savepicture

/**
 *
 * Enqueues work (via WorkManager on Android, Quartz on JVM, ? on JS) to upload a picture. This is
 * used by the ViewModels when the user saves.
 *
 * On Android/JVM, flow is as follows:
 *
 * 1) Enqueue a WorkManager/Quartz job to run SavePictureUseCase
 * 2) SavePictureUseCase will process (e.g. compress/resize) the picture and saved it as a blob.
 *    Then calls EnqueueBlobUploadClientUseCase which will do the upload (when using WorkManager
 *    on Android we can set the connectivity constraint requirement)
 * 3) BlobUploadClientUseCase actually uploads the picture. The TransferJobItem will be linked to
 *    the tableId and entityId. By default, on completion, the BlobUploadClientUseCase will insert
 *    an OutgoingReplication so that the new Uri is replicated upon successful completion of the
 *    transfer.
 *
 */
interface EnqueueSavePictureUseCase {

    /**
     * Enqueue saving the given picture. The entity (as per entityUid and tableId) MUST already exist.
     * It will be updated as the the picture is processed (compressed/uploaded) as required.
     *
     * This should be invoked AFTER the completion of the transaction in which the entity itself
     * was committed has completed.
     */
    suspend operator fun invoke(
        entityUid: Long,
        tableId: Int,
        pictureUri: String?,
    )

    companion object {

        const val DATA_LEARNINGSPACE = "endpoint"

        const val DATA_TABLE_ID = "tableId"

        const val DATA_ENTITY_UID = "entityUid"

        const val DATA_LOCAL_URI = "localUri"

    }

}