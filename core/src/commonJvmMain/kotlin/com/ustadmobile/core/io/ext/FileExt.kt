package com.ustadmobile.core.io.ext

import java.io.File

/**
 * A URI string based on using the appropriate URI class for the underlying platform. On Android
 * this is android.net.Uri, on JVM this is the java.net.URI.  This is the "Kotlin Multiplatform Uri
 * String" used as a parameter when specifying directories etc.
 */
expect fun File.toKmpUriString(): String
