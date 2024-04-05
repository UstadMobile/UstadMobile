package com.ustadmobile.core.domain.validatevideofile

import com.ustadmobile.core.domain.extractmediametadata.ExtractMediaMetadataUseCase
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile

/**
 * Use the MediaInfo command to validate if a file is really a video. In theory the VLC command
 * could get some info, however the documentation is not up to date and results are unreliable.
 *
 */
class ValidateVideoFileUseCaseMediaInfo(
    private val extractMediaMetadataUseCase: ExtractMediaMetadataUseCase,
): ValidateVideoFileUseCase {

    override suspend fun invoke(
        videoUri: DoorUri
    ): Boolean = extractMediaMetadataUseCase(videoUri.toFile()).hasVideo

}