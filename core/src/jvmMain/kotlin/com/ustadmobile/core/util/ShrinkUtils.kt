package com.ustadmobile.core.util

import com.github.aakira.napier.Napier
import com.ustadmobile.core.catalog.contenttype.VideoTypePlugin
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

object ShrinkUtils {

    fun findInPath(commandName: String, systemPropName: String? = null): String? {

        if(systemPropName != null){
            return if(File(systemPropName).exists()){
                systemPropName
            }else{
                null
            }
        }

        return System.getenv("PATH").split(File.pathSeparator).firstOrNull() {
            File(it, commandName).exists()
        }?.let { "$it/$commandName" }
    }

    fun getVideoResolutionMetadata(srcFile: File): Pair<Int, Int> {

        val ffprobePath = findInPath("ffprobe")

        val builder = ProcessBuilder(ffprobePath, "-v",
                "error", "-select_streams", "v:0",
                "-show_entries", "stream=width,height",
                "-of", "default=nw=1:nk=1", srcFile.path)

        var process: Process? = null
        try {
            process = builder.start()

            val stream = BufferedReader(InputStreamReader(process.inputStream))
            val width = stream.readLine()
            val height = stream.readLine()
            stream.close()
            process.waitFor()
            val exitValue = process.exitValue()
            if (exitValue != 0) {
                Napier.e("Error Stream for src " + srcFile.path + String(UMIOUtils.readStreamToByteArray(process.errorStream)))
                return Pair(0, 0)
            }
            process.destroy()
            return Pair(width.toInt(), height.toInt())
        } catch (e: InterruptedException) {
            Napier.e("ffprobe process interrupted", e)
        } finally {
            process?.destroy()
        }
        return Pair(0, 0)
    }


    fun optimiseVideo(srcVideo: File, destFile: File,
                      resolution: Pair<Int, Int>) {

        val ffmpegPath = findInPath("ffmpeg")

        val builder = ProcessBuilder(ffmpegPath, "-i",
                srcVideo.path, "-vf", "scale=${resolution.first}x${resolution.second}",
                "-framerate", VideoTypePlugin.VIDEO_FRAME_RATE.toString(),
                "-c:v", "libx264", "-b:v", VideoTypePlugin.VIDEO_BIT_RATE.toString(),
                "-c:a", "aac", "-b:a", VideoTypePlugin.AUDIO_BIT_RATE.toString(),
                "-ar", VideoTypePlugin.AUDIO_SAMPLE_RATE.toString(),
                "-ac", VideoTypePlugin.AUDIO_CHANNEL_COUNT.toString(),
                "-vbr", "on", "-y", destFile.path)
        builder.redirectErrorStream(true)
        var process: Process? = null
        try {
            process = builder.start()
            UMIOUtils.readStreamToByteArray(process.inputStream)
            process.waitFor()
            val exitValue = process.exitValue()
            if (exitValue != 0) {
                Napier.e("Error Stream for src " + srcVideo.path + String(UMIOUtils.readStreamToByteArray(process.errorStream)))
                throw IOException()
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

