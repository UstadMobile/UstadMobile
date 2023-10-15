package com.ustadmobile.libcache.uri

import java.io.InputStream

/**
 * Interface implemented on JVM and Android to work with Uris.
 *
 * On JVM: only converts the URI to a file
 *
 * On Android: uses application context to retrieve information. This is required when working
 * with Uris returned by file pickers etc.
 */
interface IUriHelper {

    fun contentLength(uri: IUri): Long

    fun openInputStream(uri: IUri): InputStream

    fun mimeType(uri: IUri): String?

}