package com.ustadmobile.core.io

/**
 * Represents an entry in a ConcatenatedStream. It is similar to java.util.ZipEntry. As the
 * concatenated stream is designed to be fixed-length, it does not include a name. This means the
 * header is a fixed size, and the size of the entire stream can be easily predicted.
 */
class ConcatenatedEntry(
        /**
         * The MD5 Sum of the data. If the data is compressed, this is the MD5Sum of the
         * uncompressed data!
         */
        val md5: ByteArray,

        /**
         * 0 for no compression, 1 for gzip
         */
        val compression: Byte,

        /**
         * The length of the data (after compression, if any). If the data is uncompressed,
         * then compressedSize should equal totalSize
         */
        val compressedSize: Long,

        /**
         * The total size of the data (after uncompression, if applicable).
         */
        val totalSize: Long,

        /**
         * The last modified time
         */
        val lastModified: Long) {

    val isCompressed: Boolean
        get() = compression == COMPRESSION_GZIP

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConcatenatedEntry) return false

        if (!md5.contentEquals(other.md5)) return false
        if (compression != other.compression) return false
        if (compressedSize != other.compressedSize) return false
        if (totalSize != other.totalSize) return false
        if (lastModified != other.lastModified) return false

        return true
    }

    override fun hashCode(): Int {
        var result = md5.contentHashCode()
        result = 31 * result + compression
        result = 31 * result + compressedSize.hashCode()
        result = 31 * result + totalSize.hashCode()
        result = 31 * result + lastModified.hashCode()
        return result
    }


    companion object {

        //compressed size (8 bytes), total size (8 bytes), compression (1 byte), last modified (8 bytes), md5sum (16 bytes)
        const val SIZE = 8 + 8 + 1 + 8 + 16

        const val COMPRESSION_NONE = 0.toByte()

        const val COMPRESSION_GZIP = 1.toByte()

    }


}