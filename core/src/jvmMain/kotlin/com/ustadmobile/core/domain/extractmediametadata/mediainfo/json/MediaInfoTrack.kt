package com.ustadmobile.core.domain.extractmediametadata.mediainfo.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Suppress("unused")
@Serializable
class MediaInfoTrack(
    @SerialName("@type")
    val type: String,
    @SerialName("VideoCount")
    val videoCount: String? = null,
    @SerialName("AudioCount")
    val audioCount: String? = null,
    @SerialName("Format")
    val format: String? = null,
    @SerialName("Duration")
    val duration: String? = null,
    @SerialName("Width")
    val width: String? = null,
    @SerialName("Height")
    val height: String? = null,
    @SerialName("DisplayAspectRatio")
    val displayAspectRatio: String? = null,
)
