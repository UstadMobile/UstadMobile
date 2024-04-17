package com.ustadmobile.core.domain.compress.audio

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.net.toFile
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultAudioMixer
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressProgressUpdate
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.ext.requireExtension
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 *
 */
class CompressAudioUseCaseAndroid(
    private val appContext: Context,
    private val uriHelper: UriHelper,
): CompressAudioUseCase {
    @OptIn(UnstableApi::class)
    override suspend fun invoke(
        fromUri: String,
        toUri: String?,
        params: CompressParams,
        onProgress: CompressUseCase.OnCompressProgress?
    ): CompressResult? = withContext(Dispatchers.IO) {
        val destFile = if(toUri != null) {
            Uri.parse(toUri).toFile().requireExtension("mp4")
        }else {
            File(appContext.cacheDir, UUID.randomUUID().toString() + ".mp4")
        }

        val fromDoorUri = DoorUri.parse(fromUri)

        val transformer = Transformer.Builder(appContext)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .setAudioMixerFactory(DefaultAudioMixer.Factory())
            .build()

        val sizeIn = uriHelper.getSize(fromDoorUri)
        val inputMediaItem = MediaItem.fromUri(fromDoorUri.uri)

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
                    Napier.e("CompressAudioUseCaseAndroid: onError: $exportResult", exportException)
                    completeable.completeExceptionally(exportException)
                }

            })

            transformer.start(inputMediaItem, destFile.absolutePath)
        }

        completeable.await()

        Napier.d("CompressAudioUseCaseAndroid: compressed $fromUri from $sizeIn bytes to ${destFile.length()}")
        onProgress?.invoke(CompressProgressUpdate(fromUri, sizeIn, sizeIn))
        val compressedSize = destFile.length()
        if(compressedSize < sizeIn) {
            CompressResult(
                uri = destFile.toDoorUri().toString(),
                mimeType = "audio/mp4",
                originalSize = sizeIn,
                compressedSize = compressedSize,
            )
        }else {
            destFile.delete()
            null
        }
    }
}