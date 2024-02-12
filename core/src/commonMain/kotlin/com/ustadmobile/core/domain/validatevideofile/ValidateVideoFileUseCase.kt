package com.ustadmobile.core.domain.validatevideofile

import com.ustadmobile.door.DoorUri

/**
 * UseCase that will validate a given file and determine if it actually a video. Used with importing.
 * Can be implemented by using FFProbe or MediaInfo on JVM (server and desktop), and on Android via
 * Android media APIs
 */
interface ValidateVideoFileUseCase {

    /**
     *
     * @param videoUri URI to the video to check
     */
    suspend operator fun invoke(videoUri: DoorUri): Boolean


}