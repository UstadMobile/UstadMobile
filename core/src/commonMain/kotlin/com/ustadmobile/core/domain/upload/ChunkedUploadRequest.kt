package com.ustadmobile.core.domain.upload

/**
 * Represents a request that contains a chunk of data
 */
data class ChunkedUploadRequest(
    val headers: Map<String, List<String>>,
    val chunkData: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChunkedUploadRequest) return false

        if (headers != other.headers) return false
        return chunkData.contentEquals(other.chunkData)
    }

    override fun hashCode(): Int {
        var result = headers.hashCode()
        result = 31 * result + chunkData.contentHashCode()
        return result
    }
}