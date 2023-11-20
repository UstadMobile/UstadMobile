package com.ustadmobile.libcache.io

import kotlinx.io.RawSource
import kotlinx.io.asSource
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import java.io.File
import java.io.FileInputStream

actual fun FileSystem.rangeSource(path: Path, fromByte: Long, toByte: Long): RawSource {
    val  fileIn = FileInputStream(path.name)

    return RangeInputStream(
        src = fileIn,
        fromByte  = fromByte,
        toByte = toByte,
    ).asSource()
}

actual fun FileSystem.newTmpFile(prefix: String, postfix: String): Path {
    val tmpFile = File.createTempFile(prefix, postfix)
    return Path(tmpFile.absolutePath)
}
