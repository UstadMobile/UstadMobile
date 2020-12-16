package com.ustadmobile.core.io

import kotlinx.io.InputStream
import kotlin.math.min

class RangeInputStream(var input: InputStream, start: Long, private val end: Long) : KioInputStream() {

    private var pos: Long = 0

    private var resetPos: Long = 0

    private var resetInvalidate: Long = 0


    init {
        resetPos = -1
        resetInvalidate = -1
        pos = 0

        //skip can skip up to the requested number of bytes to skip
        var startBytesSkipped: Long = 0
        while (startBytesSkipped < start) {
            startBytesSkipped += skip(start - startBytesSkipped)
        }
    }

    override fun read(): Int {
        if (pos <= end) {
            pos++
            return input.read()
        } else {
            return -1
        }
    }


    override fun read(buffer: ByteArray): Int {
        return read(buffer, 0, buffer.size)
    }


    override fun read(buffer: ByteArray, byteOffset: Int, byteCount: Int): Int {
        var byteCount = byteCount
        byteCount = min(end + 1 - pos, byteCount.toLong()).toInt()
        if (byteCount > 0) {
            val bytesRead = input.read(buffer, byteOffset, byteCount)
            pos += bytesRead.toLong()
            return bytesRead
        } else {
            return -1
        }
    }


    override fun skip(byteCount: Long): Long {
        val skipped = input.skip(byteCount).toInt()
        pos += skipped.toLong()
        return skipped.toLong()
    }



}