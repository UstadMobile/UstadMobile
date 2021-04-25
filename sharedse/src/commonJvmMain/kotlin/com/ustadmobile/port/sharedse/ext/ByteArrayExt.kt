package com.ustadmobile.sharedse.ext

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

actual fun ByteArray.compressWithGzip(): ByteArray {
    val byteArrayIn = ByteArrayInputStream(this)
    val byteArrayOut = ByteArrayOutputStream()

    val gzipOut = GZIPOutputStream(byteArrayOut)
    byteArrayIn.copyTo(gzipOut)
    gzipOut.flush()
    gzipOut.close()

    return byteArrayOut.toByteArray()
}

actual fun ByteArray.decompressWithGzip(): ByteArray {
    val gzipIn = GZIPInputStream(ByteArrayInputStream(this))
    val byteArrayOut = ByteArrayOutputStream()
    gzipIn.copyTo(byteArrayOut)
    byteArrayOut.flush()
    byteArrayOut.close()

    return byteArrayOut.toByteArray()
}
