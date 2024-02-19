package com.ustadmobile.libcache.io

import com.ustadmobile.libcache.CompressionType
import java.io.OutputStream
import java.util.zip.GZIPOutputStream

fun OutputStream.compressIfRequired(
    compressionType: CompressionType
) : OutputStream {
    return when(compressionType) {
        CompressionType.NONE -> this
        CompressionType.GZIP -> GZIPOutputStream(this)
    }
}
