package com.ustadmobile.core.contentformats.media

import kotlinx.serialization.Serializable

@Serializable
class MediaSource(
    val uri: String,
    val mimeType: String,
)

