package com.ustadmobile.core.domain.blob.upload

import kotlinx.serialization.Serializable

/**
 * Represents a response from the server to the client. This lists which items actually need uploaded
 * (e.g. does not include those that are already in the cache on the server).
 */
@Serializable
data class BlobUploadResponse(
    val blobsToUpload: List<BlobUploadResponseItem>
)

