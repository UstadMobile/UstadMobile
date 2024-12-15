package com.ustadmobile.xxhashkmp.ext


/**
 * Converts a Long to a byte array without requiring ByteBuffer
 */
fun Long.toByteArray(): ByteArray {
    val buffer = ByteArray(8)
    var temp = this
    for (i in 0 until 8) {
        buffer[7 - i] = (temp and 0xFF).toByte()
        temp = temp shr 8
    }
    return buffer
}