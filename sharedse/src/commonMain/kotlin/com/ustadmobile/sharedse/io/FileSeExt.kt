package com.ustadmobile.sharedse.io

import com.ustadmobile.core.util.UMIOUtils
import kotlinx.io.InputStream
import kotlinx.io.OutputStream
import kotlinx.serialization.stringFromUtf8Bytes
import kotlinx.serialization.toUtf8Bytes

fun FileSe.readText(): String {
    var streamIn  = null as InputStream?

    try {
        streamIn = FileInputStreamSe(this)
        return stringFromUtf8Bytes(UMIOUtils.readStreamToByteArray(streamIn))
    }catch(e: Exception) {
        throw e
    }finally {
        streamIn?.close()
    }
}

fun FileSe.writeText(text: String) {
    var streamOut = null as OutputStream?
    try {
        streamOut = FileOutputStreamSe(this)
        streamOut.write(text.toUtf8Bytes())
    }catch(e: Exception) {
        throw e
    }finally {
        streamOut?.close()
    }
}


fun FileSe.readBytes(): ByteArray {
    var streamIn  = null as InputStream?

    try {
        streamIn = FileInputStreamSe(this)
        return UMIOUtils.readStreamToByteArray(streamIn)
    }finally {
        streamIn?.close()
    }
}

expect fun FileSe.renameFile(file: FileSe): Boolean
