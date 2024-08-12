package com.ustadmobile.core.domain.compress

import kotlinx.serialization.Serializable

@Serializable
data class CompressParams(
    val maxWidth: Int = 1280,
    val maxHeight: Int = 1280,
    val compressionLevel: CompressionLevel = CompressionLevel.MEDIUM,
)


