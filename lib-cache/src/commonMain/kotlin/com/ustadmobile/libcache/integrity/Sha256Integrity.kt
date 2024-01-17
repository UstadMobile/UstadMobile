package com.ustadmobile.libcache.integrity

import io.ktor.util.encodeBase64

fun sha256Integrity(sha256: ByteArray) : String {
    return "sha256-${sha256.encodeBase64()}"
}


