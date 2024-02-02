package com.ustadmobile.core.domain.validatevideofile

import android.content.Context
import android.media.MediaMetadataRetriever
import com.ustadmobile.door.DoorUri
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ValidateVideoFileUseCaseAndroid(
    private val appContext: Context
) : ValidateVideoFileUseCase{

    override suspend fun invoke(videoUri: DoorUri): Boolean = withContext(Dispatchers.IO) {
        val metaRetriever = MediaMetadataRetriever()
        try {
            metaRetriever.setDataSource(appContext, videoUri.uri)
            val videoHeight = metaRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            videoHeight > 0
        }catch(e: Throwable) {
            Napier.w("ValidateVideoFileUseCaseAndroid: Exception validating $videoUri")
            false
        }finally {
            metaRetriever.release()
        }
    }
}