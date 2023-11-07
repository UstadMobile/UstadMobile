package com.ustadmobile.core.uri

import com.ustadmobile.door.DoorUri
import kotlinx.io.Source

/**
 * Helper interface that is implemented on platforms that can get the mime type, filename,
 * and input Source from a Uri.
 *
 * Uris use android.net.Uri on Android, java.net.Uri on JVM
 *
 * The implementation on Android uses the application context to use contentresolver as required
 * (e.g. simple expect/actual functions won't work).
 */
interface UriHelper {

    suspend fun getMimeType(uri: DoorUri): String?

    suspend fun getFileName(uri: DoorUri): String

    suspend fun getSize(uri: DoorUri): Long

    suspend fun openSource(uri: DoorUri): Source


}