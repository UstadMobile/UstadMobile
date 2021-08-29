package com.ustadmobile.core.contentjob

import com.ustadmobile.door.DoorUri
import kotlin.jvm.Volatile

data class ProcessContext(
        val tempDirUri: DoorUri,
        @Volatile
        var localUri: DoorUri? = null,
        val params: MutableMap<String, String>
)