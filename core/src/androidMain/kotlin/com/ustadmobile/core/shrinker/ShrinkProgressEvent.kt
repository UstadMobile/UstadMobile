package com.ustadmobile.core.shrinker

import com.ustadmobile.door.DoorUri

class ShrinkProgressEvent(
    val sourceUri: DoorUri,
    val outputUri: DoorUri,
    val shrinkParams: ShrinkParams,
    val progress: Int,
    val total: Int,
) {
}