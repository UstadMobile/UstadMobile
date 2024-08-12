package com.ustadmobile.core.domain.extractmediametadata.mediainfo

import com.ustadmobile.core.domain.extractmediametadata.mediainfo.json.MediaInfoResult

fun MediaInfoResult.duration(): Long {
    return this.media?.track?.maxOfOrNull {
        ((it.duration?.toFloat() ?: 0f) * 1000).toLong()
    } ?: 0
}
