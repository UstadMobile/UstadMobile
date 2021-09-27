package com.ustadmobile.core.contentjob

import com.ustadmobile.door.DoorUri
import kotlin.jvm.Volatile

data class ProcessContext(
        val tempDirUri: DoorUri,
        val params: MutableMap<String, String>
){
        @Volatile
        var localUri: DoorUri? = null
}