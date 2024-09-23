package com.ustadmobile.core.contentformats.media

import kotlinx.serialization.Serializable

@Serializable
data class SubtitleTrack(
    val uri: String,
    val mimeType: String,
    val langCode: String?,
    val title: String,
)
