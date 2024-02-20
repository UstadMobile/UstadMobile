package com.ustadmobile.util.test

import java.io.FilterInputStream
import java.io.InputStream

/**
 * Simple FilterInputStream implementation that will read a specific range.
 *
 * Copy/paste from lib-cache so it can be used within test-common
 *
 * @param src the source InputStream
 * @param fromByte inclusive
 * @param toByte **INCLUSIVE** as per http range headers
 */
internal class RangeInputStream(
    private val src: InputStream,
    private val fromByte: Long,
    private val toByte: Long,
): FilterInputStream(src) {

    @Volatile
    private var pos: Long = 0

    init {
        skipExactly(fromByte)
        pos = fromByte
    }

    override fun read(): Int {
        return if(pos <= toByte) {
            src.read().also {
                pos++
            }
        }else {
            -1
        }
    }

    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val bytesRemaining = (toByte - pos) + 1 //+1 because toByte is inclusive
        if(bytesRemaining <= 0)
            return -1

        val effectiveLen = if(len <= bytesRemaining) {
            len
        }else {
            bytesRemaining.toInt()
        }

        return super.read(b, off, effectiveLen).also {
            pos += it
        }
    }

    override fun markSupported(): Boolean {
        return false
    }
}

/**
 * Get a ranged input stream
 * @param fromByte the start byte (inclusive)
 * @param toByte end byte (**INCLUSIVE** as per HTTP range headers)
 */
fun InputStream.range(
    fromByte: Long,
    toByte: Long
) : InputStream = RangeInputStream(this, fromByte, toByte)
