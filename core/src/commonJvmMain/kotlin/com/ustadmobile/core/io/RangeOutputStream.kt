package com.ustadmobile.core.io

import java.io.FilterOutputStream
import java.io.OutputStream
import java.lang.Math.max

/**
 * RangeOutputStream will discard bytes before the start of a range and (optionally) after the end
 * of a range. This is designed to be used with partial http responses.
 */
class RangeOutputStream: FilterOutputStream {

    /**
     * The first byte that should be emitted (inclusive)
     */
    val start: Long

    /**
     * The last byte that should be emitted (inclusive), or -1 to continue until
     * end of stream.
     */
    val end: Long

    constructor(output: OutputStream, start: Long, end: Long) : super(output){
        this.start = start
        this.end = end

        if(end != -1L && start > end)
            throw IllegalArgumentException("RangeOutputStream: Invalid Range: start=$start, end=$end")

    }

    private var position: Long = 0

    private val singleByteArray = ByteArray(1)

    override fun write(p0: Int) {
        singleByteArray[0] = p0.toByte()
        write(singleByteArray, 0, 1)
    }

    override fun write(buf: ByteArray) = write(buf, 0, buf.size)

    override fun write(buf: ByteArray, offset: Int, len: Int) {
        val effectiveOffset = max(0L, start - position).toInt()
        val offsetDelta = effectiveOffset - offset

        var effectiveLen = len - offsetDelta

        if(end > 0 && position + len > end){
            effectiveLen -= ((position + len) - (end + 1)).toInt()
        }

        if(effectiveLen > 0){
            out.write(buf, effectiveOffset, effectiveLen)
        }

        position += len
    }
}