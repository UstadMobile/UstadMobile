package com.ustadmobile.core.domain.compress.video.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Progress(
    @SerialName("State")
    val state: String? = null,
    @SerialName("Working")
    val working: Working? = null,
)
