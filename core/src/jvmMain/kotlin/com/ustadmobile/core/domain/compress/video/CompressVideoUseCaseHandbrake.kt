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

/**
 * Compress a video using HandBrakeCLI
 *
 * TODO: Check handling of non-English file names and files that contain spaces
 *
 * Windows is supposed to support this:
 *   https://learn.microsoft.com/en-us/windows/uwp/audio-video-camera/transcode-media-files
 *   https://blogs.windows.com/windowsdeveloper/2018/06/06/c-console-uwp-applications/
 *
 * Unfortunately, there is no straightforward way to access this from Java/Kotlin land.
 *
 */
class CompressVideoUseCaseHandbrake(
    private val handbrakePath: String = "/usr/bin/HandBrakeCLI",
    private val workingDir: File,
    private val extractMediaMetadataUseCase: ExtractMediaMetadataUseCase,
    private val json: Json,
): CompressVideoUseCase {

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


    //Bitrate to use in bps
    private val CompressionLevel.videoBitRate: Int
        get() = when(this) {
            CompressionLevel.HIGH -> 170_000
            CompressionLevel.MEDIUM -> 500_000
            CompressionLevel.LOW -> 2_000_000
            else -> -1
        }

    //Bitrate to use in bps
    private val CompressionLevel.audioBitRate: Int
        get() = when(this) {
            CompressionLevel.HIGH -> 64_000
            CompressionLevel.MEDIUM -> 128_000
            CompressionLevel.LOW -> 192_000
            else -> -1
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
        val maxMajor = when(this) {
            CompressionLevel.HIGH -> 480
            CompressionLevel.MEDIUM -> 720
            CompressionLevel.LOW -> 1280
            else -> -1
        }

        val (outputWidth, outputHeight) = when {
            isPortrait && inputWidth > maxMajor -> Pair(maxMajor.toFloat(), maxMajor / storageAspectRatio)
            !isPortrait && inputHeight > maxMajor -> Pair(maxMajor / storageAspectRatio, maxMajor.toFloat())
            else -> Pair(inputWidth.toFloat(), inputHeight.toFloat())
        }

        return buildList {
            add("--maxWidth")
            add(outputWidth.toString())
            add("--maxHeight")
            add(outputHeight.toString())
            add("--vb")
            add("${videoBitRate / 1000}")
            add("--ab")
            add("${audioBitRate / 1000}")
            add("--rate")

            when(this@handbrakeParams) {
                CompressionLevel.HIGH ->  add("12")
                else -> add("30")
            }
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
        val mediaInfo = extractMediaMetadataUseCase(fromFile)
        if(!mediaInfo.hasVideo) {
            throw IllegalArgumentException("${fromFile.absolutePath} has no video")
        }

        val kiloBytesPerSecond = (params.compressionLevel.videoBitRate + params.compressionLevel.audioBitRate)/8
        val expectedSize = (mediaInfo.duration / 1000) * kiloBytesPerSecond

        val fromFileSize = fromFile.length()
        if(expectedSize >= (fromFileSize * COMPRESS_THRESHOLD)) {
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
            File.createTempFile(UUID.randomUUID().toString(), ".mp4")
        }

        try {
            val process = ProcessBuilder(
                buildList {
                    add(handbrakePath)
                    add("-i")
                    add(fromFile.absolutePath)
                    add("-o")
                    add(destFile.absolutePath)
                    addAll(listOf("--encoder", "x264", "--aencoder", "av_aac"))
                    addAll(params.compressionLevel.handbrakeParams(
                        mediaInfo.storageWidth, mediaInfo.storageHeight
                    ))
                    add("--json")
                })
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .directory(workingDir)
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
            CompressResult(
                uri = destFile.toDoorUri().toString(),
                mimeType = "video/mp4"
            )
        }catch(e: Throwable) {
            throw e
        }
    }

    companion object {
        const val COMPRESS_THRESHOLD = 0.95f
    }
}