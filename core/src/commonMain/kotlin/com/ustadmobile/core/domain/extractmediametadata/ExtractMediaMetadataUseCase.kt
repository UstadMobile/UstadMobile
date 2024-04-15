package com.ustadmobile.core.domain.extractmediametadata

import com.ustadmobile.door.DoorUri


interface ExtractMediaMetadataUseCase {

    /**
     * @param duration media duration in ms
     * @param hasVideo
     * @param storageWidth Width of video (which may vary from the display width)
     * @param storageHeight Height of video (which may vary from the display height)
     * @param aspectRatio Aspect ratio of the video (if any)
     */
    data class MediaMetaData(
        val duration: Long,
        val hasVideo: Boolean,
        val storageWidth: Int,
        val storageHeight: Int,
        val aspectRatio: Float,
    )

    suspend operator fun invoke(
        uri: DoorUri
    ): MediaMetaData

}