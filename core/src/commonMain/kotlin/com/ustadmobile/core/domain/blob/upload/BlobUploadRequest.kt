package com.ustadmobile.core.domain.blob.upload

import kotlinx.serialization.Serializable

/**
 * A request from the client to the server upload a batch of blobs
 *
 * @param blobs a list of blobs that the clients wants to upload
 * @param batchUuid a UUID (randomly generated by the client) used to identify this upload batch and
 *        facilitate resumption.
 */
@Serializable
data class BlobUploadRequest(
    val blobs: List<BlobUploadRequestItem>,
    val batchUuid: String,
)