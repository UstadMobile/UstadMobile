package com.ustadmobile.core.io.ext

import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.core.io.ConcatenatedEntry
import com.ustadmobile.core.util.ext.base64StringToByteArray
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5

fun ContainerEntryFile.toConcatenatedEntry() : ConcatenatedEntry{
    val md5Val = cefMd5 ?: throw IllegalArgumentException("toConcatEntry must have md5")
    return ConcatenatedEntry(md5 = md5Val.base64StringToByteArray(),
            compression = compression.toByte(), compressedSize = ceCompressedSize,
            totalSize = ceTotalSize, lastModified = lastModified)
}

/**
 * Turn this ContainerEntryWithContainerEntryFile into a ContainerEntryWithMd5.
 * This removes information about the file (e.g. size, path, etc) and only has the
 * md5sum.
 */
fun ContainerEntryWithContainerEntryFile.toContainerEntryWithMd5(): ContainerEntryWithMd5 {
    return ContainerEntryWithMd5().also {
        it.ceUid = ceUid
        it.cePath = cePath
        it.ceCefUid = ceCefUid
        it.ceContainerUid = ceContainerUid
        it.cefMd5 = containerEntryFile?.cefMd5
    }
}
