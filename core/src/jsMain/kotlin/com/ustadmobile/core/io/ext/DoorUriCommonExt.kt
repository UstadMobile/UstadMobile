package com.ustadmobile.core.io.ext

import com.ustadmobile.door.DoorUri
import org.kodein.di.DI

/**
 * Guess the mime type of the given URI. This might (or might not) involve doing some actual I/O work.
 * This will be done by underlying mechanisms on the platform
 */


actual suspend fun DoorUri.getSize(context: Any, di: DI): Long {
    TODO("Not yet implemented")
}

/**
 * Guess the mime type of the given URI. This might (or might not) involve doing some actual I/O work.
 * This will be done by underlying mechanisms on the platform
 */
actual suspend fun DoorUri.guessMimeType(context: Any, di: DI): String? {
    return DoorUri.getDoorUriProps(this.uri.toString())?.mimeType ?: "application/octet-stream"
}

actual suspend fun DoorUri.downloadUrlIfRemote(destination: DoorUri, di: DI) {
    TODO("Not yet implemented")
}

actual suspend fun DoorUri.isRemote(): Boolean {
    val prefix = this.uri.toString().substringBefore("//").lowercase()
    return prefix.startsWith("http:") || prefix.startsWith("https:")
}

/**
 * Where the receiver DoorUri is a directory, delete all its contents recursively, but do
 * not delete the directory itself
 */
actual suspend fun DoorUri.emptyRecursively() {
    TODO("Not yet implemented")
}

/**
 * Delete the Uri itself, and delete any sub items recursively. On Android/JVM, this ONLY works on
 * files and directories.
 */
actual suspend fun DoorUri.deleteRecursively() {
    TODO("Not yet implemented")
}