package com.ustadmobile.core.contentformats.media

import kotlinx.serialization.Serializable

@Serializable
class MediaSource(
    val url: String,
    val mimeType: String,
)

