package com.ustadmobile.core.shrinker

import com.ustadmobile.core.shrink.ShrinkConfig
import com.ustadmobile.core.shrink.ShrinkProgressListener
import com.ustadmobile.core.shrink.Shrinker
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile

class VideoShrinkerJvm: Shrinker {
    override suspend fun shrink(
        srcUri: DoorUri,
        destinationUri: DoorUri,
        config: ShrinkConfig,
        progressListener: ShrinkProgressListener?,
    ) {
        val srcFile = srcUri.toFile()
        val destFile = srcUri.toFile()

        //TODO here: use ffmpeg to compress --> done
        // TODO: need to discuss config implementation details

        val pb = ProcessBuilder(
            "ffmpeg",
            "-i",
            srcFile.absolutePath.toString(),
            "-vf",
            "-quality",
            "good",
            "-c:v",
            "libvpx-vp9",
            "libvorbis",
            destFile.absolutePath.toString()
        )

        pb.redirectErrorStream(true)
        val process = pb.start()
        process.waitFor()
        // progressListener = pb.start()
        // progressListener.waitFor()


    }


}