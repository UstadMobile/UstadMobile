package com.ustadmobile.libcache.headers

import com.ustadmobile.libcache.headers.MimeTypeHelper.Companion.EXTENSION_TO_MIME_TYPE

class FileMimeTypeHelperImpl(
    private val typeMap: Map<String, String> = EXTENSION_TO_MIME_TYPE,
): MimeTypeHelper {

    override fun guessByExtension(extension: String): String? {
        return typeMap[extension.lowercase()]
    }

    override fun mimeTypeByUri(uri: String): String? {
        return guessByExtension(uri.substringAfterLast("."))
    }

}