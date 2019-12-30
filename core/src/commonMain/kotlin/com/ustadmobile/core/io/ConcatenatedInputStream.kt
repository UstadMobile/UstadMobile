package com.ustadmobile.core.io

import com.ustadmobile.core.io.ConcatenatingInputStream.Companion.LEN_CHUNK_ID
import com.ustadmobile.core.io.ConcatenatingInputStream.Companion.LEN_CHUNK_LENGTH
import com.ustadmobile.core.io.ConcatenatingInputStream.Companion.LEN_NUM_CHUNKS
import kotlinx.io.ByteBuffer
import kotlinx.io.ByteOrder
import kotlinx.io.InputStream
import kotlin.math.min

data class ConcatenatedPart(val id: ByteArray, val length: Long, val uncompressedLength: Long) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ConcatenatedPart

        if (!id.contentEquals(other.id)) return false
        if (length != other.length) return false
        if (uncompressedLength != other.uncompressedLength) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.contentHashCode()
        result = 31 * result + length.hashCode()
        result = 31 * result + uncompressedLength.hashCode()
        return result
    }
}

class ConcatenatedInputStream(val src: InputStream) : KioInputStream(){

    private var numFiles: Int = 0

    private var currentPartBytesRemaining = 0L

    private var currentPartIndex = -1

    val partHeaders: List<ConcatenatedPart>

    init {
        val lenByteBuffer = ByteBuffer.allocate(LEN_NUM_CHUNKS).order(ByteOrder.LITTLE_ENDIAN)
        val lenBytes = ByteArray(LEN_NUM_CHUNKS)
        src.read(lenBytes, 0, LEN_NUM_CHUNKS)
        lenByteBuffer.put(lenBytes, 0, LEN_NUM_CHUNKS)
        lenByteBuffer.clear()
        numFiles = lenByteBuffer.getInt()

        val headerBufferSize = (numFiles * (LEN_CHUNK_ID + (LEN_CHUNK_LENGTH * 2)))
        val headerByteBuffer = ByteBuffer.allocate(headerBufferSize).order(ByteOrder.LITTLE_ENDIAN)
        var headerBytesTotalRead = 0
        val headerReadByteArrayBuf = ByteArray(8 * 1024)

        do {
            val bytesRead = src.read(headerReadByteArrayBuf, 0,
                    min(headerReadByteArrayBuf.size, headerBufferSize - headerBytesTotalRead))
            headerBytesTotalRead += bytesRead
            headerByteBuffer.put(headerReadByteArrayBuf, 0, bytesRead)
        }while(headerBytesTotalRead < headerBufferSize)

        headerByteBuffer.clear()

        partHeaders = (0 until numFiles).map {
            val idByteArr = ByteArray(LEN_CHUNK_ID)
            headerByteBuffer.get(idByteArr, 0, LEN_CHUNK_ID)
            val partLen = headerByteBuffer.getLong()
            val partLenUncompressed = headerByteBuffer.getLong()
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
        return if (currentPartBytesRemaining > 0) {
            currentPartBytesRemaining--
            src.read()
        } else {
            -1
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
}