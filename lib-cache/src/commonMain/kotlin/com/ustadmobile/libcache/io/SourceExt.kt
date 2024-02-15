package com.ustadmobile.libcache.io

import com.ustadmobile.libcache.CompressionType
import kotlinx.io.RawSource
import kotlinx.io.Source
import kotlinx.io.files.Path

/**
 * @param sha256 the SHA256 of the data transferred (if data was compressed, then the SHA256 is
 *        still the SHA256 of the uncompressed data
 * @param transferred the number of bytes transferred (if compression is used, this is the
 *        inflated/uncompressed size)
 */
data class TransferResult(
    val sha256: ByteArray,
    val transferred: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransferResult) return false

        if (!sha256.contentEquals(other.sha256)) return false
        return transferred == other.transferred
    }

    override fun hashCode(): Int {
        var result = sha256.contentHashCode()
        result = 31 * result + transferred.hashCode()
        return result
    }
}

expect fun Source.transferToAndGetSha256(
    path: Path,
    sourceCompression: CompressionType = CompressionType.NONE,
    destCompressionType: CompressionType = CompressionType.NONE,
) : TransferResult

expect fun Source.useAndReadSha256(): ByteArray

data class UnzippedEntry(
    val path: Path,
    val name: String,
    val sha256: ByteArray,
)

expect fun Source.unzipTo(
    destPath: Path
): List<UnzippedEntry>

expect fun Source.uncompress(
    compressionType: CompressionType
): Source

/**
 * Get a specific range from the given source
 *
 * @param fromByte the first byte to include (inclusive)
 * @param toByte the last byte to include (INCLUSIVE as per HTTP range requests)
 */
expect fun Source.range(fromByte: Long, toByte: Long): RawSource


