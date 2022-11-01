package com.ustadmobile.core.io

import java.io.FilterInputStream
import java.io.InputStream

class CountInputStream(src: InputStream) : FilterInputStream(src) {

    var byteReadCount = 0L
        private set

    override fun read(): Int {
        return super.read().also {
            if(it != -1) byteReadCount++
        }
    }

    override fun read(b: ByteArray) = read(b, 0, b.size)

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return super.read(b, off, len).also {
            byteReadCount += it
        }
    }

    override fun skip(n: Long): Long {
        return super.skip(n).also {
            byteReadCount += it
        }
    }
}