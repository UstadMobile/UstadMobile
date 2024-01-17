package com.ustadmobile.core.domain.blob


data class BlobTransferStatusUpdate(
    val transferItem: BlobTransferJobItem,
    val status: Int,
)