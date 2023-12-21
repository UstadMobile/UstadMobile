package com.ustadmobile.core.domain.blob.upload

import com.ustadmobile.core.account.Endpoint

/**
 * Uploads a blob to the given endpoint: in two steps
 *  1)  Initializes the session - gets a list of all the blobs the server already has,
 *      starting indexes, and session uuids (if any). Server should arrange this by creating a
 *      folder for a given batch uuid
 *
 *  2)  Uploads the data by retrieving the blobs from the local cache using ChunkUpload
 */
interface UploadBlobUseCase {

    suspend operator fun invoke(
        blobUrls: List<String>,
        endpoint: Endpoint,
        onProgress: (Int) -> Unit,
    )

}