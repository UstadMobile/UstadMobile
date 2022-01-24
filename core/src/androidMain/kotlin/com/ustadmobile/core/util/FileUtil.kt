package com.ustadmobile.core.util

import androidx.core.net.toUri
import com.ustadmobile.core.io.ext.makeTempDir
import com.ustadmobile.door.DoorUri

actual fun createTemporaryDir(prefix: String): DoorUri {
    return DoorUri(makeTempDir(prefix).toUri())
}