package com.ustadmobile.core.shrink

import kotlinx.serialization.Serializable

@Serializable
data class ShrinkConfig(
    val videoQuality: Quality = Quality.MEDIUM,
    val audioQuality: Quality = Quality.MEDIUM,
    val imageQuality: Quality = Quality.MEDIUM,
    val maxResolution: Dimension = Dimension(1920, 1080)

) {

    @Serializable
    enum class Quality {
        LOW, MEDIUM, HIGH
    }



}