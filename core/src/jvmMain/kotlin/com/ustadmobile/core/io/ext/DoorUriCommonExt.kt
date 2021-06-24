package com.ustadmobile.core.io.ext

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

actual suspend fun DoorUri.guessMimeType(): String? {
    return Files.probeContentType(Paths.get(this.uri))
}

actual suspend fun DoorUri.getSize(context: Any): Long {
    return this.toFile().length()
}
