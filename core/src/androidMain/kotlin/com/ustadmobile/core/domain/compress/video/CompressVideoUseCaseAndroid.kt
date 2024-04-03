package com.ustadmobile.core.domain.compress.video

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.net.toFile
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressProgressUpdate
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.domain.compress.CompressionLevel
import com.ustadmobile.core.ext.requireExtension
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID


/**
 * On desktop: see https://gist.github.com/belachkar/c35fd2c4832d841970035b06019f3558
 */
class CompressVideoUseCaseAndroid(
    private val appContext: Context,
    private val uriHelper: UriHelper,
): CompressUseCase {

    //As per https://developer.android.com/media/platform/supported-formats#video-encoding
    /**
     * https://developer.android.com/reference/android/media/MediaExtractor#getTrackFormat(int)
     *  See sar-width / sar-height : https://developer.android.com/reference/android/media/MediaExtractor
     *
     *
     *  https://android-developers.googleblog.com/2023/05/media-transcoding-and-editing-transform-and-roll-out.html
     *
     */
//    fun CompressionLevel.videoStrategy(
//        displayDimensions: DisplayDimensions?
//    ) : DefaultVideoStrategy {
////        val displayDimensionStrategy = displayDimensions?.let {
////            val aspectRatio =  it.height.toFloat() / it.width.toFloat()
////            DefaultVideoStrategy.aspectRatio(aspectRatio)
////        }
//        val displayDimensionStrategy = DefaultVideoStrategy.exact(480, 853)
//
//
//        return when(this) {
//            CompressionLevel.HIGH -> (displayDimensionStrategy ?: DefaultVideoStrategy.atMost(360, 640)) // Recommendation is 176x144 - but this doesn't work
//                .bitRate(56_000L)
//                .frameRate(12)
//                .mimeType("video/avc")
//                .build()
//            CompressionLevel.MEDIUM -> (displayDimensionStrategy ?: DefaultVideoStrategy.atMost(360, 640))
//                .bitRate(500_000L)
//                .frameRate(30)
//                .mimeType("video/avc")
//                .build()
//            CompressionLevel.LOW -> (displayDimensionStrategy ?: DefaultVideoStrategy.atMost(720, 1280))
//                .bitRate(2_000_000L)
//                .frameRate(30)
//                .mimeType("video/avc")
//                .build()
//
//            CompressionLevel.NONE -> throw IllegalArgumentException("")
//
//        }
//
//    }


    @OptIn(UnstableApi::class)
    override suspend fun invoke(
        fromUri: String,
        toUri: String?,
        params: CompressParams,
        onProgress: CompressUseCase.OnCompressProgress?
    ): CompressResult {
        //As per https://developer.android.com/media/platform/supported-formats


        val destFile = if(toUri != null) {
            Uri.parse(toUri).toFile().requireExtension("mp4")
        }else {
            File(appContext.cacheDir, UUID.randomUUID().toString() + ".mp4")
        }

        val sizeIn = uriHelper.getSize(DoorUri.parse(fromUri))

        val inputMediaItem = MediaItem.fromUri(Uri.parse(fromUri))

        //https://developer.android.com/media/media3/transformer/transformations
//        val editedMediaItem = EditedMediaItem.Builder(inputMediaItem)
//            .setEffects(
//                Effects(
//                    /* audio processors */ emptyList(),
//                    /* video processors */ listOf(Presentation.createForWidthAndHeight(853, 480, LAYOUT_STRETCH_TO_FIT)),
//                )
//            )
//            .build()
        val videoEncoderSettings = VideoEncoderSettings.Builder()
            .setBitrate(500_000)
            .build()
        val encoderFactory = DefaultEncoderFactory.Builder(appContext)
            .setRequestedVideoEncoderSettings(videoEncoderSettings)
            .build()


        val transformer = Transformer.Builder(appContext)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setEncoderFactory(encoderFactory)
            .build()

        val completeable = CompletableDeferred<ExportResult>()

        withContext(Dispatchers.Main) {
            transformer.addListener(object: Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    completeable.complete(exportResult)
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    Napier.e("CompressVideoUseCaseAndroid: onError: $exportResult", exportException)
                    completeable.completeExceptionally(exportException)
                }

            })

            transformer.start(inputMediaItem, destFile.absolutePath)

            val progressUpdateJob = launch {
                val progressHolder = ProgressHolder()
                while(isActive) {
                    delay(500)
                    transformer.getProgress(progressHolder)
                    progressHolder.progress
                    onProgress?.invoke(
                        CompressProgressUpdate(
                            fromUri = fromUri,
                            completed = ((progressHolder.progress.toFloat() / 100f) * sizeIn).toLong(),
                            total = sizeIn
                        )
                    )
                }
            }

            try {
                completeable.await()
            }catch(e: Exception) {
                if(e is CancellationException) {
                    withContext(NonCancellable + Dispatchers.Main) {
                        transformer.cancel()
                    }
                }

                throw e
            }finally {
                progressUpdateJob.cancel()
            }
        }

        return CompressResult(
            uri = destFile.toDoorUri().toString(),
            mimeType = "video/mp4"
        )
    }
}