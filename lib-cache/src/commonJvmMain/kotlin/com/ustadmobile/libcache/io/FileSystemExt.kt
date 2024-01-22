package com.ustadmobile.libcache.io

import kotlinx.io.RawSource
import kotlinx.io.asSource
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import java.io.File
import java.io.FileInputStream

actual fun FileSystem.rangeSource(path: Path, fromByte: Long, toByte: Long): RawSource {
    val fileIn = FileInputStream(path.toString())

    return RangeInputStream(
        src = fileIn,
        fromByte  = fromByte,
        toByte = toByte,
    ).asSource()
}


actual fun FileSystem.lastModified(path: Path): Long {
    return File(path.toString()).lastModified()
}
