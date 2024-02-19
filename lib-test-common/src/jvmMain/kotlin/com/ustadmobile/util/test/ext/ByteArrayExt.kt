package com.ustadmobile.util.test.ext

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

fun ByteArray.gzipped(): ByteArray {
    val byteArrayInputStream = ByteArrayInputStream(this)
    val byteArrayOut = ByteArrayOutputStream()
    val gzipOut = GZIPOutputStream(byteArrayOut)
    byteArrayInputStream.copyTo(gzipOut)
    gzipOut.flush()
    gzipOut.close()
    return byteArrayOut.toByteArray()
}