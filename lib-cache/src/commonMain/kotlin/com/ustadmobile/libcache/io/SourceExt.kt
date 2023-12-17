package com.ustadmobile.libcache.io

import kotlinx.io.Source
import kotlinx.io.files.Path

data class TransferResult(
    val sha256: ByteArray,
    val transferred: Long,
)
expect fun Source.transferToAndGetSha256(
    path: Path,
) : TransferResult

expect fun Source.sha256(): ByteArray

data class UnzippedEntry(
    val path: Path,
    val name: String,
    val sha256: ByteArray,
)

expect fun Source.unzipTo(
    destPath: Path
): List<UnzippedEntry>


