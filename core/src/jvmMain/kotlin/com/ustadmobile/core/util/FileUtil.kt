package com.ustadmobile.core.util

import com.ustadmobile.door.DoorUri
import java.nio.file.Files

actual fun createTemporaryDir(prefix: String): DoorUri {
    return DoorUri(Files.createTempDirectory(prefix).toUri())
}
