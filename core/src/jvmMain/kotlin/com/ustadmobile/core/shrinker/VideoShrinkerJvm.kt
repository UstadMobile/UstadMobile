package com.ustadmobile.core.shrinker

import com.ustadmobile.core.shrink.ShrinkConfig
import com.ustadmobile.core.shrink.ShrinkProgressListener
import com.ustadmobile.core.shrink.Shrinker
import com.ustadmobile.door.DoorUri

class VideoShrinkerJvm: Shrinker {
    override suspend fun shrink(
        srcUri: DoorUri,
        destinationUri: DoorUri,
        config: ShrinkConfig,
        progressListener: ShrinkProgressListener?
    ) {
        TODO("Not yet implemented")
    }
}