package com.ustadmobile.core.domain.extractmediametadata

import java.io.File

interface ExtractMediaMetadataUseCase {

    data class MediaMetaData(
        val duration: Long,
        val hasVideo: Boolean,
    )

    suspend operator fun invoke(
        file: File
    ): MediaMetaData

}