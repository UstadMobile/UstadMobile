package com.ustadmobile.core.domain.compress

import kotlinx.serialization.Serializable

@Serializable
data class CompressParams(
    val maxWidth: Int = 1280,
    val maxHeight: Int = 1280,
    val quality: Int = QUALITY_MEDIUM,
) {
    companion object {

        const val QUALITY_MEDIUM = 3

    }
}

