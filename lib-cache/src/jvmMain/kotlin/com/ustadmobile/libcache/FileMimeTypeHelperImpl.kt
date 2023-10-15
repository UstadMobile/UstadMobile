package com.ustadmobile.libcache

import com.ustadmobile.libcache.headers.MimeTypeHelper
import com.ustadmobile.libcache.headers.MimeTypeHelper.Companion.EXTENSION_TO_MIME_TYPE

class FileMimeTypeHelperImpl(
    private val typeMap: Map<String, String> = EXTENSION_TO_MIME_TYPE,
): MimeTypeHelper {

    override fun mimeTypeByUri(uri: String): String? {
        return typeMap[uri.substringAfterLast(".").lowercase()]
    }

}