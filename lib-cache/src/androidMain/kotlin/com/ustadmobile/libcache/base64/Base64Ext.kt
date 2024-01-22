package com.ustadmobile.libcache.base64

import android.util.Base64

internal actual fun ByteArray.encodeBase64(): String {
    //Note: NO_WRAP is needed to ensure that the encoding is consistent with the JVM Base64 result
    return Base64.encodeToString(this, Base64.NO_WRAP)
}

internal actual fun String.decodeBase64() : ByteArray {
    return Base64.decode(this, Base64.NO_WRAP)
}
