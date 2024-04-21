package com.ustadmobile.core.domain.compress.audio

import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.domain.compress.CompressionLevel
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExecuteMediaInfoUseCase
import com.ustadmobile.core.util.ext.isWindowsOs
import com.ustadmobile.core.util.ext.waitForAsync
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.util.SysPathUtil
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Convert audio to ogg using SOX command.
 *
 * On Windows: The Sox command does not come with support for MP3. There does not appear to be a
 * maintained, reliable version of the libmad binary for Windows that would be required for SoX
 * to be able to read an MP3.
 *
 * We can use mpg123 on Windows to convert an mp3 to wav, which sox can decode. mpg123 can decode
 * to wav by running: mpg123 --wav wavfile.wav inputfile.mp3
 *
 * Mpg123 is bundled with the desktop version for Windows. If running the server on Windows, it can
 * be downloaded from the Mpg123 website as per the main README file.
 */
class CompressAudioUseCaseSox(
    private val soxPath: String = "/usr/bin/sox",
    private val executeMediaInfoUseCase: ExecuteMediaInfoUseCase,
    private val mpg123Path: String? = null,
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

        val mpg123OutputFile = if(mpg123Path != null &&
            executeMediaInfoUseCase(inFile).media?.track?.any {
                it.format?.equals("MPEG Audio", true) == true
            } == true
        ) {
            val wavFile = File(workDir, "${UUID.randomUUID()}.wav")
            val mpg123Command = listOf(
                mpg123Path, "--wav", wavFile.absolutePath, inFile.absolutePath
            )

            Napier.v {
                "CompressAudioUseCaseSox: Running mpg123 : ${mpg123Command.joinToString(separator = " ")}"
            }

            val mpg123Result = ProcessBuilder(mpg123Command)
                .directory(workDir)
                .start()
                .waitForAsync()

            if(mpg123Result != 0) {
                Napier.w { "CompressAudioUseCaseSox: mpg123 returned non-zero for $inFile" }
                return@withContext null
            }

            wavFile
        }else {
            null
        }

        val destFile = if(toUri != null) {
            DoorUri.parse(toUri).toFile()
        }else {
            File(workDir, UUID.randomUUID().toString())
        }

        val cmd = listOf(
            soxPath, (mpg123OutputFile?.absolutePath ?: inFile.absolutePath),
            "-C", params.compressionLevel.vorbisQuality.toString(),
            "--type", "ogg",
            destFile.absolutePath
        )

        Napier.v { "CompressAudioUseCase: Running sox: ${cmd.joinToString(separator =  " ")}" }

        val process = ProcessBuilder(cmd)
            .directory(workDir)
            .start()

        val result = process.waitForAsync()

        mpg123OutputFile?.delete()

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