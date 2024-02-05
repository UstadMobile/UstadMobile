package com.ustadmobile.core.domain.blob

data class BlobTransferProgressUpdate(
    val transferItem: BlobTransferJobItem,
    val bytesTransferred: Long,
) {

    override fun toString(): String {
        return "Uid #${transferItem.transferJobItemUid} transferred=${bytesTransferred} bytes"
    }

}