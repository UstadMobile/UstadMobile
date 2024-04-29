package com.ustadmobile.core.domain.extractvideothumbnail

import com.ustadmobile.door.DoorUri

interface ExtractVideoThumbnailUseCase {

    data class VideoThumbnailResult(
        val uri: DoorUri,
        val mimeType: String,
    )

    suspend operator fun invoke(
        videoUri: DoorUri,
        position: Float,
        destinationFilePath: String,
    ): VideoThumbnailResult

}