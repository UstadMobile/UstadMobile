package com.ustadmobile.core.domain.xxhash

fun XXHasher.toLongOrHash(string: String): Long {
    return string.toLongOrNull() ?: hash(string)
}
