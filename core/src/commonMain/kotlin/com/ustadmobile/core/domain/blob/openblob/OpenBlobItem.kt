package com.ustadmobile.core.domain.blob.openblob


data class OpenBlobItem(
    val uri: String,
    val mimeType: String,
    val fileName: String,
    val fileSize: Long,
)