package com.ustadmobile.core.shrink

import com.ustadmobile.door.DoorUri

interface ShrinkProgressListener {

    fun onProgress(
        srcUri: DoorUri,
        destUri: DoorUri,
        progress: Int,
        total: Int,
    )

}