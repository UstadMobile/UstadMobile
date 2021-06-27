package com.ustadmobile.core.io.ext

import com.ustadmobile.door.DoorUri

/**
 * Guess the mime type of the given URI. This might (or might not) involve doing some actual I/O work.
 * This will be done by underlying mechanisms on the platform
 */
actual suspend fun DoorUri.guessMimeType(): String? {
    TODO("Not yet implemented")
}

actual suspend fun DoorUri.getSize(context: Any): Long {
    TODO("Not yet implemented")
}