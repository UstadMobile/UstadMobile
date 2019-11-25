package com.ustadmobile.core.io

import com.ustadmobile.core.io.ConcatenatingInputStream.Companion.LEN_CHUNK_ID
import com.ustadmobile.core.io.ConcatenatingInputStream.Companion.LEN_CHUNK_LENGTH
import com.ustadmobile.core.io.ConcatenatingInputStream.Companion.LEN_NUM_CHUNKS
import kotlinx.io.ByteBuffer
import kotlinx.io.InputStream
import kotlin.math.min

data class ConcatenatedPart(val id: ByteArray, val length: Long)

class ConcatenatedInputStream(val src: InputStream) : InputStream(){

    var numFiles: Int = 0

    private var currentPartBytesRemaining = 0L

    private var currentPartIndex = -1

    private val partHeaders: List<ConcatenatedPart>

    init {
        val numFilesByteArray = ByteArray(LEN_NUM_CHUNKS)
        src.read(numFilesByteArray)
        val numFilesBuffer = ByteBuffer.allocate(LEN_NUM_CHUNKS)
        numFilesBuffer.put(numFilesByteArray)
        numFiles = numFilesBuffer.getInt(0)

        val chunkLenArray = ByteArray(LEN_CHUNK_LENGTH)
        partHeaders = (0 until numFiles).map {
            val idByteArray = ByteArray(LEN_CHUNK_ID)
            src.read(idByteArray)
            src.read(chunkLenArray)
            val chunkLenBuffer = ByteBuffer.allocate(LEN_CHUNK_LENGTH)
            chunkLenBuffer.put(chunkLenArray,0, chunkLenArray.size)
            ConcatenatedPart(idByteArray, chunkLenBuffer.getLong(0))
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