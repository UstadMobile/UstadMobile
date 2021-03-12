package com.ustadmobile.core.io.ext

import com.ustadmobile.lib.db.entities.ContainerEntryFile
import java.io.*
import java.util.zip.GZIPInputStream

/**
 * Open an InputStream for the given ContainerEntryFile. If this ContainerEntryFile is compressed,
 * the InputStream will automatically inflate as required.
 */
fun ContainerEntryFile.openInputStream() : InputStream {
    val cefPathVal = cefPath ?: throw IllegalStateException("ContainerEntryFile $cefUid has null cefPath!")

    return if(compression == ContainerEntryFile.COMPRESSION_GZIP) {
        GZIPInputStream(FileInputStream(cefPathVal))
    }else {
        FileInputStream(cefPathVal)
    }
}