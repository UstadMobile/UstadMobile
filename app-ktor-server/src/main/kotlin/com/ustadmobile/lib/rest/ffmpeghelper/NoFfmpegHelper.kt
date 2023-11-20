package com.ustadmobile.lib.rest.ffmpeghelper

import com.ustadmobile.lib.rest.ext.ktorAppHomeFfmpegDir
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

private fun ZipFile.extractEntryToFile(
    entry: ZipEntry,
    destFile: File
) {
    getInputStream(entry).use { inStream ->
        FileOutputStream(destFile).use { fileOut ->
            inStream.copyTo(fileOut)
        }
    }
}


/**
 * Handle when the user has no ffmpeg installed.
 *
 * On Ubuntu: we just tell them to install the package
 * On Windows: we will offer to download ffmpeg
 */
@OptIn(ExperimentalPathApi::class)
@Suppress("NewApi") //This is JVM only
fun handleNoFfmpeg(
    ffmpegDestDir: File
) {
    val osName = System.getProperty("os.name") ?: ""

    if(osName.lowercase().contains("win")) {
        //Windows - offer to install it
        System.err.print("Error: FFMPEG not installed or not found in Path. It is required to process " +
                "video and audio. Would you like to download it now? (y/n) ")
        val sysInReader = System.`in`.reader().buffered()
        val userResponse = sysInReader.readLine()
        if(userResponse.lowercase().startsWith("y")) {
            /*
             * It would be nice to use the smaller, 7zip file, but Apache compress does not support
             * it a feature used in the 7z file for ffmpeg:
             *  https://issues.apache.org/jira/browse/COMPRESS-431
             */
            val url = "https://github.com/GyanD/codexffmpeg/releases/download/2023-11-05-git-44a0148fad/ffmpeg-2023-11-05-git-44a0148fad-full_build.zip"

            val okHttpClient = OkHttpClient.Builder().build()
            val tmpDir = Files.createTempDirectory("ustad-ffmpeg-tmp")
            val tmpFile = tmpDir.resolve("ffmpeg-2023-11-05-git-44a0148fad-full_build.zip")
            try {
                val request = Request.Builder().url(url).build()
                val response = okHttpClient.newCall(request).execute()
                System.err.println("Downloading $url ...")
                response.body?.source()?.use {
                    val sink = FileSystem.SYSTEM.sink(tmpFile.toOkioPath())
                    it.readAll(sink)
                }
                System.err.println("Done. Saved to $tmpFile")
                ZipFile(tmpFile.toFile()).use { ffmpegZip ->
                    val entryList = ffmpegZip.entries().toList()

                    val licenseEntry = entryList.first {
                        it.name.endsWith("/LICENSE")
                    }
                    System.err.println("===FFMPEG LICENSE===")
                    ffmpegZip.getInputStream(licenseEntry).use { licenseIn ->
                        licenseIn.copyTo(System.err)
                    }

                    System.err.print("Do you accept the FFMPEG license (y/n)? ")

                    if(sysInReader.readLine().startsWith("y")) {
                        listOf("ffmpeg.exe", "ffprobe.exe").forEach { filename ->
                            ffmpegZip.extractEntryToFile(
                                entry = entryList.first { it.name.endsWith(filename) },
                                destFile = File(ffmpegDestDir, filename)
                            )
                        }
                        System.err.println("Done! Extracted to $ffmpegDestDir")
                    }else {
                        System.err.println("Ffmpeg license declined! Cannot continue.")
                    }
                }
            }catch(e: Throwable) {
                System.err.println("Sorry, error attempting to download ffmpeg.: ${e.message}")
                e.printStackTrace()
            }finally {
                tmpDir.deleteRecursively()
            }
        }else {
            System.err.println("OK, You need to download the ffmpeg and ffprobe commands, and put them " +
                    "in one of the following locations:")
            System.err.println("1) ${ktorAppHomeFfmpegDir()}")
            System.err.println("2) Within your PATH environment variable")
            System.err.println("3) Somewhere else specified within the server config file")
            System.err.println("After you download ffmpeg and ffprobe, you can try starting the server again.")
        }

    }else {
        System.err.println("Error: FFMPEG not installed, or path not specified.")
        System.err.println("On Ubuntu, you can use apt-get install ffmpeg.")
    }
}