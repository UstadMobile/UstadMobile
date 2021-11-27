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

/**
 * Where the receiver DoorUri is a directory, delete all its contents recursively, but do
 * not delete the directory itself
 */
expect suspend fun DoorUri.emptyRecursively()

/**
 * Delete the Uri itself, and delete any sub items recursively. On Android/JVM, this ONLY works on
 * files and directories.
 */
expect suspend fun DoorUri.deleteRecursively()
