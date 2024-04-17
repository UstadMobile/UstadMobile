package com.ustadmobile.core.domain.compress.video

import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressProgressUpdate
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.domain.compress.CompressionLevel
import com.ustadmobile.core.domain.compress.video.json.Progress
import com.ustadmobile.core.domain.extractmediametadata.ExtractMediaMetadataUseCase
import com.ustadmobile.core.ext.requireExtension
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.waitForAsync
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.util.UUID
import kotlin.math.roundToInt

/**
 * Compress a video using HandBrakeCLI. Videos will be encoded as MP4 using AV1 as the video codec
 * and Opus as the audio codec. HandBrake 1.6.0+ is required.
 *
 * Handbrake settings: see
 *    https://handbrake.fr/docs/en/latest/workflow/adjust-quality.html#:~:text=Recommended%20settings%20for%20the%20SVT,for%201080p%20Full%20High%20Definition
 *
 * Using HandBrakeCLI itself from the command line (Windows and Linux) is supported. Using the
 * flatpak version is also supported.
 *
 * IMPORTANT: FlatPak does not allow access to the /tmp directory on Linux.
 *
 * Windows UWP is supposed to support this:
 *   https://learn.microsoft.com/en-us/windows/uwp/audio-video-camera/transcode-media-files
 *   https://blogs.windows.com/windowsdeveloper/2018/06/06/c-console-uwp-applications/
 *
 * Unfortunately, there is no straightforward way to access that from Java/Kotlin land.
 */
