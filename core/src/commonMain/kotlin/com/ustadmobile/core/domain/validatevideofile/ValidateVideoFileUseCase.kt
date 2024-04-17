package com.ustadmobile.core.domain.validatevideofile

import com.ustadmobile.core.domain.extractmediametadata.ExtractMediaMetadataUseCase
import com.ustadmobile.door.DoorUri
import io.github.aakira.napier.Napier

/**
 * UseCase that will validate a given file and determine if it actually a video. Used with importing.
 * Can be implemented by using FFProbe or MediaInfo on JVM (server and desktop), and on Android via
 * Android media APIs
 */
class ValidateVideoFileUseCase(
    private val extractMediaMetadataUseCase: ExtractMediaMetadataUseCase
) {

    /**
     *
     * @param videoUri URI to the video to check
     */
    suspend operator fun invoke(videoUri: DoorUri): Boolean {
        return try {
            extractMediaMetadataUseCase(videoUri).hasVideo
        }catch(e: Throwable){
            //Do nothing - not video
            Napier.w("ExtractMediaUseCase: exception occurred so seems like not video, returning false")
            false
        }
    }


}