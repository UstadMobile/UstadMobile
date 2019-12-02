package com.ustadmobile.core.io

import com.ustadmobile.core.io.ConcatenatingInputStream.Companion.LEN_CHUNK_ID
import com.ustadmobile.core.io.ConcatenatingInputStream.Companion.LEN_CHUNK_LENGTH
import com.ustadmobile.core.io.ConcatenatingInputStream.Companion.LEN_NUM_CHUNKS
import kotlinx.io.ByteBuffer
import kotlinx.io.ByteOrder
import kotlinx.io.InputStream
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.readLongLittleEndian
import kotlin.math.min

data class ConcatenatedPart(val id: ByteArray, val length: Long, val uncompressedLength: Long)

class ConcatenatedInputStream(val src: InputStream) : InputStream(){

    var numFiles: Int = 0

    private var currentPartBytesRemaining = 0L

    private var currentPartIndex = -1

    val partHeaders: List<ConcatenatedPart>

    init {
        val ioBuffer = IoBuffer.NoPool.borrow()
        val byteArrBuff = ByteArray(LEN_NUM_CHUNKS)
        src.read(byteArrBuff, 0, LEN_NUM_CHUNKS)
        ioBuffer.writeFully(byteArrBuff, 0, LEN_NUM_CHUNKS)

        numFiles = ioBuffer.readInt()
        val headerBufferSize = (numFiles * (LEN_CHUNK_ID + (LEN_CHUNK_LENGTH * 2)))
        val headerByteArr = ByteArray(headerBufferSize)
        src.read(headerByteArr)
        ioBuffer.writeFully(headerByteArr, 0, headerBufferSize)
        partHeaders = (0 until numFiles).map {
            val idByteArr = ByteArray(LEN_CHUNK_ID)
            ioBuffer.readFully(idByteArr, 0, LEN_CHUNK_ID)
            val partLen = ioBuffer.readLongLittleEndian()
            val partLenUncompressed = ioBuffer.readLongLittleEndian()
            val part = ConcatenatedPart(idByteArr, partLen, partLenUncompressed)
            part
        }
    }

    fun nextPart() : ConcatenatedPart? {
        //TODO: skip until next chunk
        if(++currentPartIndex >= partHeaders.size)
            return null



        return partHeaders[currentPartIndex].also {
            currentPartBytesRemaining = it.length
        }
    }

    override fun available(): Int {
        return src.available()
    }

    override fun close() {
        src.close()
    }

    override fun read(): Int {
        if(currentPartBytesRemaining > 0) {
            currentPartBytesRemaining--
            return src.read()
        }else {
            return -1
        }
    }

    override fun read(b: ByteArray) = read(b, 0, b.size)

    override fun read(b: ByteArray, offset: Int, len: Int): Int {
        if(currentPartBytesRemaining == 0L)
            return -1

        val bytesRead = src.read(b, offset, min(len, currentPartBytesRemaining.toInt()))
        currentPartBytesRemaining -= bytesRead
        return bytesRead
    }

    override fun skip(n: Long): Long {
        return super.skip(n)
    }
}