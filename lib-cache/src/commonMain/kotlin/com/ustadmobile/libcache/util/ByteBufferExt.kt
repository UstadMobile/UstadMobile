package com.ustadmobile.libcache.util

import java.nio.ByteBuffer

fun ByteBuffer.writeShortString(
    string: String
) {
    val stringAsBytes = string.toByteArray()
    if(stringAsBytes.size > Byte.MAX_VALUE)
        throw IllegalArgumentException("Write short string: must not exceed ${Byte.MAX_VALUE} bytes")

    put(stringAsBytes.size.toByte())
    put(stringAsBytes)
}

fun ByteBuffer.readShortString(): String {
    val stringByteSize = get()
    if(stringByteSize < 0)
        throw IllegalArgumentException("Doesn't look like size in bytes: $stringByteSize")

    if(stringByteSize == 0.toByte())
        return ""

    val byteArray = ByteArray(stringByteSize.toInt())
    get(byteArray)
    return byteArray.decodeToString()
}

/**
 * Write a short with the length of the payload, followed by the payload itself
 */
fun ByteBuffer.writePayload(payload: ByteArray) {
    if(payload.size > Short.MAX_VALUE)
        throw IllegalArgumentException("writePayload: max byte array size of ${Short.MAX_VALUE} exceeded")

    putShort(payload.size.toShort())

    if(payload.isNotEmpty())
        put(payload)
}

/**
 * Read a payload that was written using writePayload
 */
fun ByteBuffer.readPayload(): ByteArray {
    val payloadSize = getShort()
    if(payloadSize < 0)
        throw IllegalStateException("Payload size cannot be negative")

    val payload = ByteArray(payloadSize.toInt())
    if(payloadSize > 0)
        get(payload)

    return payload
}
