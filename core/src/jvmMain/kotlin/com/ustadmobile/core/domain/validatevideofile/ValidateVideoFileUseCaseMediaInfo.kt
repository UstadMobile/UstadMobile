package com.ustadmobile.core.domain.validatevideofile

import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.cachestoragepath.getLocalUriIfRemote
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
    private val getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase,
): ValidateVideoFileUseCase {

    override suspend fun invoke(
        videoUri: DoorUri
    ): Boolean {
        val localUri = getStoragePathForUrlUseCase.getLocalUriIfRemote(videoUri)
        val file = localUri.toFile()
        return if(file.exists()) {
            extractMediaMetadataUseCase(file).hasVideo
        }else {
            false
        }
    }

}