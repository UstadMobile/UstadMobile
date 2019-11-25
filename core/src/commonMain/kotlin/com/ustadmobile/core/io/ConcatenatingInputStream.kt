package com.ustadmobile.core.io

import kotlinx.io.ByteArrayInputStream
import kotlinx.io.ByteBuffer
import kotlinx.io.InputStream
import kotlin.math.min

data class ConcatenatedPartSource(val src: ()-> InputStream, val length: Long, val partId: ByteArray)

/**
 * ConcatenatingInputStream is designed to take a list of input streams and concatenate them together.
 * Each part should must be identified by a 16 byte array identifier (generally the MD5 sum). The
 * length of each part must be fixed.
 *
 * The advantage of this technique vs. zipping the given files together) is that it does not require
 * creating any new file and the size is known in advance.
 *
 * The structure of the output is as follows
 *
 * HEADER:
 * Number of parts (4 Byte Int)
 * 16 byte part ID|8 byte part length (once for each part)
 * PAYLOAD:
 * Part data (the bytes of each part, in order)
 *
 * The total size of the output will always be:
 * (sum of part data length) + (number parts x 24bytes) + 4 bytes
 *
 */
class ConcatenatingInputStream(concatenatedParts: List<ConcatenatedPartSource>): InputStream() {

    private val parts: List<ConcatenatedPartSource>

    var currentInputStream: InputStream

    private var currentBytePos = 0L

    private var currentStreamIndex = 0

    private var nextBoundary = 0L

    private val totalLength: Long

    init {
        val headerBuffer = generateHeader(concatenatedParts)

        val headerByteArr = generateHeader(concatenatedParts)
        val headerPart = ConcatenatedPartSource({ByteArrayInputStream(headerByteArr)},
                headerByteArr.size.toLong(), ByteArray(0))

        parts = listOf(headerPart, *concatenatedParts.toTypedArray())

        totalLength = concatenatedParts.fold(0L, {acc, el -> acc + el.length}) + headerBuffer.size
        currentInputStream = ByteArrayInputStream(headerBuffer)
        nextBoundary = headerBuffer.size.toLong()
    }

    fun generateHeader(concatenatedParts: List<ConcatenatedPartSource>): ByteArray {
        val buffer = ByteBuffer.allocate((concatenatedParts.size * (LEN_CHUNK_ID + LEN_CHUNK_LENGTH)) + LEN_NUM_CHUNKS)
        buffer.putInt(concatenatedParts.size)
        concatenatedParts.forEach {
            buffer.put(it.partId)
            buffer.putLong(it.length)
        }
        return buffer.array()
    }

    override fun available(): Int {
        return currentInputStream.available()
    }

    override fun close() {
        currentInputStream.close()
    }

    private fun openNextStream() {
        if(currentStreamIndex >= parts.size - 1) {
            throw IllegalStateException("Reached end of parts")
        }

        currentInputStream.close()
        currentStreamIndex++

        currentInputStream = parts[currentStreamIndex].src()
        nextBoundary += parts[currentStreamIndex].length
    }

    override fun read(): Int {
        if(currentBytePos == totalLength -1) {
            return -1
        }else if(currentBytePos == nextBoundary && currentBytePos < totalLength) {
            openNextStream()
        }
        currentBytePos++
        return currentInputStream.read()

    }

    override fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    override fun read(b: ByteArray, offset: Int, len: Int): Int {
        var totalBytesRead = 0
        var offsetCount = offset
        var currBytesRead = 0

        if(currentBytePos >= totalLength) {
            return -1
        }

        while(currentBytePos < totalLength && totalBytesRead < len && currBytesRead != -1) {
            if(currentBytePos == nextBoundary && currentStreamIndex < parts.size) {
                openNextStream()
            }

            val numBytesToRead = min(len - totalBytesRead, (nextBoundary - currentBytePos).toInt())

            currBytesRead = currentInputStream.read(b, offsetCount, numBytesToRead)
            totalBytesRead += currBytesRead
            currentBytePos += currBytesRead
            offsetCount += currBytesRead
        }

        return totalBytesRead
    }

    override fun skip(n: Long): Long {
        return super.skip(n)
    }

    companion object {

        val LEN_CHUNK_ID = 16

        val LEN_CHUNK_LENGTH = 8

        val LEN_NUM_CHUNKS = 4
    }
}