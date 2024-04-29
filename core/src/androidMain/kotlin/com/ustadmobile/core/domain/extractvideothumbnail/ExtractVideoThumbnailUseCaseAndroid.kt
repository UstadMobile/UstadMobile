package com.ustadmobile.core.domain.extractvideothumbnail

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ExtractVideoThumbnailUseCaseAndroid (
    private val appContext: Context,
): ExtractVideoThumbnailUseCase {

    override suspend fun invoke(
        videoUri: DoorUri,
        position: Float,
        destinationFilePath: String
    ): ExtractVideoThumbnailUseCase.VideoThumbnailResult = withContext(Dispatchers.IO){
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(appContext, videoUri.uri)
        try {
            val duration = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
            val bitmap = retriever.getFrameAtTime(
                ((duration * 1000) * position).toLong(),
                MediaMetadataRetriever.OPTION_CLOSEST
            )
            if(bitmap != null) {
                val destinationFile = File(destinationFilePath)
                destinationFile.parentFile?.takeIf { !it.exists() }?.mkdirs()
                destinationFile.outputStream().use { fileOut ->
                    @Suppress("DEPRECATION")
                    bitmap.compress(
                        if(Build.VERSION.SDK_INT >= 30)
                            Bitmap.CompressFormat.WEBP_LOSSY
                        else
                            Bitmap.CompressFormat.WEBP,
                        80,
                        fileOut
                    )
                    fileOut.flush()
                }
                return@withContext ExtractVideoThumbnailUseCase.VideoThumbnailResult(
                    uri = destinationFile.toDoorUri(),
                    mimeType = "image/webp"
                )
            }

            throw IllegalStateException("Could not get bitmap from $videoUri at position=$position")
        }finally {
            retriever.release()
        }
    }
}