package com.ustadmobile.core.domain.compress.image

import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.ext.requireExtension
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import net.coobird.thumbnailator.Thumbnails
import java.io.File

/**
 * Compress an Image on JVM using ImageIO via Thumbnailator.
 */
class CompressImageUseCaseJvm: CompressUseCase {

    override suspend fun invoke(
        fromUri: String,
        toUri: String?,
        params: CompressParams,
        onProgress: CompressUseCase.OnCompressProgress?
    ): CompressResult {
        //Output file MUST have the expected extension, otherwise ImageIO will fail.
        val outFile = toUri?.let { DoorUri.parse(it).toFile().requireExtension("webp") }
            ?: File.createTempFile("compress-image-out", ".webp")
        Thumbnails
            .fromFiles(listOf(DoorUri.parse(fromUri).toFile()))
            .outputFormat("webp")
            .size(params.maxWidth, params.maxHeight)
            .toFile(outFile)

        return CompressResult(
            uri = outFile.toDoorUri().toString(),
            mimeType = "image/webp"
        )
    }
}