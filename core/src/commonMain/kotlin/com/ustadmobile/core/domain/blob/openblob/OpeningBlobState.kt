package com.ustadmobile.core.domain.blob.openblob

data class OpeningBlobState(
    val item: OpenBlobItem,
    val bytesReady: Long,
    val totalBytes: Long,
    val error: String? = null,
) {
    val progress: Float
        get() = bytesReady.toFloat() / totalBytes.toFloat()
}

