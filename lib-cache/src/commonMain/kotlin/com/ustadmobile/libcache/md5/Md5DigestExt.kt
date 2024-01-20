package com.ustadmobile.libcache.md5

import io.ktor.util.encodeBase64

fun Md5Digest.urlKey(string: String): String {
    return digest(string.encodeToByteArray()).encodeBase64()
}
