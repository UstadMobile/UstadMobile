package com.ustadmobile.core.io.ext

import com.ustadmobile.door.DoorUri
import java.nio.file.Files
import java.nio.file.Paths

actual suspend fun DoorUri.guessMimeType(): String? {
    return Files.probeContentType(Paths.get(this.uri))
}
