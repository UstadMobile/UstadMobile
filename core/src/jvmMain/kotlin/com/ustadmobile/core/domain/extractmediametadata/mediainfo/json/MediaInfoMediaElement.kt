package com.ustadmobile.core.domain.extractmediametadata.mediainfo.json

import kotlinx.serialization.Serializable


@Serializable
class MediaInfoMediaElement(
    val track: List<MediaInfoTrack>
)
