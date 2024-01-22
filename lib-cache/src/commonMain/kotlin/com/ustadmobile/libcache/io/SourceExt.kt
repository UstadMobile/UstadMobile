package com.ustadmobile.libcache.io

import kotlinx.io.Source
import kotlinx.io.files.Path

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
) : TransferResult

expect fun Source.useAndReadySha256(): ByteArray

data class UnzippedEntry(
    val path: Path,
    val name: String,
    val sha256: ByteArray,
)

expect fun Source.unzipTo(
    destPath: Path
): List<UnzippedEntry>


