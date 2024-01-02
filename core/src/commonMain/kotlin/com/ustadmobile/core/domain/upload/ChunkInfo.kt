package com.ustadmobile.core.domain.upload

import kotlin.math.min

/**
 * Basic logic utility that helps getting start/end indexes when splitting binary content into chunks
 * e.g. for upload over http.
 *
 * Not related in any way to chunked http encoding
 *
 * @param totalSize the total size of the file / blob being uploaded
 * @param chunkSize the maximum size of each chunk. Each chunk will be this size, except for the last
 *        chunk
 * @param fromByte The first byte to start from (inclusive), e.g. if the upload is being resumed
 */
@Suppress("MemberVisibilityCanBePrivate")
class ChunkInfo(
    val totalSize: Long,
    val chunkSize: Int,
    val fromByte: Long = 0,
): Iterable<ChunkInfo.Chunk> {

    class ChunkIterator(private val chunkInfo: ChunkInfo): Iterator<Chunk> {
        private var index = 0
        override fun hasNext() = index < chunkInfo.numChunks

        override fun next(): Chunk = chunkInfo[index].also { index++ }

    }

    /**
     * @param start the first byte of the chunk (relative to the total size) e.g. if ChunkInfo.fromByte
     *              was x, then the start of the first Chunk will be x
     * @param end the last byte of the chunk (exclusive)
     * @param isLastChunk true if this is the last chunk, false otherwise
     * @param size the size of the chunk
     */
    data class Chunk(
        val start: Long,
        val end: Long,
        val isLastChunk: Boolean,
        val size: Int = (end - start).toInt()
    )

    //The total size that will be transferred in chunks
    private val chunksTotalSize = (totalSize - fromByte)

    val numChunks = ((chunksTotalSize / chunkSize).let {
        if(chunksTotalSize.mod(chunkSize) != 0) it + 1 else it
    }).toInt()

    override fun iterator(): Iterator<Chunk> {
        return ChunkIterator(this)
    }

    operator fun get(index: Int) : Chunk {
        val start = (index * chunkSize.toLong()) + fromByte
        val end= min(start + chunkSize, totalSize)
        val isLastChunk = (index == (numChunks - 1))
        return Chunk(
            start = start,
            end = end,
            isLastChunk = isLastChunk
        )
    }

}