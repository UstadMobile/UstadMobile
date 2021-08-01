package com.ustadmobile.core.contentjob

import com.ustadmobile.door.DoorUri

data class ProcessContext(
        val tempDirUri: DoorUri,
        var localUri: DoorUri? = null,
        val params: MutableMap<String, String>
)