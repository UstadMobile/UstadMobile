package com.ustadmobile.core.domain.compress.video

import android.content.Context
import android.media.MediaCodecList
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.net.toFile
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
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
import com.ustadmobile.core.util.UMFileUtil
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
 * Compress Videos on Android using the Jetpack Media3 transcoder. This will always use H264 for the
 * moment because:
 *  1) Jetpack Media3 will always output an MP4 file ( https://developer.android.com/media/media3/transformer/supported-formats ).
 *     An MP4 file cannot contain VP8 or VP9 video.
 *  2) Litr can encode VP9, however it does not handle display width/height vs. storage width/height
 *     and does not have the fallback logic that Jetpack Media3 has.
 *  3) H265 is more efficient and the encoder is available on many devices, and it can be contained
 *     in an MP4 file. H265 playback is not supported on Firefox (due to licensing / patent issues)
 *     and Chrome (on devices without hardware support).
 *  4) AV1 : only Android14+ requires AV1 encoding support.
 *
 * In reality, most videos will be uploaded/added using the Web version or Desktop version from a
 * computer (in both cases the encoding work will be done using the JVM UseCase).
 */
class CompressVideoUseCaseAndroid(
    private val appContext: Context,
    private val uriHelper: UriHelper,
): CompressVideoUseCase {

    @OptIn(UnstableApi::class)
    fun createPresentationHeightByCompressionLevel(
        compressionLevel: CompressionLevel,
        widthIn: Int,
        heightIn: Int,
    ) : Presentation {
        val isHd = compressionLevel.value >= CompressionLevel.MEDIUM.value

        val height = when {
            isHd && widthIn > heightIn -> 720 //HD landscape mode
            isHd -> 1280 // HD portrait mode

            widthIn > heightIn -> 480 //SD landscape mode
            else -> 720 //SD portrait mode
        }

        return Presentation.createForHeight(height)
    }

    @OptIn(UnstableApi::class)
    fun VideoEncoderSettings.Builder.setBitrateForCompressionLevel(
        compressionLevel: CompressionLevel
    ) : VideoEncoderSettings.Builder {
        when(compressionLevel) {
            CompressionLevel.HIGHEST -> setBitrate(170_000)
            CompressionLevel.HIGH -> setBitrate(250_000)
            CompressionLevel.MEDIUM -> setBitrate(500_000)
            CompressionLevel.LOW -> setBitrate(2_000_000)
            CompressionLevel.LOWEST -> setBitrate(5_000_000)
            else -> {
                //do nothing
            }
        }

        return this
    }

    /**
     * Note: Audio bitrates are estimates. Media3 API doesn't seem to have a convenient way to set
     * the audio bitrate.
     */
    private fun CompressionLevel.expectedTotalBitrate(): Int = when(this) {
        CompressionLevel.HIGHEST -> 170_000 + 96_000
        CompressionLevel.HIGH -> 250_000 + 96_000
        CompressionLevel.MEDIUM -> 500_000 + 128_000
        CompressionLevel.LOW -> 2_000_000 + 196_000
        CompressionLevel.LOWEST -> 5_000_000 + 196_000
        else -> -1
    }


    @OptIn(UnstableApi::class)
    override suspend fun invoke(
        fromUri: String,
        toUri: String?,
        params: CompressParams,
        onProgress: CompressUseCase.OnCompressProgress?
    ): CompressResult? {
        //As per https://developer.android.com/media/platform/supported-formats
        //See also: https://developer.android.com/media/optimize/sharing#hdr_to_sdr

        val destFile = if(toUri != null) {
            Uri.parse(toUri).toFile().requireExtension("mp4")
        }else {
            File(appContext.cacheDir, UUID.randomUUID().toString() + ".mp4")
        }

        val sizeIn = uriHelper.getSize(DoorUri.parse(fromUri))
        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(appContext, Uri.parse(fromUri))
        val originalWidth = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
        val originalHeight = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
        //Duration in ms.
        val duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
        metaRetriever.release()

        val expectedSize = (params.compressionLevel.expectedTotalBitrate() / 8) * (duration / 1000)
        if((expectedSize * COMPRESS_THRESHOLD) >= (sizeIn)) {
            Napier.d {
                "CompressVideoUseCaseAndroid: expected size of " +
                        "${UMFileUtil.formatFileSize(expectedSize)} is not within threshold to compress. " +
                        "Original size = ${UMFileUtil.formatFileSize(sizeIn)}."
            }
            return null
        }

        val inputMediaItem = MediaItem.fromUri(Uri.parse(fromUri))

        //https://developer.android.com/media/media3/transformer/transformations
        val presentation = if(originalWidth != 0 && originalHeight != 0) {
            createPresentationHeightByCompressionLevel(params.compressionLevel, originalWidth, originalHeight)
        }else {
            null
        }

        val editedMediaItem = if(presentation != null) {
            EditedMediaItem.Builder(inputMediaItem)
                .setEffects(
                    Effects(
                        /* audio processors */ emptyList(),
                        /* video processors */ listOf(presentation),
                    )
                )
                .build()
        }else {
            null
        }

        val videoEncoderSettings = VideoEncoderSettings.Builder()
            .setBitrateForCompressionLevel(params.compressionLevel)
            .build()
        val encoderFactory = DefaultEncoderFactory.Builder(appContext)
            .setRequestedVideoEncoderSettings(videoEncoderSettings)
            .build()


        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val hasAV1Support = codecList.codecInfos.any {
            it.isEncoder && "video/AV1" in it.supportedTypes
        }
        val videoMimeType = if(hasAV1Support) MimeTypes.VIDEO_AV1 else MimeTypes.VIDEO_H264
        Napier.d { "CompressVideoUseCaseAndroid: will encode using $videoMimeType" }

        val transformer = Transformer.Builder(appContext)
            .setVideoMimeType(videoMimeType)
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

            if(editedMediaItem != null) {
                transformer.start(editedMediaItem, destFile.absolutePath)
            }else {
                transformer.start(inputMediaItem, destFile.absolutePath)
            }

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

        val resultSize = destFile.length()
        return if(resultSize < sizeIn) {
            CompressResult(
                uri = destFile.toDoorUri().toString(),
                mimeType = "video/mp4",
                compressedSize = resultSize,
                originalSize = sizeIn,
            )
        }else {
            destFile.delete()
            null
        }
    }

    companion object {

        const val COMPRESS_THRESHOLD = 0.95f

    }
}