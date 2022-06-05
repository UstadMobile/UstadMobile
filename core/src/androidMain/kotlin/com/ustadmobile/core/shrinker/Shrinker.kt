package com.ustadmobile.core.shrinker

import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.door.DoorUri

fun interface Shrinker {

    suspend fun shrink(
        sourceUri: DoorUri,
        outputUri: DoorUri,
        params: ShrinkParams,
        progressListener: ContentJobProgressListener
    )

}