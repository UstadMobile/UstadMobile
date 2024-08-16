package com.ustadmobile.core.util.ext

fun ByteArray.toLong(): Long {
    var result: Long = 0
    for (i in 0 until 8) {
        result = result shl 8
        result = result or (this[i].toLong() and 0xFF)
    }
    return result
}
