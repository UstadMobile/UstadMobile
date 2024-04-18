package com.ustadmobile.core.domain.extractmediametadata

import android.content.Context
import android.media.MediaMetadataRetriever
import com.ustadmobile.door.DoorUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExtractMediaMetadataUseCaseAndroid(
    private val appContext: Context,
): ExtractMediaMetadataUseCase {

    override suspend fun invoke(
        uri: DoorUri
    ): ExtractMediaMetadataUseCase.MediaMetaData = withContext(Dispatchers.IO) {
        val metaRetriever = MediaMetadataRetriever()

        try {
            metaRetriever.setDataSource(appContext, uri.uri)
            val storageWidth = metaRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val storageHeight = metaRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val duration = metaRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
            val aspectRatio = if(storageWidth > 0 && storageHeight > 0) {
                storageWidth.toFloat() / storageHeight.toFloat()
            }else {
                0f
            }

            ExtractMediaMetadataUseCase.MediaMetaData(
                duration = duration,
                hasVideo = storageHeight > 0 && storageWidth > 0,
                storageWidth = storageWidth,
                storageHeight = storageHeight,
                aspectRatio = aspectRatio,
            )
        }finally {
            metaRetriever.release()
        }
    }

}