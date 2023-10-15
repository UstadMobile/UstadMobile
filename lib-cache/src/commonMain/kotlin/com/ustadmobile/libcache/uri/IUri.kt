package com.ustadmobile.libcache.uri

/**
 * Interface that represents a Uri. Wrapper for java.net.URI on JVM, android.net.Uri on Android
 */
interface IUri {
    companion object
}

expect fun IUri.Companion.parse(uri: String): IUri
