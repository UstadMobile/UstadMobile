package com.ustadmobile.core.domain.extractmediametadata.mediainfo

import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.cachestoragepath.getLocalUriIfRemote
import com.ustadmobile.core.domain.extractmediametadata.ExtractMediaMetadataUseCase
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExtractMediaMetadataUseCaseMediaInfo (
    private val executeMediaInfoUseCase: ExecuteMediaInfoUseCase,
    private val getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase,
): ExtractMediaMetadataUseCase {

    override suspend fun invoke(
        uri: DoorUri,
    ): ExtractMediaMetadataUseCase.MediaMetaData = withContext(Dispatchers.IO) {
        val localUri = getStoragePathForUrlUseCase.getLocalUriIfRemote(uri)
        val file = localUri.toFile()

        try {
            val mediaInfo = executeMediaInfoUseCase(file)
            val videoTrack = mediaInfo.media?.track?.firstOrNull {
                it.type.equals("video", true)
            }

            ExtractMediaMetadataUseCase.MediaMetaData(
                duration = mediaInfo.duration(),
                hasVideo = videoTrack != null,
                storageHeight = videoTrack?.height?.toInt() ?: -1,
                storageWidth = videoTrack?.width?.toInt() ?: -1,
                aspectRatio = videoTrack?.displayAspectRatio?.toFloat() ?: -1f,
            )
        }catch(e: Throwable) {
            throw e
        }
    }
}