package com.ustadmobile.core.domain.compress.video

import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressProgressUpdate
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.domain.compress.CompressionLevel
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.util.UUID

/**
 * Compress a video using VLC command line
 *
 * This is, unfortunately, for the moment, a fail. VLC transcoding output is buggy: output videos
 * open in Ubuntu's default video player and Chrome OK, but then don't play the first 30 seconds
 * when opened in VLC itself or Firefox.
 */
class CompressVideoUseCaseJvmVlc(
    private val vlcPath: String = "/usr/bin/vlc",
    private val workingDir: File,
    private val extractMediaMetadataUseCase: ExtractMediaMetadataUseCase,
): CompressVideoUseCase {

    /**
     * VLC generates a lot of command line output. Needs to be read to avoid process being blocked
     */
    private fun CoroutineScope.launchReader(bufferedReader: BufferedReader): Job = launch {
        bufferedReader.use {
            it.lines().forEach {
                println(it)
            }
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
    private fun CompressionLevel.vlcParams(
        inputWidth: Int,
        inputHeight: Int,
    ): String {
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

        val bitRateParams = "vb=${videoBitRate/1000},ab=${audioBitRate/1000}"
        val widthHeightParams = "width=${outputWidth.toInt()},height=${outputHeight.toInt()}"

        return when {
            this == CompressionLevel.HIGH -> "fps=12,$bitRateParams,$widthHeightParams"

            this == CompressionLevel.MEDIUM -> "fps=30,$bitRateParams,$widthHeightParams"

            this == CompressionLevel.LOW  -> "fps=30,$bitRateParams,$widthHeightParams"

            else -> ""
        }
    }


    /**
     * Use the VLC command line to compress a video as per
     * https://wiki.videolan.org/Transcode/#Command-line
     */
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

        if(expectedSize >= (fromFile.length() * COMPRESS_THRESHOLD)) {
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
            /*
             * Note the single quotes shown in command line examples from the VLC reference must be
             * removed. They would be removed by the shell when run on the real command line
             */
            val args = listOf(
                vlcPath, "-I", "dummy", "--no-repeat", "--no-loop", "-vv",
                fromFile.absolutePath,
                        "--sout=#transcode{vcodec=h264,acodec=mp4a,deinterlace," +
                        "${params.compressionLevel.vlcParams(mediaInfo.storageWidth, mediaInfo.storageHeight)}}" +
                        ":standard{access=file,mux=mp4,dst=${destFile.absolutePath}}",
                "vlc://quit"
            )
            Napier.i("CompressVideoUseCase: Running vlc: ${args.joinToString(separator = " ")}")
            val process = ProcessBuilder(args)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .directory(workingDir)
                .start()
            val outputReader = launchReader(process.inputStream.bufferedReader())
            val errReader = launchReader(process.errorStream.bufferedReader())

            val updateJob = launch {
                while (isActive) {
                    onProgress?.invoke(
                        CompressProgressUpdate(
                            fromUri = fromUri,
                            completed = ((destFile.length().toFloat() / expectedSize) * fromFileSize).toLong(),
                            total = fromFileSize
                        )
                    )

                    delay(200)
                }
            }

            process.waitForAsync()

            updateJob.cancel()
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