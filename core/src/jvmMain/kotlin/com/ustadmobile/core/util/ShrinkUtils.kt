package com.ustadmobile.core.util

import io.github.aakira.napier.Napier
import com.ustadmobile.core.catalog.contenttype.VideoTypePlugin
import com.ustadmobile.core.io.ext.readString
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

object ShrinkUtils {

    fun getVideoResolutionMetadata(
        srcFile: File,
        ffprobePath: File
    ): Triple<Int, Int, String?> {

        val builder = ProcessBuilder(ffprobePath.absolutePath, "-v",
                "error", "-select_streams", "v:0",
                "-show_entries", "stream=width,height,display_aspect_ratio",
                "-of", "default=nw=1:nk=1", srcFile.path)

        var process: Process? = null
        try {
            process = builder.start()

            val stream = BufferedReader(InputStreamReader(process.inputStream))
            val width = stream.readLine()
            val height = stream.readLine()

            //ffmpeg might return N/A if a ratio is not specified by the file. We need to validate
            //the input here so we don't give an invalid parameter to ffmpeg.
            val ratio = validateRatio(stream.readLine())

            stream.close()
            process.waitFor()
            val exitValue = process.exitValue()
            if (exitValue != 0) {
                Napier.e("Error Stream for src " + srcFile.path + process.errorStream.readString())
                return Triple(0, 0, "")
            }
            process.destroy()
            return Triple(width.toInt(), height.toInt(), ratio)
        } catch (e: InterruptedException) {
            Napier.e("ffprobe process interrupted", e)
        } finally {
            process?.destroy()
        }
        return Triple(0, 0, "")
    }


    /**
     * Validate a ratio string that should be in the form of "x:y" where x and y are positive,
     * non-zero integers. ffprobe might return N/A or something other than a valid aspect ratio,
     * so this input needs validated.
     *
     * @return the ratioStr trimmed if it is valid, null otherwise
     */
    fun validateRatio(ratioStr: String): String? {
        val parts = ratioStr.trim().split(':')
        if(parts.size != 2)
            return null //not valid

        val partInts = parts.map { it.toIntOrNull() }
        return if(partInts.all { it != null && it > 0 }) {
            ratioStr.trim()
        }else {
            null
        }
    }


    fun optimiseVideo(
        srcVideo: File,
        destFile: File,
        ffmpegPath: File,
        resolution: Pair<Int, Int>,
        aspectRatio: String?,
    ) {
        val ffmpegCommand = mutableListOf(ffmpegPath.absolutePath, "-i",
                srcVideo.path, "-vf", "scale=${resolution.first}x${resolution.second}")

        if(aspectRatio != null) {
            ffmpegCommand +=  listOf("-aspect", aspectRatio)
        }

        ffmpegCommand += listOf("-framerate", VideoTypePlugin.VIDEO_FRAME_RATE.toString(),
                "-c:v", "libx264", "-b:v", VideoTypePlugin.VIDEO_BIT_RATE.toString(),
                "-c:a", "aac", "-b:a", VideoTypePlugin.AUDIO_BIT_RATE.toString(),
                "-vbr", "on", "-y", destFile.path)

        val builder = ProcessBuilder(ffmpegCommand)

        builder.redirectErrorStream(true)
        var process: Process? = null
        try {
            process = builder.start()
            process.inputStream.readBytes()
            process.waitFor()
            val exitValue = process.exitValue()
            if (exitValue != 0) {
                val errorStreamStr =process.errorStream.readString()
                val errorMsg = "Non-zero exit value: $exitValue running " +
                        "'${ffmpegCommand.joinToString(separator = " ")}'. ErrorStream=$errorStreamStr"
                Napier.e(errorMsg)
                throw IOException(errorMsg)
            }
            process.destroy()
        } catch (e: IOException) {
            throw e
        } catch (e: InterruptedException) {
            Napier.e("ffmpeg process interrupted", e)
        } finally {
            process?.destroy()
        }

    }
}

