package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.upload.ChunkedUploadClientUseCase

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
     * Run the blob upload itself.
     *
     * @param blobUrls a list of urls (e.g. in the form of http://endpoint.com/api/sha256)
     * @param batchUuid UUID for this batch upload
     * @param endpoint the endpoint that we are uploading to
     * @param onProgress
     */
    suspend operator fun invoke(
        blobUrls: List<String>,
        batchUuid: String,
        endpoint: Endpoint,
        onProgress: (Int) -> Unit,
        chunkSize: Int = ChunkedUploadClientUseCase.DEFAULT_CHUNK_SIZE,
    )

    /**
     * Run the blob upload using data stored in the database as a TransferJob
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