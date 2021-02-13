package com.ustadmobile.core.io.ext

import com.ustadmobile.core.io.ConcatenatedEntry
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Serialize this ConcatenatedEntry to a ByteArray that can be written to a stream.
 * This is done using a ByteBuffer (Little Endian)
 */
fun ConcatenatedEntry.toBytes(): ByteArray {
    val byteBuffer = ByteBuffer.allocate(ConcatenatedEntry.SIZE)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putLong(length)
        .put(compression.toByte())
        .put(md5)

    byteBuffer.rewind()
    return ByteArray(ConcatenatedEntry.SIZE).also {
        byteBuffer.get(it, 0, it.size)
    }
}

/**
 * Where this ByteArray represents a serialized ConcatenatedEntry that was written
 * using ConcatenatedEntry.toBytes, deserialize it.
 */
fun ByteArray.toConcatenatedEntry(): ConcatenatedEntry {
    val byteBuffer = ByteBuffer.wrap(this)
        .order(ByteOrder.LITTLE_ENDIAN)
    val length = byteBuffer.getLong()
    val compression = byteBuffer.get()
    val md5 = ByteArray(16)
    byteBuffer.get(md5)

    return ConcatenatedEntry(md5, compression.toInt(), length)
}