package com.ustadmobile.core.domain.compress.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.ustadmobile.core.domain.compress.CompressParams
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
): CompressUseCase {
    override suspend fun invoke(
        fromUri: String,
        toUri: String?,
        params: CompressParams,
        onProgress: CompressUseCase.OnCompressProgress?
    ): CompressResult {
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
                Napier.d("CompressImageUseCaseAndroid: dest file = ${destFile.absolutePath}")

                @Suppress("DEPRECATION") //Must use Deprecated CompressFormat.WEBP on SDK < 30
                val resultFile = Compressor.compress(applicationContext, file) {
                    destination(destFile)

                    default(
                        format = if(Build.VERSION.SDK_INT >= 30) {
                            Bitmap.CompressFormat.WEBP_LOSSY
                        }else {
                            Bitmap.CompressFormat.WEBP
                        },
                        width = params.maxWidth,
                        height = params.maxHeight,
                        quality = 80,
                    )
                }

                CompressResult(
                    uri = resultFile.toUri().toString(),
                    mimeType = "image/webp"
                )
            }
        }finally {
            tmpInputFile.takeIf { it.exists() }?.delete()
        }
    }
}