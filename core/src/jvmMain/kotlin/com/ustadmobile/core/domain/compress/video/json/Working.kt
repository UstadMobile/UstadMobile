package com.ustadmobile.core.domain.compress.video.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Working(
    @SerialName("Progress")
    val progress: Float = 0f,
)
