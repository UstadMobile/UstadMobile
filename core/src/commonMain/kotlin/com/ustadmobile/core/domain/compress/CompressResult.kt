package com.ustadmobile.core.domain.compress

data class CompressResult(
    val uri: String,
    val mimeType: String,
    val originalSize: Long,
    val compressedSize: Long,
)