package com.ustadmobile.core.io

import com.ustadmobile.core.container.ContainerManagerCommon
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import kotlinx.io.InputStream

class ConcatenatedInputStreamEntrySource(private val part: ConcatenatedPart,
                                         private val srcInput: ConcatenatedInputStream,
                                         override val pathsInContainer: List<String>): ContainerManagerCommon.EntrySource {

    override val length: Long
        get() = if(part.uncompressedLength != -1L) { part.uncompressedLength } else { part.length }

    override val inputStream: InputStream
        get() = srcInput

    override val filePath: String?
        get() = null

    override val md5Sum: ByteArray
        get() = part.id

    override val compression: Int
        get() = if(part.uncompressedLength != -1L) { ContainerEntryFile.COMPRESSION_GZIP } else { 0 }

    override fun dispose() {
        //do nothing - we are going to continue reading from the same InputStream
    }
}