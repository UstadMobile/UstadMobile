package com.ustadmobile.libcache.headers

/**
 * Convenience extension function that will get the file extension from the given filename and then
 * run guessByExtension if the extension is present.
 */
fun MimeTypeHelper.guessByExtensionFromFilename(filename: String): String? {
    return filename.substringAfterLast(".", "")
        .takeIf { it != "" }
        ?.let {
            guessByExtension(it)
        }
}
