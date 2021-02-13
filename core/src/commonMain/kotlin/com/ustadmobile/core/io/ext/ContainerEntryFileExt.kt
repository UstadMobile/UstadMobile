package com.ustadmobile.core.io.ext

import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.core.io.ConcatenatedEntry
import com.ustadmobile.core.util.ext.base64StringToByteArray

fun ContainerEntryFile.toConcatenatedEntry() : ConcatenatedEntry{
    val md5Val = cefMd5 ?: throw IllegalArgumentException("toConcatEntry must have md5")
    return ConcatenatedEntry(md5 = md5Val.base64StringToByteArray(),
            compression = compression.toByte(), compressedSize = ceCompressedSize,
            totalSize = ceTotalSize, lastModified = lastModified)
}