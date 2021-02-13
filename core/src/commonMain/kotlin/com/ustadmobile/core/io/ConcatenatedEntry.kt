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
        val compression: Int,

        /**
         * The length of the data (after compression, if any)
         */
        val length: Long) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConcatenatedEntry) return false

        if (!md5.contentEquals(other.md5)) return false
        if (compression != other.compression) return false
        if (length != other.length) return false

        return true
    }

    override fun hashCode(): Int {
        var result = md5.contentHashCode()
        result = 31 * result + compression
        result = 31 * result + length.hashCode()
        return result
    }

    companion object {
        //16 bytes md5, 1 byte compression flag, 8 byte length
        const val SIZE = 16 + 1 + 8

        const val COMPRESSION_NONE = 0

        const val COMPRESSION_GZIP = 1

    }


}