package com.ustadmobile.core.io.ext

import com.ustadmobile.door.DoorUri
import org.kodein.di.DI

/**
 * Guess the mime type of the given URI. This might (or might not) involve doing some actual I/O work.
 * This will be done by underlying mechanisms on the platform
 */
expect suspend fun DoorUri.guessMimeType(context:Any, di: DI): String?

expect suspend fun DoorUri.getSize(context: Any, di: DI): Long

expect suspend fun DoorUri.downloadUrlIfRemote(destination: DoorUri, di: DI)

expect suspend fun DoorUri.isRemote(): Boolean