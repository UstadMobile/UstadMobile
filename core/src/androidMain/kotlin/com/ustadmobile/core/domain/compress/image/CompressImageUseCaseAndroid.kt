package com.ustadmobile.core.domain.compress.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressProgressUpdate
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.ext.requireExtension
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class CompressImageUseCaseAndroid(
    private val applicationContext: Context,
): CompressImageUseCase {

    private fun File.imageDimensions(): Pair<Int, Int> {
        if(Build.VERSION.SDK_INT >= 28) {
            //Try looking at metadata first to see if that can avoid us having to decode entire image
            val metaRetriever = MediaMetadataRetriever()
            try {
                metaRetriever.setDataSource(applicationContext, this.toUri())
                val width = metaRetriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH)?.toIntOrNull() ?: 0
                val height = metaRetriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_IMAGE_HEIGHT)?.toIntOrNull() ?: 0
                if(width > 0 && height > 0)
                    return Pair(width, height)
            }catch(e: Throwable) {
                //Do nothing
            }finally {
                metaRetriever.release()
            }
        }

        val bitmap = BitmapFactory.decodeFile(this.absolutePath)
        return bitmap.width to bitmap.height
    }


    override suspend fun invoke(
        fromUri: String,
        toUri: String?,
        params: CompressParams,
        onProgress: CompressUseCase.OnCompressProgress?
    ): CompressResult? {
        val tmpInputFile = File(applicationContext.cacheDir, UUID.randomUUID().toString())
        try {
            return withContext(Dispatchers.IO) {
                val uri = Uri.parse(fromUri)

                val uriFile = if(uri.scheme == "file") uri.toFile() else null

                /* Note: There is a bug in Compressor: in its Util.kt, if the format is changed, it
                 * uses absolutePath.substringBeforeLast("."). If the file does not have an extension,
                 * this logic goes badly wrong.
                 */
                val file = if(uriFile != null && uriFile.name.contains(".")) {
                    uri.toFile()
                }else {
                    applicationContext.contentResolver.openInputStream(uri)!!.use {inStream ->
                        tmpInputFile.outputStream().use { fileOut ->
                            inStream.copyTo(fileOut)
                            fileOut.flush()
                        }
                    }
                    tmpInputFile
                }

                val destFile = if(toUri != null) {
                    Uri.parse(toUri).toFile().requireExtension("webp")
                }else {
                    File(applicationContext.cacheDir, UUID.randomUUID().toString() + ".webp")
                }

                val (width, height) = file.imageDimensions()

                //Compressor does not consider the original image width and height.
                val compressWidth = if(width > 0) minOf(width, params.maxWidth) else params.maxWidth
                val compressHeight = if(height > 0) minOf(height, params.maxHeight) else params.maxHeight


                @Suppress("DEPRECATION") //Must use Deprecated CompressFormat.WEBP on SDK < 30
                val resultFile = Compressor.compress(applicationContext, file) {
                    destination(destFile)

                    default(
                        format = if(Build.VERSION.SDK_INT >= 30) {
                            Bitmap.CompressFormat.WEBP_LOSSY
                        }else {
                            Bitmap.CompressFormat.WEBP
                        },
                        width = compressWidth,
                        height = compressHeight,
                        quality = 80,
                    )
                }
                val sizeIn = file.length()

                onProgress?.invoke(
                    CompressProgressUpdate(
                        fromUri = fromUri,
                        completed = sizeIn,
                        total = sizeIn,
                    )
                )

                val outputSize = resultFile.length()
                if(outputSize < sizeIn) {
                    Napier.d("CompressImageUseCaseAndroid: compressed $fromUri $sizeIn bytes to ${resultFile.length()} bytes")
                    CompressResult(
                        uri = resultFile.toUri().toString(),
                        mimeType = "image/webp",
                        compressedSize = outputSize,
                        originalSize = sizeIn,
                    )
                }else {
                    Napier.d("CompressImageUseCaseAndroid: result was larger - deleting " +
                            "attempt $fromUri $sizeIn bytes to ${resultFile.length()} bytes")
                    resultFile.delete()
                    null
                }
            }
        }finally {
            tmpInputFile.takeIf { it.exists() }?.delete()
        }
    }
}