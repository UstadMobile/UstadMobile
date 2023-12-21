package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint

/**
 * Uploads a batch of blobs to the given endpoint:
 *
 *  1)  Initializes the session - gets a list of all the blobs the server already has,
 *      starting indexes, and session uuids (if any). Server should arrange this by creating a
 *      folder for a given batch uuid
 *
 *  2)  Uploads the data by retrieving the blobs from the local cache using ChunkUpload
 */
interface BlobBatchUploadUseCase {

    /**
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
    )

    companion object {

        /**
         * When the client sends the last chunk of a blob, then any http headers from the response
         * as it was stored will be added with with this prefix
         */
        const val BLOB_RESPONSE_HEADER_PREFIX = "X-Blob-Response-"

    }

}