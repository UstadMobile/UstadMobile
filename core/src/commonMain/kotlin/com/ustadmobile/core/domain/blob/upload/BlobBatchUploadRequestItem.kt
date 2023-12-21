package com.ustadmobile.core.domain.blob.upload

import kotlinx.serialization.Serializable

/**
 * Represents an individual blob that the client wants to upload
 */
@Serializable
data class BlobBatchUploadRequestItem(
    val blobUrl: String,
    val size: Long,
)
