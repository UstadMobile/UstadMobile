package com.ustadmobile.core.domain.compress.image

import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.wrappers.compressorjs.Compressor
import io.github.aakira.napier.Napier
import js.objects.jso
import js.promise.await
import kotlinx.coroutines.CompletableDeferred
import web.blob.Blob
import web.http.fetchAsync
import web.url.URL

/**
 * Implementation of image compression for Javascript. Uses compressorjs.
 */
class CompressImageUseCaseJs : CompressImageUseCase {
    override suspend fun invoke(
        fromUri: String,
        toUri: String?,
        params: CompressParams,
        onProgress: CompressUseCase.OnCompressProgress?
    ): CompressResult {
        val completeable = CompletableDeferred<Blob>()
        Napier.d("CompressImageUseCaseJs: compressing $fromUri")
        try {
            val blob = fetchAsync(fromUri).await().blob().await()
            Compressor(blob, jso {
                maxWidth = params.maxWidth
                maxHeight = params.maxHeight

                //compressorjs format conversion is ONLY to JPEG
                // As per https://github.com/fengyuanchen/compressorjs/tree/main#converttypes
                convertSize = 50_000
                convertTypes = arrayOf("image/png")

                success = {
                    Napier.d("CompressImageUseCaseJs: compressing $fromUri : success")
                    completeable.complete(it)
                }
                error = {
                    Napier.e("CompressImageUseCaseJs: compressing $fromUri : error", it)
                    completeable.completeExceptionally(it)
                }
            })

            val compressedBlob = completeable.await()
            val resultUri = URL.createObjectURL(compressedBlob)
            return CompressResult(
                uri = resultUri,
                mimeType = compressedBlob.type,
                originalSize = blob.size.toLong(),
                compressedSize = compressedBlob.size.toLong(),
            )
        }catch(e: Throwable) {
            Napier.e("CompressImageUseCase: Exception caught: ", e)
            throw e
        }
    }
}