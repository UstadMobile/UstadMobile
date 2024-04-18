package com.ustadmobile.core.domain.compress.audio

import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.domain.compress.CompressionLevel
import com.ustadmobile.core.util.ext.isWindowsOs
import com.ustadmobile.core.util.ext.waitForAsync
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.util.SysPathUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Convert audio to ogg using SOX command.
 */
class CompressAudioUseCaseJvm(
    private val soxPath: String = "/usr/bin/sox",
    private val workDir: File,
): CompressAudioUseCase {

    /**
     * Quality constants as per
     * https://en.wikipedia.org/wiki/Vorbis
     */
    private val CompressionLevel.vorbisQuality: Int
        get() = when(this) {
            CompressionLevel.HIGHEST -> -1
            CompressionLevel.HIGH -> 0
            CompressionLevel.MEDIUM -> 2
            CompressionLevel.LOW -> 4
            CompressionLevel.LOWEST -> 6
            else -> throw IllegalArgumentException()
        }

    override suspend fun invoke(
        fromUri: String,
        toUri: String?,
        params: CompressParams,
        onProgress: CompressUseCase.OnCompressProgress?
    ): CompressResult? = withContext(Dispatchers.IO) {
        val inFile = DoorUri.parse(fromUri).toFile()

        val destFile = if(toUri != null) {
            DoorUri.parse(toUri).toFile()
        }else {
            File(workDir, UUID.randomUUID().toString())
        }

        val cmd = listOf(
            soxPath, inFile.absolutePath,
            "-C", params.compressionLevel.vorbisQuality.toString(),
            "--type", "ogg",
            destFile.absolutePath
        )

        val process = ProcessBuilder(cmd)
            .directory(workDir)
            .start()

        val result = process.waitForAsync()
        if(result != 0) {
            throw IllegalStateException("SOX returned non-zero exit value: $result")
        }

        val originalSize = inFile.length()
        val compressedSize = destFile.length()

        if(compressedSize < originalSize) {
            CompressResult(
                uri = destFile.toDoorUri().toString(),
                mimeType = "audio/ogg",
                compressedSize = compressedSize,
                originalSize = originalSize,
            )
        }else {
            destFile.delete()
            null
        }
    }

    companion object {
        fun findSox() : File? = SysPathUtil.findCommandInPath(
            commandName = "sox",
            extraSearchPaths = if(isWindowsOs()) {
                File(System.getenv("PROGRAMFILES(x86)"),"sox-14-4-2").absolutePath
            }else {
                ""
            }
        )
    }
}