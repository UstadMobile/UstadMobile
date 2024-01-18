package com.ustadmobile.core.domain.blob

data class BlobTransferProgressUpdate(
    val transferItem: BlobTransferJobItem,
    val bytesTransferred: Long,
)