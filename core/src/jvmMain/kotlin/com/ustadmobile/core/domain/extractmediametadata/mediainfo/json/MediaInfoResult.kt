package com.ustadmobile.core.domain.extractmediametadata.mediainfo.json

import kotlinx.serialization.Serializable


@Suppress("unused")
@Serializable
class MediaInfoResult(
    val creatingLibrary: MediaInfoCreatingLibrary? = null,
    val media: MediaInfoMediaElement? = null,
)
