package com.ustadmobile.core.shrink

import kotlinx.serialization.Serializable

@Serializable
data class Dimension(
    val width: Int,
    val height: Int
) {
}