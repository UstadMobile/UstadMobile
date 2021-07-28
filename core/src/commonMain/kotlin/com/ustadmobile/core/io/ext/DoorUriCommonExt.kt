package com.ustadmobile.core.io.ext

import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.door.DoorUri

/**
 * Guess the mime type of the given URI. This might (or might not) involve doing some actual I/O work.
 * This will be done by underlying mechanisms on the platform
 */
expect suspend fun DoorUri.guessMimeType(): String?

expect suspend fun DoorUri.getSize(context: Any): Long

expect suspend fun DoorUri.downloadUrl(processContext: ProcessContext): DoorUri