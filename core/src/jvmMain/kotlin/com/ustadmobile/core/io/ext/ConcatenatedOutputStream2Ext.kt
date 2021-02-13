package com.ustadmobile.core.io.ext

import com.ustadmobile.core.io.ConcatenatedEntry
import com.ustadmobile.core.io.ConcatenatedOutputStream2
import com.ustadmobile.door.ext.md5Sum
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