package com.ustadmobile.core.domain.compress.image

import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.ext.requireExtension
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.coobird.thumbnailator.Thumbnails
import java.io.File

/**
 * Compress an Image on JVM using ImageIO via Thumbnailator.
 */
class CompressImageUseCaseJvm: CompressImageUseCase {

    override suspend fun invoke(
        fromUri: String,
        toUri: String?,
        params: CompressParams,
        onProgress: CompressUseCase.OnCompressProgress?
    ): CompressResult? = withContext(Dispatchers.IO){
        val fileIn = DoorUri.parse(fromUri).toFile()
        if(fileIn.length() < COMPRESS_MIN_SIZE)
            return@withContext null

        //Output file MUST have the expected extension, otherwise ImageIO will fail.
        val outFile = toUri?.let { DoorUri.parse(it).toFile().requireExtension("webp") }
            ?: File.createTempFile("compress-image-out", ".webp")
        Thumbnails
            .fromFiles(listOf(DoorUri.parse(fromUri).toFile()))
            .outputFormat("webp")
            .size(params.maxWidth, params.maxHeight)
            .toFile(outFile)

        val compressedSize = outFile.length()
        val originalSize = fileIn.length()

        if(compressedSize < originalSize) {
            CompressResult(
                uri = outFile.toDoorUri().toString(),
                mimeType = "image/webp",
                compressedSize = compressedSize,
                originalSize = originalSize,
            )
        }else {
            //Whatever we did - we made it bigger. Delete attempt and return null
            outFile.delete()
            null
        }
    }

    companion object {

        const val COMPRESS_MIN_SIZE = 4_000

    }
}