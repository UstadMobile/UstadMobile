package com.ustadmobile.core.shrink

import com.ustadmobile.door.DoorUri

interface Shrinker {

    suspend fun shrink(
        srcUri: DoorUri,
        destinationUri: DoorUri,
        config: ShrinkConfig,
        progressListener: ShrinkProgressListener?
    )

}