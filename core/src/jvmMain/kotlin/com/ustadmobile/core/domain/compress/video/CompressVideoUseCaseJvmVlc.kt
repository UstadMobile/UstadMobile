package com.ustadmobile.core.domain.compress.video

import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.ext.requireExtension
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.util.UUID

/**
 * Compress a video using VLC command line
 */
class CompressVideoUseCaseJvmVlc(
    private val vlcPath: String = "/usr/bin/vlc",
    private val workingDir: File,
): CompressUseCase {

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
        val fromFile = DoorUri.parse(fromUri).toFile()
        val destFile = if(toUri != null) {
            DoorUri.parse(toUri).toFile().requireExtension("mp4")
        }else {
            File.createTempFile(UUID.randomUUID().toString(), ".mp4")
        }

        try {
            /*
             * Note the single quotes shown in command line examples must be removed. Those would be
             * removed by the shell when run on the real command line
             */
            val args = listOf(
                vlcPath, "-I", "dummy", "--no-repeat", "--no-loop", "-vv",
                fromFile.absolutePath,
                        "--sout=#transcode{vcodec=h264,acodec=mp4a,vb=800,ab=128,deinterlace}:standard{access=file,mux=mp4,dst=${destFile.absolutePath}}",
                "vlc://quit"
            )
            println(args.joinToString(separator = " "))
            val process = ProcessBuilder(args)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .directory(workingDir)
                .start()
            val outputReader = launchReader(process.inputStream.bufferedReader())
            val errReader = launchReader(process.errorStream.bufferedReader())

            process.waitFor()

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

}