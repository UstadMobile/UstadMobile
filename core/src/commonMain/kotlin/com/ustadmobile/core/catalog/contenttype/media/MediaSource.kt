package com.ustadmobile.core.catalog.contenttype.media

import kotlinx.serialization.Serializable

@Serializable
class MediaSource(
    val url: String,
    val mimeType: String,
)

