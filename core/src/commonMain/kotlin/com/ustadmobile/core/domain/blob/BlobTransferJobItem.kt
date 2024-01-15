package com.ustadmobile.core.domain.blob

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