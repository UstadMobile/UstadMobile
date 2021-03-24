package com.ustadmobile.core.io.ext

import android.webkit.MimeTypeMap
import com.ustadmobile.door.DoorUri

actual suspend fun DoorUri.guessMimeType(): String? {
    return MimeTypeMap.getFileExtensionFromUrl(this.toString())?.let { extension ->
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
}
