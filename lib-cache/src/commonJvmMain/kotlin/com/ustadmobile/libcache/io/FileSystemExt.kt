package com.ustadmobile.libcache.io

import kotlinx.io.RawSource
import kotlinx.io.asSource
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import java.io.FileInputStream

actual fun FileSystem.rangeSource(path: Path, fromByte: Long, toByte: Long): RawSource {
    val  fileIn = FileInputStream(path.name)

    return RangeInputStream(
        src = fileIn,
        fromByte  = fromByte,
        toByte = toByte,
    ).asSource()
}
