package com.ustadmobile.core.util.digest

import com.ustadmobile.core.util.ext.encodeBase64

fun Digester.digest(byteArray: ByteArray) = digest(byteArray, 0, byteArray.size)

/**
 * urlKey function: same as in lib-cache (but we can't use lib-cache functions on commonMain because
 * it is not used on Android and Desktop).
 */
fun Digester.urlKey(url: String) : String {
    return digest(url.encodeToByteArray()).encodeBase64()
}
