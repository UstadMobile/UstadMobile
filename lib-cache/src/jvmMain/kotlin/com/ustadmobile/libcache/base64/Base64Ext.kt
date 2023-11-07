package com.ustadmobile.libcache.base64

import java.util.Base64

internal actual fun ByteArray.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(this)
}

internal actual fun String.decodeBase64(): ByteArray {
    return Base64.getDecoder().decode(this)
}


