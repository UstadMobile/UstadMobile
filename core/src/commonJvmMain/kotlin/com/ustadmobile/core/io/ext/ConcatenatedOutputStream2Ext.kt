package com.ustadmobile.core.io.ext

import com.ustadmobile.core.io.ConcatenatedEntry
import com.ustadmobile.core.io.ConcatenatedOutputStream2
import com.ustadmobile.core.ext.md5Sum
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import java.io.File
import java.io.FileInputStream
import java.io.IOException

fun ConcatenatedOutputStream2.putFile(file: File, compression: Byte) {
    if(compression != 0.toByte())
        throw IOException("putFile doesn't support compression (yet)")

    putNextEntry(ConcatenatedEntry(file.md5Sum, compression, file.length(), file.length(), 0L))
    FileInputStream(file).use { fileIn ->
        fileIn.copyTo(this)
        this.flush()
    }
}

/**
 * Put the given ContainerEntryFile into the ConcatenatedOutputStream
 */
fun ConcatenatedOutputStream2.putContainerEntryFile(containerEntryFile: ContainerEntryFile) {
    val filePath = containerEntryFile.cefPath ?: throw IllegalArgumentException("ContainerEntryFile to add must have path")
    val concatEntry = containerEntryFile.toConcatenatedEntry()
    putNextEntry(concatEntry)
    FileInputStream(filePath).use { fileIn ->
        fileIn.copyTo(this)
        this.flush()
    }
}