class CompressVideoUseCaseHandbrake(
    private val handbrakeCommand: List<String>,
    private val workDir: File,
    private val extractMediaMetadataUseCase: ExtractMediaMetadataUseCase,
    private val json: Json,
): CompressVideoUseCase {

    /**
     * @param videoBitRate guesstimate video bitrate. The actual bitrate will vary depending on how
     *        complex the video is. We only set the quality setting, we do not directly set the video
     *        bitrate. This is used to guess when compression is unlikely to yield a smaller file
     * @param audioBitRate audio bitrate to use (audio codec will always be opus)
     * @param quality the quality setting to pass to Handbrake
     * @param maxMajor the max major resolution
     * @param maxMinor the max minor resolution
     * @param frameRate the framerate setting
     */
    data class HandbrakeCompressionLevelParams(
        val videoBitRate: Int,
        val audioBitRate: Int,
        val quality: Int,
        val maxMajor: Int,
        val maxMinor: Int,
        val frameRate: Int,
    )

    /**
     * When using --json on the command line, then progress will be output. Unfortunately, the progress
     * JSON is not all on one line. A JSON begins with 'Progress: {' and then ends with a line that
     * contains only a single '}'.
     */
    private fun CoroutineScope.launchHandbrakeOutputReader(
        bufferedReader: BufferedReader,
        onProgress: (Progress) -> Unit,
    ): Job = launch {
        var jsonStr = StringBuilder()
        var inProgressLines = false

        try {
            bufferedReader.use { reader ->
                reader.lines().forEach { line ->
                    when {
                        line.startsWith("Progress:") -> {
                            jsonStr.append("{")
                            inProgressLines = true
                        }
                        inProgressLines && line == "}" -> {
                            inProgressLines = false
                            jsonStr.append("}")

                            try {
                                val progressJsonEntity = json.decodeFromString<Progress>(jsonStr.toString())
                                onProgress(progressJsonEntity)
                            }catch(e: Throwable) {
                                //do nothing - was not a progress line we wanted
                            }

                            jsonStr = StringBuilder()
                        }
                        inProgressLines -> jsonStr.append(line)
                    }
                }
            }
        }catch(e: Throwable) {
            Napier.d { "launchHandbrakeOutputReader: Exception, maybe process was canceled? ${e.message}" }
        }
    }


    private val CompressionLevel.params: HandbrakeCompressionLevelParams
        get() = when(this){
            //Output is roughly 1.1MB per minute of video
            CompressionLevel.HIGHEST -> HandbrakeCompressionLevelParams(
                videoBitRate = 130_000,
                audioBitRate = 32_000,
                quality = 55,
                maxMajor = 480,
                maxMinor = 360,
                frameRate = 15,
            )
            //Output is roughly 2MB per minute of video
            CompressionLevel.HIGH -> HandbrakeCompressionLevelParams(
                videoBitRate = 190_000,
                audioBitRate = 48_000,
                quality = 55,
                maxMajor = 480,
                maxMinor = 360,
                frameRate = 30,
            )
            //Output is roughly 3MB per minute of video
            CompressionLevel.MEDIUM -> HandbrakeCompressionLevelParams(
                videoBitRate = 300_000,
                audioBitRate = 96_000,
                quality = 55,
                maxMajor = 720,
                maxMinor = 480,
                frameRate = 30,
            )
            //Output roughly 5MB per minute of video
            CompressionLevel.LOW -> HandbrakeCompressionLevelParams(
                videoBitRate = 600_000,
                audioBitRate = 128_000,
                quality = 45,
                maxMajor = 720,
                maxMinor = 480,
                frameRate = 30,
            )
            //Roughly 11-12MB per minute of video
            CompressionLevel.LOWEST -> HandbrakeCompressionLevelParams(
                videoBitRate = 1_400_000,
                audioBitRate = 196_000,
                quality = 40,
                maxMajor = 1280,
                maxMinor = 720,
                frameRate = 30,
            )
            else -> throw IllegalArgumentException("Cannot create params for no compression")
        }


    /**
     * Whe the storage size and display size vary, the VLC transcoder preserves the display aspect
     * ratio information. The width and height passed to the VLC command to encode should therefor
     * maintain the same aspect ratio as per the input storage width and storage height.
     *
     * @param inputWidth the width of the original video (storage width)
     * @param inputHeight the height of the original video (storage height)
     */
    private fun CompressionLevel.handbrakeParams(
        inputWidth: Int,
        inputHeight: Int,
    ): List<String> {
        val isPortrait = inputWidth > inputHeight
        val storageAspectRatio = inputWidth.toFloat() / inputHeight.toFloat()
        val params = this.params
        val maxMajor = params.maxMajor

        val (outputWidth, outputHeight) = when {
            isPortrait && inputWidth > maxMajor -> Pair(maxMajor.toFloat(), maxMajor / storageAspectRatio)
            !isPortrait && inputHeight > maxMajor -> Pair(maxMajor / storageAspectRatio, maxMajor.toFloat())
            else -> Pair(inputWidth.toFloat(), inputHeight.toFloat())
        }

        return buildList {
            add("--maxWidth")
            add(outputWidth.roundToInt().toString())
            add("--maxHeight")
            add(outputHeight.roundToInt().toString())
            add("--quality")
            add("${params.quality}")
            add("--ab")
            add("${params.audioBitRate / 1000}")
            add("--rate")
            add("${params.frameRate}")
        }
    }


    override suspend fun invoke(
        fromUri: String,
        toUri: String?,
        params: CompressParams,
        onProgress: CompressUseCase.OnCompressProgress?
    ): CompressResult? = withContext(Dispatchers.IO) {
        //fromUri will always be a file here. VideoContentImporter will use path to the cached version
        //if required
        val fromFile = DoorUri.parse(fromUri).toFile()
        val mediaInfo = extractMediaMetadataUseCase(
            DoorUri.parse(fromUri)
        )
        if(!mediaInfo.hasVideo) {
            throw IllegalArgumentException("${fromFile.absolutePath} has no video")
        }
        val compressParams = params.compressionLevel.params

        val kiloBytesPerSecond = (compressParams.videoBitRate + compressParams.audioBitRate)/8
        val expectedSize = (mediaInfo.duration / 1000) * kiloBytesPerSecond

        val fromFileSize = fromFile.length()
        if((expectedSize * COMPRESS_THRESHOLD) >= (fromFileSize)) {
            Napier.d {
                "CompressVideoUseCaseJvmVlc: expected size of " +
                        "${UMFileUtil.formatFileSize(expectedSize)} is not within threshold to compress. " +
                        "Original size = ${UMFileUtil.formatFileSize(fromFile.length())}."
            }

            return@withContext null
        }

        val destFile = if(toUri != null) {
            DoorUri.parse(toUri).toFile().requireExtension("mp4")
        }else {
            File(workDir, UUID.randomUUID().toString() + ".mp4")
        }

        try {
            val command = buildList {
                addAll(handbrakeCommand)
                add("-i")
                add(fromFile.absolutePath)
                add("-o")
                add(destFile.absolutePath)
                add("--format")
                add("av_mp4")
                addAll(listOf("--encoder", "svt_av1", "--aencoder", "opus"))
                addAll(params.compressionLevel.handbrakeParams(
                    mediaInfo.storageWidth, mediaInfo.storageHeight
                ))
                add("--json")
            }
            Napier.d { "CompressVideoUseCase: running ${command.joinToString(separator = " ")} " }
            val process = ProcessBuilder(command)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .directory(workDir)
                .start()

            val onProgressUpdate: (Progress) -> Unit = {
                val progressPct = it.working?.progress
                if(progressPct != null) {
                    onProgress?.invoke(
                        CompressProgressUpdate(
                            fromUri = fromUri,
                            completed = (progressPct * fromFileSize).toLong(),
                            total = fromFileSize,
                        )
                    )
                }
            }

            val outputReader = launchHandbrakeOutputReader(
                process.inputStream.bufferedReader(),
                onProgress = onProgressUpdate,
            )
            val errReader = launchHandbrakeOutputReader(
                process.errorStream.bufferedReader(),
                onProgress = onProgressUpdate,
            )

            process.waitForAsync()

            outputReader.cancel()
            errReader.cancel()

            //Check if the result is smaller than the original. We won't know for sure because
            // more complex video will be bigger
            val compressedFileSize = destFile.length()

            if(compressedFileSize < fromFileSize) {
                CompressResult(
                    uri = destFile.toDoorUri().toString(),
                    mimeType = "video/mp4",
                    originalSize = fromFileSize,
                    compressedSize = compressedFileSize,
                )
            }else {
                //The result was bigger
                Napier.d {
                    "CompressVideoUseCaseHandbrake: result was ${destFile.length()} - bigger " +
                    "than original ${fromFile.length()}. Deleting attempt file ${destFile.absolutePath}"
                }
                destFile.delete()
                null
            }
        }catch(e: Throwable) {
            Napier.w("CompressVideoUseCase: Exception attempting to encode fromUri=$fromUri", e)
            throw e
        }
    }

    companion object {

        const val COMPRESS_THRESHOLD = 0.20f

    }
}