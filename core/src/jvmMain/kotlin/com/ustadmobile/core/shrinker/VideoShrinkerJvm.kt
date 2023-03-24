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
        progressListener: ShrinkProgressListener? = null,
    ) {
        val srcFile = srcUri.toFile()
        val destFile = srcUri.toFile()

        //TODO here: use ffmpeg to compress
        TODO("Not yet implemented")
    }
}