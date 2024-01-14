package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint

/**
 * Client to upload a batch of blobs to the given endpoint:
 *
 *  1)  Initializes the session - gets a list of all the blobs the server already has,
 *      starting indexes, and session uuids (if any). Server should arrange this by creating a
 *      folder for a given batch uuid
 *
 *  2)  Uploads blob data by retrieving it in chunks from the local cache (e.g. lib-cache) and
 *      passing to ChunkedUploadUseCase to upload the blob (in chunks) to the server.
 *
 * This is only used on Android and JVM. It is used by BlobBatchSaveUseCase on Android and Jvm
 * after the blobs have been stored in the local httpcache.
 *
 * This use case implements the client logic. The server side logic is implemented in
 * BlobBatchUploadEndpoint.
 */
interface BlobUploadClientUseCase {

    /**
     * Represents the item to be uploaded
     *
     * @param blobUrl the blob URL.
     * @param transferJobItemUid The TransferJobItem.tjiUid if the upload is connected to a TransferJobItem,
     *        0 otherwise
     * @param lockIdToRelease the id of the cache retention lock to release (if any)
     */
    data class BlobTransferJobItem(
        val blobUrl: String,
        val transferJobItemUid: Int,
        val lockIdToRelease: Int = 0,
    )

    data class BlobUploadProgressUpdate(
        val uploadItem: BlobTransferJobItem,
        val bytesTransferred: Long,
    )

    data class BlobUploadStatusUpdate(
        val uploadItem: BlobTransferJobItem,
        val status: Int,
    )


    /**
     * Run the blob upload itself.
     *
     * @param blobUrls a list of urls (e.g. in the form of http://endpoint.com/api/sha256)
     * @param batchUuid UUID for this batch upload
     * @param endpoint the endpoint that we are uploading to
     * @param onProgress
     */
    suspend operator fun invoke(
        blobUrls: List<BlobTransferJobItem>,
        batchUuid: String,
        endpoint: Endpoint,
        onProgress: (BlobUploadProgressUpdate) -> Unit = { },
        onStatusUpdate: (BlobUploadStatusUpdate) -> Unit = { },
    )

    /**
     * Run the blob upload using data stored in the database as a TransferJob. Progress updates will
     * be saved to the database to the TransferJobItem table.
     *
     * @param transferJobUid the uid of the TransferJob to run
     */
    suspend operator fun invoke(
        transferJobUid: Int,
    )


    companion object {

        /**
         * When the client sends the last chunk of a blob, then any http headers from the response
         * as it was stored will be added with with this prefix
         */
        const val BLOB_RESPONSE_HEADER_PREFIX = "X-Blob-Response-"

        const val BLOB_UPLOAD_HEADER_BATCH_UUID = "Blob-Upload-Batch-Uuid"

    }

}