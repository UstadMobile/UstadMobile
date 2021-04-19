package com.ustadmobile.core.io.ext
import java.io.File

/**
 * Where this String is a Kmp Uri String that represents a file, parse it with the Uri class most
 * appropriate for the underlying system (e.g. java.net.URI on JVM, android.net.Uri on Android)
 * and convert it to a java.io.File object.
 */
expect fun String.parseKmpUriStringToFile(): File
